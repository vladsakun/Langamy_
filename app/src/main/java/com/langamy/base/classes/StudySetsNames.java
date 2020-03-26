package com.langamy.base.classes;

import com.google.gson.annotations.SerializedName;

public class StudySetsNames {

    private String name;
    private int id;
    @SerializedName("amount_of_words")
    private String amountOfWords;

    public int getId() {
        return id;
    }

    public String getAmountOfWords() {
        return amountOfWords;
    }

    public String getName() {
        return name;
    }

    public StudySetsNames(String name, String amountOfWords, int id) {
        this.name = name;
        this.amountOfWords = amountOfWords;
        this.id = id;
    }
}
