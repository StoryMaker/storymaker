package org.storymaker.app.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.storymaker.app.StoryMakerApp;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.bican.wordpress.Comment;
import net.bican.wordpress.Page;

import io.scal.secureshareui.lib.CaptchaException;
import io.scal.secureshareui.lib.SMWrapper;
import scal.io.liger.IndexManager;
import scal.io.liger.model.ExpansionIndexItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class ServerManager {
    private static final String TAG = "ServerManager";
    //private Wordpress mWordpress;
    private String mServerUrl;
    private Context mContext;
    
    //private final static String PATH_XMLRPC = "/xmlrpc.php";
    private final static String PATH_REGISTER = "accounts/signup/?chrome=0";
    //private final static String PATH_LOGIN = "/wp-admin";
    //public final static String PATH_REGISTERED = "/wp-login.php?checkemail=registered";
    
    //public final static String CUSTOM_FIELD_MEDIUM = "medium"; //Text, Audio, Photo, Video
    //public final static String CUSTOM_FIELD_MEDIUM_TEXT = "Text";
    public final static String CUSTOM_FIELD_MEDIUM_AUDIO = "Audio";
    public final static String CUSTOM_FIELD_MEDIUM_PHOTO = "Photo";
    public final static String CUSTOM_FIELD_MEDIUM_VIDEO = "Video";
    
    //public final static String CUSTOM_FIELD_MEDIA_HOST = "media_value"; //youtube or soundcloud
    //public final static String CUSTOM_FIELD_MEDIA_HOST_YOUTUBE = "youtube"; //youtube or soundcloud
    //public final static String CUSTOM_FIELD_MEDIA_HOST_SOUNDCLOUD = "soundcloud"; //youtube or soundcloud

    private SharedPreferences mSettings;

    private SMWrapper smWrapper;
    
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

        // credentials should be deleted instead of expired
        checkAuth.delete();
    }
    
    private void connect () throws IOException // MalformedURLException, XmlRpcFault
    {
        //if (mWordpress == null)
        if (smWrapper == null)
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
    
    public void connect (String username, String password) throws IOException // MalformedURLException, XmlRpcFault
    {
        smWrapper = new SMWrapper(mContext);

        // wrapper now checks tor settings
        try {
            smWrapper.login(username, password);
        } catch (CaptchaException ce) {
            Log.e(TAG, "connect() bailing out, server returned captcha challenge");
            // throw exception so LoginActivity doesn't save credentials
            throw new IOException("Some TOR IPs may be flagged as suspicious, try restarting TOR");
        } catch (IOException ioe) {
            Log.e(TAG, "connect() bailing out, user credentials are not valid");
            // throw exception so LoginActivity doesn't save credentials
            throw new IOException("Login failed");
        }
    }
    
    public String getPostUrl (String postId) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        return smWrapper.getPostUrl(postId); // TODO: implement method in wrapper
    }
    
    public Page getPost (String postId) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        return (Page)smWrapper.getPost(postId); // TODO: implement method in wrapper
    }
    
    public List<Page> getRecentPosts (int num) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        return null; // smWrapper.getRecentPosts(num); // TODO: implement method in wrapper
    }
    
    public List<Comment> getComments (Page page) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        return null; // smWrapper.getComments(page); // TODO: implement method in wrapper
    }

    public String post (String title, String body, String embed, String[] cats, String medium, String mediaService, String mediaGuid) throws IOException // XmlRpcFault, MalformedURLException
    {
        return post (title, body, embed, cats, medium, mediaService, mediaGuid, null, null);
    }
    
    public String addMedia (String mimeType, File file) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        return smWrapper.addMedia(mimeType, file); // TODO: implement method in wrapper
    }
    
    public String post (String title, String body, String embed, String[] catstrings, String medium, String mediaService, String mediaGuid, String mimeType, File file) throws IOException // XmlRpcFault, MalformedURLException
    {
        connect();
        
        // need user name for group id
        Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
        if (auth != null) {
            String user = auth.getUserName();
            if (user != null && user.length() > 0) {
                return smWrapper.post(user, title, body, embed, catstrings, medium, mediaService, mediaGuid, mimeType, file);
            }
        }

        Log.e(TAG, "can't post, no user name found");
        return null;
	}

    // NEW/TEMP
    // DOWNLOAD AVAILABE INDEX FOR CURRENT USER AND SAVE TO TARGET FILE
    // RETURN TRUE IF SUCCESSFUL
    public boolean index () {

        // version in constants, or pass in?
        // id value in auth table or use user name?

        try {
            connect();
        } catch (IOException ioe) {
            Log.e("INDEX", "UNABLE TO CONNECT TO SERVER, CAN'T GET INDEX");
            return false;
        }

        // TODO: IF ENDPOINT ONLY REQUIRES TOKEN, DO WE CARE ABOUT AUTH/USER?
        Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
        if (auth != null) {
            String user = auth.getUserName();
            if (user != null && user.length() > 0) {

                // load baseline index
                HashMap<String, ExpansionIndexItem> contentItems = IndexManager.loadAvailableOrderIndex(mContext);

                // download users index
                JSONArray jArray = smWrapper.index(1); // TODO: WHERE IS INTERFACE VERSION SPECIFIED?

                if (jArray == null) {
                    Log.e("INDEX", "FAILED TO DOWNLOAD NEW ASSIGNMENTS");
                    return false;
                }

                if (jArray.length() == 0) {
                    Log.d("INDEX", "NO ASSIGNMENTS FOUND, INDEX WILL NOT BE UPDATED");
                    return true;
                }

                // convert items and add to index
                for (int i = 0; i < jArray.length(); i++) {

                    try {
                        JSONObject jObject = jArray.getJSONObject(i);

                        String filePath = "Android/data/" + mContext.getPackageName() + "/files/";

                        ExpansionIndexItem contentItem = new ExpansionIndexItem(jObject.getString("organization"), // packageName
                                                                                jObject.getString("uuid"), // expansionId
                                                                                "" + (-1 - i), // patchOrder (using arbitrary negative value to avoid collisions with existing items
                                                                                "" + jObject.getInt("version"), // expansionFileVersion
                                                                                filePath, // expansionFilePath
                                                                                jObject.getString("obb_file").substring(0, jObject.getString("obb_file").lastIndexOf("/") + 1), // expansionFileUrl - obb filename
                                                                                jObject.getString("thumbnail_path")); // expansionThumbnail

                        contentItem.setTitle(jObject.getString("title"));
                        contentItem.setDescription(jObject.getString("description"));
                        contentItem.setExpansionFileSize(jObject.getLong("size"));
                        contentItem.setExpansionFileChecksum(jObject.getString("obb_checksum"));

                        contentItems.put(contentItem.getPatchOrder(), contentItem);

                        Log.d("INDEX", "ADDED ITEM TO INDEX (" + contentItem.getTitle() + ")");

                    } catch (JSONException je) {
                        Log.e("INDEX", "FAILED TO EXTRACT VALUE FROM JSON OBJECT (ARRAY ELEMENT " + i + "): " + je.getMessage());
                    }
                }

                IndexManager.saveAvailableIndex(mContext, contentItems);

                Log.d("INDEX", "SAVED UPDATED INDEX");
                return true;
            } else {
                Log.e("INDEX", "NO USER NAME");
            }
        } else {
            Log.e("INDEX", "NOT LOGGED IN");
        }

        Log.e("INDEX", "CAN'T DOWNLOAD NEW ASSIGNMENTS");
        return false;
    }
	
    public void createAccount (Activity activity)
    {
        //open web view here to reg form
        Intent intent = new Intent(mContext, WordPressAuthWebViewActivity.class);
        intent.putExtra("title", "New Account");
        intent.putExtra("url", mServerUrl + PATH_REGISTER);
        
        activity.startActivity(intent);
    }
}
