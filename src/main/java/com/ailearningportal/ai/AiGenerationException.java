package com.ailearningportal.ai;

public class AiGenerationException extends RuntimeException {

    public static final String FRIENDLY_MESSAGE = "AI generation is temporarily unavailable. Please try again later.";

    public AiGenerationException() {
        super(FRIENDLY_MESSAGE);
    }

    public AiGenerationException(Throwable cause) {
        super(FRIENDLY_MESSAGE, cause);
    }
}
