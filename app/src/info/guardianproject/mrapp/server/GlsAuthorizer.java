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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

@SuppressLint("NewApi")
public class GlsAuthorizer implements Authorizer {

  public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

//  public static final String PICASA_AUTH_TOKEN_TYPE = "lh2";
 // private static final String[] PICASA_FEATURES = new String[] { "service_lh2" };

  public static final String YOUTUBE_AUTH_TOKEN_TYPE = "youtube";
  public static final String[] YOUTUBE_FEATURES = new String[] { "service_youtube" };

  private AccountManager accountManager;

//  private final String[] features;
  private Handler handler;
  
  private AuthToken mLastAuthToken = new AuthToken ();
  private String[] mAccountFeatures = YOUTUBE_FEATURES;
	  
  private Context mContext = null;
  private Activity mActivity = null;
  
  private int mAuthMethod = 0;
  
  private static class Config {
	  private static final String APP_NAME = "GlsAuthorizer";
  }
  
  public GlsAuthorizer(Context context) {
    accountManager = AccountManager.get(context);
    mContext = context;
  }

  public void setAccountFeatures (String[] features)
  {
	  mAccountFeatures = features;
  }
  
  public void setAuthMethod (int authMethod)
  {
	  mAuthMethod = authMethod;
  }
  
  public void setParentActivity (Activity activity)
  {
	  mActivity = activity;
  }
  
  public void setAccountType (String aType)
  {
	  if (mLastAuthToken == null)
		  mLastAuthToken = new AuthToken ();
	  
	  mLastAuthToken.mAccountType = aType;
  }
  
  public void setAuthTokenType (String tType)
  {
	  if (mLastAuthToken == null)
		  mLastAuthToken = new AuthToken ();
	  
	  mLastAuthToken.mTokenType = tType;
  }
  
  public void setHandler (Handler handler)
  {
	  this.handler = handler;
  }

  public static class GlsAuthorizerFactory implements AuthorizerFactory {
    public Authorizer getAuthorizer(Context context, String authTokenType) {
    	GlsAuthorizer auth = new GlsAuthorizer(context);
    	auth.setAuthTokenType(authTokenType);
    	return auth;
    }
  }

  public String getAuthToken(String accountName) {
	  
    Log.d(Config.APP_NAME, "Getting " + mLastAuthToken.mTokenType + " authToken for " + accountName);
    Account account = getAccount(accountName);
    if (accountName == null)
    	accountName = account.name;
    
    if (account != null) {
      try {
        
    	String authToken = accountManager.blockingGetAuthToken(account, mLastAuthToken.mTokenType, true);
        mLastAuthToken = new AuthToken(authToken, mLastAuthToken.mTokenType);
        return authToken;
        
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


  @Override
  public String getFreshAuthToken(String accountName, String token) {
	  
    Log.d(Config.APP_NAME, "Refreshing authToken for " + accountName + " token=" + token);
    accountManager.invalidateAuthToken(mLastAuthToken.mAccountType, token);
    
    return getAuthToken(accountName);
  }

  public void fetchAuthToken(final String accountName, Activity activity,
      final AuthorizationListener<String> listener) {
	  
    final Account account = getAccount(accountName);
    
    if (account != null) {
    	
    	AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
              
            	try {
                    
	            	Bundle extras = future.getResult();
	            	
	            	if (extras.containsKey(AccountManager.KEY_AUTHTOKEN))
	            	{
	            		mLastAuthToken.mToken = extras.getString(AccountManager.KEY_AUTHTOKEN);
	            		mLastAuthToken.mAccountType = extras.getString(AccountManager.KEY_ACCOUNT_TYPE);

	            		Log.d(Config.APP_NAME, "Got authToken: " + account.name + " type=" + mLastAuthToken.mAccountType + " token=" + mLastAuthToken.mToken);

	                	listener.onSuccess(mLastAuthToken.mToken);
	            	}
	            	else if (extras.containsKey(AccountManager.KEY_INTENT))
	            	{
	            		Intent intent = (Intent)extras.get(AccountManager.KEY_INTENT);
	                    if(intent != null){              
	                    	
	                    	if (mActivity != null)
	                    	{
	                    		mActivity.startActivity(intent);
	                    	}
	                    	else
	                    	{
	                    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                    		mContext.startActivity(intent);
	                    	}
	                    	
	                    	//In the activity that runs this I check for a flag in the onResume, 
	                        //which tells me that we returned from this activity

	                    }

	            	}
	                else
	                {	            		
	                	String err =  extras.getString(AccountManager.KEY_AUTH_FAILED_MESSAGE);

	                	Log.d(Config.APP_NAME, "Auth err message: " + err);
	                	listener.onError(new Exception("Unable to authenticate account"));
	                }
              } catch (OperationCanceledException e) {
                listener.onCanceled();
              } catch (IOException e) {
                listener.onError(e);
              } catch (AuthenticatorException e) {
                listener.onError(e);
              }
            }
          };
          
          Bundle options = new Bundle ();
          if (mAuthMethod == 0)
          {
        	  //this is the most common one that works
          	accountManager.getAuthToken(account, mLastAuthToken.mTokenType, options, activity, callback, handler);

          }
          else if (mAuthMethod == 1)
          {        	  
        	  //this one also works, using the older method
        	  accountManager.getAuthToken(account, mLastAuthToken.mTokenType, false, callback, handler);

          }
          else if (mAuthMethod == 2)
          {
        	  //this is the API 14 approach
        	  accountManager.getAuthToken(account, mLastAuthToken.mTokenType, options, false, callback, handler);

          }
          else if (mAuthMethod == 3)
          {        	  
        	  //this is the older method with a null handler
        	  accountManager.getAuthToken(account, mLastAuthToken.mTokenType, false, callback, null);

          }
          else if (mAuthMethod == 4)
          {        	  
        	  //this shows a notification alert
        	  accountManager.getAuthToken(account, mLastAuthToken.mTokenType, true, callback, null);

          }
            	
          
    } else {
      listener.onError(new Exception("Could not find account " + accountName));
    }
  }

  public void fetchAccounts(final AuthorizationListener<String[]> listener) {
	  
    accountManager.getAccountsByTypeAndFeatures(
        mLastAuthToken.mAccountType,
        mAccountFeatures,
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

  
  public class AuthToken 
  {
	  public AuthToken (String token, String tokenType)
	  {
		  mToken = token;
		  mTokenType = tokenType;
	  }
	  
	  public AuthToken ()
	  {
		  
	  }
	  
	  public String mToken;
	  public String mTokenType;
	  public String mAccountType;
  }

@Override
public void addAccount(Activity activity, AuthorizationListener<String> listener) {
	
	//we don't care about this
	
}

}