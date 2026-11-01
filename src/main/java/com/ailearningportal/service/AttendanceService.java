package com.ailearningportal.service;

import com.ailearningportal.dto.AttendanceRequest;
import com.ailearningportal.dto.AttendanceSummary;
import com.ailearningportal.entity.Attendance;

import java.util.List;

public interface AttendanceService {

    List<Attendance> listRecentAttendance();

    Attendance markAttendance(AttendanceRequest request, String teacherEmail);

    List<AttendanceSummary> listStudentAttendanceSummary(String studentEmail);
}
