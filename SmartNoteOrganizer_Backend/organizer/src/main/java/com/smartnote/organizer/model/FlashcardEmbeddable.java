package com.smartnote.organizer.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class FlashcardEmbeddable {

    private String question;
    private String answer;

    public FlashcardEmbeddable() {}

    public FlashcardEmbeddable(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}