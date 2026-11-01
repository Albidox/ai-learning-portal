package com.ailearningportal.service.impl;

import com.ailearningportal.dto.AttendanceRequest;
import com.ailearningportal.dto.AttendanceSummary;
import com.ailearningportal.entity.Attendance;
import com.ailearningportal.entity.AttendanceStatus;
import com.ailearningportal.entity.Course;
import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import com.ailearningportal.entity.Role;
import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AttendanceRepository;
import com.ailearningportal.repository.CourseRepository;
import com.ailearningportal.repository.EnrollmentRepository;
import com.ailearningportal.repository.UserRepository;
import com.ailearningportal.service.AttendanceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> listRecentAttendance() {
        return attendanceRepository.findRecentAttendance(PageRequest.of(0, 50));
    }

    @Override
    @Transactional
    public Attendance markAttendance(AttendanceRequest request, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.isActive()) {
            throw new IllegalArgumentException("Attendance can be marked only for active course");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Selected user is not a student");
        }

        if (request.getClassDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Class date cannot be in the future");
        }

        checkActiveEnrollment(student.getId(), course.getId());

        if (attendanceRepository.existsByCourseIdAndStudentIdAndClassDate(
                course.getId(),
                student.getId(),
                request.getClassDate()
        )) {
            throw new IllegalArgumentException("Attendance is already marked for this student, course, and date");
        }

        Attendance attendance = new Attendance(
                course,
                student,
                request.getClassDate(),
                request.getStatus(),
                teacher
        );

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummary> listStudentAttendanceSummary(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentAndStatusWithCourse(
                student.getId(),
                EnrollmentStatus.ACTIVE
        );

        return enrollments.stream()
                .map(enrollment -> buildSummary(student.getId(), enrollment.getCourse()))
                .toList();
    }

    private AttendanceSummary buildSummary(Long studentId, Course course) {
        long totalClasses = attendanceRepository.countByStudentIdAndCourseId(studentId, course.getId());
        long presentCount = attendanceRepository.countByStudentIdAndCourseIdAndStatus(
                studentId,
                course.getId(),
                AttendanceStatus.PRESENT
        );
        long absentCount = attendanceRepository.countByStudentIdAndCourseIdAndStatus(
                studentId,
                course.getId(),
                AttendanceStatus.ABSENT
        );
        double percentage = totalClasses == 0 ? 0 : (presentCount * 100.0) / totalClasses;

        return new AttendanceSummary(
                course.getId(),
                course.getName(),
                totalClasses,
                presentCount,
                absentCount,
                percentage
        );
    }

    private User getTeacherByEmail(String teacherEmail) {
        String normalizedEmail = teacherEmail == null ? "" : teacherEmail.trim().toLowerCase();
        User teacher = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Only teacher can mark attendance");
        }

        return teacher;
    }

    private User getStudentByEmail(String studentEmail) {
        String normalizedEmail = studentEmail == null ? "" : studentEmail.trim().toLowerCase();
        User student = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student account not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student can view attendance");
        }

        return student;
    }

    private void checkActiveEnrollment(Long studentId, Long courseId) {
        // Attendance is valid only when student is enrolled in the course.
        boolean enrolled = enrollmentRepository.existsByStudentAndCourseAndStatus(
                studentId,
                courseId,
                EnrollmentStatus.ACTIVE
        );

        if (!enrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }
    }
}
