package com.ailearningportal.controller;

import com.ailearningportal.entity.Submission;
import com.ailearningportal.service.FileStorageService;
import com.ailearningportal.service.SubmissionService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

@Controller
public class FileController {

    private final FileStorageService fileStorageService;
    private final SubmissionService submissionService;

    public FileController(FileStorageService fileStorageService, SubmissionService submissionService) {
        this.fileStorageService = fileStorageService;
        this.submissionService = submissionService;
    }

    @GetMapping("/files/submissions/{submissionId}")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @PathVariable Long submissionId,
            Authentication authentication
    ) {
        Submission submission = submissionService.getSubmissionFileForCurrentUser(submissionId, authentication.getName());
        Resource resource = fileStorageService.loadSubmissionFile(submission.getFilePath());
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(submission.getOriginalFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }
}
