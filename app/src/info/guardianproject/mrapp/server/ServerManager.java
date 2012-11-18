package info.guardianproject.mrapp.server;

import info.guardianproject.mrapp.MediaAppConstants;

import java.net.MalformedURLException;

import redstone.xmlrpc.XmlRpcFault;

import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServerManager {

	private Wordpress mWordpress;	
	private String mServerUrl;
	private Context mContext;
	
	//public final static String DEFAULT_STORYMAKER_SERVER = "http://storymaker.smallworldnews.tv";
	public final static String DEFAULT_STORYMAKER_SERVER = "https://guardianproject.info";
	
	private final static String PATH_XMLRPC = "/xmlrpc.php";
	private final static String PATH_REGISTER = "/wp-login.php?action=register";
	private final static String PATH_LOGIN = "/wp-admin";
	
	public ServerManager (Context context)
	{
		this(context, DEFAULT_STORYMAKER_SERVER);
	}
	
	public ServerManager (Context context, String serverUrl)
	{
		mContext = context;
		mServerUrl = serverUrl;
	}
	
	public void connect (String username, String password) throws MalformedURLException, XmlRpcFault
	{
		mWordpress = new Wordpress(username, password, mServerUrl + PATH_XMLRPC);	
		mWordpress.sayHello();
		
		for (String method : mWordpress.supportedMethods())
		{
			Log.d(MediaAppConstants.TAG,"method supported: " + method);
		}
		
	}
	
	public void post (String title, String body)
	{
		
		Page page = new Page ();
		page.setTitle(title);
		page.setDescription(body);
		boolean publish = true;
		//mWordpress.newPost(page, publish);
		
	}
	
	public void createAccount (Activity activity)
	{
		//open web view here to reg form
		Intent intent = new Intent(mContext,WebViewActivity.class);
		intent.putExtra("title", "New Account");
		intent.putExtra("url", mServerUrl + PATH_REGISTER);
		
		activity.startActivity(intent);
	}
	
	public void showPost (String title, String url)
	{		
		Intent intent = new Intent(mContext,WebViewActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("url", url);
		
		mContext.startActivity(intent);
	}
	
}
