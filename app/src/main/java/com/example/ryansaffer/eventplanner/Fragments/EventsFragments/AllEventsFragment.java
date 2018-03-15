package com.example.ryansaffer.eventplanner.Fragments.EventsFragments;

import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class AllEventsFragment extends PostListFragment {

    public AllEventsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // All upcoming posts
        return databaseReference.child("events")
                .orderByChild("userResponses/" + uid)
                .startAt(1); // using this to return only events where this location has an existing string
    }
}
