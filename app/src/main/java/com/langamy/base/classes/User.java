package com.langamy.base.classes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {

    public static final int USER_ID = 0;

    @PrimaryKey(autoGenerate = false)
    private int id;

    private String email;

    public User(String email) {
        this.email = email;
        this.id=USER_ID;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
