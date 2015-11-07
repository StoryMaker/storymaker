package org.storymaker.app.tests;

import timber.log.Timber;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import scal.io.liger.StorageHelper;

/**
 * As Spoon screenshots don't work on Android 5
 * (see https://github.com/square/spoon/issues/189),
 * this is the beginning of a class that could potentially
 * save screenshots to a location on the device for retrieval
 * after test copletion.
 *
 * Created by davidbrodsky on 12/19/14.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Screenshooter {
    public final String TAG = getClass().getSimpleName();

    private Instrumentation mInstrumentation;

    public Screenshooter(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    public void takeScreenshot(String title) {
        if (mInstrumentation.getContext() == null) {
            Timber.e("Provided instrumentation has non Context reference. Cannot take screenshot");
            return;
        }

        Bitmap screen = mInstrumentation.getUiAutomation().takeScreenshot();
        try {

            // File output = new File(mInstrumentation.getContext().getExternalFilesDir(null), title);
            File output = new File(StorageHelper.getActualStorageDirectory(mInstrumentation.getContext()), title);

            FileOutputStream os = new FileOutputStream(output);
            screen.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (IOException e) {
            Timber.w("Unable to save: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
