package com.ailearningportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LessonRequest {

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotBlank(message = "Lesson title is required")
    @Size(max = 150, message = "Lesson title must be 150 characters or less")
    private String title;

    @NotBlank(message = "Lesson content is required")
    @Size(min = 20, max = 20000, message = "Lesson content must be 20 to 20000 characters")
    private String content;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
