package org.storymaker.app;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by josh on 12/18/15.
 */
public class BaseHomeActivity extends BaseActivity {
    protected void checkForTor() {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

         boolean useTor = settings.getBoolean("pusetor", false);

         if (useTor) {

             if (!OrbotHelper.isOrbotInstalled(this)) {
                startActivity(OrbotHelper.getOrbotInstallIntent(this));
             } else if (!OrbotHelper.isOrbotRunning(this)) {
                OrbotHelper.requestStartTor(this);
             }
         }
    }
}
