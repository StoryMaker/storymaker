package org.storymaker.app.server;

import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;

public class RegisterActivity extends Activity {
    private static String TAG = "RegisterActivity";

    private static final String STORYMAKER_API_REGISTER_ENDPOINT = "http://beta.storymaker.org/api/v0/user/";

    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        init();
    }

    private void init() {
        etEmail = (EditText) findViewById(R.id.et_email);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        etPasswordConfirm = (EditText) findViewById(R.id.et_password_confirm);

        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
        loginScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // return to login screen
                finish();
            }
        });

        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(performValidations()) {
                    register();
                }
            }
        });
    }

    private boolean performValidations() {
        boolean isValid = true;

        //clear validations
        etEmail.setError(null);
        etUsername.setError(null);
        etPassword.setError(null);
        etPasswordConfirm.setError(null);

        //check email
        String email = etEmail.getText().toString().trim();
        if(!isStringValid(etEmail.getText().toString().trim())) {
            etEmail.setError(getString(R.string.error_lbl_required));
            isValid = false;
        } else if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_email_not_valid));
            isValid = false;
        }

        //check username
        if(!isStringValid(etUsername.getText().toString().trim())) {
            etUsername.setError(getString(R.string.error_lbl_required));
            isValid = false;
        }

        //check password
        if (!passwordMinimalLength()) {
            etPassword.setError(getString(R.string.error_password_length));
        }
        if(!passwordsMatch()) {
            etPasswordConfirm.setError(getString(R.string.error_passwords_not_matching));
            isValid = false;
        }

        return isValid;
    }

    // text valid check
    private boolean isStringValid(String input) {
        return (null != input && input.length() > 1);
    }

    // text valid check
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // password length check
    private boolean passwordMinimalLength() {
        String password = etPassword.getText().toString().trim();
        return password.length() >= 6;
    }

    // password match check
    private boolean passwordsMatch() {
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        return password.equals(passwordConfirm);
    }

    // build request and start
    private void register() {
        OkHttpClient client = new OkHttpClient();

        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        RequestBody postBody = new FormEncodingBuilder()
                .add("username", username)
                .add("first_name", "dummy")
                .add("last_name", "dummy")
                .add("email", email)
                .add("password", password)
                .add("groups", "1") //FIXME add to correct group
                .add("user_permissions", "1") //FIXME add correct user permissions
                .build();

        Request request = new Request.Builder()
                .url(STORYMAKER_API_REGISTER_ENDPOINT)
                .post(postBody)
                .build();

        Log.d(TAG, "uploading to url: " + STORYMAKER_API_REGISTER_ENDPOINT);
        RegistrationTask uploadFileTask = new RegistrationTask(client, request);
        uploadFileTask.execute(username, password);
    }

    private class RegistrationTask extends AsyncTask<String, String, String> {
        private OkHttpClient client;
        private Request request;
        private Response response;

        public RegistrationTask(OkHttpClient client, Request request) {
            this.client = client;
            this.request = request;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "Begin Upload");

            try {
                response = client.newCall(request).execute();
                Log.d(TAG, "response: " + response + ", body: " + response.body().string());
                if (response.isSuccessful()) {
                    onRegisterSuccess(params[0], params[1]);
                } else {
                    onRegisterFailure(response.toString());
                }
            } catch (IOException e) {
                try {
                    Log.d(TAG, response.body().string());
                } catch (IOException e1) {
                    Log.d(TAG, "exception: " + e1.getLocalizedMessage() + ", stacktrace: " + e1.getStackTrace());
                }
            }

            return "-1";
        }
    }

    private void onRegisterSuccess(String username, String password) {
        saveCredentials(username, password);
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }

    private void onRegisterFailure(String response) {
        //FIXME
        //check for duplicate username/email
    }

    private void saveCredentials(String user, String pass) {
        ArrayList<Auth> results = (new AuthTable()).getAuthsAsList(getApplicationContext(), Auth.SITE_STORYMAKER);
        for (Auth deleteAuth : results) {
            // only a single username/password is stored at a time
            deleteAuth.delete();
        }

        Auth storymakerAuth = new Auth(getApplicationContext(),
                "StoryMaker.cc",
                Auth.SITE_STORYMAKER,
                user,
                pass,
                null,
                null,
                new Date());
        storymakerAuth.save();
    }
}