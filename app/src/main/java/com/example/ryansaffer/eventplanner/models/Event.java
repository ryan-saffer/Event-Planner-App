package com.example.ryansaffer.eventplanner.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class Event {

    private static final String TAG = "Event";

    public enum Response {
        ATTENDING,
        NOT_ATTENDING,
        PENDING
    }

    public String uid;
    public String author;
    public String title;
    public String details;
    public int day;
    public int month;
    public int year;
    public int hour;
    public int minute;
    public HashMap<String, Response> userResponses;

    public Event() {}

    public Event(String uid, String author, String title, String details, HashMap<String, Response> userResponses, int day, int month, int year, int hour, int minute) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.details = details;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.userResponses = userResponses;
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
        result.put("userResponses", userResponses);

        return result;
    }
}
