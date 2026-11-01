package com.ailearningportal.service;

import com.ailearningportal.dto.LessonRequest;
import com.ailearningportal.entity.Lesson;

import java.util.List;

public interface LessonService {

    List<Lesson> listLessonsForTeacher(String teacherEmail);

    Lesson createLesson(LessonRequest request, String teacherEmail);

    Lesson publishLesson(Long lessonId, String teacherEmail);
}
