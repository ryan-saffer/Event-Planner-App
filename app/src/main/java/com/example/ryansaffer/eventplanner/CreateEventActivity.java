package com.example.ryansaffer.eventplanner;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ryansaffer.eventplanner.PickerFragments.DatePickerFragment;
import com.example.ryansaffer.eventplanner.PickerFragments.TimePickerFragment;
import com.example.ryansaffer.eventplanner.models.Event;
import com.example.ryansaffer.eventplanner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "NewEventActivity";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;
    private String mUid;

    // a flag used to determine if creating a brand new event, or editing existing one
    private Boolean mEditingEvent;
    private String mEventKey;
    // if editing existing, store that event
    private Event mEvent;

    EditText mTitleField, mDetailField;
    TextView mDateTimeField;
    Button mSelectDateTimeBtn;
    FloatingActionButton mSubmitButton;

    private String mTitle, mBody;

    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // determine if editing an existing event
        mEventKey = getIntent().getStringExtra(EventDetailActivity.EXTRA_EVENT_KEY);
        mEditingEvent = mEventKey != null;

        mTitleField = findViewById(R.id.et_event_name);
        mDetailField = findViewById(R.id.et_event_details);
        mDateTimeField = findViewById(R.id.selected_date_tv);
        mSelectDateTimeBtn = findViewById(R.id.date_selection_btn);
        mSubmitButton = findViewById(R.id.fab_create_event);

        if (mEditingEvent) {
            prefillAllFields();
        }
    }

    private void prefillAllFields() {
        mDatabase.child("events").child(mEventKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEvent = dataSnapshot.getValue(Event.class);
                mTitleField.setText(mEvent.title);
                mDetailField.setText(mEvent.details);
                Calendar calendar = Calendar.getInstance();
                calendar.set(mEvent.year, mEvent.month, mEvent.day, mEvent.hour, mEvent.minute);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy 'at' hh:mm a", Locale.ENGLISH);
                mDateTimeField.setText(dateFormat.format(calendar.getTime()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "prefillAllFields:onCancelled", databaseError.toException());
            }
        });
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.mYear = year;
        this.mMonth = month;
        this.mDay = dayOfMonth;

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.mHour = hourOfDay;
        this.mMinute = minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(this.mYear, this.mMonth, this.mDay, this.mHour, this.mMinute);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy 'at' hh:mm a", Locale.ENGLISH);

        mDateTimeField.setText(dateFormat.format(calendar.getTime()));
    }

    public void submitEvent(View v) {
        mTitle = mTitleField.getText().toString();
        mBody = mDetailField.getText().toString();
        final String dateTime = mDateTimeField.getText().toString();

        // Title required
        if (TextUtils.isEmpty(mTitle)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Details required
        if (TextUtils.isEmpty(mBody)) {
            mDetailField.setError(REQUIRED);
            return;
        }

        // Date and Time required
        if (TextUtils.isEmpty(dateTime)) {
            mSelectDateTimeBtn.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, mEditingEvent ? this.getString(R.string.updating) : this.getString(R.string.posting), Toast.LENGTH_SHORT).show();

        // if editing, use existing userResponses
        if (mEditingEvent) {
            createEvent(mEvent.userResponses);
        } else {
            // create a response map of all users, then create the event
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                HashMap<String, Event.Response> userResponses = new HashMap<>();
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Map> map = (Map<String, Map>) dataSnapshot.getValue();
                    for (String uid : map.keySet()) {
                        if (uid.equals(mUid)) {
                            userResponses.put(uid, Event.Response.ATTENDING);
                        } else {
                            userResponses.put(uid, Event.Response.PENDING);
                        }
                    }
                    createEvent(userResponses);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "createUserResponses:onCancelled", databaseError.toException());
                }
            });
        }
    }

    public void createEvent(final HashMap<String, Event.Response> userResponses) {
        // get current user, then create the event
        mDatabase.child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);

                // if user is null error out
                if (user == null) {
                    Log.e(TAG, "User " + mUid + " is unexpectedly null");
                    Toast.makeText(CreateEventActivity.this, CreateEventActivity.this.getString(R.string.err_user_null), Toast.LENGTH_SHORT).show();
                } else {
                    // write new event
                    writeNewEvent(mUid, user.username, mTitle, mBody, userResponses);
                }

                // Finish activity, back to the stream
                setEditingEnabled(true);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                setEditingEnabled(true);
            }
        });
    }

    public void writeNewEvent(String userId, String username, String title, String body, HashMap<String, Event.Response> userResponses) {
        if (!mEditingEvent) {
            // create new event at "events/@event-id
            mEventKey = mDatabase.child("events").push().getKey();
        }
        Event event = new Event(userId,username,title,body,userResponses,this.mDay,this.mMonth,this.mYear,this.mHour,this.mMinute);
        Map<String, Object> eventValues = event.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/events/" + mEventKey, eventValues);
        mDatabase.updateChildren(childUpdates);
    }

    public void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mDetailField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }
}

