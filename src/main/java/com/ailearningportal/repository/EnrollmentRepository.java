package com.ailearningportal.repository;

import com.ailearningportal.entity.Enrollment;
import com.ailearningportal.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    List<Enrollment> findAllByOrderByEnrolledAtDesc();

    @Query("""
            select e from Enrollment e
            join fetch e.course c
            where e.student.id = :studentId
              and e.status = :status
            order by c.name asc
            """)
    List<Enrollment> findByStudentAndStatusWithCourse(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status
    );

    @Query("""
            select count(e) > 0 from Enrollment e
            where e.student.id = :studentId
              and e.course.id = :courseId
              and e.status = :status
            """)
    boolean existsByStudentAndCourseAndStatus(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status
    );
}
