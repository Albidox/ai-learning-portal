package com.ailearningportal.service.impl;

import com.ailearningportal.dto.AssignmentRequest;
import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AssignmentRepository;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> listAssignmentsForTeacher(String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        return assignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId());
    }

    @Override
    @Transactional
    public Assignment createAssignment(AssignmentRequest request, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.isActive()) {
            throw new IllegalArgumentException("Assignment can be added only to active course");
        }

        if (request.getDueDate() == null || request.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        if (request.getMaxMarks() == null || request.getMaxMarks() <= 0) {
            throw new IllegalArgumentException("Max marks must be greater than 0");
        }

        Assignment assignment = new Assignment(
                course,
                teacher,
                request.getTitle().trim(),
                request.getInstructions().trim(),
                request.getDueDate(),
                request.getMaxMarks(),
                request.isActive()
        );

        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignmentForTeacher(String teacherEmail, Long assignmentId) {
        User teacher = getTeacherByEmail(teacherEmail);
        Assignment assignment = assignmentRepository.findByIdWithCourseAndTeacher(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Assignment not found for this teacher");
        }

        return assignment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> listAssignmentsForStudent(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        return assignmentRepository.findActiveAssignmentsForStudent(student.getId(), EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getActiveAssignmentForStudent(String studentEmail, Long assignmentId) {
        User student = getStudentByEmail(studentEmail);
        Assignment assignment = assignmentRepository.findByIdWithCourseAndTeacher(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException("Assignment is not active");
        }

        checkActiveEnrollment(student.getId(), assignment.getCourse().getId());

        return assignment;
    }

    private User getTeacherByEmail(String teacherEmail) {
        String normalizedEmail = teacherEmail == null ? "" : teacherEmail.trim().toLowerCase();
        User teacher = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Only teacher can manage assignments");
        }

        return teacher;
    }

    private User getStudentByEmail(String studentEmail) {
        String normalizedEmail = studentEmail == null ? "" : studentEmail.trim().toLowerCase();
        User student = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student account not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student can view assignments");
        }

        return student;
    }

    private void checkActiveEnrollment(Long studentId, Long courseId) {
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
