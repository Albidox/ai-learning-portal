package com.ailearningportal.controller;

import com.ailearningportal.dto.LessonRequest;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherLessonController {

    private final LessonService lessonService;
    private final CourseService courseService;

    public TeacherLessonController(LessonService lessonService, CourseService courseService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
    }

    @GetMapping("/teacher/lessons")
    public String lessons(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "Lessons");
        model.addAttribute("lessons", lessonService.listLessonsForTeacher(authentication.getName()));
        return "teacher/lessons";
    }

    @GetMapping("/teacher/lessons/new")
    public String newLesson(Model model) {
        model.addAttribute("pageTitle", "Create Lesson");
        if (!model.containsAttribute("lessonRequest")) {
            model.addAttribute("lessonRequest", new LessonRequest());
        }
        addLessonFormData(model);
        return "teacher/lesson-form";
    }

    @PostMapping("/teacher/lessons")
    public String createLesson(
            @Valid @ModelAttribute("lessonRequest") LessonRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Create Lesson");
        addLessonFormData(model);

        if (bindingResult.hasErrors()) {
            return "teacher/lesson-form";
        }

        try {
            lessonService.createLesson(request, authentication.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("lesson.error", ex.getMessage());
            return "teacher/lesson-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Lesson saved as draft.");
        return "redirect:/teacher/lessons";
    }

    @PostMapping("/teacher/lessons/{id}/publish")
    public String publishLesson(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            lessonService.publishLesson(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Lesson published successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/teacher/lessons";
    }

    private void addLessonFormData(Model model) {
        model.addAttribute("subjects", courseService.listActiveSubjects());
    }
}
