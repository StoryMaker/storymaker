package org.codeforafrica.listeningpost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.codeforafrica.listeningpost.R;
import org.codeforafrica.listeningpost.location.GPSTracker;
import org.codeforafrica.listeningpost.model.Media;
import org.codeforafrica.listeningpost.model.Project;
import org.codeforafrica.listeningpost.server.LoginActivity;
import org.codeforafrica.listeningpost.server.OAuthAccessTokenActivity;
import org.codeforafrica.listeningpost.server.ServerManager;
import org.codeforafrica.listeningpost.server.YouTubeSubmit;
import org.codeforafrica.listeningpost.server.Authorizer.AuthorizationListener;
import org.codeforafrica.listeningpost.server.soundcloud.SoundCloudUploader;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.animoto.android.views.DraggableGridView;
/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
@SuppressLint("ValidFragment")
public class PublishFragment extends Fragment {
    private final static String TAG = "PublishFragment";
    
    private final static int REQ_SOUNDCLOUD = 777;
    
    public ViewPager mAddClipsViewPager;
    View mView = null;
 
    private EditorBaseActivity mActivity;
    private Handler mHandlerPub;
    
    private String mMediaUploadAccount = null;
    private String mMediaUploadAccountKey = null;
    
    EditText mTitle;
    EditText mDescription;
   
    EditText etLocation;
	GPSTracker gpsT; 
    private ToggleButton toggleGPS;
    
    private YouTubeSubmit mYouTubeClient = null;

     private Thread mThreadYouTubeAuth;
     private Thread mThreadPublish;
     private boolean mUseOAuthWeb = true;
     
    private SharedPreferences mSettings = null;
    
    private File mFileLastExport = null;

    /**
     * The sortable grid view that contains the clips to reorder on the
     * Order tab
     */
    
    protected DraggableGridView mOrderClipsDGV;

    

    private void initFragment ()
    {
    	mActivity = (EditorBaseActivity)getActivity();
    	
        mHandlerPub = mActivity.mHandlerPub;
        

        mSettings = PreferenceManager
        .getDefaultSharedPreferences(getActivity().getApplicationContext());
	
    }
    
    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	initFragment ();
    	
    	int layout = getArguments().getInt("layout");
    	
        mView = inflater.inflate(layout, null);
        if (layout == R.layout.fragment_story_publish) {
        	
        	ImageView ivThumb = (ImageView)mView.findViewById(R.id.storyThumb);

            Media[] medias = mActivity.mMPM.mScene.getMediaAsArray();
            if (medias.length > 0)
            {
                Bitmap bitmap = Media.getThumbnail(mActivity,medias[0],mActivity.mMPM.mProject);
            	if (bitmap != null) ivThumb.setImageBitmap(bitmap);
            }
            
           
            mTitle = (EditText) mView.findViewById(R.id.etStoryTitle);
            mDescription = (EditText) mView.findViewById(R.id.editTextDescribe);
            etLocation = (EditText)  mView.findViewById(R.id.editTextLocation);

            mTitle.setText(mActivity.mMPM.mProject.getTitle());
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
        	if(prefs.getString("categories", null)==null){
            
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            		  mActivity, R.array.story_sections, android.R.layout.simple_spinner_item );
            		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            		
			Spinner s = (Spinner) mView.findViewById( R.id.spinnerSections );
			s.setAdapter( adapter );
        	}else{
        		
        		setCategories();
        	}
        	
