package com.ailearningportal.controller;

import com.ailearningportal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Users");
        model.addAttribute("users", userService.listUsers());
        return "admin/users";
    }
}
