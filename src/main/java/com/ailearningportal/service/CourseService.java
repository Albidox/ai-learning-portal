package com.ailearningportal.service;

import com.ailearningportal.dto.CourseRequest;
import com.ailearningportal.dto.SubjectRequest;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Subject;

import java.util.List;

public interface CourseService {

    List<Course> listCourses();

    List<Course> listActiveCourses();

    Course createCourse(CourseRequest request);

    List<Subject> listSubjects();

    List<Subject> listActiveSubjects();

    Subject createSubject(SubjectRequest request);
}
