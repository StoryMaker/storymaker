package info.guardianproject.mrapp.server;

import java.net.MalformedURLException;

import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;
import android.content.Context;
import android.content.Intent;

public class ServerManager {

	private Wordpress mWordpress;	
	private String mServerUrl;
	private Context mContext;
	
	public final static String DEFAULT_STORYMAKER_SERVER = "http://storymaker.smallworldnews.tv";
	
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
	
	public void connect (String username, String password) throws MalformedURLException
	{
		mWordpress = new Wordpress(username, password, mServerUrl + PATH_XMLRPC);		
	}
	
	public void post (String title, String body)
	{
		Page page = new Page ();
		page.setTitle(title);
		page.setDescription(body);
		boolean publish = true;
		//mWordpress.newPost(page, publish);
	}
	
	public void createAccount ()
	{
		//open web view here to reg form
		Intent intent = new Intent(mContext,WebViewActivity.class);
		intent.putExtra("title", "New Account");
		intent.putExtra("url", mServerUrl + PATH_REGISTER);
		
		mContext.startActivity(intent);
	}
	
	public void showPost (Page page)
	{		
		Intent intent = new Intent(mContext,WebViewActivity.class);
		intent.putExtra("title", page.getTitle());
		intent.putExtra("url", page.getPermaLink());
		
		mContext.startActivity(intent);
	}
	
	public void getComments (Page page)
	{
		//get comment list
	}
}