            Button btnRender = (Button) mView.findViewById(R.id.btnRender);
            btnRender.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0) {
                    saveForm();
                    
                    mFileLastExport = mActivity.mMPM.getExportMediaFile();
               
                	//do local render, overwrite always
                    handlePublish(false, false, true);
                }
                
            });
            
            Button btnPlay = (Button) mView.findViewById(R.id.btnPlay);
        	//File fileExport = mActivity.mMPM.getExportMediaFile();
        	//btnPlay.setEnabled(fileExport.exists());
            
            btnPlay.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0) {
                    
                	if (mFileLastExport != null && mFileLastExport.exists())
                	{
                		
                		mActivity.mMPM.mMediaHelper.playMedia(mFileLastExport, null);
                	}
                }
                
            });
            
            Button btnShare = (Button) mView.findViewById(R.id.btnShare);
          //  btnShare.setEnabled(fileExport.exists());
        	
            btnShare.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0) {
                    
                	if (mFileLastExport != null && mFileLastExport.exists())
                	{
                		
                		mActivity.mMPM.mMediaHelper.shareMedia(mFileLastExport, null);
                	}
                }
                
            });
            
            Button btnCaption = (Button) mView.findViewById(R.id.addCaption);
            btnCaption.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    saveForm();
                }
            });
            
            Button btn = (Button) mView.findViewById(R.id.btnPublish);
            btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    saveForm();
                    setUploadAccount(); //triggers do publish! 
                }
            });

            toggleGPS = (ToggleButton) mView.findViewById(R.id.toggleButton1);
            toggleGPS.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                	                	
                    if(toggleGPS.isChecked()){
                    	etLocation.setEnabled(false);
                    	setLocation();
                    	
                    }
                    else{
                    	etLocation.setEnabled(true);
                    	etLocation.setText("");

                    }
                }
            });
        }
        return mView;
    }
    
    Handler handlerUI = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
	    	if (mFileLastExport != null && mFileLastExport.exists())
	    	{


		    	Button btnPlay = (Button) mView.findViewById(R.id.btnPlay);
		    	
		    	Button btnShare = (Button)mView.findViewById(R.id.btnShare);
		        
	    		btnShare.setEnabled(true);
	    		btnPlay.setEnabled(true);
	    	}    
		    
		    
		}
    	
    };
    
    public void doPublish() {

        ServerManager sm = StoryMakerApp.getServerManager();
      
        if (!sm.hasCreds())
            showLogin();
        else
        {
        	// do render + publish, don't overwrite
        	handlePublish(true, true, true);
        	
        	
        }
    	
        
    }
    public void setCategories(){
		Spinner s = (Spinner) mView.findViewById( R.id.spinnerSections );

        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
        	try {
        	    JSONArray jsonArray2 = new JSONArray(prefs.getString("categories", "[]"));
        	    ArrayList<String> list=new ArrayList<String>();
        	
    			for(int i=0;i<jsonArray2.length();i++)
    			{
    				list.add(jsonArray2.getString(i));
    			}
    			
    			ArrayAdapter<String> spinnerMenu = new ArrayAdapter<String>(mActivity.getApplicationContext(),  R.layout.custom_spinner, list);
    			s.setAdapter(spinnerMenu);
    			
        	}catch (Exception e) {
        	    e.printStackTrace();
        	}
 
    }
    private void saveForm() {
    	String caption = mTitle.getText().toString();
    	if(caption.equals("")){
    		caption = "no caption";
    	}
    	
        mActivity.mMPM.mProject.setTitle(caption);
        //commenting this out for now until merges are fixed
        //mActivity.mMPM.mProject.setDescription(mDescription.getText().toString());
        mActivity.mMPM.mProject.save();
        
        //add to Q
        ArrayList<Media> lMedia = mActivity.mMPM.mProject.getMediaAsList();
        
        for(int i = 0; i<lMedia.size(); i++){
        	
        	Media m = lMedia.get(i);
        	try {
				addToQ(m);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        //Load report screen
        int reportid = mActivity.mMPM.mProject.getReport();
        Intent i = new Intent(mActivity, ReportActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("rid", reportid);
        mActivity.startActivity(i);
        
        mActivity.finish();
    }
    public void addToQ(Media media) throws JSONException{
    	//create and store thumbnails in hidden folder on sd
    	
    	File thumbDir = new File(Environment.getExternalStorageDirectory() + "/" + AppConstants.TAG + "/.thumbs");
    	if(!thumbDir.exists()){
    		thumbDir.mkdirs();
    	}
    	
        //Create thumbnail
        Bitmap bitThumb = null;
        String filename=null;
        
        if(media.getMimeType().contains("video")){
           bitThumb = MediaUtils.getVideoFrame(media.getPath(), -1);
        }else if(media.getMimeType().contains("image")){
        	bitThumb = BitmapFactory.decodeFile(media.getPath());
        }
        	try{
        		filename = thumbDir + "/" + media.getId()+".jpg";
        		FileOutputStream out = new FileOutputStream(filename);
        		bitThumb.compress(Bitmap.CompressFormat.JPEG, 5, out);
        		out.close();
		       } catch (Exception e) {
		               e.printStackTrace();
		}
        
        //Add to Queue
        
        //First read all we have
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
        JSONArray jsonArray2 = null;
        try {
            jsonArray2 = new JSONArray(prefs.getString("eQ", "[]"));
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(media!=null){
	        String media_id = String.valueOf(media.getId());
	        
	        boolean isAdded = false; 
	        
	        //Check if media is already encrypted
	        if(media.getEncrypted()==0){
	        	//Check if value is already added 
	        	
	        	//TODO: find faster way to do this
	        	for (int i = 0; i < jsonArray2.length(); i++) {
	                if(jsonArray2.getString(i).equals(media_id)){
	                	isAdded = true;
	                }
	           }
	        }
	        Editor editor = prefs.edit();
	        if(isAdded==false){
		        //Then add new value
		        jsonArray2.put(media_id);
		        editor.putString("eQ", jsonArray2.toString());
		        
	        }
	        System.out.println(jsonArray2.toString());
	        editor.commit();
        }
    }
    private void showLogin() {
        mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
    }

    private String setUploadAccount() {
       

        mMediaUploadAccountKey = null;
        
        if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
                || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                )
        {
        	mMediaUploadAccountKey = "youTubeUserName";
        	mMediaUploadAccount = mSettings.getString(mMediaUploadAccountKey, null);
        }
        else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
        {
        	mMediaUploadAccountKey = "soundCloudUserName";
        	mMediaUploadAccount = mSettings.getString(mMediaUploadAccountKey, null);
        }
         

        if (mMediaUploadAccountKey != null && (mMediaUploadAccount == null || mMediaUploadAccount.length() == 0)) {
        
        	AccountManager accountManager = AccountManager.get(mActivity.getBaseContext());
            final Account[] accounts = accountManager.getAccounts();

            if (accounts.length > 0) {
            	
                String[] accountNames = new String[accounts.length];

                for (int i = 0; i < accounts.length; i++) {
                    accountNames[i] = accounts[i].name + " (" + accounts[i].type + ")";
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.choose_account_for_youtube_upload);
                builder.setItems(accountNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mMediaUploadAccount = accounts[item].name;
                        
                        Editor editor = mSettings.edit();
                        
                        editor.putString(mMediaUploadAccountKey, mMediaUploadAccount);
                        editor.commit();
                        
                        doPublish();
                        

                    }
                }).show();
                
              
            }
            else
            {
            	Toast.makeText(mActivity,R.string.err_you_need_at_least_one_account_configured_on_your_device,Toast.LENGTH_LONG).show();
            }
            
        }
        else
        {
        	 doPublish();
        }
        
        return mMediaUploadAccount;
    }

    
    private void handlePublish(final boolean doYouTube, final boolean doStoryMaker, final boolean doOverwrite) {
        
    	initFragment();
    	
        EditText etTitle = (EditText) mView.findViewById(R.id.etStoryTitle);
        EditText etDesc = (EditText) mView.findViewById(R.id.editTextDescribe);

		Spinner s = (Spinner) mView.findViewById( R.id.spinnerSections );

		//only one item can be selected
		ArrayList<String> alCats = new ArrayList<String>();
		if (s.getSelectedItem() != null)
			alCats.add((String)s.getSelectedItem());
		
		//now support location with comma in it and set each one as a place category
		StringTokenizer st = new StringTokenizer(etLocation.getText().toString());
		while (st.hasMoreTokens())
		{
			alCats.add(st.nextToken());
		}
		
		//now add story type to categories: event, breaking-news, issue, feature.
		String catTag = mActivity.mMPM.mProject.getTemplateTag();
		if (catTag != null)
			alCats.add(catTag);
		
		String[] cattmp = new String[alCats.size()];
		int i = 0;
		for (String catstring: alCats)
			cattmp[i++] = catstring;
		
		final String[] categories = cattmp;

        final String title = etTitle.getText().toString();
        final String desc = etDesc.getText().toString();
        
        String ytdesc = desc;
        if (ytdesc.length() == 0) {
            ytdesc = getString(R.string.default_youtube_desc); // can't
                                                                             // leave
                                                                             // the
                                                                             // description
                                                                             // blank
                                                                             // for
                                                                             // YouTube
        }
        
        ytdesc += "\n\n" + getString(R.string.created_with_storymaker_tag);

        if (doYouTube)
        {
        	mYouTubeClient = new YouTubeSubmit(null, title, ytdesc, new Date(),
                mActivity, mHandlerPub, mActivity.getBaseContext());
			mYouTubeClient.setDeveloperKey(getString(R.string.dev_key,Locale.US));
        
	        mThreadYouTubeAuth = new Thread() {
	            public void run() {
	
	
	        		Account account = mYouTubeClient.setYouTubeAccount(mMediaUploadAccount);
	
		    			mYouTubeClient.getAuthTokenWithPermission(new AuthorizationListener<String>() {
		                    @Override
		                    public void onCanceled() {
		                    }
		
		                    @Override
		                    public void onError(Exception e) {
		                  	  Log.d("YouTube","error on auth",e);
		                  	 Message msgErr = new Message();
		                     msgErr.what = -1;
		                     msgErr.getData().putString("err", e.getLocalizedMessage());
		                     mHandlerPub.sendMessage(msgErr);
		                  	  
		                    }
		
		                    @Override
		                    public void onSuccess(String result) {
		                    	mYouTubeClient.setClientLoginToken(result);
		                      
		                      Log.d("YouTube","got client token: " + result);
		                      mThreadPublish.start();
		                      
	
		                    }});
	            	
	            	 
	            }
	            
	        	};
        }
            
        mThreadPublish = new Thread() {

            public void run ()
            {
            	
                mHandlerPub.sendEmptyMessage(999);
   
                Message msg = mHandlerPub.obtainMessage(888);
                msg.getData().putString("status",
                        getActivity().getString(R.string.rendering_clips_));
                mHandlerPub.sendMessage(msg);

                try {
                    
                	mFileLastExport = mActivity.mMPM.getExportMediaFile();

                    boolean compress = mSettings.getBoolean("pcompress",false);//compress video?
                    
                    mActivity.mMPM.doExportMedia(mFileLastExport, compress, doOverwrite);
                    
                    mActivity.mdExported = mActivity.mMPM.getExportMedia();
                    
                    File mediaFile = new File(mActivity.mdExported.path);

                    if (mediaFile.exists()) {

                        Message message = mHandlerPub.obtainMessage(777);
                        message.getData().putString("fileMedia", mActivity.mdExported.path);
                        message.getData().putString("mime", mActivity.mdExported.mimeType);

                        if (doYouTube) {

                            String mediaEmbed = "";
                            
                            String medium = null;
                            String mediaService = null;
                            String mediaGuid = null;

                            if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
                                    || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                                    
                                    ) {
                            	
                            	
                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_VIDEO;
                            	
                                msg = mHandlerPub.obtainMessage(888);
                                msg.getData().putString("statusTitle",
                                        getActivity().getString(R.string.uploading));
                                msg.getData().putString("status", getActivity().getString(
                                        R.string.connecting_to_youtube_));
                                mHandlerPub.sendMessage(msg);

                                mYouTubeClient.setVideoFile(mediaFile, mActivity.mdExported.mimeType);
                                mYouTubeClient.upload(YouTubeSubmit.RESUMABLE_UPLOAD_URL);
                                
                                while (mYouTubeClient.videoId == null) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                    	Log.e(AppConstants.TAG,"unable to sleep during youtube upload",e);
                                    }
                                }

                                mediaEmbed = "[youtube]" + mYouTubeClient.videoId + "[/youtube]";
                                mediaService = "youtube";
                                mediaGuid = mYouTubeClient.videoId;
                                
                                message.getData().putString("youtubeid", mYouTubeClient.videoId);
                            }
                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
                            	
                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_AUDIO;
                            	
                                boolean installed = SoundCloudUploader
                                        .isCompatibleSoundCloudInstalled(mActivity.getBaseContext());

                                if (installed) {
                                	
                                
                                
                                    String scDesc = desc + "\n\n" + getString(R.string.created_with_storymaker_tag);;
                                    
                                    SoundCloudUploader scu = new SoundCloudUploader();
                                    
                                    String scurl = scu.uploadSound(mediaFile, title, scDesc,
                                            REQ_SOUNDCLOUD, mActivity, mHandlerPub);

                                    if (scurl != null)
                                    {
		                                mediaEmbed = "[soundcloud]" + scurl + "[/soundcloud]";
		
		                                mediaService = "soundcloud";
		                                mediaGuid = scurl;
                                    }
                                    else
                                    {
                                    	throw new IOException("SoundCloud upload failed");
                                    }
                                }
                                else {
                                    SoundCloudUploader.installSoundCloud(mActivity);
                                }
                            }
                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
                            {
                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_PHOTO;


                                ServerManager sm = StoryMakerApp.getServerManager();
                                sm.setContext(mActivity.getBaseContext());
                                
                                String murl = sm.addMedia(mActivity.mdExported.mimeType, mediaFile);
                                mediaEmbed = "<img src=\"" + murl + "\"/>";
                                
                            }
                            

                            if (doStoryMaker) {
                            
                            	String postUrl = postToStoryMaker (title, desc, mediaEmbed, categories, medium, mediaService, mediaGuid);

                                message.getData().putString("urlPost", postUrl);

                            	
                            }
                            
                        }
                        

                        handlerUI.sendEmptyMessage(0);

                        mHandlerPub.sendMessage(message);
                        
                    }
                    else {
                        Message msgErr = new Message();
                        msgErr.what = -1;
                        msgErr.getData().putString("err", "Media export failed");
                        mHandlerPub.sendMessage(msgErr);
                    }
                        
                        
                } catch (XmlRpcFault e) {
                    Message msgErr = new Message();
                    msgErr.what = -1;
                    msgErr.getData().putString("err", e.getLocalizedMessage());
                    mHandlerPub.sendMessage(msgErr);
                    Log.e(AppConstants.TAG, "error posting", e);
                }
                catch (Exception e) {
                    Message msgErr = new Message();
                    msgErr.what = -1;
                    msgErr.getData().putString("err", e.getLocalizedMessage());
                    mHandlerPub.sendMessage(msgErr);
                    Log.e(AppConstants.TAG, "error posting", e);
                }
            }
        };
        

	   	 if ((mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
	                || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
	               &&  doYouTube 
	                ) {
	   		
	   		 //if do youtube, get the auth token!
	   		 
	   		 mUseOAuthWeb = mSettings.getBoolean("pyoutubewebauth", false);
	   		 
	   		 if (mUseOAuthWeb)
	   		 {
	   			 Intent intent = new Intent(mActivity.getApplicationContext(),OAuthAccessTokenActivity.class);
	   		 
	   			 mActivity.startActivityForResult(intent,EditorBaseActivity.REQ_YOUTUBE_AUTH);
	   		 }
	   		 else
	   		 {
			 			mThreadYouTubeAuth.start();
	   		 }
	   	 }
	   	 else
	   	 {
	   		 mThreadPublish.start();
	   	 }
    }
    
    public String postToStoryMaker (String title, String desc, String mediaEmbed, String[] categories, String medium, String mediaService, String mediaGuid) throws MalformedURLException, XmlRpcFault
    {


        ServerManager sm = StoryMakerApp.getServerManager();
        sm.setContext(mActivity.getBaseContext());

    	Message msgStatus = mHandlerPub.obtainMessage(888);
    	msgStatus.getData().putString("status",
                getActivity().getString(R.string.uploading_to_storymaker));
        mHandlerPub.sendMessage(msgStatus);
    	
        String descWithMedia = desc + "\n\n" + mediaEmbed;
        String postId = sm.post(title, descWithMedia, categories, medium, mediaService, mediaGuid);
        
        String urlPost = sm.getPostUrl(postId);
        return urlPost;
        
    }
    
    public void setYouTubeAuth (String token)
    {
    	mYouTubeClient.setAuthMode("Bearer");
    	mYouTubeClient.setClientLoginToken(token);
    	mThreadPublish.start();
    }
    public void setLocation(){
		gpsT = new GPSTracker(mActivity); 
		  
        // check if GPS enabled 
        if(gpsT.canGetLocation()){ 

            double latitude = gpsT.getLatitude(); 
            double longitude = gpsT.getLongitude(); 

            etLocation.setText(latitude+", "+longitude); 

            if((String.valueOf(latitude).equals("0"))&&(String.valueOf(longitude).equals("0"))){
                gpsT.showSettingsAlert(); 
                toggleGPS.setChecked(false);
            }
        }else{ 
            // can't get location 
            // GPS or Network is not enabled 
            // Ask user to enable GPS/network in settings 
            gpsT.showSettingsAlert(); 
            toggleGPS.setChecked(false);

        } 
    }
}