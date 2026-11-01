package com.ailearningportal.service;

import com.ailearningportal.dto.LessonRequest;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.SubjectRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LessonServiceTests {

    @Test
    void createLessonShouldSaveDraftLesson() {
        LessonRepository lessonRepository = mock(LessonRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LessonService lessonService = new LessonServiceImpl(lessonRepository, subjectRepository, userRepository);

        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);

        Course course = new Course();
        course.setId(10L);
        course.setActive(true);

        Subject subject = new Subject();
        subject.setId(20L);
        subject.setCourse(course);

        LessonRequest request = new LessonRequest();
        request.setSubjectId(20L);
        request.setTitle("  Spring Boot Controller  ");
        request.setContent("  This lesson explains Spring MVC controller basics.  ");

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(20L)).thenReturn(Optional.of(subject));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lesson savedLesson = lessonService.createLesson(request, "teacher@example.test");

        assertEquals("Spring Boot Controller", savedLesson.getTitle());
        assertEquals("This lesson explains Spring MVC controller basics.", savedLesson.getContent());
        assertFalse(savedLesson.isPublished());
        assertNull(savedLesson.getPublishedAt());
    }

    @Test
    void createLessonShouldBlockNonTeacherUser() {
        LessonRepository lessonRepository = mock(LessonRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LessonService lessonService = new LessonServiceImpl(lessonRepository, subjectRepository, userRepository);

        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);

        LessonRequest request = new LessonRequest();
        request.setSubjectId(20L);
        request.setTitle("Lesson");
        request.setContent("This lesson has enough content.");

        when(userRepository.findByEmail("student@example.test")).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> lessonService.createLesson(request, "student@example.test"));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void publishLessonShouldBlockLessonOwnedByAnotherTeacher() {
        LessonRepository lessonRepository = mock(LessonRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LessonService lessonService = new LessonServiceImpl(lessonRepository, subjectRepository, userRepository);

        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findByIdAndTeacherId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> lessonService.publishLesson(99L, "teacher@example.test"));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void publishLessonShouldSetPublishedDate() {
        LessonRepository lessonRepository = mock(LessonRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LessonService lessonService = new LessonServiceImpl(lessonRepository, subjectRepository, userRepository);

        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);

        Lesson lesson = new Lesson();
        lesson.setId(50L);
        lesson.setPublished(false);

        when(userRepository.findByEmail("teacher@example.test")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findByIdAndTeacherId(50L, 1L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lesson publishedLesson = lessonService.publishLesson(50L, "teacher@example.test");

        assertTrue(publishedLesson.isPublished());
        assertNotNull(publishedLesson.getPublishedAt());
    }
}
