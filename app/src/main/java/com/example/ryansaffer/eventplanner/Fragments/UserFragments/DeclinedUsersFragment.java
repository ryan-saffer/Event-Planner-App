package com.example.ryansaffer.eventplanner.Fragments.UserFragments;

import android.os.Bundle;

import com.example.ryansaffer.eventplanner.models.Event;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Ryan on 05-Mar-18.
 */

public class DeclinedUsersFragment extends UserListFragment {

    public static DeclinedUsersFragment newInstance(String eventid) {
        DeclinedUsersFragment fragment = new DeclinedUsersFragment();
        Bundle args = new Bundle();
        args.putString("eventid", eventid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Query getQuery(DatabaseReference reference) {
        String eventKey = getArguments().getString("eventid");
        return reference.child("events").child(eventKey).child("userResponses")
                .orderByValue()
                .equalTo(Event.Response.NOT_ATTENDING.toString());
    }
}
