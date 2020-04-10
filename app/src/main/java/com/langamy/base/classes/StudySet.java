package com.langamy.base.classes;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "study_set_table")
public class StudySet implements Serializable {

    @PrimaryKey
    private int id;

    private String creator;
    private String name;
    private String words, marked_words;
    private String language_to;
    private String language_from;
    private boolean sync_status;
    private int amount_of_words;

    public void setWords(String words) {
        this.words = words;
    }

    public void setAmount_of_words(int amount_of_words) {
        this.amount_of_words = amount_of_words;
    }

    public String getMarked_words() {
        return marked_words;
    }

    public void setMarked_words(String marked_words) {
        this.marked_words = marked_words;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getWords() {
        return words;
    }

    public int getAmount_of_words() {
        return amount_of_words;
    }

    public String getLanguage_to() {
        return language_to;
    }

    public String getLanguage_from() {
        return language_from;
    }

    public String getCreator() {
        return creator;
    }

    public StudySet(String creator, String name, String words, String language_to, String language_from, int amount_of_words) {
        this.creator = creator;
        this.name = name;
        this.words = words;
        this.language_to = language_to;
        this.language_from = language_from;
        this.amount_of_words = amount_of_words;
    }
    @Ignore
    public StudySet(String words, int amount_of_words){
        this.words = words;
        this.amount_of_words = amount_of_words;
    }
    @Ignore
    public StudySet(int id, String words, int amount_of_words){
        this.words = words;
        this.id = id;
        this.amount_of_words = amount_of_words;
    }

    public boolean isSync_status() {
        return sync_status;
    }

    public void setSync_status(boolean sync_status) {
        this.sync_status = sync_status;
    }
}
