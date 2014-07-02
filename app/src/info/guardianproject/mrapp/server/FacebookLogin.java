package info.guardianproject.mrapp.server;

import org.holoeverywhere.widget.Toast;

import info.guardianproject.mrapp.BaseActivity;
import info.guardianproject.mrapp.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.facebook.*;
import com.facebook.model.*;

public class FacebookLogin extends BaseActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_panels);

    // start Facebook Login
    Session.openActiveSession(this, true, new Session.StatusCallback() {

      // callback when session changes state
      @Override
      public void call(Session session, SessionState state, Exception exception) {
        if (session.isOpened()) {

          // make request to the /me API
          Request.newMeRequest(session, new Request.GraphUserCallback() {

            // callback after Graph API response with user object
            @Override
            public void onCompleted(GraphUser user, Response response) {
              if (user != null) {
               // TextView welcome = (TextView) findViewById(R.id.welcome);
               // welcome.setText("Hello " + user.getName() + "!");
               Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_LONG).show();              }
            }
          }).executeAsync();
        }
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
  }

}