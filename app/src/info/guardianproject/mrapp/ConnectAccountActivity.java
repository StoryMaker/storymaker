
package info.guardianproject.mrapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;

public class ConnectAccountActivity extends BaseActivity {
    
    private boolean mActivityJustCreated;
    private TextView mTitleText;
    private Button mCreateAccountBtn;
    private Button mSignInOrOutBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        ServerManager serverManager = ((StoryMakerApp) this.getApplication()).getServerManager();
        if ( !mActivityJustCreated && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Globals.PREFERENCES_WP_REGISTERED, false) ) {
            // The user is returning to this Activity after a successful WordPress signup
            // that originated here.
            Intent homeIntent = new Intent(ConnectAccountActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }
        // TODO: Better way to determine if a user is logged in?
        else if (serverManager.hasCreds()) {
            // A WordPress account was already used to log in. Show as "Signed In Screen"
            // TODO: How to get a user's display name, not username
            mTitleText.setText(serverManager.getUserName());
            mCreateAccountBtn.setVisibility(View.INVISIBLE);
            mSignInOrOutBtn.setText(getString(R.string.sign_out));
        }
        mActivityJustCreated = false;
    }
    
    public void onCreateAccountButtonClick(View v) {
        StoryMakerApp.getServerManager().createAccount(this);
    }
    
    public void onSignInButtonClick(View v) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
