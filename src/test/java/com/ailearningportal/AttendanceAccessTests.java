package com.ailearningportal;

import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.controller.StudentAttendanceController;
import com.ailearningportal.controller.TeacherAttendanceController;
import com.ailearningportal.dto.AttendanceSummary;
import com.ailearningportal.entity.Attendance;
import com.ailearningportal.entity.AttendanceStatus;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.security.SecurityConfig;
import com.ailearningportal.service.AttendanceService;
import com.ailearningportal.service.CourseService;
import com.ailearningportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        TeacherAttendanceController.class,
        StudentAttendanceController.class
})
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class AttendanceAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanOpenAttendancePage() throws Exception {
        when(courseService.listActiveCourses()).thenReturn(List.of(course()));
        when(userService.listStudents()).thenReturn(List.of(student()));
        when(attendanceService.listRecentAttendance()).thenReturn(List.of(attendance()));

        mockMvc.perform(get("/teacher/attendance"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Attendance")))
                .andExpect(content().string(containsString("Java Basics")));
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCannotOpenTeacherAttendancePage() throws Exception {
        mockMvc.perform(get("/teacher/attendance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotOpenTeacherAttendancePage() throws Exception {
        mockMvc.perform(get("/teacher/attendance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.test", roles = "STUDENT")
    void studentCanOpenOwnAttendancePage() throws Exception {
        AttendanceSummary summary = new AttendanceSummary(10L, "Java Basics", 4, 3, 1, 75.0);
        when(attendanceService.listStudentAttendanceSummary("student@example.test"))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/student/attendance"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Attendance")))
                .andExpect(content().string(containsString("75.00%")));
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotOpenStudentAttendancePage() throws Exception {
        mockMvc.perform(get("/student/attendance"))
                .andExpect(status().isForbidden());
    }

    private Attendance attendance() {
        User teacher = new User();
        teacher.setName("Teacher User");
        teacher.setRole(Role.TEACHER);

        Attendance attendance = new Attendance();
        attendance.setCourse(course());
        attendance.setStudent(student());
        attendance.setClassDate(LocalDate.of(2026, 8, 4));
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setMarkedByTeacher(teacher);
        return attendance;
    }

    private Course course() {
        Course course = new Course();
        course.setId(10L);
        course.setName("Java Basics");
        course.setActive(true);
        return course;
    }

    private User student() {
        User student = new User();
        student.setId(2L);
        student.setName("Student User");
        student.setEmail("student@example.test");
        student.setRole(Role.STUDENT);
        return student;
    }
}
