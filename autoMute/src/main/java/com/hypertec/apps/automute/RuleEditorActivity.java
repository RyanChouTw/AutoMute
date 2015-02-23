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
package com.hypertec.apps.automute;

import java.util.Calendar;

import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;
import com.hypertec.apps.automute.Utils.Utils;
import com.hypertec.apps.automute.fragments.RuleEditorFragment;
import com.hypertec.apps.automute.provider.Rule;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


/**
 * @author ryan chou (ryanchou0210@gmail.com)
 *
 */
public class RuleEditorActivity extends Activity { 
    
    private RuleEditorFragment mRuleEditorFragment;
    private Rule mRule = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_rule_editor);
	
	FragmentManager fragmentManager = getFragmentManager();
	
	mRule = getIntent().getParcelableExtra(Rule.RULE_ITEM);
	mRuleEditorFragment = RuleEditorFragment.newInstance(this,  mRule);
        fragmentManager.beginTransaction()
                .replace(R.id.rule_editor_content, mRuleEditorFragment)
                .commit();
        
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu items for use in the action bar
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.rule_editor, menu);
	
        getActionBar().setIcon(R.drawable.ic_action_cancel);
        
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);	
	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	int id = item.getItemId();
        if (id == R.id.action_ok) {
            Calendar startTime = mRuleEditorFragment.getStartTime();
            Calendar endTime = mRuleEditorFragment.getEndTime();
            Rule rule;
            if (mRule == null) {
            	rule = new Rule(startTime, endTime);
            }
            else { // Edit
            	/* 	1. Cancel the previous alarm
            	 	2. If the rule is active, cancel the notification and unmute sound */ 
            	mRule.deleteAlarm();	// Cancel original alarms
            	if (mRule.isActive()) {
            		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            		notificationManager.cancel((int)mRule.getId());
            		
            		Utils.stopAutoMuteService();
            		Utils.AudioUnmute();
            	}
            	
	        	rule = mRule;
	        	rule.setStartTime(startTime);
	        	rule.setEndTime(endTime);
            }
            
            rule.setTitle(mRuleEditorFragment.getTitle());
            rule.setEnable(true);
            rule.setVibrate(mRuleEditorFragment.getVibrate());
            rule.setRepeat(mRuleEditorFragment.getRRule());
            
            if (CheckValidity(rule)) {
                if (mRule != null) {
                    Rule.updateRule(getContentResolver(), rule);
                } else {
                    Rule.addRule(getContentResolver(), rule);
                }        	
                finish();
            }
            
            return true;
        }
        else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean CheckValidity(Rule rule) {
	boolean isValid = true;
	
	if (rule.isRepeat()) {
	    if (rule.getEndTime().getTimeInMillis() - rule.getStartTime().getTimeInMillis() 
		    >= AutoMuteAlarmMgr.MILLS_IN_A_DAY - 1) {
		Toast.makeText(this, R.string.warning_repeat_less_than_24, AutoMuteApp.TOAST_DURATION).show();
		isValid = false;
	    }
	}
	else {
	    long startTimeInMillis = rule.getStartTime().getTimeInMillis();
	    long endTimeInMillis = rule.getEndTime().getTimeInMillis();
	    if (startTimeInMillis >= endTimeInMillis) {
		// start time must be eariler than end time
		Toast.makeText(this, R.string.warning_start_must_earlier_than_end, AutoMuteApp.TOAST_DURATION).show();
		isValid = false;
	    }
	    else if (rule.getStartTime().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
		// start time must be after current time
		Toast.makeText(this, R.string.warning_start_time_must_after_current, AutoMuteApp.TOAST_DURATION).show();
		isValid = false;
	    }
	    else if (rule.getEndTime().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
		// end time must be after current time
		Toast.makeText(this, R.string.warning_end_time_must_after_current, AutoMuteApp.TOAST_DURATION).show();
		isValid = false;
	    }
	}
	return isValid;
    }
}
