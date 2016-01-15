
package org.storymaker.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import timber.log.Timber;

public class SMPanicResponderActivity extends Activity implements ICacheWordSubscriber {

    public static final String PANIC_TRIGGER_ACTION = "info.guardianproject.panic.action.TRIGGER";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // need to check for pin before locking

        String CACHEWORD_SET = getText(R.string.cacheword_state_set).toString();

        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {

            //Timber.d("panic - pin set, panic!");

            Intent intent = getIntent();
            if (intent != null && PANIC_TRIGGER_ACTION.equals(intent.getAction())) {
                CacheWordHandler cacheWordHandler = new CacheWordHandler(this, 60);
                cacheWordHandler.connectToService();
                cacheWordHandler.lock();
                cacheWordHandler.disconnectFromService();
                ExitActivity.exitAndRemoveFromRecentApps(this);
            }

        } else {
            //Timber.d("panic - no pin set, don't panic!");
        }

        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    @Override
    public void onCacheWordUninitialized() {

    }

    @Override
    public void onCacheWordLocked() {

    }

    @Override
    public void onCacheWordOpened() {

    }
}
