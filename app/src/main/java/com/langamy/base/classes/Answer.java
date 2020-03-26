package com.langamy.base.classes;

import java.io.Serializable;

public class Answer implements Serializable {

    private String userAnswer, correctAnswer, term;
    private boolean status;

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getTerm() {
        return term;
    }

    public boolean getStatus() {
        return status;
    }

    public Answer(String userAnswer, String correctAnswer, String term, boolean status) {
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.term = term;
        this.status = status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
