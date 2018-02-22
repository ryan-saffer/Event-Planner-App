package com.example.ryansaffer.eventplanner.Fragments.EventsFragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class UpcomingEventsFragment extends PostListFragment {

    public UpcomingEventsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All upcoming posts
        return databaseReference.child("posts");
    }
}
