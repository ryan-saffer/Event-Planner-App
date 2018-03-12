package com.example.ryansaffer.eventplanner.Fragments.UserFragments;

import android.os.Bundle;

import com.example.ryansaffer.eventplanner.models.Event;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * Created by andre on 24/02/2018.
 */

public class InvitedUsersFragment extends UserListFragment {

    private static final String TAG = "InvitedUsersFragment";

    public static InvitedUsersFragment newInstance(String eventid) {
        InvitedUsersFragment fragment = new InvitedUsersFragment();
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
                .equalTo(Event.Response.PENDING.toString());
    }
}
