
package info.guardianproject.mrapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import info.guardianproject.mrapp.server.LoginActivity;

public class ConnectAccountActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    public void onCreateAccountButtonClick(View v) {
        StoryMakerApp.getServerManager().createAccount(this);
    }
    
    public void onSignInButtonClick(View v) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

}
