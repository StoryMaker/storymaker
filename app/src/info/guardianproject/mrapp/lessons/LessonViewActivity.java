package info.guardianproject.mrapp.lessons;

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

import com.WazaBe.HoloEverywhere.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class LessonViewActivity extends SherlockActivity {

	WebView mWebView;
	MediaHelper mMediaHelper;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        
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
        
        	mWebView.setWebViewClient(new WebViewClient ()
        	{

        		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					boolean isMedia = false;
					boolean handled = false;
					
					String mimeType = mMediaHelper.getMimeType(url);
					
					if (mimeType != null && (!mimeType.startsWith("text")))
							isMedia = true;
					
					if (isMedia)
					{
						//launch video player
						mMediaHelper.playMedia(Uri.parse(url), mimeType);
						handled = true;
					}
					else
					{
						//now check for *special* URLs for lesson complete
						
						if (url.startsWith("stmk://lesson/complete/"))
						{
							int sIdx = url.lastIndexOf('/');
							String lessonId = url.substring(sIdx+1);
							lessonCompleted(lessonId);
							handled = true;
						}
					}
					
					return handled;// super.shouldOverrideUrlLoading(view, url);
				}
        		
        	});
        	
        	mWebView.loadUrl(url); 
        
        }
        
        mMediaHelper = new MediaHelper(this, null);
        
    }
    
    private void lessonCompleted(String lessonId)
    {
    	//TODO do something here to mark lesson as completed; need to update database
    	Toast.makeText(this, "Congratulations. You have completed lesson " + lessonId, Toast.LENGTH_LONG).show();
    	finish();
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
