package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by bofcarbon1 on 8/15/2017.
 */

//Ch 27 Broadcasts

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReciver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received Broadcast Intent " + intent.getAction());

        //Turn on the Alarm when receving a boot broadcast
        boolean isOn = QueryPreferences.IsAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);

    }

}
