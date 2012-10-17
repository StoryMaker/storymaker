package info.guardianproject.mrapp.lessons;

import java.io.File;
import java.net.URL;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.media.MediaHelper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_web_view);
        
        //getSherlock().getActionBar().hide();
        
        Intent intent = getIntent();
        if (intent != null)
        {
        	String title = intent.getStringExtra("title");
        	if (title != null)
        		setTitle(title);
        
        	String url = intent.getStringExtra("url");
        	mWebView = (WebView) findViewById(R.id.web_engine);  
        	
        	mWebView.getSettings().setPluginsEnabled(true);
        	mWebView.getSettings().setPluginState(PluginState.ON);

        	
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
	
	

}
