package com.ailearningportal;

import com.ailearningportal.controller.AuthController;
import com.ailearningportal.controller.DashboardController;
import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.controller.HomeController;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        HomeController.class,
        AuthController.class,
        DashboardController.class
})
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class SecurityAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void homePageShouldOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AI Learning Portal")));
    }

    @Test
    void registerPageShouldOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Student Account")));
    }

    @Test
    void teacherRegisterPageShouldOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/register/teacher"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Teacher Account")));
    }

    @Test
    void studentDashboardShouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void adminDashboardShouldBlockStudentUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentDashboardShouldAllowStudentUser() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Student Dashboard")));
    }
}
