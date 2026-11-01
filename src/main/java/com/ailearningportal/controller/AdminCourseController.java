package com.ailearningportal.controller;

import com.ailearningportal.dto.CourseRequest;
import com.ailearningportal.dto.EnrollmentRequest;
import com.ailearningportal.dto.SubjectRequest;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.EnrollmentService;
import com.ailearningportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    public AdminCourseController(
            CourseService courseService,
            EnrollmentService enrollmentService,
            UserService userService
    ) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }

    @GetMapping("/admin/courses")
    public String courses(Model model) {
        model.addAttribute("pageTitle", "Courses");
        model.addAttribute("courses", courseService.listCourses());
        return "admin/courses";
    }

    @GetMapping("/admin/courses/new")
    public String newCourse(Model model) {
        model.addAttribute("pageTitle", "Create Course");
        if (!model.containsAttribute("courseRequest")) {
            model.addAttribute("courseRequest", new CourseRequest());
        }
        return "admin/course-form";
    }

    @PostMapping("/admin/courses")
    public String createCourse(
            @Valid @ModelAttribute("courseRequest") CourseRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Create Course");

        if (bindingResult.hasErrors()) {
            return "admin/course-form";
        }

        try {
            courseService.createCourse(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("course.error", ex.getMessage());
            return "admin/course-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Course created successfully.");
        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/subjects")
    public String subjects(Model model) {
        model.addAttribute("pageTitle", "Subjects");
        model.addAttribute("subjects", courseService.listSubjects());
        return "admin/subjects";
    }

    @GetMapping("/admin/subjects/new")
    public String newSubject(Model model) {
        model.addAttribute("pageTitle", "Create Subject");
        if (!model.containsAttribute("subjectRequest")) {
            model.addAttribute("subjectRequest", new SubjectRequest());
        }
        addSubjectFormData(model);
        return "admin/subject-form";
    }

    @PostMapping("/admin/subjects")
    public String createSubject(
            @Valid @ModelAttribute("subjectRequest") SubjectRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Create Subject");
        addSubjectFormData(model);

        if (bindingResult.hasErrors()) {
            return "admin/subject-form";
        }

        try {
            courseService.createSubject(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("subject.error", ex.getMessage());
            return "admin/subject-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Subject created successfully.");
        return "redirect:/admin/subjects";
    }

    @GetMapping("/admin/enrollments")
    public String enrollments(Model model) {
        model.addAttribute("pageTitle", "Enrollments");
        model.addAttribute("enrollments", enrollmentService.listEnrollments());
        return "admin/enrollments";
    }

    @GetMapping("/admin/enrollments/new")
    public String newEnrollment(Model model) {
        model.addAttribute("pageTitle", "Create Enrollment");
        if (!model.containsAttribute("enrollmentRequest")) {
            model.addAttribute("enrollmentRequest", new EnrollmentRequest());
        }
        addEnrollmentFormData(model);
        return "admin/enrollment-form";
    }

    @PostMapping("/admin/enrollments")
    public String createEnrollment(
            @Valid @ModelAttribute("enrollmentRequest") EnrollmentRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Create Enrollment");
        addEnrollmentFormData(model);

        if (bindingResult.hasErrors()) {
            return "admin/enrollment-form";
        }

        try {
            enrollmentService.createEnrollment(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("enrollment.error", ex.getMessage());
            return "admin/enrollment-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Student enrolled successfully.");
        return "redirect:/admin/enrollments";
    }

    private void addSubjectFormData(Model model) {
        model.addAttribute("courses", courseService.listActiveCourses());
    }

    private void addEnrollmentFormData(Model model) {
        model.addAttribute("courses", courseService.listActiveCourses());
        model.addAttribute("students", userService.listStudents());
    }
}
