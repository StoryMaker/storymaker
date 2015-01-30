package org.storymaker.app.server;

import org.storymaker.app.AppConstants;
import org.storymaker.app.StoryMakerApp;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import net.bican.wordpress.Comment;
import net.bican.wordpress.MediaObject;
import net.bican.wordpress.Page;
import net.bican.wordpress.Wordpress;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;

public class ServerManager {
    private static final String TAG = "ServerManager";
	private Wordpress mWordpress;	
	private String mServerUrl;
	private Context mContext;
	
	private final static String PATH_XMLRPC = "/xmlrpc.php";
	private final static String PATH_REGISTER = "/wp-login.php?action=register";
	private final static String PATH_LOGIN = "/wp-admin";
	public final static String PATH_REGISTERED = "/wp-login.php?checkemail=registered";
	
	public final static String CUSTOM_FIELD_MEDIUM = "medium"; //Text, Audio, Photo, Video
	
	public final static String CUSTOM_FIELD_MEDIUM_TEXT = "Text";
	public final static String CUSTOM_FIELD_MEDIUM_AUDIO = "Audio";
	public final static String CUSTOM_FIELD_MEDIUM_PHOTO = "Photo";
	public final static String CUSTOM_FIELD_MEDIUM_VIDEO = "Video";
	
	public final static String CUSTOM_FIELD_MEDIA_HOST = "media_value"; //youtube or soundcloud
	public final static String CUSTOM_FIELD_MEDIA_HOST_YOUTUBE = "youtube"; //youtube or soundcloud
	public final static String CUSTOM_FIELD_MEDIA_HOST_SOUNDCLOUD = "soundcloud"; //youtube or soundcloud

	private SharedPreferences mSettings;
	
	public ServerManager (Context context)
	{
		this(context, StoryMakerApp.initServerUrls(context));
		
	}
	
	public ServerManager (Context context, String serverUrl)
	{
		mContext = context;
		mServerUrl = serverUrl;
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
	       
	}
	
	public void setContext (Context context)
	{
		mContext = context;
	}
	
	//if the user hasn't logged in, show the login screen
    public boolean hasCreds ()
    {
        Auth checkAuth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
        if (checkAuth == null) // added null check to prevent uncaught null pointer exception
            return false;
        else
            return checkAuth.credentialsAreValid();
    }
    
    /**
     * Return the StoryMaker user's username
     * 
     * @return
     */
    public String getUserName() {
        Auth checkAuth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
        if (checkAuth != null) {
            return checkAuth.getUserName();
        } else {
            throw new IllegalStateException("No Storymaker Authentication records found!");
        }
    }
    
    /**
     * Log a user out of their StoryMaker account
     */
    public void logOut() {
        Auth checkAuth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
        Date expiryDate = new Date();
        expiryDate.setTime(0);
        checkAuth.setExpires(expiryDate);
        checkAuth.save();
    }
    
    private void connect () throws MalformedURLException, XmlRpcFault
    {
        if (mWordpress == null)
        {
            Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
            if (auth != null) {
                String user = auth.getUserName();
                String pass = auth.getCredentials();
                if (user != null && user.length() > 0) {
                    connect(user, pass);
                    return;
                }
            }
            Log.e(TAG, "connect() bailing out, user credentials are null or blank");
        }
    }
	
	public void connect (String username, String password) throws MalformedURLException, XmlRpcFault
	{
		XmlRpcClient.setContext(mContext);
		

	    boolean useTor = mSettings.getBoolean("pusetor", false);
	    
		if (useTor)
		{
			XmlRpcClient.setProxy(true, "SOCKS", AppConstants.TOR_PROXY_HOST, AppConstants.TOR_PROXY_PORT);
		}
		else
		{
			XmlRpcClient.setProxy(false, null, null, -1);

		}
		
		Log.d(TAG, "Logging into Wordpress: " + username + '@' + mServerUrl + PATH_XMLRPC);
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

	public String post (String title, String body, String[] cats, String medium, String mediaService, String mediaGuid) throws XmlRpcFault, MalformedURLException
	{
		return post (title, body, cats, medium, mediaService, mediaGuid, null, null);
	}
	
	public String addMedia (String mimeType, File file) throws XmlRpcFault, MalformedURLException
	{
		connect();
		
		MediaObject mObj = null;
		
		if (file != null)
			mObj = mWordpress.newMediaObject(mimeType, file, false);
		
		return mObj.getUrl();
	}
	
	public String post (String title, String body, String[] catstrings, String medium, String mediaService, String mediaGuid, String mimeType, File file) throws XmlRpcFault, MalformedURLException
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
		
		if (catstrings != null && catstrings.length > 0)
		{
			XmlRpcArray cats = new XmlRpcArray();
			for (String catstr : catstrings)
				cats.add(catstr);
			page.setCategories(cats);
		}
		
		XmlRpcArray custom_fields = new XmlRpcArray();

		
		if (medium != null)
		{

			XmlRpcStruct struct = new XmlRpcStruct();
			struct.put("key","medium");
			struct.put("value",medium);			
			custom_fields.add(struct);

		}

		if (mediaService != null)
		{
			
			
			XmlRpcStruct struct = new XmlRpcStruct();
			struct.put("key","media_value");
			struct.put("value",mediaService);
			custom_fields.add(struct);

		}
		
		if (mediaGuid != null)
		{
			
			XmlRpcStruct struct = new XmlRpcStruct();
			struct.put("key","media_guid");
			struct.put("value",mediaGuid);
			custom_fields.add(struct);

		}
		
		

		page.setCustom_fields(custom_fields);
		
		boolean publish = true; //let's push it out!
		String postId = mWordpress.newPost(page, publish);
		
		
		return postId;
	}
	
	public void createAccount (Activity activity)
	{
		//open web view here to reg form
		Intent intent = new Intent(mContext,RegisterActivity.class);
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
