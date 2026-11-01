package com.ailearningportal.service.impl;

import com.ailearningportal.dto.EnrollmentRequest;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentServiceImpl(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> listEnrollments() {
        return enrollmentRepository.findAllByOrderByEnrolledAtDesc();
    }

    @Override
    @Transactional
    public Enrollment createEnrollment(EnrollmentRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.isActive()) {
            throw new IllegalArgumentException("Student can be enrolled only in active course");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Selected user is not a student");
        }

        if (enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), student.getId())) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(course, student, EnrollmentStatus.ACTIVE);
        return enrollmentRepository.save(enrollment);
    }
}
