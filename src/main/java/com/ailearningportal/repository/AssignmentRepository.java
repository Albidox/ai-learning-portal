package com.ailearningportal.repository;

import com.ailearningportal.entity.Assignment;
import com.ailearningportal.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);

    @Query("""
            select a from Assignment a
            join fetch a.course c
            join fetch a.teacher t
            where a.id = :assignmentId
            """)
    Optional<Assignment> findByIdWithCourseAndTeacher(@Param("assignmentId") Long assignmentId);

    @Query("""
            select a from Assignment a
            join fetch a.course c
            join fetch a.teacher t
            where a.active = true
              and exists (
                  select e.id from Enrollment e
                  where e.course = c
                    and e.student.id = :studentId
                    and e.status = :status
              )
            order by a.dueDate asc, a.createdAt desc
            """)
    List<Assignment> findActiveAssignmentsForStudent(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status
    );
}
