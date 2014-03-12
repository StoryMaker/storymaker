
package info.guardianproject.mrapp;

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Closeable;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.CheckBox;

//import com.google.analytics.tracking.android.GoogleAnalytics;

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept
 * before using the application. Your application should call
 * {@link Eula#show(android.app.Activity)} in the onCreate() method of the first
 * activity. If the user accepts the EULA, it will never be shown again. If the
 * user refuses, {@link android.app.Activity#finish()} is invoked on your
 * activity.
 */
class Eula {

    /**
     * callback to let the activity know when the user has accepted the EULA.
     */
    static interface OnEulaAgreedTo {

        /**
         * Called when the user has accepted the eula and the dialog closes.
         */
        void onEulaAgreedTo();
    }

    Activity mActivity;

    // CheckBox cb;

    Eula(final Activity activity) {
        mActivity = activity;
    }

    /**
     * Displays the EULA if necessary. This method should be called from the
     * onCreate() method of your main Activity.
     * 
     * @param mActivity The Activity to finish if the user rejects the EULA.
     * @return Whether the user has agreed already.
     */
    boolean show() {
        final SharedPreferences prefsEula = mActivity.getSharedPreferences(Globals.PREFERENCES_EULA, Activity.MODE_PRIVATE);
        final SharedPreferences prefsAnalytics = mActivity.getSharedPreferences(Globals.PREFERENCES_ANALYTICS, Activity.MODE_PRIVATE);
        // boolean noOptIn =
        // !prefsAnalytics.contains(Globals.PREFERENCE_ANALYTICS_OPTIN);
        boolean noEula = !prefsEula.getBoolean(Globals.PREFERENCE_EULA_ACCEPTED, false);

        // if (noEula || noOptIn) {
        if (noEula) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            LayoutInflater adbInflater = LayoutInflater.from(mActivity);
            View view = adbInflater.inflate(R.layout.activity_eula, null);
            // cb = (CheckBox) view.findViewById(R.id.checkbox);
            builder.setView(view);
            builder.setTitle(R.string.eula_title);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.eula_accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    accept(prefsEula);
                    if (mActivity instanceof OnEulaAgreedTo) {
                        ((OnEulaAgreedTo) mActivity).onEulaAgreedTo();
                    }
                }
            });
            builder.setNegativeButton(R.string.eula_refuse, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    refuse(mActivity);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    refuse(mActivity);
                }
            });
            // builder.setMessage(readEula(activity));
            builder.create().show();
            return false;
        }
        return true;
    }

    /**
     * Return whether the EULA was accepted. Use this method in case you don't
     * wish to show a EULA dialog for the negative condition
     * 
     * @param context
     * @return
     */
    public static boolean isAccepted(Context context) {
        SharedPreferences prefsEula = context.getSharedPreferences(
                Globals.PREFERENCES_EULA, Activity.MODE_PRIVATE);
        return prefsEula.getBoolean(Globals.PREFERENCE_EULA_ACCEPTED, false);
    }

    private void accept(SharedPreferences preferences) {
        preferences.edit().putBoolean(Globals.PREFERENCE_EULA_ACCEPTED, true).commit();

//     final SharedPreferences prefsAnalytics = mActivity.getSharedPreferences(Globals.PREFERENCES_ANALYTICS, Activity.MODE_PRIVATE);
//        prefsAnalytics.edit().putBoolean(Globals.PREFERENCE_ANALYTICS_OPTIN, cb.isChecked()).commit();
//        if (cb.isChecked()) {
//             GoogleAnalytics.getInstance(mActivity).setAppOptOut(false);
//        }
    }

    private static void refuse(Activity activity) {
        activity.finish();
    }

    private CharSequence readEula() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(mActivity.getAssets().open(Globals.ASSET_EULA)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            return buffer;
        } catch (IOException e) {
            return "";
        } finally {
            closeStream(in);
        }
    }

    /**
     * Closes the specified stream.
     * 
     * @param stream The stream to close.
     */
    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
