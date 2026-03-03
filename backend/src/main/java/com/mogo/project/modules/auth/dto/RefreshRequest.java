package com.mogo.project.modules.auth.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
