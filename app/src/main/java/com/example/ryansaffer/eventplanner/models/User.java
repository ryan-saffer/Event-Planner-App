package com.example.ryansaffer.eventplanner.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class User {

    public String username;
    public String email;

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", this.username);
        result.put("email", this.email);
        return result;
    }
}
