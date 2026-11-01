package com.ailearningportal.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    @ModelAttribute
    public void addUserInfo(Model model, Authentication authentication) {
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            model.addAttribute("currentUserEmail", authentication.getName());
            model.addAttribute("isAdmin", hasRole(authentication, "ROLE_ADMIN"));
            model.addAttribute("isTeacher", hasRole(authentication, "ROLE_TEACHER"));
            model.addAttribute("isStudent", hasRole(authentication, "ROLE_STUDENT"));
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isTeacher", false);
            model.addAttribute("isStudent", false);
        }
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
    }
}
