
package info.guardianproject.mrapp.server;

import info.guardianproject.mrapp.FirstStartActivity;
import info.guardianproject.mrapp.Globals;
import info.guardianproject.mrapp.HomeActivity;
import info.guardianproject.mrapp.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;

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
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
        View dialogBody = inflater.inflate(R.layout.dialog_account_created);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.acct_created_dialog_title))
            .setView(dialogBody)
            .setPositiveButton(getString(R.string.acct_created_dialog_positive_button), positiveBtnClickListener)
            .show();
    }
}
