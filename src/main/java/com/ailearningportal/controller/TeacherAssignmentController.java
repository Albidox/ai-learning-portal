package com.ailearningportal.controller;

import com.ailearningportal.dto.AssignmentRequest;
import com.ailearningportal.dto.MarkRequest;
import com.ailearningportal.entity.Submission;
import com.ailearningportal.service.AssignmentService;
import com.ailearningportal.service.CourseService;
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
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final SubmissionService submissionService;

    public TeacherAssignmentController(
            AssignmentService assignmentService,
            CourseService courseService,
            SubmissionService submissionService
    ) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.submissionService = submissionService;
    }

    @GetMapping("/teacher/assignments")
    public String assignments(Authentication authentication, Model model) {
        model.addAttribute("pageTitle", "Assignments");
        model.addAttribute("assignments", assignmentService.listAssignmentsForTeacher(authentication.getName()));
        return "teacher/assignments";
    }

    @GetMapping("/teacher/assignments/new")
    public String newAssignment(Model model) {
        model.addAttribute("pageTitle", "Create Assignment");
        if (!model.containsAttribute("assignmentRequest")) {
            model.addAttribute("assignmentRequest", new AssignmentRequest());
        }
        addAssignmentFormData(model);
        return "teacher/assignment-form";
    }

    @PostMapping("/teacher/assignments")
    public String createAssignment(
            @Valid @ModelAttribute("assignmentRequest") AssignmentRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("pageTitle", "Create Assignment");

        if (bindingResult.hasErrors()) {
            addAssignmentFormData(model);
            return "teacher/assignment-form";
        }

        try {
            assignmentService.createAssignment(request, authentication.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("assignment.error", ex.getMessage());
            addAssignmentFormData(model);
            return "teacher/assignment-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Assignment created successfully.");
        return "redirect:/teacher/assignments";
    }

    @GetMapping("/teacher/assignments/{assignmentId}/submissions")
    public String submissions(
            @PathVariable Long assignmentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            model.addAttribute("pageTitle", "Submissions");
            model.addAttribute("assignment", assignmentService.getAssignmentForTeacher(authentication.getName(), assignmentId));
            model.addAttribute("submissions", submissionService.listSubmissionsForAssignment(assignmentId, authentication.getName()));
            return "teacher/submissions";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    @GetMapping("/teacher/submissions/{submissionId}/mark")
    public String markForm(
            @PathVariable Long submissionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Submission submission = submissionService.getSubmissionForMark(submissionId, authentication.getName());
            model.addAttribute("pageTitle", "Mark Submission");
            model.addAttribute("submission", submission);
            if (!model.containsAttribute("markRequest")) {
                model.addAttribute("markRequest", toMarkRequest(submission));
            }
            return "teacher/mark-form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    @PostMapping("/teacher/submissions/{submissionId}/mark")
    public String saveMark(
            @PathVariable Long submissionId,
            @Valid @ModelAttribute("markRequest") MarkRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Submission submission = submissionService.getSubmissionForMark(submissionId, authentication.getName());
        model.addAttribute("pageTitle", "Mark Submission");
        model.addAttribute("submission", submission);

        if (bindingResult.hasErrors()) {
            return "teacher/mark-form";
        }

        try {
            submissionService.saveMark(submissionId, request, authentication.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("mark.error", ex.getMessage());
            return "teacher/mark-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Marks saved successfully.");
        return "redirect:/teacher/assignments/" + submission.getAssignment().getId() + "/submissions";
    }

    private void addAssignmentFormData(Model model) {
        model.addAttribute("courses", courseService.listActiveCourses());
    }

    private MarkRequest toMarkRequest(Submission submission) {
        MarkRequest request = new MarkRequest();
        if (submission.getMark() != null) {
            request.setMarksAwarded(submission.getMark().getMarksAwarded());
            request.setFeedback(submission.getMark().getFeedback());
        }
        return request;
    }
}
