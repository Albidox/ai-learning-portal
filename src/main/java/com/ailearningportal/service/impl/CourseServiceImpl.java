package com.ailearningportal.service.impl;

import com.ailearningportal.dto.CourseRequest;
import com.ailearningportal.dto.SubjectRequest;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.SubjectRepository;
import com.ailearningportal.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public CourseServiceImpl(CourseRepository courseRepository, SubjectRepository subjectRepository) {
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> listCourses() {
        return courseRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> listActiveCourses() {
        return courseRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional
    public Course createCourse(CourseRequest request) {
        String name = request.getName().trim();
        if (courseRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Course name already exists");
        }

        Course course = new Course(
                name,
                cleanText(request.getDescription()),
                request.getStartDate(),
                request.getEndDate(),
                request.isActive()
        );

        return courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> listSubjects() {
        return subjectRepository.findAllByOrderByCourseNameAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> listActiveSubjects() {
        return subjectRepository.findByCourseActiveTrueOrderByCourseNameAscNameAsc();
    }

    @Override
    @Transactional
    public Subject createSubject(SubjectRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.isActive()) {
            throw new IllegalArgumentException("Subject can be added only to active course");
        }

        String name = request.getName().trim();
        if (subjectRepository.existsByCourseIdAndNameIgnoreCase(course.getId(), name)) {
            throw new IllegalArgumentException("Subject name already exists in this course");
        }

        Subject subject = new Subject(course, name, cleanText(request.getDescription()));
        return subjectRepository.save(subject);
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
