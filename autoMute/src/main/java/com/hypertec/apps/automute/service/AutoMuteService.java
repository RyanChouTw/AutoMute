/**
 * Copyright (c) 2014/10/28 Hypertec Corporation
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
package com.hypertec.apps.automute.service;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;
import com.hypertec.apps.automute.provider.Rule;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;

/**
 * @author ryanchou
 *
 */
public class AutoMuteService extends Service {

    public final static String ACTION_REGISTER_REGINER_MODE = "register-riger-mode";
    
    BroadcastReceiver mRingerModeBroadcastReceiver = null;
    private long mActiveRuleId;
    private int mActiveRequestCode;
    private int mTriggerTime = 0;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    protected void handleCommand(Intent intent) {
	
		if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_REGISTER_REGINER_MODE)) {
		    mActiveRuleId = intent.getLongExtra(Rule._ID, Rule.INVALID_ID);
		    mActiveRequestCode = intent.getIntExtra(Rule.REQUEST_CODE, Rule.REQUEST_CODE_INVALID);
			
		    mRingerModeBroadcastReceiver = new BroadcastReceiver() {
		    	@Override
		    	public void onReceive(Context context, Intent intent) {
		    		if (intent.getAction() != null && intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION))
		    		{
		    			AudioManager audioManager = (AudioManager) AutoMuteApp.context.getSystemService(Context.AUDIO_SERVICE);
		    	    	if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && mTriggerTime > 0) {
		    				stopActiveUnmuteAlarm(context);
		    				stopSelf();
		    			}
		    		}
		    		mTriggerTime++;
		    	}
		    };
		    
		    IntentFilter filter=new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
		    registerReceiver(mRingerModeBroadcastReceiver,filter);
		}
    }
    
    protected boolean stopActiveUnmuteAlarm(Context context) {
		if (mActiveRuleId != Rule.INVALID_ID) {
		    int requestCode = ((mActiveRequestCode & 0xFFFFFF00) | Rule.REQUST_CODE_UNMUTE_ONCE);
		    AutoMuteAlarmMgr.deleteAlarm(requestCode, mActiveRuleId);
		    
		    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		    manager.cancel((int)mActiveRuleId);
		    
		    Rule rule = Rule.getRule(context.getContentResolver(), mActiveRuleId);
		    // Because it is possible that the ringer mode is changed after the muter is deleted
		    // we have to check it first
		    if (rule != null && rule.isRepeat() == false) {
		    	rule.setEnable(false);
		    	Rule.updateRule(context.getContentResolver(), rule);
		    }

		    return true;
		}
		return false;
    }
    
    @Override
    public void onDestroy() {
    	if (mRingerModeBroadcastReceiver != null) {
    		unregisterReceiver(mRingerModeBroadcastReceiver);
    	}
    }

    @Override
    public IBinder onBind(Intent intent) {	
    	return null;
    }

}
