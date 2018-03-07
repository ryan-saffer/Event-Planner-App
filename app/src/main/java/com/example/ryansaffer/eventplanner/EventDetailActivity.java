package com.example.ryansaffer.eventplanner;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryansaffer.eventplanner.Fragments.EventsFragments.UpcomingEventsFragment;
import com.example.ryansaffer.eventplanner.Fragments.UserFragments.AcceptedUsersFragment;
import com.example.ryansaffer.eventplanner.Fragments.UserFragments.DeclinedUsersFragment;
import com.example.ryansaffer.eventplanner.Fragments.UserFragments.InvitedUsersFragment;
import com.example.ryansaffer.eventplanner.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    public static final String EXTRA_EVENT_KEY = "event_key";

    private DatabaseReference mEventReference;
    private ValueEventListener mEventListener;
    private String mEventKey;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mDateTimeView;
    private RadioGroup mResponseRadioGroup;
    private RadioButton mAttendingRadiobutton;
    private RadioButton mRejectedRadioButton;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Get event key from Intent
        mEventKey = getIntent().getStringExtra(EXTRA_EVENT_KEY);
        if (mEventKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_EVENT_KEY");
        }

        // initialise the fragments
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    AcceptedUsersFragment.newInstance(mEventKey),
                    DeclinedUsersFragment.newInstance(mEventKey),
                    InvitedUsersFragment.newInstance(mEventKey)
            };
            private final String[] mFragmentNames = new String[] {
                    "Attending",
                    "Not Attending",
                    "Yet To Respond"
            };

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        mViewPager = findViewById(R.id.container_detail);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs_details);
        tabLayout.setupWithViewPager(mViewPager);

        // Initialize the database
        mEventReference = FirebaseDatabase.getInstance().getReference().child("events").child(mEventKey);

        // Initialize Views
        mAuthorView = findViewById(R.id.include_author_email);
        mTitleView = findViewById(R.id.post_title);
        mBodyView = findViewById(R.id.post_details);
        mDateTimeView = findViewById(R.id.post_date_time);
        mResponseRadioGroup = findViewById(R.id.response_radio_group);
        mAttendingRadiobutton = findViewById(R.id.radio_button_attending);
        mRejectedRadioButton = findViewById(R.id.radio_button_rejected);

        // set on click listeners for the radio buttons
        mResponseRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = mResponseRadioGroup.findViewById(checkedId);
                int index = mResponseRadioGroup.indexOfChild(radioButton);
                switch (index) {
                    case 0: // attending clicked
                        updateUserResponse(true);
                        break;
                    case 1: // rejected clicked
                        updateUserResponse(false);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // add value event listener to the post
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get Event object and use the values to update the UI
                Event event = dataSnapshot.getValue(Event.class);

                Calendar calendar = Calendar.getInstance();
                calendar.set(event.year,event.month,event.day,event.hour,event.minute);
                SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyy hh:mm a");

                mAuthorView.setText(event.author);
                mTitleView.setText(event.title);
                mBodyView.setText(event.details);
                mDateTimeView.setText(format.format(calendar.getTime()));

                updateSelectedRadioButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // getting post failed, log a message
                Log.w(TAG, "loadPost:onCancelled",databaseError.toException());
                Toast.makeText(EventDetailActivity.this, "Failed to load poast.",Toast.LENGTH_SHORT).show();
            }
        };
        mEventReference.addValueEventListener(eventListener);

        // Keep copy of post listener so we can remove it when app stops
        mEventListener = eventListener;
    }

    private void updateSelectedRadioButton() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("events")
                .child(mEventKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("accepted-users").child(uid).exists()) {
                            mAttendingRadiobutton.setChecked(true);
                        }
                        else if (dataSnapshot.child("rejected-users").child(uid).exists()) {
                            mRejectedRadioButton.setChecked(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "updateSelectedRadioButton:onCancelled", databaseError.toException());
                    }
                });
    }

    private void updateUserResponse(Boolean acceptedClicked) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("events")
                .child(mEventKey);
        if (acceptedClicked) {
            ref.child("accepted-users").child(uid).setValue(true);
            // don't forget to remove user from the declined/pending lists
            ref.child("rejected-users").child(uid).removeValue();
            ref.child("pending-users").child(uid).removeValue();
        }
        else {
            ref.child("rejected-users").child(uid).setValue(true);
            // dont' forget to remove user from the accepted/pending lists
            ref.child("accepted-users").child(uid).removeValue();
            ref.child("pending-users").child(uid).removeValue();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mEventListener != null) {
            mEventReference.removeEventListener(mEventListener);
        }
    }
}
