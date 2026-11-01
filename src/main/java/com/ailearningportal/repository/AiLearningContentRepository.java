package com.ailearningportal.repository;

import com.ailearningportal.entity.AiAudienceType;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiLearningContentRepository extends JpaRepository<AiLearningContent, Long> {

    Optional<AiLearningContent> findTopByLessonIdAndCreatedByUserIdAndContentTypeAndAudienceTypeOrderByCreatedAtDesc(
            Long lessonId,
            Long createdByUserId,
            AiContentType contentType,
            AiAudienceType audienceType
    );

    Optional<AiLearningContent> findTopByLessonIdAndContentTypeAndAudienceTypeAndApprovedTrueOrderByCreatedAtDesc(
            Long lessonId,
            AiContentType contentType,
            AiAudienceType audienceType
    );

    long countByCreatedByUserIdAndCreatedAtBetween(
            Long createdByUserId,
            LocalDateTime start,
            LocalDateTime end
    );
}
