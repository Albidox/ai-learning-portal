package com.ailearningportal.service;

import com.ailearningportal.dto.EnrollmentRequest;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnrollmentServiceTests {

    @Test
    void createEnrollmentShouldBlockDuplicateStudentEnrollment() {
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        EnrollmentService enrollmentService = new EnrollmentServiceImpl(
                enrollmentRepository,
                courseRepository,
                userRepository
        );

        Course course = new Course();
        course.setId(1L);
        course.setActive(true);

        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(1L);
        request.setStudentId(2L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> enrollmentService.createEnrollment(request));
        verify(enrollmentRepository, never()).save(any());
    }
}
