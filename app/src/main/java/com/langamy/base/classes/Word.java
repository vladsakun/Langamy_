package com.langamy.base.classes;

import java.io.Serializable;

public class Word implements Serializable {

    private String term;
    private String translation;
    private boolean firstStage;
    private boolean secondStage;
    private boolean thirdStage;
    private boolean forthStage;
    private boolean marked;

    public Word(String term, String translation) {
        this.term = term;
        this.translation = translation;
        this.firstStage = false;
        this.secondStage = false;
        this.thirdStage = false;
        this.forthStage = false;
    }

    public Word(String term, String translation, boolean firstStage, boolean secondStage, boolean thirdStage, boolean forthStage) {
        this.term = term;
        this.translation = translation;
        this.firstStage = firstStage;
        this.secondStage = secondStage;
        this.thirdStage = thirdStage;
        this.forthStage = forthStage;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public void setFirstStage(boolean firstStage) {
        this.firstStage = firstStage;
    }

    public void setSecondStage(boolean secondStage) {
        this.secondStage = secondStage;
    }

    public void setThirdStage(boolean thirdStage) {
        this.thirdStage = thirdStage;
    }

    public boolean isFirstStage() {
        return firstStage;
    }

    public boolean isSecondStage() {
        return secondStage;
    }

    public boolean isThirdStage() {
        return thirdStage;
    }

    public boolean isForthStage() {
        return forthStage;
    }

    public void setForthStage(boolean forthStage) {
        this.forthStage = forthStage;
    }

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
}
