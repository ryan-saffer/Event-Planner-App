package com.example.ryansaffer.eventplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    public static final String EXTRA_EVENT_KEY = "event_key";
    public static final String EXTRA_AUTHOR_ID = "author_id";

    private DatabaseReference mEventReference;
    private ValueEventListener mEventListener;
    private String mEventKey;
    private String mAuthorID;
    private String mUid;

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

        // Get author id from Intent
        mAuthorID = getIntent().getStringExtra(EXTRA_AUTHOR_ID);
        if (mAuthorID == null) {
            throw new IllegalArgumentException("Must pass EXTRA_AUTHOR_ID");
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

        // get the users uid
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // determine if the user owns this event, is so inflate the menu
        if (mUid.equals(mAuthorID)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_event_details, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_event:
                Intent intent = new Intent(EventDetailActivity.this, CreateEventActivity.class);
                intent.putExtra(EXTRA_EVENT_KEY, mEventKey);
                startActivity(intent);
                return true;
            case R.id.action_delete_event:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_deletion_title)
                        .setMessage(R.string.confirm_deletion_message)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteEvent();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mEventReference.child("userResponses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, String> userResponses = (HashMap<String, String>)dataSnapshot.getValue();
                        if (userResponses.get(mUid).equals(Event.Response.ATTENDING.toString())) {
                            mAttendingRadiobutton.setChecked(true);
                        }
                        else if (userResponses.get(mUid).equals(Event.Response.NOT_ATTENDING.toString())) {
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
        DatabaseReference ref = mEventReference
                .child("userResponses")
                .child(mUid);
        if (acceptedClicked) {
            ref.setValue(Event.Response.ATTENDING);
        }
        else {
            ref.setValue(Event.Response.NOT_ATTENDING);
        }
    }

    private void deleteEvent() {
        mEventReference.removeEventListener(mEventListener);
        mEventReference.removeValue();
        finish();
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
