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
package com.hypertec.apps.automute.provider;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.R;
import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Ryan Chou (ryanchou0210@gmail.com)
 *
 */
public class Rule implements Parcelable, AutoMuteContract.RulesColumns {
    
    /**
     * Rules start with an invalid id when it hasn't been saved to the database.
     */
    public static final long INVALID_ID = -1;

    /**
     * The sort order is:
     * 1) rules with an earlier start time
     * 2) rules with an later end time
     * 3) the title (unnecessary, but nice)
     */
    private static final String DEFAULT_SORT_ORDER = "";
    public static final String RULE_ITEM = "ruleitem";
    
    // The projection to use when querying instances to build a list of rules
    private static final String[] QUERY_COLUMNS = {
        _ID,
        TITLE,
        START_YEAR,
        START_MONTH,
        START_DAY,
        START_HOUR,
        START_MINUTE,
        END_YEAR,
        END_MONTH,
        END_DAY,
        END_HOUR,
        END_MINUTE,
        DAYS_OF_WEEK,
        ENABLED,
        VIBRATE,
    };

    // The indices for the projection array above.
    private static final int ID_INDEX = 0;
    private static final int TITLE_INDEX = 1;
    private static final int START_YEAR_INDEX = 2;
    private static final int START_MONTH_INDEX = 3;
    private static final int START_DAY_INDEX = 4;
    private static final int START_HOUR_INDEX = 5;
    private static final int START_MINUTE_INDEX = 6;
    private static final int END_YEAR_INDEX = 7;
    private static final int END_MONTH_INDEX = 8;
    private static final int END_DAY_INDEX = 9;
    private static final int END_HOUR_INDEX = 10;
    private static final int END_MINUTE_INDEX = 11;
    private static final int DAYSOFWEEK_INDEX = 12;
    private static final int ENABLE_INDEX = 13;
    private static final int VIBRATE_INDEX = 14;
    private static final int COLUMN_COUNT = VIBRATE_INDEX + 1;

    public static final String REQUEST_CODE = "requestcode";
    public static final int REQUEST_CODE_INVALID = -1;
    public static final int REQUST_CODE_MUTE_ONCE = 0x0000;
    public static final int REQUST_CODE_UNMUTE_ONCE = 0x0001;

