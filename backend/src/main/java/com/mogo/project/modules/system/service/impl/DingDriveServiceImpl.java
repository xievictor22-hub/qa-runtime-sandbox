package com.mogo.project.modules.system.service.impl;

import com.aliyun.dingtalkstorage_1_0.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.system.manager.DingTalkManager;
import com.mogo.project.modules.system.service.IDingDriveService;
import com.aliyun.teaopenapi.models.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DingDriveServiceImpl implements IDingDriveService {

    @Value("${dingtalk.space-id}")
    private String spaceId;
    @Value("${dingtalk.target-folder-id}")
    private String parentId; // 目标文件夹ID，根目录通常为 "0"
    @Value("${dingtalk.operator-union-id}")
    private String unionId;

    private final DingTalkManager dingTalkManager;

    // Client 建议作为实例变量或静态变量（只要配置是静态的）
    private static final com.aliyun.dingtalkstorage_1_0.Client storageClient;

    static {
        try {
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            storageClient = new com.aliyun.dingtalkstorage_1_0.Client(config);
        } catch (Exception e) {
            // 静态块初始化失败通常是致命的，直接抛出 Error 或 RuntimeException
            log.error("初始化钉盘客户端失败", e);
            throw new RuntimeException("钉盘服务初始化异常", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 获取上传凭证 (Get Key)
        GetFileUploadInfoResponse uploadInfo = getUploadKey(file);

        // 2. 执行物理上传 (PUT Stream)
        handleUpload(uploadInfo, file);

        // 3. 提交文件 (Commit)
        CommitFileResponse commitResponse = handleFinishFileUpload(uploadInfo, file);

        // 4. 返回文件 ID
        if (commitResponse.getBody() != null && commitResponse.getBody().getDentry() != null) {
            return commitResponse.getBody().getDentry().getId();
        }
        throw new ServiceException("上传成功但未返回文件ID");
    }

    /**
     * Step 1: 获取上传信息
     */
    private GetFileUploadInfoResponse getUploadKey(MultipartFile file) {
        try {
            GetFileUploadInfoHeaders headers = new GetFileUploadInfoHeaders();
            headers.xAcsDingtalkAccessToken = dingTalkManager.getAccessToken();

            GetFileUploadInfoRequest.GetFileUploadInfoRequestOptionPreCheckParam preCheckParam =
                    new GetFileUploadInfoRequest.GetFileUploadInfoRequestOptionPreCheckParam()
                            .setName(file.getOriginalFilename())
                            .setSize(file.getSize())
                            .setParentId(this.parentId); // 使用配置的变量，而非硬编码

            GetFileUploadInfoRequest.GetFileUploadInfoRequestOption option =
                    new GetFileUploadInfoRequest.GetFileUploadInfoRequestOption()
                            .setStorageDriver("DINGTALK")
                            .setPreCheckParam(preCheckParam)
                            .setPreferRegion("ZHANGJIAKOU")
                            .setPreferIntranet(true);

            GetFileUploadInfoRequest request = new GetFileUploadInfoRequest()
                    .setUnionId(this.unionId) // 使用配置的变量
                    .setProtocol("HEADER_SIGNATURE")
                    .setMultipart(false) // 单文件上传模式
                    .setOption(option);

            // 注意：SDK 方法的第一个参数是 spaceId
            return storageClient.getFileUploadInfoWithOptions(
                    this.spaceId,
                    request,
                    headers,
                    new RuntimeOptions());

        } catch (TeaException err) {
            // 标准 SDK 异常处理：提取 code 和 message
            log.error("获取上传凭证失败: code=[{}], msg=[{}]", err.getCode(), err.getMessage(), err);
            throw new ServiceException("钉盘服务拒绝请求: " + err.getMessage());
        } catch (Exception e) {
            log.error("获取上传凭证系统异常", e);
            throw new ServiceException("钉盘服务连接失败");
        }
    }

    /**
     * Step 2: 处理流上传 (HTTP PUT)
     */
    private void handleUpload(GetFileUploadInfoResponse uploadInfo, MultipartFile file) {
        // 安全检查：防止空指针
        if (uploadInfo.getBody() == null || uploadInfo.getBody().getHeaderSignatureInfo() == null) {
            throw new ServiceException("钉盘未返回有效的上传地址");
        }

        String resourceUrl = uploadInfo.getBody().getHeaderSignatureInfo().getResourceUrls().get(0);
        Map<String, String> headers = uploadInfo.getBody().getHeaderSignatureInfo().getHeaders();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(resourceUrl);
            connection = (HttpURLConnection) url.openConnection();

            // 设置鉴权头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setUseCaches(false);
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时 (大文件需调大)

            // 使用 try-with-resources 自动关闭流
            try (OutputStream out = connection.getOutputStream();
                 InputStream is = file.getInputStream()) {

                byte[] buffer = new byte[8192]; // 8KB 缓存
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 201) {
                // 关键点：非 200 必须抛出异常，否则流程会继续往下走
                log.error("文件流上传失败，HTTP状态码: {}", responseCode);
                throw new ServiceException("文件流传输到钉盘失败，状态码: " + responseCode);
            }

        } catch (ServiceException se) {
            throw se; // 业务异常直接抛出
        } catch (Exception e) {
            log.error("文件流上传过程中发生IO异常", e);
            throw new ServiceException("文件上传中断: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Step 3: 提交保存 (Commit)
     */
    private CommitFileResponse handleFinishFileUpload(GetFileUploadInfoResponse uploadInfo, MultipartFile file) {
        try {
            String uploadKey = uploadInfo.getBody().getUploadKey();

            CommitFileHeaders headers = new CommitFileHeaders();
            headers.xAcsDingtalkAccessToken = dingTalkManager.getAccessToken();

            CommitFileRequest.CommitFileRequestOption option = new CommitFileRequest.CommitFileRequestOption()
                    .setSize(file.getSize())
                    .setConflictStrategy("AUTO_RENAME"); // 遇到同名文件自动重命名

            CommitFileRequest request = new CommitFileRequest()
                    .setUnionId(this.unionId)
                    .setUploadKey(uploadKey)
                    .setName(file.getOriginalFilename())
                    .setParentId(this.parentId)
                    .setOption(option);

            return storageClient.commitFileWithOptions(
                    this.spaceId,
                    request,
                    headers,
                    new RuntimeOptions());

        } catch (TeaException err) {
            log.error("提交文件保存失败: code=[{}], msg=[{}]", err.getCode(), err.getMessage(), err);
            throw new ServiceException("文件提交保存失败: " + err.getMessage());
        } catch (Exception e) {
            log.error("提交文件保存系统异常", e);
            throw new ServiceException("文件提交保存异常");
        }
    }
}