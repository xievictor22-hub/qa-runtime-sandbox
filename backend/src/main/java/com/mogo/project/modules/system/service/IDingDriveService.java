package com.mogo.project.modules.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface IDingDriveService {
    String uploadFile(MultipartFile file);
}
