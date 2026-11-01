package com.ailearningportal.ai;

import com.ailearningportal.dto.AiContentView;
import com.ailearningportal.entity.AiAudienceType;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import com.ailearningportal.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiResponseValidatorTests {

    private final AiResponseValidator validator = new AiResponseValidator(new ObjectMapper());

    @Test
    void validSummaryShouldNormalizeJson() {
        String rawJson = """
                {
                  "summary": "This is a short summary.",
                  "importantPoints": ["Point 1"],
                  "keyTerms": ["Controller - handles request"]
                }
                """;

        String normalized = validator.validateAndNormalize(AiContentType.SUMMARY, rawJson);

        assertTrue(normalized.contains("This is a short summary."));
    }

    @Test
    void invalidQuizShouldThrowFriendlyAiException() {
        String rawJson = """
                {
                  "questions": [
                    {
                      "question": "What is MVC?"
                    }
                  ]
                }
                """;

        assertThrows(
                AiGenerationException.class,
                () -> validator.validateAndNormalize(AiContentType.QUIZ, rawJson)
        );
    }

    @Test
    void toViewShouldReadQuizQuestions() {
        User teacher = new User();
        teacher.setName("Teacher User");

        AiLearningContent content = new AiLearningContent();
        content.setId(5L);
        content.setCreatedByUser(teacher);
        content.setContentType(AiContentType.QUIZ);
        content.setAudienceType(AiAudienceType.CLASS_SHARED);
        content.setOutputJson("""
                {
                  "questions": [
                    {
                      "question": "What does controller do?",
                      "options": ["Handles request", "Stores file"],
                      "correctAnswer": "Handles request",
                      "explanation": "Controller receives web requests."
                    }
                  ]
                }
                """);

        AiContentView view = validator.toView(content);

        assertEquals(1, view.getQuestions().size());
        assertEquals("What does controller do?", view.getQuestions().get(0).getQuestion());
    }
}
