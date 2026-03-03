package com.mogo.project.modules.auth.dto;

import lombok.Data;

@Data
public class TokenResponse {
    String accessToken;
    String refreshToken;
}
