package com.langamy.base.classes;

public class CardItem {

    private String term;
    private String translation;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public CardItem(String term, String translation) {
        this.term = term;
        this.translation = translation;
    }
}
