
package org.storymaker.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.storymaker.app.server.LoginActivity;
import org.storymaker.app.server.ServerManager;

public class ConnectAccountActivity extends BaseActivity {
    
    private boolean mActivityJustCreated;
    private TextView mTitleText;
    private Button mCreateAccountBtn;
    private Button mSignInOrOutBtn;
    
    private ServerManager mServerManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_account);
        mServerManager = ((StoryMakerApp) this.getApplication()).getServerManager();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mActivityJustCreated = true;
        assignViewReferences();
    }
    
    private void assignViewReferences() {
        mCreateAccountBtn = (Button) findViewById(R.id.btnCreateAccount);
        mSignInOrOutBtn = (Button) findViewById(R.id.btnSignIn);
        mTitleText = (TextView) findViewById(R.id.titleText);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if ( !mActivityJustCreated && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Globals.PREFERENCES_WP_REGISTERED, false) ) {
            // The user is returning to this Activity after a successful WordPress signup
            // that originated here.
            startHomeActivityAsNewTask();
        }
        else if (mServerManager.hasCreds()) {
            // A WordPress account was already used to log in. Show as "Signed In Screen"
            mTitleText.setText(mServerManager.getUserName());
            mCreateAccountBtn.setVisibility(View.INVISIBLE);
            mSignInOrOutBtn.setText(getString(R.string.sign_out));
        }
        mActivityJustCreated = false;
    }
    
    public void onCreateAccountButtonClick(View v) {
        StoryMakerApp.getServerManager().createAccount(this);
    }
    
    public void onSignInButtonClick(View v) {
        if (!mServerManager.hasCreds()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            
            startActivity(loginIntent);
        } else {
            mServerManager.logOut();
            startHomeActivityAsNewTask();
        }
    }
    
    private void startHomeActivityAsNewTask() {
        Intent homeIntent = new Intent(ConnectAccountActivity.this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}
