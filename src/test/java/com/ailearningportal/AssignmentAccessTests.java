package com.ailearningportal;

import com.ailearningportal.controller.FileController;
import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.controller.StudentAssignmentController;
import com.ailearningportal.controller.TeacherAssignmentController;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Mark;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Submission;
import com.ailearningportal.entity.SubmissionStatus;
import com.ailearningportal.entity.User;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.AssignmentService;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.FileStorageService;
import com.ailearningportal.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        TeacherAssignmentController.class,
        StudentAssignmentController.class,
        FileController.class
})
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class AssignmentAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private SubmissionService submissionService;

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanOpenAssignmentPage() throws Exception {
        when(assignmentService.listAssignmentsForTeacher("teacher@example.test")).thenReturn(List.of(assignment()));

        mockMvc.perform(get("/teacher/assignments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Assignments")))
                .andExpect(content().string(containsString("Java Assignment")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCannotOpenTeacherAssignmentPage() throws Exception {
        mockMvc.perform(get("/teacher/assignments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenOwnAssignmentPage() throws Exception {
        when(assignmentService.listAssignmentsForStudent("student@example.test")).thenReturn(List.of(assignment()));
        when(submissionService.findSubmittedAssignmentIds("student@example.test")).thenReturn(Set.of());

        mockMvc.perform(get("/student/assignments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Assignments")))
                .andExpect(content().string(containsString("Java Assignment")));
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotOpenStudentAssignmentPage() throws Exception {
        mockMvc.perform(get("/student/assignments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenMarksPage() throws Exception {
        when(submissionService.listStudentSubmissionsWithMarks("student@example.test")).thenReturn(List.of(submission(true)));

        mockMvc.perform(get("/student/marks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Marks")))
                .andExpect(content().string(containsString("18 / 20")));
    }

    private Assignment assignment() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setName("Teacher User");
        teacher.setRole(Role.TEACHER);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setCourse(course());
        assignment.setTeacher(teacher);
        assignment.setTitle("Java Assignment");
        assignment.setInstructions("Write a Java class.");
        assignment.setDueDate(LocalDate.now().plusDays(7));
        assignment.setMaxMarks(20);
        assignment.setActive(true);
        return assignment;
    }

    private Submission submission(boolean withMark) {
        User student = new User();
        student.setId(2L);
        student.setName("Student User");
        student.setEmail("student@example.test");
        student.setRole(Role.STUDENT);

        Submission submission = new Submission();
        submission.setId(30L);
        submission.setAssignment(assignment());
        submission.setStudent(student);
        submission.setAnswerText("My answer");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        if (withMark) {
            Mark mark = new Mark();
            mark.setSubmission(submission);
            mark.setMarksAwarded(18);
            mark.setFeedback("Good work");
            submission.setMark(mark);
        }

        return submission;
    }

    private Course course() {
        Course course = new Course();
        course.setId(5L);
        course.setName("Java Basics");
        course.setActive(true);
        return course;
    }
}
