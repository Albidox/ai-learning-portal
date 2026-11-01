package com.ailearningportal.repository;

import com.ailearningportal.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    Optional<Mark> findBySubmissionId(Long submissionId);
}
