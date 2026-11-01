package com.ailearningportal.repository;

import com.ailearningportal.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("""
            select s.assignment.id from Submission s
            where s.student.id = :studentId
            """)
    List<Long> findSubmittedAssignmentIdsByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select s from Submission s
            join fetch s.assignment a
            join fetch a.course c
            join fetch a.teacher t
            join fetch s.student st
            left join fetch s.mark m
            where a.id = :assignmentId
            order by s.submittedAt desc
            """)
    List<Submission> findByAssignmentIdWithDetails(@Param("assignmentId") Long assignmentId);

    @Query("""
            select s from Submission s
            join fetch s.assignment a
            join fetch a.course c
            join fetch a.teacher t
            join fetch s.student st
            left join fetch s.mark m
            where s.id = :submissionId
            """)
    Optional<Submission> findByIdWithDetails(@Param("submissionId") Long submissionId);

    @Query("""
            select s from Submission s
            join fetch s.assignment a
            join fetch a.course c
            join fetch a.teacher t
            join fetch s.student st
            left join fetch s.mark m
            where st.id = :studentId
            order by s.submittedAt desc
            """)
    List<Submission> findByStudentIdWithDetails(@Param("studentId") Long studentId);
}
