
package org.storymaker.app.server;

import org.storymaker.app.Globals;
import org.storymaker.app.R;
import org.storymaker.app.StoryMakerApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings.PluginState;
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

    //private String mFinishUrl = ServerManager.PATH_REGISTERED;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setPluginState(PluginState.ON);
        mWebView.getSettings().setAllowFileAccess(true);
        
        Intent intent = getIntent();
        if (intent != null)
        {

            mWebView.setWebViewClient(new WebViewClient()
            {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // new site redirects to https://storymaker.org/
                    if (url.equals(StoryMakerApp.STORYMAKER_DEFAULT_SERVER_URL)) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putBoolean(Globals.PREFERENCES_WP_REGISTERED, true)
                                .apply();
                        showAccountCreatedDialog(new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        return true;
                    }
                    return false;
                }

            });
        }
    }
    
    /**
     * Show a dialog explaining the next
     * steps for a user who has just
     * created a WordPress account.
     * 
     * e.g: You must now check your email
     * for a password that you can then use to login
     */
    private void showAccountCreatedDialog(DialogInterface.OnClickListener positiveBtnClickListener) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogBody = inflater.inflate(R.layout.dialog_account_created, null);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.acct_created_dialog_title))
            .setView(dialogBody)
            .setPositiveButton(getString(R.string.acct_created_dialog_positive_button), positiveBtnClickListener)
            .show();
    }
    
    @Override
	public void finish() {		
		super.finish();
	}
}
