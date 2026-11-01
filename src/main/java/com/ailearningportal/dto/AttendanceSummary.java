package com.ailearningportal.dto;

public class AttendanceSummary {

    private final Long courseId;
    private final String courseName;
    private final long totalClasses;
    private final long presentCount;
    private final long absentCount;
    private final double percentage;

    public AttendanceSummary(
            Long courseId,
            String courseName,
            long totalClasses,
            long presentCount,
            long absentCount,
            double percentage
    ) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.percentage = percentage;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public long getAbsentCount() {
        return absentCount;
    }

    public double getPercentage() {
        return percentage;
    }
}
