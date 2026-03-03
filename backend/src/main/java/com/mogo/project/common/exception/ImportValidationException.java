package com.mogo.project.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 导入时出错中断流程并且返回含错误信息的文件下载地址
 */
@Getter
@Setter
public class ImportValidationException extends RuntimeException {
    private final String errorUrl; // 错误文件的下载链接

    public ImportValidationException(String errorUrl, String message) {
        super(message);
        this.errorUrl = errorUrl;
    }
}