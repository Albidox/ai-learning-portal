package com.ailearningportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "marks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mark_submission", columnNames = "submission_id")
        }
)
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(name = "marks_awarded", nullable = false)
    private Integer marksAwarded;

    @Lob
    @Column
    private String feedback;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @ManyToOne
    @JoinColumn(name = "graded_by_teacher_id", nullable = false)
    private User gradedByTeacher;

    public Mark() {
    }

    public Mark(Submission submission, Integer marksAwarded, String feedback, User gradedByTeacher) {
        this.submission = submission;
        this.marksAwarded = marksAwarded;
        this.feedback = feedback;
        this.gradedByTeacher = gradedByTeacher;
    }

    @PrePersist
    @PreUpdate
    public void updateGradedAt() {
        gradedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public User getGradedByTeacher() {
        return gradedByTeacher;
    }

    public void setGradedByTeacher(User gradedByTeacher) {
        this.gradedByTeacher = gradedByTeacher;
    }
}
