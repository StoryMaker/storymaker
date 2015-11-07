package org.storymaker.app.server;

import timber.log.Timber;

import org.storymaker.app.R;
import org.storymaker.app.media.MediaHelper;
import io.scal.secureshareui.lib.Util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	protected WebView mWebView;
	MediaHelper mMediaHelper;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        if (intent != null)
        {
        	String title = intent.getStringExtra("title");
        	if (title != null)
        		setTitle(title);
        
        	String url = intent.getStringExtra("url");
        	mWebView = (WebView) findViewById(R.id.web_engine);  
        	
        	mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        	mWebView.getSettings().setJavaScriptEnabled(true);
        	mWebView.getSettings().setPluginState(PluginState.ON);
        	mWebView.getSettings().setAllowFileAccess(true);
        	mWebView.getSettings().setSupportZoom(false);
        
        	mWebView.setWebViewClient(new WebViewClient ()
        	{

        		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					boolean isMedia = false;
					
					String mimeType = mMediaHelper.getMimeType(url);
					
					if (mimeType != null && (!mimeType.startsWith("text")))
							isMedia = true;
					
					if (isMedia)
					{
						//launch video player
						mMediaHelper.playMedia(Uri.parse(url), mimeType);
					}
					
					return isMedia;
				}
        		
        	});
        	
        	mWebView.loadUrl(url); 
        
        }
        
        mMediaHelper = new MediaHelper(this, null);
        
    }
    
    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case android.R.id.home:
       
            finish();
            return true;

       
        }
        
        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (mWebView.canGoBack())
			{
				mWebView.goBack();
				return true;
			}
			
				
		}
		
		return super.onKeyDown(keyCode, event);
	}



	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void finish() {		
		super.finish();		
		Util.clearWebviewAndCookies(mWebView, this);
	}
}
