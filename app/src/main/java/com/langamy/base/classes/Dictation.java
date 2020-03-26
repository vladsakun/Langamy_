package com.langamy.base.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Dictation implements Serializable {

    private String creator;
    private String name, type_of_questions;
    private String words, marked_words, language_from, language_to;
    private int amount_of_words;
    private int amount_of_words_for_dictation, id;
    private String code;
    private ArrayList<Integer> members;
    private int question_time;

    public Dictation(String creator, String name, String words, String marked_words, int amount_of_words, int amount_of_words_for_dictation,
                     String type_of_questions) {
        this.creator = creator;
        this.name = name;
        this.words = words;
        this.marked_words = marked_words;
        this.amount_of_words = amount_of_words;
        this.amount_of_words_for_dictation = amount_of_words_for_dictation;
        this.type_of_questions = type_of_questions;
    }

    public String getLanguage_from() {
        return language_from;
    }

    public void setLanguage_from(String language_from) {
        this.language_from = language_from;
    }

    public String getLanguage_to() {
        return language_to;
    }

    public void setLanguage_to(String language_to) {
        this.language_to = language_to;
    }

    public String getType_of_questions() {
        return type_of_questions;
    }

    public ArrayList getMembers() {
        return members;
    }

    public String getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public String getWords() {
        return words;
    }

    public String getMarked_words() {
        return marked_words;
    }

    public int getAmount_of_words() {
        return amount_of_words;
    }

    public int getAmount_of_words_for_dictation() {
        return amount_of_words_for_dictation;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setQuestion_time(int question_time) {
        this.question_time = question_time;
    }

    public int getQuestion_time() {
        return question_time;
    }
}
