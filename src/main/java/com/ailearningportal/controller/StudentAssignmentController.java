package com.ailearningportal.controller;

import com.ailearningportal.dto.SubmissionRequest;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.service.AssignmentService;
import com.ailearningportal.service.SubmissionService;
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
public class StudentAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    public StudentAssignmentController(AssignmentService assignmentService, SubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
    }

    @GetMapping("/student/assignments")
    public String assignments(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "Assignments");
        model.addAttribute("assignments", assignmentService.listAssignmentsForStudent(authentication.getName()));
        model.addAttribute("submittedAssignmentIds", submissionService.findSubmittedAssignmentIds(authentication.getName()));
        return "student/assignments";
    }

    @GetMapping("/student/assignments/{assignmentId}/submit")
    public String submitForm(
            @PathVariable Long assignmentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Assignment assignment = assignmentService.getActiveAssignmentForStudent(authentication.getName(), assignmentId);
            if (submissionService.findSubmittedAssignmentIds(authentication.getName()).contains(assignmentId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You already submitted this assignment.");
                return "redirect:/student/assignments";
            }

            model.addAttribute("pageTitle", "Submit Assignment");
            model.addAttribute("assignment", assignment);
            if (!model.containsAttribute("submissionRequest")) {
                model.addAttribute("submissionRequest", new SubmissionRequest());
            }
            return "student/submission-form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/assignments";
        }
    }

    @PostMapping("/student/assignments/{assignmentId}/submit")
    public String submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @ModelAttribute("submissionRequest") SubmissionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Assignment assignment = assignmentService.getActiveAssignmentForStudent(authentication.getName(), assignmentId);
        model.addAttribute("pageTitle", "Submit Assignment");
        model.addAttribute("assignment", assignment);

        if (bindingResult.hasErrors()) {
            return "student/submission-form";
        }

        try {
            submissionService.submitAssignment(assignmentId, request, authentication.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("submission.error", ex.getMessage());
            return "student/submission-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Assignment submitted successfully.");
        return "redirect:/student/assignments";
    }

    @GetMapping("/student/marks")
    public String marks(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "Marks");
        model.addAttribute("submissions", submissionService.listStudentSubmissionsWithMarks(authentication.getName()));
        return "student/marks";
    }
}
