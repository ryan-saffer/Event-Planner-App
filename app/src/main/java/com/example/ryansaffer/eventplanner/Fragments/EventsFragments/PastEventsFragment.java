package com.example.ryansaffer.eventplanner.Fragments.EventsFragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by ryansaffer on 21/2/18.
 */

public class PastEventsFragment extends PostListFragment {

    public PastEventsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All Past Events
        return databaseReference.child("events");
    }
}
