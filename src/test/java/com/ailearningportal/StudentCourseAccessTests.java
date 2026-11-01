package com.ailearningportal;

import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.controller.StudentCourseController;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.StudentLearningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentCourseController.class)
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class StudentCourseAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentLearningService studentLearningService;

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenMyCourses() throws Exception {
        when(studentLearningService.listMyCourses("student@example.test"))
                .thenReturn(List.of(enrollment()));

        mockMvc.perform(get("/student/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My Courses")))
                .andExpect(content().string(containsString("Java Basics")));
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotOpenStudentCourses() throws Exception {
        mockMvc.perform(get("/student/courses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotOpenStudentCourses() throws Exception {
        mockMvc.perform(get("/student/courses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenPublishedLessonListForCourse() throws Exception {
        when(studentLearningService.listPublishedLessonsForCourse("student@example.test", 10L))
                .thenReturn(List.of(lesson()));

        mockMvc.perform(get("/student/courses/10/lessons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Published Lessons")))
                .andExpect(content().string(containsString("Spring Boot MVC")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenLessonDetail() throws Exception {
        when(studentLearningService.getPublishedLessonForStudent("student@example.test", 50L))
                .thenReturn(lesson());

        mockMvc.perform(get("/student/lessons/50"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Spring Boot MVC")))
                .andExpect(content().string(containsString("Controller reads request and returns page.")));
    }

    private Enrollment enrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return enrollment;
    }

    private Lesson lesson() {
        Subject subject = new Subject();
        subject.setCourse(course());
        subject.setName("Spring Boot");

        User teacher = new User();
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

    private Course course() {
        Course course = new Course();
        course.setId(10L);
        course.setName("Java Basics");
        course.setDescription("Beginner Java course");
        return course;
    }
}
