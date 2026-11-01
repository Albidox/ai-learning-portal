package com.ailearningportal.ai;

import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.Lesson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiPromptService {

    private final int maxLessonCharacters;

    public AiPromptService(@Value("${app.ai.max-lesson-characters:8000}") int maxLessonCharacters) {
        this.maxLessonCharacters = Math.max(1000, maxLessonCharacters);
    }

    public String buildPrompt(Lesson lesson, AiContentType contentType) {
        String lessonContent = trimLessonContent(lesson.getContent());

        if (contentType == AiContentType.SUMMARY) {
            return """
                    You are helping beginner students understand a lesson.
                    Use simple English.
                    Return only valid JSON.
                    Do not use markdown.
                    JSON format:
                    {
                      "summary": "short lesson summary",
                      "importantPoints": ["point 1", "point 2", "point 3"],
                      "keyTerms": ["term - simple meaning"]
                    }

                    Lesson title:
                    %s

                    Lesson content:
                    %s
                    """.formatted(lesson.getTitle(), lessonContent);
        }

        return """
                You are helping beginner students practice a lesson.
                Use simple English.
                Return only valid JSON.
                Do not use markdown.
                Create 5 multiple choice questions.
                JSON format:
                {
                  "questions": [
                    {
                      "question": "question text",
                      "options": ["option A", "option B", "option C", "option D"],
                      "correctAnswer": "exact correct option text",
                      "explanation": "short reason"
                    }
                  ]
                }

                Lesson title:
                %s

                Lesson content:
                %s
                """.formatted(lesson.getTitle(), lessonContent);
    }

    private String trimLessonContent(String content) {
        String safeContent = content == null ? "" : content.trim();
        if (safeContent.length() <= maxLessonCharacters) {
            return safeContent;
        }
        // Keep prompts small for student accounts and low-cost API usage.
        return safeContent.substring(0, maxLessonCharacters);
    }
}
