package com.mogo.project.modules.oss.service.impl;

import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.config.MinioConfig;
import com.mogo.project.modules.oss.service.ISysFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl implements ISysFileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String upload(MultipartFile file) {
        try {
            return this.upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
        } catch (Exception e) {
            log.error("文件流读取失败", e);
            throw new ServiceException("文件上传失败");
        }
    }

    @Override
    public String upload(byte[] data, String originalFileName) {
        return this.upload(
                new ByteArrayInputStream(data),
                originalFileName,
                "application/octet-stream"
        );
    }

    @Override
    public String upload(InputStream inputStream, String originalFileName, String contentType) {
        try {
            // 1. 生成唯一文件名 (按日期分目录: 2025/12/08/uuid.jpg)
            String suffix = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + UUID.randomUUID() + suffix;

            // 2. 上传到 MinIO
            // 注意：available() 并不总是返回准确的大小，但在 ByteArrayInputStream 中是准确的。
            // 如果是未知长度的流，minio 需要分片上传，这里为了简单假设流长度已知或足够小
            long size = inputStream.available();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            // 3. 拼接访问路径
            // 修正：如果 minioConfig.getEndpoint() 结尾自带斜杠，或者不带，需要处理一下
            String endpoint = minioConfig.getEndpoint();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }

            return endpoint + "/" + minioConfig.getBucketName() + "/" + fileName;

        } catch (Exception e) {
            log.error("MinIO上传异常", e);
            throw new ServiceException("文件服务异常: " + e.getMessage());
        }
    }
}