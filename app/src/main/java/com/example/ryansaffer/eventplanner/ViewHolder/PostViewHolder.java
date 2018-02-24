package com.example.ryansaffer.eventplanner.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.models.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ryansaffer on 20/2/18.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView authorView;
    public TextView titleView;
    public TextView detailView;
    public TextView dateTimeView;
    public TextView attendingCountView;
    public TextView rejectedCountView;
    public TextView awaitingResponseCountView;

    public PostViewHolder(View itemView) {
        super(itemView);

        authorView = itemView.findViewById(R.id.include_author_email);
        titleView = itemView.findViewById(R.id.post_title);
        detailView = itemView.findViewById(R.id.post_details);
        dateTimeView = itemView.findViewById(R.id.post_date_time);
        attendingCountView = itemView.findViewById(R.id.post_accepted_num);
        rejectedCountView = itemView.findViewById(R.id.post_rejected_num);
        awaitingResponseCountView = itemView.findViewById(R.id.post_awaiting_response_num);
    }

    public void bindToEvent(Event event){

        SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMM yyyy 'at' hh:mm a");
        Calendar cal = Calendar.getInstance();
        cal.set(event.year,event.month,event.day,event.hour,event.minute);

        authorView.setText(event.author);
        titleView.setText(event.title);
        detailView.setText(event.details);
        dateTimeView.setText(format.format(cal.getTime()));
    }
}
