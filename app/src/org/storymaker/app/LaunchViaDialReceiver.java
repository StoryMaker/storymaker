package org.storymaker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by n8fr8 on 11/2/15.
 */
public class LaunchViaDialReceiver extends BroadcastReceiver {

    private static final String LAUNCHER_NUMBER = "98765";

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNubmer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (LAUNCHER_NUMBER.equals(phoneNubmer)) {
            setResultData(null);
            Intent appIntent = new Intent(context, HomeActivity.class);
            appIntent.putExtra("showlauncher",true);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}