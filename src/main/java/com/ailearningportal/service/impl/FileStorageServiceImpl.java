package com.ailearningportal.service.impl;

import com.ailearningportal.dto.StoredFile;
import com.ailearningportal.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final String uploadDir;
    private final Set<String> allowedExtensions;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads/assignments}") String uploadDir,
            @Value("${app.upload.allowed-extensions:pdf,docx,txt,png,jpg,jpeg,zip,java}") String allowedExtensions
    ) {
        this.uploadDir = uploadDir;
        this.allowedExtensions = Arrays.stream(allowedExtensions.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public StoredFile storeSubmissionFile(MultipartFile file, Long submissionId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalFileName = cleanOriginalFileName(file.getOriginalFilename());
        String extension = getExtension(originalFileName);

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File type is not allowed");
        }

        String storedFileName = submissionId + "_" + System.currentTimeMillis() + "_" + safeFileName(originalFileName);
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("File name is not safe");
        }

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save uploaded file", ex);
        }

        String relativePath = Paths.get(uploadDir).resolve(storedFileName).toString().replace("\\", "/");
        return new StoredFile(relativePath, originalFileName);
    }

    @Override
    public Resource loadSubmissionFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Submission file not found");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = Paths.get(filePath).normalize();

        if (!targetPath.isAbsolute()) {
            targetPath = Paths.get("").toAbsolutePath().resolve(targetPath).normalize();
        }

        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("File path is not allowed");
        }

        try {
            Resource resource = new UrlResource(targetPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Submission file not found");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Submission file not found", ex);
        }
    }

    private String cleanOriginalFileName(String originalFileName) {
        String value = Objects.requireNonNullElse(originalFileName, "").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        String fileName = Paths.get(value).getFileName().toString();
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("File name is not safe");
        }

        return fileName;
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("File type is not allowed");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String safeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
