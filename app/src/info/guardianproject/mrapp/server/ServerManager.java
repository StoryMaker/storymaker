package info.guardianproject.mrapp.server;

import java.net.MalformedURLException;
import java.util.List;

import redstone.xmlrpc.XmlRpcFault;

import net.bican.wordpress.Comment;
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
	
	//TODO switch this to HTTPS!
	public final static String DEFAULT_STORYMAKER_SERVER = "http://mrapp.alive.in/";
	
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
		
	}
	
	public List<Page> getRecentPosts (int num) throws XmlRpcFault
	{
		List<Page> rPosts = mWordpress.getRecentPosts(num);
		return rPosts;
	}
	
	public List<Comment> getComments (Page page) throws XmlRpcFault
	{
		return mWordpress.getComments(null, page.getPostid(), null, null);
	}
	
	public String post (String title, String body) throws XmlRpcFault
	{
		
		Page page = new Page ();
		page.setTitle(title);
		page.setDescription(body);
		
		boolean publish = true;
		String postId = mWordpress.newPost(page, publish);
		
		return postId;
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
