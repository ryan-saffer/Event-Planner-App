package com.example.ryansaffer.eventplanner.PickerFragments;

import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.example.ryansaffer.eventplanner.Activities.CreateEventActivity;

import java.util.Calendar;

/**
 * Created by ryansaffer on 21/2/18.
 */

public class TimePickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), (CreateEventActivity)getActivity(), hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}
