package com.hypertec.apps.automute.receiver;

import com.hypertec.apps.automute.AutoMuteApp;
import com.hypertec.apps.automute.R;
import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;
import com.hypertec.apps.automute.Utils.Utils;
import com.hypertec.apps.automute.provider.Rule;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

public class AutoMuteAlarmReceiver extends BroadcastReceiver {
       
    @TargetApi(16)
    @Override
    public void onReceive(Context context, Intent intent) {
		if (intent == null) return;
		
		/* Handle boot completed : To restart all active alarms if necessary */
		if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			if (Utils.IsAutoMuteActive() == true) {
				AutoMuteAlarmMgr.startAllAlarms();
			}
		    return;
		}
		
		/* Handle package upgraded : To restart all active alarms and set automute enabled */
		if (intent.getAction() != null && intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
		    AutoMuteAlarmMgr.startAllAlarms();
		    
		    // Force to turn-on AutoMute after upgrading
		    if (Utils.IsAutoMuteActive() == false) {
		    	Utils.SetAutoMuteStatus(true);
		    }
		    return;			
		}
		
		int requestCode = intent.getIntExtra(Rule.REQUEST_CODE, Rule.REQUEST_CODE_INVALID);
		long ruleId = intent.getLongExtra(Rule._ID, Rule.INVALID_ID);
		Rule rule = Rule.getRule(context.getContentResolver(), ruleId);
		
		if (rule != null)
		{
		    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		    if ((requestCode & 0x000000FF) == Rule.REQUST_CODE_MUTE_ONCE) {
	
				CharSequence startTimeText = rule.getStartTimeDispText(context);
				CharSequence endTimeText = rule.getEndTimeDispText(context);
			    
				//Set the icon, scrolling text and time-stamp
				CharSequence contentTitle = context.getResources().getString(R.string.notification_title);
				CharSequence contentText = context.getResources().getString(R.string.notification_content);
				CharSequence contentDetail = String.format(context.getResources().getString(R.string.notification_content_text_fmt),
										startTimeText, endTimeText);
		
				Notification notification = null;
				Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify_mute_large);
		
				Intent startAutoMuteIntent = context.getPackageManager().getLaunchIntentForPackage("com.hypertec.apps.automute");		
				PendingIntent pendingIntent = PendingIntent.getActivity(AutoMuteApp.context, requestCode, startAutoMuteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	
				if (Build.VERSION.SDK_INT >= 16) {
				    notification = new Notification.Builder(context)
							.setWhen(System.currentTimeMillis())
							.setContentTitle(contentTitle)
							.setContentText(contentText)
							.setSmallIcon(R.drawable.ic_stat_notify_mute)
							.setLargeIcon(largeIcon)
							.setContentIntent(pendingIntent)
							.setOngoing(true)
							.setStyle(new Notification.BigTextStyle()
								.bigText(contentDetail))
							.build();
				} else {
				    notification = new Notification.Builder(context)
				    			.setWhen(System.currentTimeMillis())
				    			.setContentTitle(contentTitle)
				    			.setContentText(contentText)
				    			.setSmallIcon(R.drawable.ic_stat_notify_mute)
				    			.setLargeIcon(largeIcon)
				    			.setContentIntent(pendingIntent)
				    			.setOngoing(true)
				    			.setStyle(new Notification.BigTextStyle()
								.bigText(contentDetail))
				    			.getNotification();		
				}
	
				notificationManager.notify((int) ruleId, notification);	    
		
				Utils.AudioMute(rule.isVibrate());
					
				// Start a service to monitor the change of ringer mode
				// If user changes the ringer mode by himself, then turn off the unmute alarm
				Utils.startAutoMuteService(ruleId, requestCode);
		    }
		    else if ((requestCode & 0x000000FF) == Rule.REQUST_CODE_UNMUTE_ONCE) {
				// stop to monitor the change of ringer mode
				Utils.stopAutoMuteService();
		
		    	notificationManager.cancel((int)ruleId);
				Utils.AudioUnmute();
				
				if (rule.isRepeat() == false) {
				    rule.setEnable(false);
				    Rule.updateRule(context.getContentResolver(), rule);
				}		
		    }
		}
    }    
}
