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

    EditText mTitleField;
    EditText mDetailField;
    TextView mDateTimeField;
    Button mSelectDateTimeBtn;
    FloatingActionButton mSubmitButton;

    private static String title;
    private static String body;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleField = findViewById(R.id.et_event_name);
        mDetailField = findViewById(R.id.et_event_details);
        mDateTimeField = findViewById(R.id.selected_date_tv);
        mSelectDateTimeBtn = findViewById(R.id.date_selection_btn);
        mSubmitButton = findViewById(R.id.fab_create_event);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(this.year, this.month, this.day, this.hour, this.minute);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy 'at' hh:mm a", Locale.ENGLISH);

        ((TextView) findViewById(R.id.selected_date_tv)).setText(dateFormat.format(calendar.getTime()));
    }

    public void submitEvent(View v) {
        title = mTitleField.getText().toString();
        body = mDetailField.getText().toString();
        final String dateTime = mDateTimeField.getText().toString();

        // Title required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Details required
        if (TextUtils.isEmpty(body)) {
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
        Toast.makeText(this, this.getString(R.string.posting), Toast.LENGTH_SHORT).show();

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);

                // if user is null error out
                if (user == null) {
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(CreateEventActivity.this, CreateEventActivity.this.getString(R.string.err_user_null), Toast.LENGTH_SHORT).show();
                } else {
                    // write new event
                    writeNewEvent(userId, user.username, title, body);
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

    public void writeNewEvent(String userId, String username, String title, String body) {
        // create new event at "events/@post-id
        String key = mDatabase.child("events").push().getKey();
        Event event = new Event(userId,username,title,body,this.day,this.month,this.year,this.hour,this.minute);
        Map<String, Object> eventValues = event.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/events/" + key, eventValues);
        mDatabase.updateChildren(childUpdates);

        // now write to "responses -> eventKey" the uid and status of all users
        writeUserReponses(key);
    }

    // Using the eventKey, add all users under /responses/eventKey
    // and set status as 'pending'
    public void writeUserReponses(final String eventKey) {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // a map of all users
                Map<String, Object> allUsersMap = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Boolean currentUser = snapshot.getKey().equals(uid);
                    User user = snapshot.getValue(User.class);
                    // a map of just the users name, and response status
                    if (user != null) {
                        Map<String, Object> singleUserMap = new HashMap<>();
                        // event creator defaults to attending, rest to pending
                        String response = (currentUser) ? "accepted" : "pending";
                        singleUserMap.put("status", response);
                        singleUserMap.put("username", user.username);
                        allUsersMap.put(snapshot.getKey(), singleUserMap);
                    }
                    else {
                        Log.e(TAG, "User " + snapshot.getKey() + " was unexpectedly null");
                    }
                }
                Map<String, Object> finalMap = new HashMap<>();
                finalMap.put("/responses/" + eventKey, allUsersMap);
                mDatabase.updateChildren(finalMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"getUser:onCancelled",databaseError.toException());
            }
        });
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

