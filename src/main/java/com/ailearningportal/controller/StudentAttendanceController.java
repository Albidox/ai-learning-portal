package com.ailearningportal.controller;

import com.ailearningportal.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/student/attendance")
    public String attendance(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "Attendance");
        model.addAttribute("summaries", attendanceService.listStudentAttendanceSummary(authentication.getName()));
        return "student/attendance";
    }
}
