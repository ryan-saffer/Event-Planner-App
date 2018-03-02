package com.example.ryansaffer.eventplanner.Fragments.UserFragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by andre on 24/02/2018.
 */

public class InvitedUsersFragment extends UserListFragment {
    @Override
    public Query getQuery(DatabaseReference reference) {
        return reference.child("users");
    }
}
