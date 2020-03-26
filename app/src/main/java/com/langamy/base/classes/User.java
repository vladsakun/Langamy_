package com.langamy.base.classes;

public class User {

    String email, marks;

    public String getMarks() {
        return marks;
    }

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
