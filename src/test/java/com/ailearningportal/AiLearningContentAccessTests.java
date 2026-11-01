package com.ailearningportal;

import com.ailearningportal.ai.AiGenerationException;
import com.ailearningportal.ai.LearningContentAiService;
import com.ailearningportal.controller.AiLearningContentController;
import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.dto.AiContentView;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiLearningContentController.class)
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class AiLearningContentAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LearningContentAiService learningContentAiService;

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanOpenTeacherAiPage() throws Exception {
        when(learningContentAiService.getTeacherLesson(50L, "teacher@example.test")).thenReturn(lesson());
        when(learningContentAiService.getTeacherContent(50L, AiContentType.SUMMARY, "teacher@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.SUMMARY));
        when(learningContentAiService.getTeacherContent(50L, AiContentType.QUIZ, "teacher@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.QUIZ));

        mockMvc.perform(get("/teacher/lessons/50/ai"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AI Summary")))
                .andExpect(content().string(containsString("AI Quiz")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenStudentAiPage() throws Exception {
        when(learningContentAiService.getStudentLesson(50L, "student@example.test")).thenReturn(lesson());
        when(learningContentAiService.getApprovedClassContentForStudent(50L, AiContentType.SUMMARY, "student@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.SUMMARY));
        when(learningContentAiService.getApprovedClassContentForStudent(50L, AiContentType.QUIZ, "student@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.QUIZ));
        when(learningContentAiService.getStudentPersonalContent(50L, AiContentType.SUMMARY, "student@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.SUMMARY));
        when(learningContentAiService.getStudentPersonalContent(50L, AiContentType.QUIZ, "student@example.test"))
                .thenReturn(AiContentView.empty(AiContentType.QUIZ));

        mockMvc.perform(get("/student/lessons/50/ai"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Teacher Approved Summary")))
                .andExpect(content().string(containsString("My Personal Quiz")));
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotOpenTeacherAiPage() throws Exception {
        mockMvc.perform(get("/teacher/lessons/50/ai"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotOpenStudentAiPage() throws Exception {
        mockMvc.perform(get("/student/lessons/50/ai"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherAiFailureShowsFriendlyMessage() throws Exception {
        doThrow(new AiGenerationException())
                .when(learningContentAiService)
                .generateTeacherContent(50L, AiContentType.SUMMARY, "teacher@example.test");

        mockMvc.perform(post("/teacher/lessons/50/ai/summary").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/lessons/50/ai"))
                .andExpect(flash().attribute("errorMessage", AiGenerationException.FRIENDLY_MESSAGE));
    }

    private Lesson lesson() {
        Course course = new Course();
        course.setId(10L);
        course.setName("Java Basics");

        Subject subject = new Subject();
        subject.setId(20L);
        subject.setCourse(course);
        subject.setName("Spring Boot");

        User teacher = new User();
        teacher.setId(1L);
        teacher.setName("Teacher User");
        teacher.setRole(Role.TEACHER);

        Lesson lesson = new Lesson();
        lesson.setId(50L);
        lesson.setSubject(subject);
        lesson.setTeacher(teacher);
        lesson.setTitle("Spring Boot MVC");
        lesson.setContent("Controller reads request and returns page.");
        lesson.setPublished(true);
        lesson.setPublishedAt(LocalDateTime.of(2026, 8, 4, 15, 0));
        return lesson;
    }
}
