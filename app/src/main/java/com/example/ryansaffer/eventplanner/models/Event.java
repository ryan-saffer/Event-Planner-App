package com.example.ryansaffer.eventplanner.models;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private static final String TAG = "Event";

    public String uid;
    public String author;
    public String title;
    public String details;
    public int day;
    public int month;
    public int year;
    public int hour;
    public int minute;
    public HashMap<String, String> invitedUsers;

    public Event() {}

    public Event(String uid, String author, String title, String details, HashMap<String, String> invitedUsers, int day, int month, int year, int hour, int minute) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.details = details;
        this.invitedUsers = invitedUsers;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
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
