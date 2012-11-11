package info.guardianproject.mrapp.lessons;

import java.io.File;
import java.net.URL;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.media.MediaHelper;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class LessonViewActivity extends SherlockActivity {

	WebView mWebView;
	MediaHelper mMediaHelper;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.activity_web_view);
        
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
        	mWebView.getSettings().setPluginsEnabled(true);
        	mWebView.getSettings().setPluginState(PluginState.ON);
        	mWebView.getSettings().setAllowFileAccess(true);
        	mWebView.getSettings().setSupportZoom(false);
        	mWebView.getSettings().setEnableSmoothTransition(true);
        	
    		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    		{
    			mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
    		}
    		
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
					
					return isMedia;// super.shouldOverrideUrlLoading(view, url);
				}
        		
        	});
        	
        	mWebView.loadUrl(url); 
        
        }
        
        mMediaHelper = new MediaHelper(this, null);
        
    }
    
    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // getSupportMenuInflater().inflate(R.menu.activity_lesson_list, menu);
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
	
	

}
