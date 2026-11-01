package com.ailearningportal.service;

import com.ailearningportal.dto.AttendanceRequest;
import com.ailearningportal.dto.AttendanceSummary;
import com.ailearningportal.entity.Attendance;
import com.ailearningportal.entity.AttendanceStatus;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AttendanceRepository;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttendanceServiceTests {

    @Test
    void markAttendanceShouldSaveValidAttendance() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceServiceImpl(
                attendanceRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        AttendanceRequest request = attendanceRequest();
        Course course = course();
        User student = student();
        User teacher = teacher();

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(true);
        when(attendanceRepository.existsByCourseIdAndStudentIdAndClassDate(10L, 2L, request.getClassDate()))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance savedAttendance = service.markAttendance(request, "teacher@example.test");

        assertSame(course, savedAttendance.getCourse());
        assertSame(student, savedAttendance.getStudent());
        assertSame(teacher, savedAttendance.getMarkedByTeacher());
        assertEquals(AttendanceStatus.PRESENT, savedAttendance.getStatus());
    }

    @Test
    void markAttendanceShouldBlockDuplicateAttendance() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceServiceImpl(
                attendanceRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        AttendanceRequest request = attendanceRequest();

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(true);
        when(attendanceRepository.existsByCourseIdAndStudentIdAndClassDate(10L, 2L, request.getClassDate()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAttendance(request, "teacher@example.test")
        );
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void markAttendanceShouldBlockStudentNotEnrolledInCourse() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceServiceImpl(
                attendanceRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        AttendanceRequest request = attendanceRequest();

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAttendance(request, "teacher@example.test")
        );
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void markAttendanceShouldBlockFutureDate() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceServiceImpl(
                attendanceRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        AttendanceRequest request = attendanceRequest();
        request.setClassDate(LocalDate.now().plusDays(1));

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAttendance(request, "teacher@example.test")
        );
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void listStudentAttendanceSummaryShouldCalculatePercentage() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceServiceImpl(
                attendanceRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        User student = student();
        Course course = course();
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentAndStatusWithCourse(2L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment));
        when(attendanceRepository.countByStudentIdAndCourseId(2L, 10L)).thenReturn(4L);
        when(attendanceRepository.countByStudentIdAndCourseIdAndStatus(2L, 10L, AttendanceStatus.PRESENT))
                .thenReturn(3L);
        when(attendanceRepository.countByStudentIdAndCourseIdAndStatus(2L, 10L, AttendanceStatus.ABSENT))
                .thenReturn(1L);

        List<AttendanceSummary> summaries = service.listStudentAttendanceSummary("student@example.test");

        assertEquals(1, summaries.size());
        assertEquals("Java Basics", summaries.get(0).getCourseName());
        assertEquals(4, summaries.get(0).getTotalClasses());
        assertEquals(3, summaries.get(0).getPresentCount());
        assertEquals(1, summaries.get(0).getAbsentCount());
        assertEquals(75.0, summaries.get(0).getPercentage(), 0.01);
    }

    private AttendanceRequest attendanceRequest() {
        AttendanceRequest request = new AttendanceRequest();
        request.setCourseId(10L);
        request.setStudentId(2L);
        request.setClassDate(LocalDate.of(2026, 8, 4));
        request.setStatus(AttendanceStatus.PRESENT);
        return request;
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
        student.setRole(Role.STUDENT);
        return student;
    }

    private User teacher() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);
        return teacher;
    }
}
