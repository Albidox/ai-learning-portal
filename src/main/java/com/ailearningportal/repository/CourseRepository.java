package com.ailearningportal.repository;

import com.ailearningportal.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Course> findByNameIgnoreCase(String name);

    List<Course> findAllByOrderByNameAsc();

    List<Course> findByActiveTrueOrderByNameAsc();
}
