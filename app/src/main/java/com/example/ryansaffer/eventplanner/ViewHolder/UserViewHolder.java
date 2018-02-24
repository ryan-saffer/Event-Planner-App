package com.example.ryansaffer.eventplanner.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.models.User;

/**
 * Created by andre on 24/02/2018.
 */

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView userView;

    public UserViewHolder(View itemView) {
        super(itemView);

        this.userView = itemView.findViewById(R.id.include_author_email);
    }

    public void bindToUser(User user) {
        this.userView.setText(user.username);
    }
}
