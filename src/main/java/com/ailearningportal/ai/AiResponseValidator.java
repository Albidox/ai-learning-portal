package com.ailearningportal.ai;

import com.ailearningportal.dto.AiContentView;
import com.ailearningportal.dto.QuizQuestionView;
import com.ailearningportal.entity.AiContentType;
import com.ailearningportal.entity.AiLearningContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiResponseValidator {

    private final ObjectMapper objectMapper;

    public AiResponseValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String validateAndNormalize(AiContentType contentType, String rawOutput) {
        try {
            JsonNode root = objectMapper.readTree(cleanJson(rawOutput));

            if (contentType == AiContentType.SUMMARY) {
                validateSummary(root);
            } else {
                validateQuiz(root);
            }

            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new AiGenerationException(ex);
        }
    }

    public AiContentView toView(AiLearningContent content) {
        if (content == null) {
            return AiContentView.empty(null);
        }

        AiContentView view = new AiContentView();
        view.setPresent(true);
        view.setId(content.getId());
        view.setContentType(content.getContentType());
        view.setAudienceType(content.getAudienceType());
        view.setApproved(content.isApproved());
        view.setCreatedByName(content.getCreatedByUser().getName());
        view.setCreatedAt(content.getCreatedAt());
        view.setApprovedAt(content.getApprovedAt());

        try {
            JsonNode root = objectMapper.readTree(content.getOutputJson());
            if (content.getContentType() == AiContentType.SUMMARY) {
                view.setSummary(text(root, "summary"));
                view.setImportantPoints(readStringList(root, "importantPoints"));
                view.setKeyTerms(readStringList(root, "keyTerms"));
            } else {
                view.setQuestions(readQuestions(root));
            }
        } catch (JsonProcessingException ex) {
            view.setSummary("Saved AI output is not in the expected format.");
        }

        return view;
    }

    private void validateSummary(JsonNode root) {
        requireText(root, "summary");
        requireStringArray(root, "importantPoints", 1);
        requireStringArray(root, "keyTerms", 1);
    }

    private void validateQuiz(JsonNode root) {
        JsonNode questions = root.get("questions");
        if (questions == null || !questions.isArray() || questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz questions are missing");
        }

        for (JsonNode question : questions) {
            requireText(question, "question");
            requireStringArray(question, "options", 2);
            requireText(question, "correctAnswer");
            requireText(question, "explanation");
        }
    }

    private List<QuizQuestionView> readQuestions(JsonNode root) {
        List<QuizQuestionView> questions = new ArrayList<>();
        JsonNode questionNodes = root.get("questions");
        if (questionNodes == null || !questionNodes.isArray()) {
            return questions;
        }

        for (JsonNode questionNode : questionNodes) {
            questions.add(new QuizQuestionView(
                    text(questionNode, "question"),
                    readStringList(questionNode, "options"),
                    text(questionNode, "correctAnswer"),
                    text(questionNode, "explanation")
            ));
        }

        return questions;
    }

    private void requireText(JsonNode root, String fieldName) {
        if (!hasText(text(root, fieldName))) {
            throw new IllegalArgumentException(fieldName + " is missing");
        }
    }

    private void requireStringArray(JsonNode root, String fieldName, int minimumSize) {
        JsonNode array = root.get(fieldName);
        if (array == null || !array.isArray() || array.size() < minimumSize) {
            throw new IllegalArgumentException(fieldName + " is missing");
        }

        for (JsonNode value : array) {
            if (!value.isTextual() || !hasText(value.asText())) {
                throw new IllegalArgumentException(fieldName + " has empty value");
            }
        }
    }

    private List<String> readStringList(JsonNode root, String fieldName) {
        List<String> values = new ArrayList<>();
        JsonNode array = root.get(fieldName);
        if (array == null || !array.isArray()) {
            return values;
        }

        for (JsonNode value : array) {
            if (value.isTextual()) {
                values.add(value.asText());
            }
        }

        return values;
    }

    private String text(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node != null && node.isTextual() ? node.asText() : "";
    }

    private String cleanJson(String rawOutput) {
        String cleaned = rawOutput == null ? "" : rawOutput.trim();
        if (!cleaned.startsWith("```")) {
            return cleaned;
        }

        int firstNewLine = cleaned.indexOf('\n');
        int lastFence = cleaned.lastIndexOf("```");

        if (firstNewLine >= 0 && lastFence > firstNewLine) {
            return cleaned.substring(firstNewLine + 1, lastFence).trim();
        }

        return cleaned;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
