package com.ailearningportal.service;

import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.Lesson;

import java.util.List;

public interface StudentLearningService {

    List<Enrollment> listMyCourses(String studentEmail);

    List<Lesson> listPublishedLessonsForCourse(String studentEmail, Long courseId);

    Lesson getPublishedLessonForStudent(String studentEmail, Long lessonId);
}
