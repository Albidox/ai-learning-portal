package com.ailearningportal.repository;

import com.ailearningportal.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByCourseIdAndNameIgnoreCase(Long courseId, String name);

    Optional<Subject> findByCourseIdAndNameIgnoreCase(Long courseId, String name);

    List<Subject> findAllByOrderByCourseNameAscNameAsc();

    List<Subject> findByCourseActiveTrueOrderByCourseNameAscNameAsc();
}
