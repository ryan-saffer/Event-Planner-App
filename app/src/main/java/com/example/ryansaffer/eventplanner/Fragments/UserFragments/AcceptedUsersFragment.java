package com.example.ryansaffer.eventplanner.Fragments.UserFragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Ryan on 05-Mar-18.
 */

public class AcceptedUsersFragment extends UserListFragment {

    public static AcceptedUsersFragment newInstance(String eventid) {
        AcceptedUsersFragment fragment = new AcceptedUsersFragment();
        Bundle args = new Bundle();
        args.putString("eventid", eventid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Query getQuery(DatabaseReference reference) {
        String eventKey = getArguments().getString("eventid");
        return reference.child("responses")
                .child(eventKey)
                .orderByChild("status")
                .equalTo("accepted");
    }
}
