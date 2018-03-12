package com.example.ryansaffer.eventplanner.ViewHolder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    public static final String TAG = "PostViewHolder";

    private Context context;

    public TextView authorView;
    public TextView titleView;
    public TextView detailView;
    public TextView dateTimeView;
    public TextView attendingCountView;
    public TextView rejectedCountView;
    public TextView awaitingResponseCountView;
    public TextView responseTextView;

    public PostViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();

        authorView = itemView.findViewById(R.id.include_author_email);
        titleView = itemView.findViewById(R.id.post_title);
        detailView = itemView.findViewById(R.id.post_details);
        dateTimeView = itemView.findViewById(R.id.post_date_time);
        attendingCountView = itemView.findViewById(R.id.post_accepted_num);
        rejectedCountView = itemView.findViewById(R.id.post_rejected_num);
        awaitingResponseCountView = itemView.findViewById(R.id.post_awaiting_response_num);
        responseTextView = itemView.findViewById(R.id.tv_post_user_response);
    }

    public void bindToEvent(String eventKey, final Event event) {
        // count the number of accepted/rejected/pending
        FirebaseDatabase.getInstance().getReference()
                .child("events")
                .child(eventKey)
                .child("userResponses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long attendingCount = 0;
                long notAttendingCount = 0;
                long pendingCount = 0;
                Map<String, String> responses = (Map<String, String>) dataSnapshot.getValue();
                if (responses != null) {
                    for (String response : responses.values()) {
                        Event.Response res = Event.Response.valueOf(response);
                        switch (res) {
                            case ATTENDING:
                                attendingCount++;
                                break;
                            case NOT_ATTENDING:
                                notAttendingCount++;
                                break;
                            case PENDING:
                                pendingCount++;
                                break;
                            default:
                                break;
                        }
                    }
                }

                // find out the logged in users response
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String rawResponse = responses.get(uid);
                Event.Response response = Event.Response.valueOf(rawResponse);
                setText(attendingCount, notAttendingCount, pendingCount, response, event);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getAllInvitedUsers:onCancelled", databaseError.toException());
            }
        });
    }

    private void setText(long attendingCount, long notAttendingCount, long pendingCount, Event.Response response, Event event) {

        SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMM yyyy 'at' hh:mm a");
        Calendar cal = Calendar.getInstance();
        cal.set(event.year,event.month,event.day,event.hour,event.minute);

        authorView.setText(event.author);
        titleView.setText(event.title);
        detailView.setText(event.details);
        dateTimeView.setText(format.format(cal.getTime()));
        attendingCountView.setText(String.valueOf(attendingCount));
        rejectedCountView.setText(String.valueOf(notAttendingCount));
        awaitingResponseCountView.setText(String.valueOf(pendingCount));

        // set the user response
        switch(response) {
            case ATTENDING:
                responseTextView.setText(context.getResources().getString(R.string.user_response_attending));
                break;
            case NOT_ATTENDING:
                responseTextView.setText(context.getResources().getString(R.string.user_response_rejected));
                break;
            case PENDING:
                responseTextView.setText(context.getResources().getString(R.string.user_response_pending));
                break;
            default:
                break;
        }
    }
}
