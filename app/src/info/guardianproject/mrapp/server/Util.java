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

import info.guardianproject.onionkit.trust.StrongHttpsClient;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Util {
  private static final String LOG_TAG = Util.class.getSimpleName();
  private Activity activity = null;

  public Util(Activity activity) {
    this.activity = activity;
  }

  public Activity getActivity() {
    return this.activity;
  }

  public void popup(String msg) {
    CharSequence text = msg;
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(activity, text, duration);
    toast.show();
  }

  public String getLocalClassName() {
    return activity.getLocalClassName();
  }

  public void gotoActivity(Class<? extends Activity> clazz) {
    Intent intent = new Intent(activity, clazz);
    activity.startActivity(intent);
  }

  public void d(String msg) {
    Log.d(activity.getLocalClassName(), msg);
  }

  public static String makeJsonRpcCall(String jsonRpcUrl, JSONObject payload, Context context) {
    return makeJsonRpcCall(jsonRpcUrl, payload, null, context);
  }

  public static String makeJsonRpcCall(String jsonRpcUrl, JSONObject payload, String authToken, Context context) {
    Log.d(LOG_TAG, jsonRpcUrl + " " + payload.toString());
    try {
      HttpClient client = new StrongHttpsClient(context);
      HttpPost httpPost = new HttpPost(jsonRpcUrl);

      if (authToken != null) {
        httpPost.addHeader(new BasicHeader("Authorization", "GoogleLogin auth=" + authToken));
      }

      httpPost.setEntity(new StringEntity(payload.toString(), "UTF-8"));

      HttpResponse httpResponse = client.execute(httpPost);
      if (200 == httpResponse.getStatusLine().getStatusCode()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity()
            .getContent(), "UTF-8"), 8 * 1024);

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }

        return sb.toString();
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getGoogleAuth(String type) {
    AccountManager mgr = AccountManager.get(activity);
    Account[] accts = mgr.getAccountsByType("com.google");

    if (accts.length == 0) {
      return null;
    }

    try {
      Account acct = accts[0];
      Log.d(LOG_TAG, "acct name=" + acct.name);
      AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(acct, type, null,
          activity, null, null);

      Bundle authTokenBundle = accountManagerFuture.getResult();

      if (authTokenBundle.containsKey(AccountManager.KEY_INTENT)) {
        Intent authRequestIntent = (Intent) authTokenBundle.get(AccountManager.KEY_INTENT);
        activity.startActivity(authRequestIntent);
      }

      return authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
    } catch (OperationCanceledException e) {
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean isCallable(Intent intent) {
    List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,
        PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

 
  public static CharSequence readFile(Activity activity, int id) {
    BufferedReader in = null;
    try {
      in =
          new BufferedReader(new InputStreamReader(activity.getResources()
              .openRawResource(id)));
      String line;
      StringBuilder buffer = new StringBuilder();
      while ((line = in.readLine()) != null) {
        buffer.append(line).append('\n');
      }
      // Chomp the last newline
      buffer.deleteCharAt(buffer.length() - 1);
      return buffer;
    } catch (IOException e) {
      return "";
    } finally {
      closeStream(in);
    }
  }

  /**
   * Closes the specified stream.
   *
   * @param stream The stream to close.
   */
  private static void closeStream(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }  
  
  public static String truncate(String text, int charLimit) {
    if (text.length() > charLimit) {
      BreakIterator bi = BreakIterator.getWordInstance();
      bi.setText(text);
      int cutOff = bi.following(charLimit);
      text = text.substring(0, cutOff) + " ...";
    }
    return text;
  }

  public static String getYtdJsonRpcUrl(String ytdDomain) {
    return "http://" + ytdDomain + "/jsonrpc";
  }

  public static boolean isNullOrEmpty(String str) {
    if (str == null || str.trim().length() == 0) {
      return true;
    } else {
      return false;
    }
  }
}
