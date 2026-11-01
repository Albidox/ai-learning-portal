package com.ailearningportal.controller;

import com.ailearningportal.entity.Lesson;
import com.ailearningportal.service.StudentLearningService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StudentCourseController {

    private final StudentLearningService studentLearningService;

    public StudentCourseController(StudentLearningService studentLearningService) {
        this.studentLearningService = studentLearningService;
    }

    @GetMapping("/student/courses")
    public String courses(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "My Courses");
        model.addAttribute("enrollments", studentLearningService.listMyCourses(authentication.getName()));
        return "student/courses";
    }

    @GetMapping("/student/courses/{courseId}/lessons")
    public String courseLessons(
            @PathVariable Long courseId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            List<Lesson> lessons = studentLearningService.listPublishedLessonsForCourse(authentication.getName(), courseId);
            model.addAttribute("pageTitle", "Published Lessons");
            model.addAttribute("courseId", courseId);
            model.addAttribute("lessons", lessons);
            return "student/course-lessons";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/courses";
        }
    }

    @GetMapping("/student/lessons/{lessonId}")
    public String lessonDetail(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Lesson lesson = studentLearningService.getPublishedLessonForStudent(authentication.getName(), lessonId);
            model.addAttribute("pageTitle", lesson.getTitle());
            model.addAttribute("lesson", lesson);
            return "student/lesson-view";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/courses";
        }
    }
}
