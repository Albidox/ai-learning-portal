package com.ailearningportal.controller;

import com.ailearningportal.ai.AiGenerationException;
import com.ailearningportal.ai.LearningContentAiService;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import com.ailearningportal.entity.Lesson;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AiLearningContentController {

    private final LearningContentAiService learningContentAiService;

    public AiLearningContentController(LearningContentAiService learningContentAiService) {
        this.learningContentAiService = learningContentAiService;
    }

    @GetMapping("/teacher/lessons/{lessonId}/ai")
    public String teacherAiPage(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            addTeacherAiModel(lessonId, authentication.getName(), model);
            return "teacher/ai-content";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/lessons";
        }
    }

    @PostMapping("/teacher/lessons/{lessonId}/ai/summary")
    public String generateTeacherSummary(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        return generateTeacherContent(lessonId, AiContentType.SUMMARY, authentication, redirectAttributes);
    }

    @PostMapping("/teacher/lessons/{lessonId}/ai/quiz")
    public String generateTeacherQuiz(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        return generateTeacherContent(lessonId, AiContentType.QUIZ, authentication, redirectAttributes);
    }

    @PostMapping("/teacher/ai-content/{contentId}/approve")
    public String approveTeacherContent(
            @PathVariable Long contentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AiLearningContent content = learningContentAiService.approveTeacherContent(contentId, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "AI content approved for students.");
            return "redirect:/teacher/lessons/" + content.getLesson().getId() + "/ai";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/lessons";
        }
    }

    @GetMapping("/student/lessons/{lessonId}/ai")
    public String studentAiPage(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            addStudentAiModel(lessonId, authentication.getName(), model);
            return "student/ai-content";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/courses";
        }
    }

    @PostMapping("/student/lessons/{lessonId}/ai/summary")
    public String generateStudentSummary(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        return generateStudentContent(lessonId, AiContentType.SUMMARY, authentication, redirectAttributes);
    }

    @PostMapping("/student/lessons/{lessonId}/ai/quiz")
    public String generateStudentQuiz(
            @PathVariable Long lessonId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        return generateStudentContent(lessonId, AiContentType.QUIZ, authentication, redirectAttributes);
    }

    private String generateTeacherContent(
            Long lessonId,
            AiContentType contentType,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            learningContentAiService.generateTeacherContent(lessonId, contentType, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "AI " + label(contentType) + " is ready.");
        } catch (AiGenerationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", AiGenerationException.FRIENDLY_MESSAGE);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/teacher/lessons/" + lessonId + "/ai";
    }

    private String generateStudentContent(
            Long lessonId,
            AiContentType contentType,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            learningContentAiService.generateStudentContent(lessonId, contentType, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Personal AI " + label(contentType) + " is ready.");
        } catch (AiGenerationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", AiGenerationException.FRIENDLY_MESSAGE);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/student/lessons/" + lessonId + "/ai";
    }

    private void addTeacherAiModel(Long lessonId, String teacherEmail, Model model) {
        Lesson lesson = learningContentAiService.getTeacherLesson(lessonId, teacherEmail);
        model.addAttribute("pageTitle", "AI Content");
        model.addAttribute("lesson", lesson);
        model.addAttribute("teacherSummary", learningContentAiService.getTeacherContent(
                lessonId,
                AiContentType.SUMMARY,
                teacherEmail
        ));
        model.addAttribute("teacherQuiz", learningContentAiService.getTeacherContent(
                lessonId,
                AiContentType.QUIZ,
                teacherEmail
        ));
    }

    private void addStudentAiModel(Long lessonId, String studentEmail, Model model) {
        Lesson lesson = learningContentAiService.getStudentLesson(lessonId, studentEmail);
        model.addAttribute("pageTitle", "AI Notes And Quiz");
        model.addAttribute("lesson", lesson);
        model.addAttribute("classSummary", learningContentAiService.getApprovedClassContentForStudent(
                lessonId,
                AiContentType.SUMMARY,
                studentEmail
        ));
        model.addAttribute("classQuiz", learningContentAiService.getApprovedClassContentForStudent(
                lessonId,
                AiContentType.QUIZ,
                studentEmail
        ));
        model.addAttribute("personalSummary", learningContentAiService.getStudentPersonalContent(
                lessonId,
                AiContentType.SUMMARY,
                studentEmail
        ));
        model.addAttribute("personalQuiz", learningContentAiService.getStudentPersonalContent(
                lessonId,
                AiContentType.QUIZ,
                studentEmail
        ));
    }

    private String label(AiContentType contentType) {
        return contentType == AiContentType.SUMMARY ? "summary" : "quiz";
    }
}
