package com.mogo.project.modules.system.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DingFileResponseDTO {
    Map<String, String> headers;
    String resourceUrl;
    String uploadKey;
    
}