    @Override
    public int describeContents() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
	// TODO Auto-generated method stub
        p.writeLong(mId);
        p.writeString(mTitle);
        p.writeInt(mStartYear);
        p.writeInt(mStartMonth);
        p.writeInt(mStartDay);
        p.writeInt(mStartHour);
        p.writeInt(mStartMinute);
        p.writeInt(mEndYear);
        p.writeInt(mEndMonth);
        p.writeInt(mEndDay);
        p.writeInt(mEndHour);
        p.writeInt(mEndMinute);
        p.writeInt(mDaysOfWeek.getBitSet());
        p.writeInt(mEnabled ? 1 : 0);
        p.writeInt(mVibrate ? 1 : 0);
    }

    private long mId;	// Be careful, mId must be limited in 16 bits. It is upper-two bytes of requestCode(32bits)
    private String mTitle;
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private int mStartHour;
    private int mStartMinute;
    private int mEndYear;
    private int mEndMonth;
    private int mEndDay;
    private int mEndHour;
    private int mEndMinute;
    private DaysOfWeek mDaysOfWeek;
    private boolean mEnabled;
    private boolean mVibrate;
    
    private int mRepeatIterIndex;
    
    // Creates a default rule at the current time.
    public Rule() {
	
	Calendar startTime = Calendar.getInstance();
	startTime.clear(Calendar.SECOND);
	startTime.clear(Calendar.MILLISECOND);
	
	Calendar endTime = (Calendar) startTime.clone();
	endTime.add(Calendar.HOUR_OF_DAY, 1);

        this.mId = INVALID_ID;
        this.mTitle = "";

        this.mStartYear = startTime.get(Calendar.YEAR);
        this.mStartMonth = startTime.get(Calendar.MONTH);
        this.mStartDay = startTime.get(Calendar.DAY_OF_MONTH);
        this.mStartHour = startTime.get(Calendar.HOUR_OF_DAY);
        this.mStartMinute = startTime.get(Calendar.MINUTE);
        
        this.mEndYear = endTime.get(Calendar.YEAR);
        this.mEndMonth = endTime.get(Calendar.MONTH);
        this.mEndDay = endTime.get(Calendar.DAY_OF_MONTH);
        this.mEndHour = endTime.get(Calendar.HOUR_OF_DAY);
        this.mEndMinute = endTime.get(Calendar.MINUTE);
        
        this.mDaysOfWeek = new DaysOfWeek(0);
        this.mEnabled = true;
        this.mVibrate = false;
    }

    public Rule(Calendar startTime, Calendar endTime) {
        this.mId = INVALID_ID;
        this.mTitle = "";

        this.mStartYear = startTime.get(Calendar.YEAR);
        this.mStartMonth = startTime.get(Calendar.MONTH);
        this.mStartDay = startTime.get(Calendar.DAY_OF_MONTH);
        this.mStartHour = startTime.get(Calendar.HOUR_OF_DAY);
        this.mStartMinute = startTime.get(Calendar.MINUTE);
        
        this.mEndYear = endTime.get(Calendar.YEAR);
        this.mEndMonth = endTime.get(Calendar.MONTH);
        this.mEndDay = endTime.get(Calendar.DAY_OF_MONTH);
        this.mEndHour = endTime.get(Calendar.HOUR_OF_DAY);
        this.mEndMinute = endTime.get(Calendar.MINUTE);
        
        this.mDaysOfWeek = new DaysOfWeek(0);
        this.mEnabled = true;
        this.mVibrate = false;	
    }
    
    public Rule(int sYear, int sMonth, int sDay, int sHour, int sMinute,
	    	int eYear, int eMonth, int eDay, int eHour, int eMinute) {
        this.mId = INVALID_ID;
        this.mTitle = "";

        this.mStartYear = sYear;
        this.mStartMonth = sMonth;
        this.mStartDay = sDay;
        this.mStartHour = sHour;
        this.mStartMinute = sMinute;
        
        this.mEndYear = eYear;
        this.mEndMonth = eMonth;
        this.mEndDay = eDay;
        this.mEndHour = eHour;
        this.mEndMinute = eMinute;
        
        this.mDaysOfWeek = new DaysOfWeek(0);
        this.mEnabled = true;
        this.mVibrate = false;
    }

    public Rule(Cursor c) {
        mId = c.getLong(ID_INDEX);
        mTitle = c.getString(TITLE_INDEX);
        mStartYear = c.getInt(START_YEAR_INDEX);
        mStartMonth = c.getInt(START_MONTH_INDEX);
        mStartDay = c.getInt(START_DAY_INDEX);
        mStartHour = c.getInt(START_HOUR_INDEX);
        mStartMinute = c.getInt(START_MINUTE_INDEX);
        mEndYear = c.getInt(END_YEAR_INDEX);
        mEndMonth = c.getInt(END_MONTH_INDEX);
        mEndDay = c.getInt(END_DAY_INDEX);
        mEndHour = c.getInt(END_HOUR_INDEX);
        mEndMinute = c.getInt(END_MINUTE_INDEX);
        mDaysOfWeek = new DaysOfWeek(c.getInt(DAYSOFWEEK_INDEX));
        mEnabled = c.getInt(ENABLE_INDEX) == 1;
        mVibrate = c.getInt(VIBRATE_INDEX) == 1;
    }

    Rule(Parcel p) {
        mId = p.readLong();
        mTitle = p.readString();
        mStartYear = p.readInt();
        mStartMonth = p.readInt();
        mStartDay = p.readInt();
        mStartHour = p.readInt();
        mStartMinute = p.readInt();
        mEndYear = p.readInt();
        mEndMonth = p.readInt();
        mEndDay = p.readInt();
        mEndHour = p.readInt();
        mEndMinute = p.readInt();        
        mDaysOfWeek = new DaysOfWeek(p.readInt());
        mEnabled = p.readInt() == 1;
        mVibrate = p.readInt() == 1;
    }

    public long getId() {
	return mId;
    }

    public String getTitleOrDefault(Context context) {
        if (mTitle == null || mTitle.length() == 0) {
            return context.getString(R.string.rule_default_title);
        }
        return mTitle;
    }
    
    public void setTitle(CharSequence title) {
	mTitle = title.toString();
    }
    
    public void setStartTime(Calendar time) {
        mStartYear = time.get(Calendar.YEAR);
        mStartMonth = time.get(Calendar.MONTH);
        mStartDay = time.get(Calendar.DAY_OF_MONTH);
        mStartHour = time.get(Calendar.HOUR_OF_DAY);
        mStartMinute = time.get(Calendar.MINUTE);
    }
    
    public void setEndTime(Calendar time) {
        mEndYear = time.get(Calendar.YEAR);
        mEndMonth = time.get(Calendar.MONTH);
        mEndDay = time.get(Calendar.DAY_OF_MONTH);
        mEndHour = time.get(Calendar.HOUR_OF_DAY);
        mEndMinute = time.get(Calendar.MINUTE);
    }    
    
    public DaysOfWeek getRepeat() {
	return mDaysOfWeek;
    }
    
    public void setRepeat(DaysOfWeek repeat) {
	mDaysOfWeek = repeat;
    }

    public boolean isEnabled() {
	return mEnabled;
    }
    
    public void setEnable(boolean bOn) {
	mEnabled = bOn;
    }
    
    public boolean isVibrate() {
	return mVibrate;
    }
    
    public boolean isRepeat() {
	return mDaysOfWeek.isRepeating();
    }
     
    public void setVibrate(boolean bOn) {
	mVibrate = bOn;
    }
    
    public Calendar getStartTime() {
        Calendar ret = Calendar.getInstance();
        ret.clear();
        ret.set(mStartYear, mStartMonth, mStartDay, mStartHour, mStartMinute);
        return ret;
    }
    
    public Calendar getEndTime() {
        Calendar ret = Calendar.getInstance();
        ret.clear();
        ret.set(mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute);
        ret.add(Calendar.MILLISECOND, -1); // to decrease 1 second to avoid appending rule
        return ret;
    }
    
    public CharSequence getStartTimeDispText(Context context)
    {
    	DateFormat dfDate_day = android.text.format.DateFormat.getDateFormat(context);
    	DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
    	Calendar startTime = getStartTime();
	
    	if (isRepeat())
    	{
    		return tf.format(startTime.getTime());
    	}
    	else {
    		return dfDate_day.format(startTime.getTime()) + " " + tf.format(startTime.getTime());
    	}
    }
    
    public CharSequence getEndTimeDispText(Context context)
    {
		DateFormat dfDate_day= android.text.format.DateFormat.getDateFormat(context);
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
		Calendar endTime = getEndTime();
		endTime.add(Calendar.MILLISECOND, 1);	
		
		if (isRepeat())
		{
		    return tf.format(endTime.getTime());
		}
		else
		{
		    return dfDate_day.format(endTime.getTime()) + " " + tf.format(endTime.getTime());  
		}	
    }

    public void resetOccurence()
    {
    	mRepeatIterIndex = -1;
    }
    
    public boolean hasNextOccurence() 
    {
		if(isRepeat() == false || mEnabled == false) 
		    return false;
		
		mRepeatIterIndex++;
		Calendar currentTime = Calendar.getInstance();
		Calendar startTime = getStartTime();
		Calendar endTime = getEndTime();
		long duration = endTime.getTimeInMillis() - startTime.getTimeInMillis();
		
		//startTime.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		endTime.set(currentTime.get(Calendar.YEAR),  currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		startTime.setTimeInMillis(endTime.getTimeInMillis() - duration);
			
		startTime.add(Calendar.DAY_OF_WEEK, mRepeatIterIndex);
		while (mRepeatIterIndex < DaysOfWeek.DAYS_IN_A_WEEK)
		{
		    int dayOfWeek = startTime.get(Calendar.DAY_OF_WEEK);
		    if (mDaysOfWeek.isDaysOfWeekEnabled(dayOfWeek) == true) {
		    	return true;
		    }
		    startTime.add(Calendar.DAY_OF_WEEK, 1);
		    mRepeatIterIndex++;
		}
	
		return false;
    }

    public Calendar getNextStartTime() 
    {
		Calendar currentTime = Calendar.getInstance();
		Calendar startTime = getStartTime();
		Calendar endTime = getEndTime();
		long duration = endTime.getTimeInMillis() - startTime.getTimeInMillis();
		
		endTime.set( currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		startTime.setTimeInMillis(endTime.getTimeInMillis() - duration);
		
		startTime.add(Calendar.DAY_OF_WEEK, mRepeatIterIndex);
		return startTime;
    }

    public Calendar getNextEndTime() {
		Calendar currentTime = Calendar.getInstance();
		Calendar endTime = getEndTime();
		endTime.set( currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		
		endTime.add(Calendar.DAY_OF_WEEK, mRepeatIterIndex);
		return endTime;
    }
    
    /*
     * Used to check if the rule active now
     * It just check whether the current time is located between start and end time.
     * 
     * @Return: true means the rule is active; false for otherwise
     */
    public boolean isActive() {
		Calendar currentTime = Calendar.getInstance();
		boolean bActive = false;

        if (mEnabled == false)
            return false;
		
		if (isRepeat()) {
		    resetOccurence();
		    while(hasNextOccurence()) {
		    	Calendar startTime = getNextStartTime();
		    	Calendar endTime = getNextEndTime();

				if ((startTime.getTimeInMillis() <= currentTime.getTimeInMillis()) 
						&& (currentTime.getTimeInMillis() <= endTime.getTimeInMillis()))
				{
				    bActive = true;
				}
		    }
		} else {
		    if ((getStartTime().getTimeInMillis() <= currentTime.getTimeInMillis())
			    && (currentTime.getTimeInMillis() <= getEndTime().getTimeInMillis()))
		    {
		    	bActive = true;
		    }
		}
		
		return bActive;
    }
    
    public boolean setAlarm() {
        // Add a alarm at user-defined time
        if (isRepeat()) {
            resetOccurence();
            while(hasNextOccurence()) {
        	int dayOfWeekStart = getNextStartTime().get(Calendar.DAY_OF_WEEK);
        	int dayOfWeekEnd = getNextEndTime().get(Calendar.DAY_OF_WEEK);
                AutoMuteAlarmMgr.setAlarm(((int)mId<<16)|dayOfWeekStart<<8|Rule.REQUST_CODE_MUTE_ONCE, mId, true, getNextStartTime());
                AutoMuteAlarmMgr.setAlarm(((int)mId<<16)|dayOfWeekEnd<<8|Rule.REQUST_CODE_UNMUTE_ONCE, mId, true, getNextEndTime());        	
            }
        } else {
            AutoMuteAlarmMgr.setAlarm(((int)mId<<16)|Rule.REQUST_CODE_MUTE_ONCE, mId, false, getStartTime());
            AutoMuteAlarmMgr.setAlarm(((int)mId<<16)|Rule.REQUST_CODE_UNMUTE_ONCE, mId, false, getEndTime());
        }
        
        return true;
    }
    
    public boolean deleteAlarm() {
	// Cancel the alarm associated with rule ID
        if (isRepeat()) {
            resetOccurence();
            while(hasNextOccurence()) {
        	int dayOfWeekStart = getNextStartTime().get(Calendar.DAY_OF_WEEK);
        	int dayOfWeekEnd = getNextEndTime().get(Calendar.DAY_OF_WEEK);
                AutoMuteAlarmMgr.deleteAlarm(((int)mId<<16)|dayOfWeekStart<<8|Rule.REQUST_CODE_MUTE_ONCE, mId);
                AutoMuteAlarmMgr.deleteAlarm(((int)mId<<16)|dayOfWeekEnd<<8|Rule.REQUST_CODE_UNMUTE_ONCE, mId);        	
            }
        } else {
            AutoMuteAlarmMgr.deleteAlarm(((int)mId<<16)|Rule.REQUST_CODE_MUTE_ONCE, mId);
            AutoMuteAlarmMgr.deleteAlarm(((int)mId<<16)|Rule.REQUST_CODE_UNMUTE_ONCE, mId);            
        }
        
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rule)) return false;
        final Rule other = (Rule) o;
        return mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mId).hashCode();
    }

    @Override
    public String toString() {
        return "Rule{" +
                ", id=" + mId +
                ", title=" + mTitle +
                ", start_year=" + mStartYear +
                ", start_month=" + mStartMonth +
                ", start_day=" + mStartDay +
                ", start_hour=" + mStartHour +
                ", start_minutes=" + mStartMinute +
                ", end_year=" + mEndYear +
                ", end_month=" + mEndMonth +
                ", end_day=" + mEndDay +
                ", end_hour=" + mEndHour +
                ", end_minutes=" + mEndMinute +
                ", daysOfWeek=" + mDaysOfWeek +
                ", enabled=" + mEnabled +
                ", vibrate=" + mVibrate +
                '}';
    }
    /******************************************************************/
    public static ContentValues createContentValues(Rule rule) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (rule.mId != INVALID_ID) {
            values.put(_ID, rule.mId);
        }

        values.put(TITLE, rule.mTitle);
        values.put(START_YEAR, rule.mStartYear);
        values.put(START_MONTH, rule.mStartMonth);
        values.put(START_DAY, rule.mStartDay);
        values.put(START_HOUR, rule.mStartHour);
        values.put(START_MINUTE, rule.mStartMinute);
        values.put(END_YEAR, rule.mEndYear);
        values.put(END_MONTH, rule.mEndMonth);
        values.put(END_DAY, rule.mEndDay);
        values.put(END_HOUR, rule.mEndHour);
        values.put(END_MINUTE, rule.mEndMinute);
        values.put(DAYS_OF_WEEK, rule.mDaysOfWeek.getBitSet());
        values.put(ENABLED, rule.mEnabled ? 1 : 0);
        values.put(VIBRATE, rule.mVibrate ? 1 : 0);

        return values;
    }

    public static Intent createIntent(String action, long ruleId) {
        return new Intent(action).setData(getUri(ruleId));
    }

    public static Intent createIntent(Context context, Class<?> cls, long ruleId) {
        return new Intent(context, cls).setData(getUri(ruleId));
    }

    public static Uri getUri(long ruleId) {
        return ContentUris.withAppendedId(CONTENT_URI, ruleId);
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }
    
    /**
     * Get rule cursor loader for all rules.
     *
     * @param context to query the database.
     * @return cursor loader with all the rules.
     */
    public static CursorLoader getRulesCursorLoader(Context context) {
        return new CursorLoader(context, AutoMuteContract.RulesColumns.CONTENT_URI,
                QUERY_COLUMNS, null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * Get rule by id.
     *
     * @param contentResolver to perform the query on.
     * @param ruleId for the desired rule.
     * @return rule if found, null otherwise
     */
    public static Rule getRule(ContentResolver contentResolver, long ruleId) {
    	if (ruleId == Rule.INVALID_ID) return null;

        Cursor cursor = contentResolver.query(getUri(ruleId), QUERY_COLUMNS, null, null, null);
        Rule result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Rule(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * Get all rules given conditions.
     *
     * @param contentResolver to perform the query on.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @return list of rules matching where clause or empty list if none found.
     */
    public static List<Rule> getRules(ContentResolver contentResolver, String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<Rule> result = new LinkedList<Rule>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new Rule(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static Rule addRule(ContentResolver contentResolver, Rule rule) {
        ContentValues values = createContentValues(rule);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        rule.mId = getId(uri);
        
        rule.setAlarm();

        return rule;
    }

    public static boolean updateRule(ContentResolver contentResolver, Rule rule) {
        if (rule.mId == Rule.INVALID_ID) return false;
        ContentValues values = createContentValues(rule);
        long rowsUpdated = contentResolver.update(getUri(rule.mId), values, null, null);
        
        // Do NOT need to cancel the previous alarm
        // Because it is done before the content of rule is changed
        
        if (rule.isEnabled()) {
        	rule.setAlarm();
        }
        
        return rowsUpdated == 1;
    }

    public static boolean deleteRule(ContentResolver contentResolver, Rule rule) {
        if (rule.mId == INVALID_ID) return false;

        // For active rule, it must cancel the notification
        if (rule.isActive()) {
            NotificationManager manager = (NotificationManager) AutoMuteApp.context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel((int)rule.mId);

            AudioManager audioManager = (AudioManager) AutoMuteApp.context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }

        rule.deleteAlarm();
        int deletedRows = contentResolver.delete(getUri(rule.mId), "", null);
        return deletedRows == 1;
    }

    public static final Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        public Rule createFromParcel(Parcel p) {
            return new Rule(p);
        }

        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };
}
