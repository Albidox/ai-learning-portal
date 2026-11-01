package com.ailearningportal.repository;

import com.ailearningportal.entity.Attendance;
import com.ailearningportal.entity.AttendanceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByCourseIdAndStudentIdAndClassDate(Long courseId, Long studentId, LocalDate classDate);

    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, AttendanceStatus status);

    List<Attendance> findByStudentIdAndCourseIdOrderByClassDateDesc(Long studentId, Long courseId);

    @Query("""
            select a from Attendance a
            join fetch a.course c
            join fetch a.student s
            join fetch a.markedByTeacher t
            order by a.classDate desc, a.createdAt desc
            """)
    List<Attendance> findRecentAttendance(Pageable pageable);
}
