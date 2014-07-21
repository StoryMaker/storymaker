package org.codeforafrica.listeningpost.server.soundcloud;

import java.io.IOException;

import org.codeforafrica.listeningpost.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Token;


/**
 * This is the main activity which gets launched first.
 *
 * It first checks if SoundCloud is installed on the device - if it is it will try
 * to obtain an OAuth2 account token. If this fails (most likely because the user has not logged in using
 * the app) it will prompt the user to log in, launching the SoundCloud start screen.
 *
 * Once a token is obtained the {@link UploadFile} activity takes over.
 *
 * If the SoundCloud app is not installed a dialog box opens, asking the user if she wants to install
 * the application.
 */
public class TokenSharing extends Activity {
    public static final String TAG = "soundcloud-token-sharing-example";
    public static final String SC_ACCOUNT_TYPE = "com.soundcloud.android.account";
    public static final String ACCESS_TOKEN = "access_token";

    private static final Uri MARKET_URI = Uri.parse("market://details?id=com.soundcloud.android");
    private static final int DIALOG_NOT_INSTALLED = 0;

    private AccountManager mAccountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);

        final Account account = getAccount();
        if (account != null) {
            new Thread(mGetToken).start();
        } else {
            addAccount();
        }
    }

    private final Runnable mGetToken = new Runnable() {
        @Override
        public void run() {
            final Token token = getToken(getAccount());
            if (token != null) {
                success(token);
            } else {
                notifyUser(R.string.could_not_get_token);
            }
        }
    };

    // request a new SoundCloud account to be added
    private void addAccount() {
        notifyUser(R.string.no_active_sc_account);
        mAccountManager.addAccount(SC_ACCOUNT_TYPE, ACCESS_TOKEN, null, null, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle result = future.getResult();
                            String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                            Log.d(TAG, "created account for " + name);

                            // should have an account by now
                            Account account = getAccount();
                            if (account != null) {
                                new Thread(mGetToken).start();
                            } else {
                                notifyUser(R.string.could_not_create_account);
                            }
                        } catch (OperationCanceledException e) {
                            notifyUser(R.string.operation_canceled);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (AuthenticatorException e) {
                            // SoundCloud app is not installed
                            appNotInstalled();
                        }
                    }
                }, null);
    }


    private void appNotInstalled() {
        showDialog(DIALOG_NOT_INSTALLED);
    }


    private void success(Token token) {
        // create the API wrapper with the token
        Api.wrapper = new ApiWrapper(null, null, null, token, Env.LIVE);

        // and launch our main activity
     //   startActivity(new Intent(this, UploadFile.class));
    }

    private Account getAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(SC_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }

    private Token getToken(Account account) {
        try {
            String access = mAccountManager.blockingGetAuthToken(account, ACCESS_TOKEN, false);
            return new Token(access, null, Token.SCOPE_NON_EXPIRING);
        } catch (OperationCanceledException e) {
            notifyUser(R.string.operation_canceled);
            return null;
        } catch (IOException e) {
            Log.w(TAG, "error", e);
            return null;
        } catch (AuthenticatorException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    private void notifyUser(final int id) {
        notifyUser(getResources().getString(id));
    }

    private void notifyUser(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TokenSharing.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle data) {
        if (DIALOG_NOT_INSTALLED == id) {
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.sc_app_not_found)
                    .setMessage(R.string.sc_app_not_found_message)
                    .setPositiveButton(android.R.string.yes, new Dialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, MARKET_URI));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();
        } else {
            return null;
        }
    }
}
