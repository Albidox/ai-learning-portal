package com.ailearningportal.service;

import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.StudentLearningServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentLearningServiceTests {

    @Test
    void listMyCoursesShouldReturnActiveEnrollments() {
        UserRepository userRepository = mock(UserRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        StudentLearningService service = new StudentLearningServiceImpl(
                userRepository,
                enrollmentRepository,
                lessonRepository
        );

        User student = studentUser();
        Enrollment enrollment = new Enrollment();

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentAndStatusWithCourse(2L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment));

        List<Enrollment> enrollments = service.listMyCourses("student@example.test");

        assertEquals(1, enrollments.size());
    }

    @Test
    void getPublishedLessonShouldAllowStudentWhenEnrolled() {
        UserRepository userRepository = mock(UserRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        StudentLearningService service = new StudentLearningServiceImpl(
                userRepository,
                enrollmentRepository,
                lessonRepository
        );

        User student = studentUser();
        Lesson lesson = publishedLesson();

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(lessonRepository.findPublishedLessonById(50L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(true);

        Lesson result = service.getPublishedLessonForStudent("student@example.test", 50L);

        assertSame(lesson, result);
    }

    @Test
    void getPublishedLessonShouldBlockStudentWithoutEnrollment() {
        UserRepository userRepository = mock(UserRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        StudentLearningService service = new StudentLearningServiceImpl(
                userRepository,
                enrollmentRepository,
                lessonRepository
        );

        User student = studentUser();
        Lesson lesson = publishedLesson();

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(lessonRepository.findPublishedLessonById(50L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(2L, 10L, EnrollmentStatus.ACTIVE))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getPublishedLessonForStudent("student@example.test", 50L)
        );
    }

    @Test
    void getPublishedLessonShouldBlockDraftLesson() {
        UserRepository userRepository = mock(UserRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        StudentLearningService service = new StudentLearningServiceImpl(
                userRepository,
                enrollmentRepository,
                lessonRepository
        );

        User student = studentUser();

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));
        when(lessonRepository.findPublishedLessonById(50L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getPublishedLessonForStudent("student@example.test", 50L)
        );
    }

    private User studentUser() {
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);
        return student;
    }

    private Lesson publishedLesson() {
        Course course = new Course();
        course.setId(10L);

        Subject subject = new Subject();
        subject.setCourse(course);

        Lesson lesson = new Lesson();
        lesson.setId(50L);
        lesson.setSubject(subject);
        lesson.setPublished(true);

        return lesson;
    }
}
