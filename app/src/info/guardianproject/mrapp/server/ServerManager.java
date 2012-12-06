package info.guardianproject.mrapp.server;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import redstone.xmlrpc.XmlRpcFault;

import net.bican.wordpress.Comment;
import net.bican.wordpress.MediaObject;
import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ServerManager {

	private Wordpress mWordpress;	
	private String mServerUrl;
	private Context mContext;
	
	//TODO switch this to HTTPS!
	public final static String DEFAULT_STORYMAKER_SERVER = "https://mrapp.alive.in/";
	
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
	
	public void setContext (Context context)
	{
		mContext = context;
	}
	
	//if the user hasn't registered with the user, show the login screen
    public boolean hasCreds ()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
       
        String user = settings.getString("user","");
        
        return (user != null && user.length() > 0);
        
    }
    
    private void connect () throws MalformedURLException, XmlRpcFault
    {
    	if (mWordpress == null)
    	{
    	   SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
           
           String user = settings.getString("user","");
           String pass = settings.getString("pass", "");
         
           if (user != null && user.length() > 0)
        	   connect (user, pass);
    	}
    }
	
	public void connect (String username, String password) throws MalformedURLException, XmlRpcFault
	{
		mWordpress = new Wordpress(username, password, mServerUrl + PATH_XMLRPC);	
		mWordpress.getRecentPosts(1); //need to do a test to force authentication
	}
	
	public String getPostUrl (String postId) throws XmlRpcFault, MalformedURLException
	{
		connect();
		Page post = mWordpress.getPost(Integer.parseInt(postId));
		return post.getPermaLink();
		
	}
	
	public Page getPost (String postId) throws XmlRpcFault, MalformedURLException
	{
		connect();
		Page post = mWordpress.getPost(Integer.parseInt(postId));
		return post;
		
	}
	
	public List<Page> getRecentPosts (int num) throws XmlRpcFault, MalformedURLException
	{
		connect();
		List<Page> rPosts = mWordpress.getRecentPosts(num);
		return rPosts;
	}
	
	public List<Comment> getComments (Page page) throws XmlRpcFault, MalformedURLException
	{
		connect();
		return mWordpress.getComments(null, page.getPostid(), null, null);
	}

	public String post (String title, String body) throws XmlRpcFault, MalformedURLException
	{
		return post (title, body, null, null);
	}
	
	public String post (String title, String body, String mimeType, File file) throws XmlRpcFault, MalformedURLException
	{
		connect();
		
		MediaObject mObj = null;
		
		if (file != null)
			mObj = mWordpress.newMediaObject(mimeType, file, false);
		
		Page page = new Page ();
		page.setTitle(title);
		
		StringBuffer sbBody = new StringBuffer();
		sbBody.append(body);
		
		if (mObj != null)
		{
			sbBody.append("\n\n");
			sbBody.append(mObj.getUrl());
		}
		
		page.setDescription(sbBody.toString());
		
		boolean publish = false; //submit as draft only for review
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
