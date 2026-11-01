package com.ailearningportal.controller;

import com.ailearningportal.dto.AttendanceRequest;
import com.ailearningportal.entity.AttendanceStatus;
import com.ailearningportal.service.AttendanceService;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;
    private final CourseService courseService;
    private final UserService userService;

    public TeacherAttendanceController(
            AttendanceService attendanceService,
            CourseService courseService,
            UserService userService
    ) {
        this.attendanceService = attendanceService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/teacher/attendance")
    public String attendance(Model model) {
        model.addAttribute("pageTitle", "Attendance");
        if (!model.containsAttribute("attendanceRequest")) {
            model.addAttribute("attendanceRequest", new AttendanceRequest());
        }
        addAttendancePageData(model);
        return "teacher/attendance";
    }

    @PostMapping("/teacher/attendance")
    public String markAttendance(
            @Valid @ModelAttribute("attendanceRequest") AttendanceRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Attendance");

        if (bindingResult.hasErrors()) {
            addAttendancePageData(model);
            return "teacher/attendance";
        }

        try {
            attendanceService.markAttendance(request, authentication.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("attendance.error", ex.getMessage());
            addAttendancePageData(model);
            return "teacher/attendance";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Attendance marked successfully.");
        return "redirect:/teacher/attendance";
    }

    private void addAttendancePageData(Model model) {
        model.addAttribute("courses", courseService.listActiveCourses());
        model.addAttribute("students", userService.listStudents());
        model.addAttribute("attendanceStatuses", AttendanceStatus.values());
        model.addAttribute("recentAttendance", attendanceService.listRecentAttendance());
    }
}
