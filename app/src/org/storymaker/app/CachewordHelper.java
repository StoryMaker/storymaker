package org.storymaker.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.CacheWordHandler;
import timber.log.Timber;

/**
 * Created by josh on 2/22/16.
 */
public class CachewordHelper {
    public static void initializeDefaultPin(Context context, CacheWordHandler cacheWordHandler) {
        // set default pin, prompt for actual pin on first lock
        try {
            CharSequence defaultPinSequence = context.getText(R.string.cacheword_default_pin);
            char[] defaultPin = defaultPinSequence.toString().toCharArray();
            cacheWordHandler.setPassphrase(defaultPin);
            SharedPreferences sp = context.getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString("cacheword_status", BaseActivity.CACHEWORD_UNSET);
            e.commit();
            Timber.d("set default cacheword pin");
        } catch (GeneralSecurityException gse) {
            Timber.e(gse, "failed to set default cacheword pin: " + gse.getMessage());
        }
    }
}
