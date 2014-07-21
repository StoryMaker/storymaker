package org.codeforafrica.listeningpost.server;

import java.io.IOException;

import org.codeforafrica.listeningpost.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * After the request is authorized by the user, the callback URL will be intercepted here.
 * 
 */
public class OAuthAccessTokenActivity extends Activity implements Runnable {

	final String TAG = getClass().getName();
	
	private SharedPreferences prefs;

	private String mCode; //returned code from web
	
	private boolean isDebuggable = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        
        init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void init ()
	{
		WebView webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);  
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);
        String authorizationUrl = null;
        
        isDebuggable =  ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    	//authorizationUrl = new GoogleAuthorizationRequestUrl(getString(R.string.client_id_debug), OAuth2ClientCredentials.REDIRECT_URI, OAuth2ClientCredentials.SCOPE).build();

        //if (isDebuggable)
        	authorizationUrl = new GoogleAuthorizationRequestUrl(getString(R.string.client_id_debug), OAuth2ClientCredentials.REDIRECT_URI, OAuth2ClientCredentials.SCOPE).build();
        //else
        // 	authorizationUrl = new GoogleAuthorizationRequestUrl(getString(R.string.client_id_release), OAuth2ClientCredentials.REDIRECT_URI, OAuth2ClientCredentials.SCOPE).build();
        
        /* WebViewClient must be set BEFORE calling loadUrl! */  
        webview.setWebViewClient(new WebViewClient() {  

        	@Override  
            public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
        	//	System.out.println("onPageStarted : " + url);
            }
        	
        	@Override  
            public void onPageFinished(WebView view, String url)  {  
            	
            	if (url.startsWith(OAuth2ClientCredentials.REDIRECT_URI)) {
            		
        			if (url.indexOf("code=")!=-1) {
        			
        				if (mCode == null)
        				{
        					mCode = extractCodeFromUrl(url);
        					view.setVisibility(View.INVISIBLE);
        				
        					new Thread(OAuthAccessTokenActivity.this).start();
        				}
        				
        			} else if (url.indexOf("error=")!=-1) {
        				view.setVisibility(View.INVISIBLE);
        				
        				
			  		      setResult(RESULT_CANCELED);

        			}
            			

            	}
  		      
            }
			private String extractCodeFromUrl(String url) {
				return url.substring(OAuth2ClientCredentials.REDIRECT_URI.length()+7,url.length());
			}  
        });  
        
        webview.loadUrl(authorizationUrl);		
	}
	
	public void run ()
	{

		
		try
		{
			
		String clientId = getString(R.string.client_id_debug);
		String clientSecret = getString(R.string.client_id_debug_secret);
		
		/*
		if (!isDebuggable)
		{
			clientId = getString(R.string.client_id_release);
			clientSecret = "";
		}*/
		
	      mAuthResp = new GoogleAuthorizationCodeGrant(new NetHttpTransport(),
					      new JacksonFactory(),
					      clientId,
					      clientSecret,
					      mCode,
					      OAuth2ClientCredentials.REDIRECT_URI).execute();


		}
		catch (IOException ioe)
		{
			Log.d("oauth","error on auth",ioe);

			
		}
		

	      finish();
		
	}
	
	private AccessTokenResponse mAuthResp;
	
	public void finish ()
	{
		if (mAuthResp != null)
		{
			Log.d("oauth","got access token: " + mAuthResp.accessToken);
		      

				Bundle bundle = new Bundle();
				    
			    bundle.putString("token", mAuthResp.accessToken);
			    bundle.putLong("expires", mAuthResp.expiresIn);
			    bundle.putString("refresh", mAuthResp.refreshToken);
			    bundle.putString("scope", mAuthResp.scope);
			    
			    Intent intent = new Intent();
			    intent.putExtras(bundle);
	      
			    
			    if (getParent() == null) {
			        setResult(Activity.RESULT_OK, intent);
			    } else {
			        getParent().setResult(Activity.RESULT_OK, intent);
			    }
		}
			 
		super.finish();
			
		
		
	};

}
