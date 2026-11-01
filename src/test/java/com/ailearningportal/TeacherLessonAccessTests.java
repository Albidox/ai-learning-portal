package com.ailearningportal;

import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.controller.TeacherLessonController;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.LessonService;
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

@WebMvcTest(TeacherLessonController.class)
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class TeacherLessonAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private CourseService courseService;

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanOpenLessonList() throws Exception {
        when(lessonService.listLessonsForTeacher("teacher@example.test")).thenReturn(List.of());

        mockMvc.perform(get("/teacher/lessons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lessons")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCannotOpenTeacherLessonList() throws Exception {
        mockMvc.perform(get("/teacher/lessons"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotOpenTeacherLessonList() throws Exception {
        mockMvc.perform(get("/teacher/lessons"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanOpenLessonForm() throws Exception {
        when(courseService.listActiveSubjects()).thenReturn(List.of());

        mockMvc.perform(get("/teacher/lessons/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Lesson")));
    }
}
