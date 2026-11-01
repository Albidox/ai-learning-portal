package com.ailearningportal.service.impl;

import com.ailearningportal.dto.LessonRequest;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.Subject;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.SubjectRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public LessonServiceImpl(
            LessonRepository lessonRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> listLessonsForTeacher(String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        return lessonRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId());
    }

    @Override
    @Transactional
    public Lesson createLesson(LessonRequest request, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (!subject.getCourse().isActive()) {
            throw new IllegalArgumentException("Lesson can be added only to active course subject");
        }

        Lesson lesson = new Lesson(
                subject,
                teacher,
                request.getTitle().trim(),
                request.getContent().trim()
        );

        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public Lesson publishLesson(Long lessonId, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        Lesson lesson = lessonRepository.findByIdAndTeacherId(lessonId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found for this teacher"));

        if (!lesson.isPublished()) {
            lesson.setPublished(true);
            lesson.setPublishedAt(LocalDateTime.now());
        }

        return lessonRepository.save(lesson);
    }

    private User getTeacherByEmail(String teacherEmail) {
        String normalizedEmail = teacherEmail == null ? "" : teacherEmail.trim().toLowerCase();
        User teacher = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Only teacher can manage lessons");
        }

        return teacher;
    }
}
