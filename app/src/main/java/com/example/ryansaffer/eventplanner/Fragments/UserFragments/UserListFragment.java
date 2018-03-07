package com.example.ryansaffer.eventplanner.Fragments.UserFragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.ViewHolder.UserViewHolder;
import com.example.ryansaffer.eventplanner.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by ryansaffer on 21/2/18.
 */

public abstract class UserListFragment extends Fragment {

    private static final String TAG = "UserListFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<User, UserViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_users, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.users_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        // set up FirebaseRecyclerAdapter with the query
        Query userQuery = getQuery(mDatabase);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                .setIndexedQuery(userQuery, ref, User.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                holder.bindToUser(model);
            }

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new UserViewHolder(inflater.inflate(R.layout.include_user, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public abstract Query getQuery(DatabaseReference reference);
}
