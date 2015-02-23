/**
 * Copyright (c) 2014/10/20 Hypertec Corporation
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
package com.hypertec.apps.automute.Utils;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.provider.Rule;
import com.hypertec.apps.automute.receiver.AutoMuteAlarmReceiver;

/**
 * @author ryanchou
 *
 */
public class AutoMuteAlarmMgr {

    public final static int DAYS_IN_A_WEEK = 7;
    public final static int HOURS_IN_A_DAY = 24;
    public final static int MINS_IN_A_HOUR = 60;
    public final static int SECS_IN_A_MIN = 60;
    public final static int MILLIS_IN_A_SEC = 1000;
    
    public final static long MILLS_IN_A_WEEK = DAYS_IN_A_WEEK*HOURS_IN_A_DAY*MINS_IN_A_HOUR*SECS_IN_A_MIN*MILLIS_IN_A_SEC;
    public final static long MILLS_IN_A_DAY = HOURS_IN_A_DAY*MINS_IN_A_HOUR*SECS_IN_A_MIN*MILLIS_IN_A_SEC;
    
    public static void setAlarm(int requestCode, long id, boolean isWeekly, Calendar triggerTime) 
    {
        Intent intent = new Intent(AutoMuteApp.context, AutoMuteAlarmReceiver.class);
        intent.putExtra(Rule.REQUEST_CODE, requestCode);
        intent.putExtra(Rule._ID, id);
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(AutoMuteApp.context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) AutoMuteApp.context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar currTime = Calendar.getInstance();
        if (isWeekly) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), MILLS_IN_A_WEEK, alarmIntent);
        } else {
            if (triggerTime.after(currTime)) {
        	alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), alarmIntent);
            }
        }
    }
    
    public static void deleteAlarm(int requestCode, long id) 
    {
    	Intent intent = new Intent(AutoMuteApp.context, AutoMuteAlarmReceiver.class);
    	intent.putExtra(Rule.REQUEST_CODE, requestCode);
    	intent.putExtra(Rule._ID, id);
	
    	PendingIntent alarmIntent = PendingIntent.getBroadcast(AutoMuteApp.context, (int) requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	
    	AlarmManager alarmMgr = (AlarmManager) AutoMuteApp.context.getSystemService(Context.ALARM_SERVICE);
    	alarmMgr.cancel(alarmIntent);
    }
    
    public static void stopAllAlarms()
    {
		// When user checks "disable automute service" in Settings, 
		// all alarms must be canceled.
		List<Rule> enabledRules = Rule.getRules(AutoMuteApp.context.getContentResolver(), Rule.ENABLED + "=1");
		
		for (Rule rule : enabledRules) {
		    rule.deleteAlarm();
		}
    }
    
    public static void startAllAlarms() 
    {
		// The alarm will all be turned-off after powering-cycle
		// Thus, after booting, it must set all alarms again
		List<Rule> enabledRules = Rule.getRules(AutoMuteApp.context.getContentResolver(), Rule.ENABLED + "=1");
		
		for (Rule rule : enabledRules) {
		    rule.setAlarm();
		}
    }
}
