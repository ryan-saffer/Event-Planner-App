package com.example.ryansaffer.eventplanner.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class Event {

    public String uid;
    public String author;
    public String title;
    public String details;
    public int day;
    public int month;
    public int year;
    public int hour;
    public int minute;
    public ArrayList<User> invitedUsers;

    public Event() {}

    public Event(String uid, String author, String title, String details, int day, int month, int year, int hour, int minute) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.details = details;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("users");
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("details", details);
        result.put("day", day);
        result.put("month", month);
        result.put("year", year);
        result.put("hour", hour);
        result.put("minute", minute);
        result.put("invited-users", invitedUsers);

        return result;
    }
}
