package com.example.ryansaffer.eventplanner.Fragments.EventsFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;

import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.ViewHolder.PostViewHolder;
import com.example.ryansaffer.eventplanner.EventDetailActivity;
import com.example.ryansaffer.eventplanner.models.Event;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;


public abstract class PostListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "PostListFragment";

    private DatabaseReference mDatabase;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseRecyclerAdapter<Event, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public PostListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_event);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecycler = rootView.findViewById(R.id.events_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up layout manager, reverse layout
        mManager = new LinearLayoutManager(getActivity()) {
            @Override
            public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
                super.onAdapterChanged(oldAdapter, newAdapter);

            }
        };
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        reloadRecyclerViewData();
    }

    @Override
    public void onRefresh() {
        reloadRecyclerViewData();
    }

    public void reloadRecyclerViewData() {
        mSwipeRefreshLayout.setRefreshing(true);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(postsQuery, Event.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Event, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Event model) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String eventKey = postRef.getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Launch EventDetailActivity
                        Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                        intent.putExtra(EventDetailActivity.EXTRA_EVENT_KEY, eventKey);
                        intent.putExtra(EventDetailActivity.EXTRA_AUTHOR_ID, model.uid);
                        startActivity(intent);
                    }
                });
                holder.bindToEvent(eventKey, model);
            }

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, parent, false));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        mRecycler.setAdapter(mAdapter);
        mAdapter.startListening();
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public abstract Query getQuery(DatabaseReference databaseReference);
}
