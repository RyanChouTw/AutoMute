/**
 * Copyright (c) 2014 Hypertec Corporation
 * All rights reserved. This program and the accompanying materials
 * are licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   Ryan Chou - initial API and implementation
 */
package com.hypertec.apps.automute.fragments;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

/**
 * @author Ryan Chou (ryanchou0210@gmail.com)
 *
 */
public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

    public final static String YEAR = "year";
    public final static String MONTH = "month";
    public final static String DAY_OF_MONTH = "day";

    public final static int RETURN_CODE_OK = 0;
    public final static int RETURN_CODE_FAIL = 1;

    Calendar mDate;

    public static DatePickerFragment newInstance(Calendar c) {
        DatePickerFragment result = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable("calendar_offset", c);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDate = (Calendar) getArguments().getSerializable("calendar_offset");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
		int year = mDate.get(Calendar.YEAR);
		int month = mDate.get(Calendar.MONTH);
		int day = mDate.get(Calendar.DAY_OF_MONTH);

		return new AutoMuteDatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
		Intent result = new Intent();
		result.putExtra(DatePickerFragment.YEAR, year);
		result.putExtra(DatePickerFragment.MONTH, monthOfYear);
		result.putExtra(DatePickerFragment.DAY_OF_MONTH, dayOfMonth);

		getTargetFragment().onActivityResult(getTargetRequestCode(), DatePickerFragment.RETURN_CODE_OK, result);
    }

    public class AutoMuteDatePickerDialog extends DatePickerDialog {

        public AutoMuteDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
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