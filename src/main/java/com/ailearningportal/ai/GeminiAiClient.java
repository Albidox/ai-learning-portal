package com.ailearningportal.ai;

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiAiClient {

    private final String apiKey;
    private final String modelName;

    public GeminiAiClient(
            @Value("${spring.ai.google.genai.api-key:}") String apiKey,
            @Value("${spring.ai.google.genai.chat.options.model:gemini-3.5-flash-lite}") String modelName
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelName = hasText(modelName) ? modelName.trim() : "gemini-3.5-flash-lite";
    }

    public String generateJson(String prompt) {
        if (!hasText(apiKey)) {
            throw new AiGenerationException();
        }

        try (Client client = Client.builder().apiKey(apiKey).build()) {
            GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                    .model(modelName)
                    .temperature(0.2)
                    .responseMimeType("application/json")
                    .build();

            GoogleGenAiChatModel chatModel = GoogleGenAiChatModel.builder()
                    .genAiClient(client)
                    .defaultOptions(options)
                    .build();

            ChatResponse response = chatModel.call(new Prompt(prompt));
            String text = response.getResult().getOutput().getText();

            if (!hasText(text)) {
                throw new AiGenerationException();
            }

            return text.trim();
        } catch (AiGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiGenerationException(ex);
        }
    }

    public String getModelName() {
        return modelName;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
