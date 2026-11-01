package com.ailearningportal.dto;

import com.ailearningportal.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class AttendanceRequest {

    @NotNull(message = "Course is required")
    private Long courseId;

    @NotNull(message = "Student is required")
    private Long studentId;

    @NotNull(message = "Class date is required")
    @PastOrPresent(message = "Class date cannot be in the future")
    private LocalDate classDate = LocalDate.now();

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDate getClassDate() {
        return classDate;
    }

    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
