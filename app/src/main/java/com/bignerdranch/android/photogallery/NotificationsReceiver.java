package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by bofcarbon1 on 8/16/2017.
 */

public class NotificationsReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationsReceiver";

    @Override
    public void onReceive(Context c, Intent i) {
        Log.i(TAG, "received result:" + getResultData());

        if (getResultCode() != Activity.RESULT_OK) {
            //a foregrand activity cancelled the broadcast
            return;
        }

        int requestCode = i.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification)
                i.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(c);
        notificationManager.notify(requestCode, notification);

    }
}
