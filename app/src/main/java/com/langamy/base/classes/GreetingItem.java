package com.langamy.base.classes;

import android.graphics.drawable.Drawable;

public class GreetingItem {

    Drawable image;
    String text;

    public GreetingItem(Drawable image, String text) {
        this.image = image;
        this.text = text;
    }

    public Drawable getImage() {
        return image;
    }

    public String getText() {
        return text;
    }
}
