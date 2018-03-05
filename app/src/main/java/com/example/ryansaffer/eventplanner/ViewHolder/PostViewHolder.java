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

        FirebaseDatabase.getInstance().getReference()
                .child("responses")
                .child(eventKey)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // while we are here, lets also find out what the current user's response is
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                int attendingCount = 0;
                int notAttendingCount = 0;
                int pendingCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String response = snapshot.child("status").getValue().toString();
                    Boolean currentUser = snapshot.getKey().equals(uid);

                    switch (response) {
                        case "accepted":
                            attendingCount ++;
                            if (currentUser) {
                                responseTextView.setText(context.getResources().getString(R.string.user_response_attending));
                            }
                            break;
                        case "rejected":
                            notAttendingCount ++;
                            if (currentUser) {
                                responseTextView.setText(context.getResources().getString(R.string.user_response_rejected));
                            }
                            break;
                        case "pending":
                            pendingCount ++;
                            if (currentUser) {
                                responseTextView.setText(context.getResources().getString(R.string.user_response_pending));
                            }
                            break;
                        default:
                            break;
                    }
                }
                setText(attendingCount, notAttendingCount, pendingCount, event);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getAllInvitedUsers:onCancelled", databaseError.toException());
            }
        });
    }

    private void setText(int attendingCount, int notAttendingCount, int pendingCount, Event event) {

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
    }
}
