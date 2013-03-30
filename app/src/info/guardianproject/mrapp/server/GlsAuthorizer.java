/* Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.guardianproject.mrapp.server;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class GlsAuthorizer implements Authorizer {

  public static final String ACCOUNT_TYPE = "com.google";

  public static final String PICASA_AUTH_TOKEN_TYPE = "lh2";
  private static final String[] PICASA_FEATURES = new String[] { "service_lh2" };

  public static final String YOUTUBE_AUTH_TOKEN_TYPE = "youtube";
  private static final String[] YOUTUBE_FEATURES = new String[] { "service_youtube" };


  private AccountManager accountManager;

  private final String authTokenType;

  private final String[] features;
  private Handler handler;
  
  private static class Config {
	  private static final String APP_NAME = "GlsAuthorizer";
  }
  
  public GlsAuthorizer(Context context, String authTokenType, String[] features) {
    accountManager = AccountManager.get(context);

    this.authTokenType = authTokenType;
    this.features = features;
  }
  
  public void setHandler (Handler handler)
  {
	  this.handler = handler;
  }

  public static class GlsAuthorizerFactory implements AuthorizerFactory {
    public Authorizer getAuthorizer(Context context, String authTokenType) {
      if (PICASA_AUTH_TOKEN_TYPE.equals(authTokenType)) {
        return new GlsAuthorizer(context, PICASA_AUTH_TOKEN_TYPE, PICASA_FEATURES);
      } else if (YOUTUBE_AUTH_TOKEN_TYPE.equals(authTokenType)) {
        return new GlsAuthorizer(context, YOUTUBE_AUTH_TOKEN_TYPE, YOUTUBE_FEATURES);
      } else {
        return new GlsAuthorizer(context, authTokenType, new String[] {});
      }
    }
  }

  public String getAuthToken(String accountName) {
    Log.d(Config.APP_NAME, "Getting " + authTokenType + " authToken for " + accountName);
    Account account = getAccount(accountName);
    if (accountName == null)
    	accountName = account.name;
    
    if (account != null) {
      try {
        return accountManager.blockingGetAuthToken(account, authTokenType, true);
      } catch (OperationCanceledException e) {
        Log.w(Config.APP_NAME, e);
      } catch (IOException e) {
        Log.w(Config.APP_NAME, e);
      } catch (AuthenticatorException e) {
        Log.w(Config.APP_NAME, e);
      }
    }
    return null;
  }

  public String getFreshAuthToken(String accountName, String authToken) {
    Log.d(Config.APP_NAME, "Refreshing authToken for " + accountName);
    accountManager.invalidateAuthToken(ACCOUNT_TYPE, authToken);
    return getAuthToken(accountName);
  }

  public void fetchAuthToken(final String accountName, Activity activity,
      final AuthorizationListener<String> listener) {
    final Account account = getAccount(accountName);
    Bundle bundle = new Bundle();
    
    if (account != null) {
    	
      accountManager.getAuthToken(
          account,
          authTokenType,
          bundle, // loginOptions,
          activity,
          new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
              try {
                Log.d(Config.APP_NAME, "Got authToken for " + account.name);
                Bundle extras = future.getResult();
                String authToken = extras.getString(AccountManager.KEY_AUTHTOKEN);
                listener.onSuccess(authToken);
              } catch (OperationCanceledException e) {
                listener.onCanceled();
              } catch (IOException e) {
                listener.onError(e);
              } catch (AuthenticatorException e) {
                listener.onError(e);
              }
            }
          },
          handler); // handler
    } else {
      listener.onError(new Exception("Could not find account " + accountName));
    }
  }

  public void fetchAccounts(final AuthorizationListener<String[]> listener) {
    accountManager.getAccountsByTypeAndFeatures(
        ACCOUNT_TYPE,
        features,
        new AccountManagerCallback<Account[]>() {
          public void run(AccountManagerFuture<Account[]> future) {
            try {
              Account[] accounts = future.getResult();
              String[] accountNames = new String[accounts.length];
              for (int i = 0; i < accounts.length; i++) {
                accountNames[i] = accounts[i].name;
              }
              Log.d(Config.APP_NAME, "Got " + accounts.length + " accounts");
              listener.onSuccess(accountNames);
            } catch (OperationCanceledException e) {
              listener.onCanceled();
            } catch (IOException e) {
              listener.onError(e);
            } catch (AuthenticatorException e) {
              listener.onError(e);
            }
          }
        },
        null); // handler
  }

  public Account getAccount(String name) {
    Account[] accounts = accountManager.getAccounts();
    
    if (name == null)
    	return accounts[0];
    
    for (Account account : accounts) {
      if (account.name.equals(name)) {
        return account;
      }
    }
    return null;
  }

  public void addAccount(Activity activity,
      final AuthorizationListener<String> listener) {
    accountManager.addAccount(
        ACCOUNT_TYPE,
        authTokenType,
        features,
        null, // addAccountOptions,
        activity,
        new AccountManagerCallback<Bundle>() {
          public void run(AccountManagerFuture<Bundle> future) {
            try {
              Bundle extras = future.getResult();
              String accountName = extras.getString(AccountManager.KEY_ACCOUNT_NAME);
              Log.d(Config.APP_NAME, "Added account " + accountName);
              listener.onSuccess(accountName);
            } catch (OperationCanceledException e) {
              listener.onCanceled();
            } catch (IOException e) {
              listener.onError(e);
            } catch (AuthenticatorException e) {
              listener.onError(e);
            }
          }
        },
        null); // handler
  }

}