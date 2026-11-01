package com.ailearningportal;

import com.ailearningportal.controller.AdminCourseController;
import com.ailearningportal.controller.AdminUserController;
import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.EnrollmentService;
import com.ailearningportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminCourseController.class,
        AdminUserController.class
})
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class AdminAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCanOpenCourseList() throws Exception {
        when(courseService.listCourses()).thenReturn(List.of());

        mockMvc.perform(get("/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Courses")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCannotOpenAdminCourseList() throws Exception {
        mockMvc.perform(get("/admin/courses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCanOpenSubjectForm() throws Exception {
        when(courseService.listActiveCourses()).thenReturn(List.of());

        mockMvc.perform(get("/admin/subjects/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Subject")));
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCanOpenEnrollmentForm() throws Exception {
        when(courseService.listActiveCourses()).thenReturn(List.of());
        when(userService.listStudents()).thenReturn(List.of());

        mockMvc.perform(get("/admin/enrollments/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Enrollment")));
    }
}
