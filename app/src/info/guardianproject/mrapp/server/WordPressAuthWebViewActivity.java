
package info.guardianproject.mrapp.server;

import info.guardianproject.mrapp.Globals;
import info.guardianproject.mrapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Hosts a WebView specifically for presenting
 * WordPress authentication. Upon registration,
 * the Activity finishes, saving registered state
 * to the default SharedPreferences
 * 
 * TODO: Upon registration, redirect to Login url 
 * with a message to check email for password?
 * 
 * @author David Brodsky
 *
 */
public class WordPressAuthWebViewActivity extends WebViewActivity {

    private String mFinishUrl = ServerManager.PATH_REGISTERED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null)
        {

            mWebView.setWebViewClient(new WebViewClient()
            {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(mFinishUrl)) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putBoolean(Globals.PREFERENCES_WP_REGISTERED, true)
                                .apply();
                        finish();
                        return true;
                    }
                    return false;
                }

            });
        }
    }
}
