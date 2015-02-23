package com.hypertec.apps.automute.fragments;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

    public final static String HOUR = "hour";
    public final static String MINUTE = "minute";

    public final static int RETURN_CODE_OK = 0;
    public final static int RETURN_CODE_FAIL = 1;

    Calendar mTime;
    public static TimePickerFragment newInstance(Calendar c) {
        TimePickerFragment result = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable("calendar_offset", c);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTime = (Calendar) getArguments().getSerializable("calendar_offset");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour = mTime.get(Calendar.HOUR_OF_DAY);
        int minute = mTime.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new AutoMuteTimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    	// Do something with the time chosen by the user
    	Intent result = new Intent();
    	result.putExtra(TimePickerFragment.HOUR, hourOfDay);
    	result.putExtra(TimePickerFragment.MINUTE, minute);

    	getTargetFragment().onActivityResult(getTargetRequestCode(), TimePickerFragment.RETURN_CODE_OK, result);
    }

    public class AutoMuteTimePickerDialog extends TimePickerDialog {

        public AutoMuteTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, is24HourView);
        }

        @Override
        protected void onStop() {
            // Replacing tryNotifyDateSet() with nothing - this is a workaround for Android bug https://android-review.googlesource.com/#/c/61270/A

            // Would also like to clear focus, but we cannot get at the private members, so we do nothing.  It seems to do no harm...
            // mDatePicker.clearFocus();

            // Now we would like to call super on onStop(), but actually what we would mean is super.super, because
            // it is super.onStop() that we are trying NOT to run, because it is buggy.  However, doing such a thing
            // in Java is not allowed, as it goes against the philosophy of encapsulation (the Creators never thought
            // that we might have to patch parent classes from the bottom up :)
            // However, we do not lose much by doing nothing at all, because in Android 2.* onStop() in androd.app.Dialog actually
            // does nothing and in 4.* it does:
            //      if (mActionBar != null) mActionBar.setShowHideAnimationEnabled(false);
            // which is not essential for us here because we use no action bar... QED
            // So we do nothing and we intend to keep this workaround forever because of users with older devices, who might
            // run Android 4.1 - 4.3 for some time to come, even if the bug is fixed in later versions of Android.
        }
    }
}
