package com.hypertec.apps.automute;

import com.splunk.mint.Mint;

import android.app.Application;
import android.content.Context;

public class AutoMuteApp extends Application {

    public static Context context;
    public static final int TOAST_DURATION = 1500; 

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // The following line triggers the initialization of Splunk
        Mint.initAndStartSession(context, "58a02578");
    }
}
