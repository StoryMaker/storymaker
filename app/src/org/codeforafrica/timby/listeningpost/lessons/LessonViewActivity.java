package org.codeforafrica.timby.listeningpost.lessons;

import java.io.IOException;

import org.codeforafrica.timby.listeningpost.AppConstants;
import org.codeforafrica.timby.listeningpost.BaseActivity;
import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.StoryMakerApp;
import org.codeforafrica.timby.listeningpost.media.MediaHelper;
import org.codeforafrica.timby.listeningpost.model.Lesson;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

@SuppressLint("NewApi")
public class LessonViewActivity extends BaseActivity {

    WebView mWebView;
    MediaHelper mMediaHelper;
    String mUrl;
    String mLessonPath;
    String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            mTitle = intent.getStringExtra("title");
            if (mTitle != null) {
                setTitle(mTitle);
            }

            mUrl = intent.getStringExtra("url");

            if (!mUrl.startsWith("file://")) {
                mUrl = "file://" + mUrl;
            }

            mLessonPath = intent.getStringExtra("lessonPath");

            mWebView = (WebView) findViewById(R.id.web_engine);

            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setPluginState(PluginState.ON);

            mWebView.getSettings().setAllowFileAccess(true);

            if (Build.VERSION.SDK_INT >= 16) {
                new WebViewSetupJB(mWebView);
            }

            mWebView.getSettings().setSupportZoom(true);

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.w(AppConstants.TAG, "web console: " + consoleMessage.lineNumber() + ": "
                            + consoleMessage.message());
                    return super.onConsoleMessage(consoleMessage);
                }
            });

            mWebView.setWebViewClient(new WebViewClient() {
                
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    boolean isMedia = false;
                    boolean handled = false;

                    String mimeType = mMediaHelper.getMimeType(url);

                    if (mimeType != null && (!mimeType.startsWith("text"))) {
                        isMedia = true;
                    }

                    if (isMedia) {
                        // launch video player
                        mMediaHelper.playMedia(Uri.parse(url), mimeType);
                        handled = true;
                    } else {
                        // now check for *special* URLs for lesson complete

                        if (url.startsWith("stmk://lesson/complete/")) {
                            lessonCompleted();
                            handled = true;
                        }
                    }

                    return handled;// super.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode,
                        String description, String failingUrl) {

                    Log.e(AppConstants.TAG, "web error occured for " + failingUrl + "; "
                            + errorCode + "=" + description);
                    // super.onReceivedError(view, errorCode, description,
                    // failingUrl);
                }

            });

            mWebView.loadUrl(mUrl);
        }

        mMediaHelper = new MediaHelper(this, null);
    }

    private void lessonInProgress() {
        try {
            StoryMakerApp.getLessonManager().updateLessonStatus(mLessonPath, Lesson.STATUS_IN_PROGRESS);
        } catch (IOException e) {
            Log.e(AppConstants.TAG, "error updating app status", e);
        }
    }

    private void lessonCompleted() {
        try {
            StoryMakerApp.getLessonManager()
                    .updateLessonStatus(mLessonPath, Lesson.STATUS_COMPLETE);
            // TODO do something here to mark lesson as completed; need to
            // update database
            Toast.makeText(this, R.string.lessons_congratulations_you_have_completed_the_lesson_,
                    Toast.LENGTH_LONG).show();

            setResult(RESULT_OK);
            finish();

        } catch (IOException e) {
            Log.e(AppConstants.TAG, "error updating app status", e);
        }
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
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
