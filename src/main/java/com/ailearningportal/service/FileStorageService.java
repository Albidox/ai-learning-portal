package com.ailearningportal.service;

import com.ailearningportal.dto.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile storeSubmissionFile(MultipartFile file, Long submissionId);

    Resource loadSubmissionFile(String filePath);
}
