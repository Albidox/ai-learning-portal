package com.ailearningportal.ai;

import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.Lesson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptServiceTests {

    @Test
    void summaryPromptShouldUseLessonContentAndAskForJson() {
        AiPromptService service = new AiPromptService(8000);
        Lesson lesson = lesson();

        String prompt = service.buildPrompt(lesson, AiContentType.SUMMARY);

        assertTrue(prompt.contains("Return only valid JSON"));
        assertTrue(prompt.contains("Spring Boot MVC"));
        assertTrue(prompt.contains("Controller reads request and returns page."));
        assertTrue(prompt.contains("importantPoints"));
    }

    @Test
    void promptShouldTrimVeryLongLessonContent() {
        AiPromptService service = new AiPromptService(1000);
        Lesson lesson = lesson();
        lesson.setContent("a".repeat(1500));

        String prompt = service.buildPrompt(lesson, AiContentType.QUIZ);

        assertTrue(prompt.contains("questions"));
        assertFalse(prompt.contains("a".repeat(1200)));
    }

    private Lesson lesson() {
        Lesson lesson = new Lesson();
        lesson.setTitle("Spring Boot MVC");
        lesson.setContent("Controller reads request and returns page.");
        return lesson;
    }
}
