package com.ailearningportal.dto;

public class StoredFile {

    private final String filePath;
    private final String originalFileName;

    public StoredFile(String filePath, String originalFileName) {
        this.filePath = filePath;
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }
}
