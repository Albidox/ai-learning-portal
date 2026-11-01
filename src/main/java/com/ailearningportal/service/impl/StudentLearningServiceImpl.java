package com.ailearningportal.service.impl;

import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Lesson;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.LessonRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.StudentLearningService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentLearningServiceImpl implements StudentLearningService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;

    public StudentLearningServiceImpl(
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository
    ) {
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> listMyCourses(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        return enrollmentRepository.findByStudentAndStatusWithCourse(student.getId(), EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> listPublishedLessonsForCourse(String studentEmail, Long courseId) {
        User student = getStudentByEmail(studentEmail);
        checkActiveEnrollment(student.getId(), courseId);
        return lessonRepository.findPublishedLessonsByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson getPublishedLessonForStudent(String studentEmail, Long lessonId) {
        User student = getStudentByEmail(studentEmail);
        Lesson lesson = lessonRepository.findPublishedLessonById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        Long courseId = lesson.getSubject().getCourse().getId();
        checkActiveEnrollment(student.getId(), courseId);

        return lesson;
    }

    private User getStudentByEmail(String studentEmail) {
        String normalizedEmail = studentEmail == null ? "" : studentEmail.trim().toLowerCase();
        User student = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student account not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student can view learning pages");
        }

        return student;
    }

    private void checkActiveEnrollment(Long studentId, Long courseId) {
        // Student can open a course only when admin enrolled the student.
        boolean enrolled = enrollmentRepository.existsByStudentAndCourseAndStatus(
                studentId,
                courseId,
                EnrollmentStatus.ACTIVE
        );

        if (!enrolled) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }
    }
}
