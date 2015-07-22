
package org.storymaker.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import org.storymaker.app.Eula.OnEulaAgreedTo;

/**
 * Prompt the user to view & agree to the StoryMaker TOS / EULA
 * and present the choice to create a StoryMaker Account.
 * 
 * Should be launched as the start of a new Task, because
 * when this Activity finishes without starting another,
 * it is the result of the user rejecting the TOS / EULA,
 * and so should result in the app exiting.
 * 
 * @author David Brodsky
 *
 */
public class FirstStartActivity extends Activity implements OnEulaAgreedTo {

    private boolean mTosAccepted;
    private Button mTosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_first_start);
        mTosAccepted = false;
        mTosButton = (Button) findViewById(R.id.btnTos);
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if ( PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.PREFERENCES_WP_REGISTERED, false) ) {
            // The user is returning to this Activity after a successful WordPress signup
            Intent homeIntent = new Intent(FirstStartActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }
    }

    /**
     * When the EULA / TOS button is clicked, show the EULA if it hasn't been shown.
     * Else, allow the user to accept immediately. 
     */
    public void onTosButtonClick(View v) {
        mTosAccepted  = new Eula(this).show();
        if(mTosAccepted) {
            markTosButtonAccepted();
        }
    }

    public void onNoThanksButtonClick(View v) {
        if (assertTosAccepted()) {
            Intent mainIntent = new Intent(this, HomeActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    public void onSignupButtonClick(View v) {
        if (assertTosAccepted()) {
//            StoryMakerApp.getServerManager().createAccount(this);
            Intent i = new Intent(this, ConnectAccountActivity.class);
            this.startActivity(i);
        }

    }

    /**
     * Show an AlertDialog prompting the user to 
     * accept the EULA / TOS
     * @return
     */
    private boolean assertTosAccepted() {
        if (!mTosAccepted) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.tos_dialog_title))
                    .setMessage(getString(R.string.tos_dialog_msg))
                    .setPositiveButton(getString(R.string.tos_dialog_positive_button),
                            new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.dismiss();
                                }

                            }).show();
            return false;
        }
        return true;
    }
    
    private void markTosButtonAccepted() {
        Drawable tosStateDrawable = FirstStartActivity.this.getResources().getDrawable(
                R.drawable.ic_contextsm_checkbox_checked);
        mTosButton.setCompoundDrawablesWithIntrinsicBounds(tosStateDrawable, null, null, null);
    }

    @Override
    public void onEulaAgreedTo() {
        mTosAccepted = true;
        markTosButtonAccepted();
    }
}
