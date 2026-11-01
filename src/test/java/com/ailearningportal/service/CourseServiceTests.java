package com.ailearningportal.service;

import com.ailearningportal.dto.CourseRequest;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.SubjectRepository;
import com.ailearningportal.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseServiceTests {

    @Test
    void createCourseShouldBlockDuplicateCourseName() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        CourseService courseService = new CourseServiceImpl(courseRepository, subjectRepository);

        CourseRequest request = new CourseRequest();
        request.setName("Java Basics");
        request.setActive(true);

        when(courseRepository.existsByNameIgnoreCase("Java Basics")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> courseService.createCourse(request));
        verify(courseRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
