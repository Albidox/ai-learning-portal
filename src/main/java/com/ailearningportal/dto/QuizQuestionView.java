package com.ailearningportal.dto;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionView {

    private String question;
    private List<String> options = new ArrayList<>();
    private String correctAnswer;
    private String explanation;

    public QuizQuestionView() {
    }

    public QuizQuestionView(String question, List<String> options, String correctAnswer, String explanation) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
