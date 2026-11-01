package com.ailearningportal.repository;

import com.ailearningportal.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    Optional<Lesson> findByIdAndTeacherId(Long id, Long teacherId);

    boolean existsBySubjectIdAndTitleIgnoreCase(Long subjectId, String title);

    @Query("""
            select l from Lesson l
            join fetch l.subject s
            join fetch s.course c
            join fetch l.teacher t
            where c.id = :courseId
              and l.published = true
            order by l.publishedAt desc
            """)
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            select l from Lesson l
            join fetch l.subject s
            join fetch s.course c
            join fetch l.teacher t
            where l.id = :lessonId
              and l.published = true
            """)
    Optional<Lesson> findPublishedLessonById(@Param("lessonId") Long lessonId);
}
