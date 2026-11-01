package com.ailearningportal.ai;

import com.ailearningportal.entity.User;
import com.ailearningportal.repository.AiLearningContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AiUsageService {

    private final AiLearningContentRepository aiLearningContentRepository;
    private final int dailyLimit;

    public AiUsageService(
            AiLearningContentRepository aiLearningContentRepository,
            @Value("${app.ai.daily-limit:5}") int dailyLimit
    ) {
        this.aiLearningContentRepository = aiLearningContentRepository;
        this.dailyLimit = Math.max(1, dailyLimit);
    }

    public void checkDailyLimit(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long usedToday = aiLearningContentRepository.countByCreatedByUserIdAndCreatedAtBetween(
                user.getId(),
                start,
                end
        );

        if (usedToday >= dailyLimit) {
            throw new IllegalArgumentException("Daily AI limit reached. Please try tomorrow.");
        }
    }
}
