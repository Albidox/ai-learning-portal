package com.ailearningportal.controller;

import com.ailearningportal.dto.RegisterRequest;
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
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        return studentRegisterForm(model);
    }

    @GetMapping("/register/student")
    public String studentRegisterForm(Model model) {
        configureRegistrationPage(
                model,
                "Student Registration",
                "Student",
                "Create Student Account",
                "Register as Student",
                "/register/student"
        );
        return "auth/register";
    }

    @GetMapping("/register/teacher")
    public String teacherRegisterForm(Model model) {
        configureRegistrationPage(
                model,
                "Teacher Registration",
                "Teacher",
                "Create Teacher Account",
                "Register as Teacher",
                "/register/teacher"
        );
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerLegacyStudent(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        return registerStudent(request, bindingResult, redirectAttributes, model);
    }

    @PostMapping("/register/student")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        return registerStudent(request, bindingResult, redirectAttributes, model);
    }

    @PostMapping("/register/teacher")
    public String registerTeacher(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        configureRegistrationPage(
                model,
                "Teacher Registration",
                "Teacher",
                "Create Teacher Account",
                "Register as Teacher",
                "/register/teacher"
        );

        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerTeacher(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "registration.failed", ex.getMessage());
            return "auth/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Teacher registration successful. Please login.");
        return "redirect:/login?registered";
    }

    private String registerStudent(
            RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        configureRegistrationPage(
                model,
                "Student Registration",
                "Student",
                "Create Student Account",
                "Register as Student",
                "/register/student"
        );

        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerStudent(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "registration.failed", ex.getMessage());
            return "auth/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Student registration successful. Please login.");
        return "redirect:/login?registered";
    }

    private void configureRegistrationPage(
            Model model,
            String pageTitle,
            String roleLabel,
            String heading,
            String buttonText,
            String actionPath
    ) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("registrationRoleLabel", roleLabel);
        model.addAttribute("registrationHeading", heading);
        model.addAttribute("registrationButtonText", buttonText);
        model.addAttribute("registrationAction", actionPath);
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        return "error/access-denied";
    }
}
