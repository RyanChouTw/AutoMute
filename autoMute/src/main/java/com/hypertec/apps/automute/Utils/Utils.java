/**
 * 
 */
package com.hypertec.apps.automute.Utils;

import java.util.List;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.provider.Rule;
import com.hypertec.apps.automute.service.AutoMuteService;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

/**
 * @author ryanchou
 *
 */
public class Utils {

	final static String TAG = "UTILS";
	final static String PREFS_NAME = "MyPrefsFile";
	
	final static String AUTOMUTE_STATUS = "app-status";
	public static boolean IsAutoMuteActive() {
		boolean appStatus = true;

		SharedPreferences settings = AutoMuteApp.context.getSharedPreferences(PREFS_NAME, 0);

		if (settings.getBoolean(AUTOMUTE_STATUS, true)) {
		    //the app is being launched for first time, do something        
		    Log.d(TAG, "AutoMute is ON");
		    appStatus = true;
		} else {
			Log.d(TAG, "AutoMute is OFF");
			appStatus = false;
		}
		return appStatus;
	}
	
	public static void SetAutoMuteStatus(boolean bActive) {
		
		SharedPreferences settings = AutoMuteApp.context.getSharedPreferences(PREFS_NAME, 0);
	    settings.edit().putBoolean(AUTOMUTE_STATUS, bActive).commit(); 
	}

	public static void AudioMute(boolean isVibrate) {
		AudioManager audioManager = (AudioManager) AutoMuteApp.context.getSystemService(Context.AUDIO_SERVICE);
		
		if(isVibrate) {
		    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else {
		    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}
	}
	
	public static void AudioUnmute() {
    	AudioManager audioManager = (AudioManager) AutoMuteApp.context.getSystemService(Context.AUDIO_SERVICE);
    	audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	
	public static void startAutoMuteService(long ruleId, int requestCode) {
		Intent intent = new Intent(AutoMuteApp.context, AutoMuteService.class);
		
		intent.setAction(AutoMuteService.ACTION_REGISTER_REGINER_MODE);
		intent.putExtra(Rule._ID, ruleId);
		intent.putExtra(Rule.REQUEST_CODE, requestCode);
		
		AutoMuteApp.context.startService(intent);
	}
	
	public static void stopAutoMuteService() {
		Intent intent = new Intent(AutoMuteApp.context, AutoMuteService.class);
		
		intent.setAction(AutoMuteService.ACTION_REGISTER_REGINER_MODE);
		
		AutoMuteApp.context.stopService(intent);
	}
	
	/*
	 * @Return 
	 * 	boolean : true if there is active rule; false otherwise
	 */
	public static boolean stopActiveRule() {
		List<Rule> enabledRules = Rule.getRules(AutoMuteApp.context.getContentResolver(), Rule.ENABLED + "=1");
		
		for (Rule rule : enabledRules) {
		    if (rule.isActive())
		    {
        		NotificationManager notificationManager = (NotificationManager) AutoMuteApp.context.getSystemService(Context.NOTIFICATION_SERVICE);
        		notificationManager.cancel((int)rule.getId());
        		
        		Utils.stopAutoMuteService();
        		Utils.AudioUnmute();
        		
        		return true;
		    }
		}

		return false;
	}
}
