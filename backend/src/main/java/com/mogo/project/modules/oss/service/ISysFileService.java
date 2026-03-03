package com.mogo.project.modules.oss.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface ISysFileService {

    /**
     * 前端上传的文件 (MultipartFile)
     * @param file 前端传来的文件
     * @return 文件访问 URL
     */
    String upload(MultipartFile file);

    /**
     * 后端生成的流/文件 (如 Excel 导出)
     * @param inputStream 文件流
     * @param originalFileName 原始文件名 (如: error_report.xlsx)
     * @param contentType 文件类型 (如: application/vnd.ms-excel)
     * @return 文件访问 URL
     */
    String upload(InputStream inputStream, String originalFileName, String contentType);

    /**
     * 后端生成的字节数组 (如 ByteArrayOutputStream 转出的 Excel)
     * @param data 字节数据
     * @param originalFileName 文件名
     * @return 文件访问 URL
     */
    String upload(byte[] data, String originalFileName);
}