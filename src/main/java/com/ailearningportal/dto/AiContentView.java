package com.ailearningportal.dto;

import com.ailearningportal.entity.AiAudienceType;
import com.ailearningportal.entity.AiContentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiContentView {

    private boolean present;
    private Long id;
    private AiContentType contentType;
    private AiAudienceType audienceType;
    private boolean approved;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String summary;
    private List<String> importantPoints = new ArrayList<>();
    private List<String> keyTerms = new ArrayList<>();
    private List<QuizQuestionView> questions = new ArrayList<>();

    public static AiContentView empty(AiContentType contentType) {
        AiContentView view = new AiContentView();
        view.setContentType(contentType);
        return view;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AiContentType getContentType() {
        return contentType;
    }

    public void setContentType(AiContentType contentType) {
        this.contentType = contentType;
    }

    public AiAudienceType getAudienceType() {
        return audienceType;
    }

    public void setAudienceType(AiAudienceType audienceType) {
        this.audienceType = audienceType;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getImportantPoints() {
        return importantPoints;
    }

    public void setImportantPoints(List<String> importantPoints) {
        this.importantPoints = importantPoints;
    }

    public List<String> getKeyTerms() {
        return keyTerms;
    }

    public void setKeyTerms(List<String> keyTerms) {
        this.keyTerms = keyTerms;
    }

    public List<QuizQuestionView> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestionView> questions) {
        this.questions = questions;
    }
}
