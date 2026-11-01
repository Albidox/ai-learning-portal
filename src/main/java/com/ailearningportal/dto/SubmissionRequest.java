package com.ailearningportal.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class SubmissionRequest {

    @Size(max = 5000, message = "Answer text must be 5000 characters or less")
    private String answerText;

    private MultipartFile file;

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
