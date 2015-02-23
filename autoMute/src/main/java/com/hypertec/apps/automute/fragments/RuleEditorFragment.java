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

import java.text.DateFormat;
import java.util.Calendar;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.R;
import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;
import com.hypertec.apps.automute.provider.DaysOfWeek;
import com.hypertec.apps.automute.provider.Rule;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Ryan Chou (ryanchou0210@gmail.com)
 *
 */
public class RuleEditorFragment extends Fragment {
    final static int MINUTE_PER_HOUR = 60;
    final static int MINUTE_PER_HALF_HOUR = 30;

    final static int DEFAULT_PERIOD_IN_MINUTES = 60;

    final static int REQUEST_CODE_GET_START_DATE = 1;
    final static int REQUEST_CODE_GET_START_TIME = 2;
    final static int REQUEST_CODE_GET_END_DATE = 3;
    final static int REQUEST_CODE_GET_END_TIME = 4;

    final static String EXTRA_PARAM_RULE = "param-rule";

    View.OnClickListener mStartDateClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
    	    showDatePickerDialog(mStartTime, REQUEST_CODE_GET_START_DATE);

        }
    };
    View.OnClickListener mStartTimeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showTimePickerDialog(mStartTime, REQUEST_CODE_GET_START_TIME);
        }
    };
    View.OnClickListener mEndDateClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
    	    showDatePickerDialog(mEndTime, REQUEST_CODE_GET_END_DATE);

        }
    };
    View.OnClickListener mEndTimeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showTimePickerDialog(mEndTime, REQUEST_CODE_GET_END_TIME);
        }
    };

    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

    	@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    if (isChecked && mEndTime.getTimeInMillis()-mStartTime.getTimeInMillis() >= AutoMuteAlarmMgr.MILLS_IN_A_DAY) {
				buttonView.setChecked(false);
				Toast.makeText(AutoMuteApp.context, R.string.warning_repeat_less_than_24, AutoMuteApp.TOAST_DURATION).show();
		    }

		    for (int i = 0; i < REPEAT_DAY_CHECK_BOX.length; i++) {
				if (buttonView.getId() == REPEAT_DAY_CHECK_BOX[i])
				{
				    mRepeat.setDaysOfWeek(isChecked, DAY_ORDER[i]);
				}
		    }
		}
    };

    public static final RuleEditorFragment newInstance(Context context, Rule rule)
    {
    	RuleEditorFragment fragment = new RuleEditorFragment();

    	final Bundle args = new Bundle(1);
    	args.putParcelable(EXTRA_PARAM_RULE, rule);
    	fragment.setArguments(args);

    	return fragment;
    }

    public void showDatePickerDialog(Calendar c, int requestCode)
    {
		DialogFragment newFragment = DatePickerFragment.newInstance(c);
		newFragment.setTargetFragment(this, requestCode);
		newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(Calendar c, int requestCode)
    {
		DialogFragment newFragment = TimePickerFragment.newInstance(c);
		newFragment.setTargetFragment(this, requestCode);
		newFragment.show(getFragmentManager(), "timePicker");
    }

    private Context mContext;
    private Calendar mStartTime;
    private Calendar mEndTime;
    private DaysOfWeek mRepeat;
    private EditText mTitleView;
    private Rule mRule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	mRule = getArguments().getParcelable(EXTRA_PARAM_RULE);
    }

    @Override
    public void onAttach(Activity activity)
    {
    	super.onAttach(activity);
    	mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_rule_editor, container, false);
		return v;
    }

    public void updateDateTime(Calendar c, boolean bStart) {
		TextView dateView;
		EditText timeView;

		if (bStart) {
		    dateView = (TextView) this.getActivity().findViewById(R.id.rule_editor_start_date);
		    timeView = (EditText) this.getActivity().findViewById(R.id.rule_editor_start_time);
		} else {
		    dateView = (TextView) this.getActivity().findViewById(R.id.rule_editor_end_date);
		    timeView = (EditText) this.getActivity().findViewById(R.id.rule_editor_end_time);
		}

		if (dateView != null) {
		    DateFormat dfDate_day= android.text.format.DateFormat.getDateFormat(mContext);
		    dateView.setText(dfDate_day.format(c.getTime()));
		}
		if (timeView != null) {
		    DateFormat tf = android.text.format.DateFormat.getTimeFormat(mContext);
		    timeView.setHint(tf.format(c.getTime()));
		}
    }

    public void updateTitle(CharSequence title) {
		TextView titleView = (TextView) this.getActivity().findViewById(R.id.rule_editor_title);
		titleView.setText(title);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
    	super.onActivityCreated(savedInstanceState);

		String title;
		CheckBox checkBox;

		if (mRule == null) { // Create a new one
		    Calendar date = Calendar.getInstance();
		    date.clear(Calendar.SECOND);
		    date.clear(Calendar.MILLISECOND);

		    int minute = date.get(Calendar.MINUTE);
		    if (minute > 30 ) {
			date.add(Calendar.MINUTE, MINUTE_PER_HOUR-minute);
		    } else if (minute > 0) {
			date.add(Calendar.MINUTE, MINUTE_PER_HALF_HOUR-minute);
		    }

		    updateDateTime(date, true);
		    mStartTime = (Calendar) date.clone();

		    date.add(Calendar.MINUTE, DEFAULT_PERIOD_IN_MINUTES);
		    updateDateTime(date, false);
		    mEndTime = (Calendar) date.clone();
		    mRepeat = new DaysOfWeek(0);

		    getActivity().getActionBar().setTitle(R.string.title_add_muter);
		} else { // Edit
		    mStartTime = mRule.getStartTime();
		    updateDateTime(mStartTime, true);
		    mEndTime = mRule.getEndTime();
		    mEndTime.add(Calendar.MILLISECOND, 1);
		    updateDateTime(mEndTime, false);
		    mRepeat = mRule.getRepeat();
		    title = mRule.getTitleOrDefault(getActivity());
		    updateTitle(title);

		    checkBox = (CheckBox) this.getActivity().findViewById(R.id.rule_editor_checkbox_vibrate);
		    checkBox.setChecked(mRule.isVibrate());

		    getActivity().getActionBar().setTitle(R.string.title_edit_muter);
		}

		for (int i=0; i < DAY_ORDER.length; i++) {
		    checkBox = (CheckBox) this.getActivity().findViewById(REPEAT_DAY_CHECK_BOX[i]);
		    checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
		    if (mRepeat.isDaysOfWeekEnabled(DAY_ORDER[i])) {
			checkBox.setChecked(true);
		    }
		}

		View startDateView = getActivity().findViewById(R.id.rule_editor_start_date);
		startDateView.setOnClickListener(mStartDateClickListener);
		View startTimeView = getActivity().findViewById(R.id.rule_editor_start_time);
		startTimeView.setOnClickListener(mStartTimeClickListener);
		View endDateView = getActivity().findViewById(R.id.rule_editor_end_date);
		endDateView.setOnClickListener(mEndDateClickListener);
		View endTimeView = getActivity().findViewById(R.id.rule_editor_end_time);
		endTimeView.setOnClickListener(mEndTimeClickListener);

		mTitleView = (EditText) getActivity().findViewById(R.id.rule_editor_title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	long oldMillis;
	int year, month, day, hour, minute;
	switch (requestCode) {
    	case REQUEST_CODE_GET_START_DATE:
    	{
    	    oldMillis = mStartTime.getTimeInMillis();
    	    year = data.getIntExtra(DatePickerFragment.YEAR, mStartTime.get(Calendar.YEAR));
    	    month = data.getIntExtra(DatePickerFragment.MONTH, mStartTime.get(Calendar.MONTH));
    	    day = data.getIntExtra(DatePickerFragment.DAY_OF_MONTH, mStartTime.get(Calendar.DAY_OF_MONTH));

    	    mStartTime.set(year, month, day);
    	    mEndTime.setTimeInMillis(mEndTime.getTimeInMillis()+mStartTime.getTimeInMillis()-oldMillis);
    	    updateDateTime(mStartTime, true);
    	    updateDateTime(mEndTime, false);
    	}
	    break;
	case REQUEST_CODE_GET_START_TIME:
	{
	    oldMillis = mStartTime.getTimeInMillis();
	    hour = data.getIntExtra(TimePickerFragment.HOUR, mStartTime.get(Calendar.HOUR_OF_DAY));
	    minute = data.getIntExtra(TimePickerFragment.MINUTE, mStartTime.get(Calendar.MINUTE));

	    mStartTime.set(Calendar.HOUR_OF_DAY, hour);
	    mStartTime.set(Calendar.MINUTE, minute);

	    mEndTime.setTimeInMillis(mEndTime.getTimeInMillis()+mStartTime.getTimeInMillis()-oldMillis);

	    updateDateTime(mStartTime, true);
	    updateDateTime(mEndTime, false);
	}
	    break;
	case REQUEST_CODE_GET_END_DATE:
	{
	    year = data.getIntExtra(DatePickerFragment.YEAR, mEndTime.get(Calendar.YEAR));
	    month = data.getIntExtra(DatePickerFragment.MONTH, mEndTime.get(Calendar.MONTH));
	    day = data.getIntExtra(DatePickerFragment.DAY_OF_MONTH, mEndTime.get(Calendar.DAY_OF_MONTH));

	    mEndTime.set(year, month, day);
	    updateDateTime(mEndTime, false);
	}
	    break;
	case REQUEST_CODE_GET_END_TIME:
	{
	    hour = data.getIntExtra(TimePickerFragment.HOUR, mEndTime.get(Calendar.HOUR_OF_DAY));
	    minute = data.getIntExtra(TimePickerFragment.MINUTE, mEndTime.get(Calendar.MINUTE));

	    mEndTime.set(Calendar.HOUR_OF_DAY, hour);
	    mEndTime.set(Calendar.MINUTE, minute);
	    updateDateTime(mEndTime, false);
	}
	    break;
	default:
	    break;
	}
    }

    public CharSequence getTitle() {
	return mTitleView.getText();
    }

    public Calendar getStartTime() {
	return mStartTime;
    }

    public Calendar getEndTime() {
	return mEndTime;
    }

    public boolean getVibrate() {
	CheckBox cbVibrate = (CheckBox) this.getActivity().findViewById(R.id.rule_editor_checkbox_vibrate);
	return cbVibrate.isChecked();
    }

    private final int[] REPEAT_DAY_CHECK_BOX = new int[] {
            R.id.rule_editor_repeat_checkbox_monday,
            R.id.rule_editor_repeat_checkbox_tuesday,
            R.id.rule_editor_repeat_checkbox_wednesday,
            R.id.rule_editor_repeat_checkbox_thursday,
            R.id.rule_editor_repeat_checkbox_friday,
            R.id.rule_editor_repeat_checkbox_saturday,
            R.id.rule_editor_repeat_checkbox_sunday,
    };

    private final int[] DAY_ORDER = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
    };
    public DaysOfWeek getRRule() {
	DaysOfWeek repeat = new DaysOfWeek(0);
	CheckBox cbDay;
	for (int i = 0; i < REPEAT_DAY_CHECK_BOX.length; i++) {
	    cbDay = (CheckBox) this.getActivity().findViewById(REPEAT_DAY_CHECK_BOX[i]);
	    if (cbDay.isChecked()) {
		repeat.setDaysOfWeek(true, DAY_ORDER[i]);
	    }
	}
	return repeat;
    }
}
