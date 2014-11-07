package org.storymaker.app.lessons;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebView;

public class WebViewSetupJB {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public WebViewSetupJB (WebView webView)
	{

		webView.getSettings().setAllowFileAccessFromFileURLs(true);
		webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
	}
}
