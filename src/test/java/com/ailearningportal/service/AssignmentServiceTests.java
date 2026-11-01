package com.ailearningportal.service;

import com.ailearningportal.dto.AssignmentRequest;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AssignmentRepository;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignmentServiceTests {

    @Test
    void createAssignmentShouldSaveValidAssignment() {
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AssignmentService service = new AssignmentServiceImpl(
                assignmentRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course(true)));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment savedAssignment = service.createAssignment(request(), "teacher@example.test");

        assertEquals("Java Assignment", savedAssignment.getTitle());
        assertEquals(20, savedAssignment.getMaxMarks());
    }

    @Test
    void createAssignmentShouldBlockNonTeacher() {
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AssignmentService service = new AssignmentServiceImpl(
                assignmentRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        User student = student();
        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> service.createAssignment(request(), "student@example.test"));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignmentShouldBlockInactiveCourse() {
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AssignmentService service = new AssignmentServiceImpl(
                assignmentRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher()));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course(false)));

        assertThrows(IllegalArgumentException.class, () -> service.createAssignment(request(), "teacher@example.test"));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void listAssignmentsForStudentShouldUseOnlyEnrolledAssignments() {
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AssignmentService service = new AssignmentServiceImpl(
                assignmentRepository,
                courseRepository,
                enrollmentRepository,
                userRepository
        );

        Assignment assignment = new Assignment();
        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student()));
        when(assignmentRepository.findActiveAssignmentsForStudent(2L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(assignment));

        List<Assignment> assignments = service.listAssignmentsForStudent("student@example.test");

        assertEquals(1, assignments.size());
    }

    private AssignmentRequest request() {
        AssignmentRequest request = new AssignmentRequest();
        request.setCourseId(5L);
        request.setTitle(" Java Assignment ");
        request.setInstructions(" Write a Java class. ");
        request.setDueDate(LocalDate.now().plusDays(7));
        request.setMaxMarks(20);
        request.setActive(true);
        return request;
    }

    private Course course(boolean active) {
        Course course = new Course();
        course.setId(5L);
        course.setName("Java Basics");
        course.setActive(active);
        return course;
    }

    private User teacher() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);
        return teacher;
    }

    private User student() {
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);
        return student;
    }
}
