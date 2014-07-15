package info.guardianproject.mrapp;

import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.model.PublishJobTable;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublishController.PublishListener;
import info.guardianproject.mrapp.publish.PublishService;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.ui.ToggleImageButton;
import io.scal.secureshareui.lib.ChooseAccountFragment;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.holoeverywhere.widget.TextView;

import redstone.xmlrpc.XmlRpcFault;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.animoto.android.views.DraggableGridView;
//import com.hipmob.gifanimationdrawable.GifAnimationDrawable;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
@SuppressLint("ValidFragment")
public class PublishFragment extends Fragment implements PublishListener {
    private final static String TAG = "PublishFragment";
    
    private final static int REQ_SOUNDCLOUD = 777;
    private static Intent serviceIntent = null;
    private static Activity attachedActivity = null;
    
    public ViewPager mAddClipsViewPager;
    View mView = null;

    private EditorBaseActivity mActivity;
    private Handler mHandlerPub;

    private String mMediaUploadAccount = null;
    private String mMediaUploadAccountKey = null;

    ToggleImageButton mButtonUpload;
    TextView mProgressText;
    ToggleImageButton mButtonPlay;
    boolean mPlaying = false; // spnning after user pressed play... we may be rendering
    boolean mUploading = false; // spinning after user pressed upload... we may be rendering or uploading

    Animation mFadeIn;
    Animation mFadeOut;
    Animation mHorizExpand;
    Animation mExpandingFade;
    Animation mSpinConstant;
    ViewAnimator mRenderStateWidget;
    
    String[] mSiteKeys = null;

    private Thread mThreadYouTubeAuth;
//    private Thread mThreadPublish;
    private boolean mUseOAuthWeb = true;

    private SharedPreferences mSettings = null;

    private File mFileLastExport = null;
    private Job mMatchingRenderJob = null;

    /**
     * The sortable grid view that contains the clips to reorder on the
     * Order tab
     */
    protected DraggableGridView mOrderClipsDGV;

    

    private void initFragment()
    {
        mActivity = (EditorBaseActivity) getActivity();
        mHandlerPub = mActivity.mHandlerPub;
        mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }
    

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	initFragment ();
//    	purgePublishTables(); // FIXME for debuging, don't purgePublishTables on load!
    	int layout = getArguments().getInt("layout");
        mView = inflater.inflate(layout, null);
        if (layout == R.layout.fragment_complete_story) {
            
            ProjectInfoFragment infoFrag = ProjectInfoFragment.newInstance(mActivity.getProject().getId(), false, false);
            this.getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_info_container, infoFrag)
                .commit();
            
            View view = mView.findViewById(R.id.fl_info_container);
            view.findViewById(R.id.fl_info_container).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    launchStoryInfoEditMode();
                }

            });
//            view.setOnTouchListener(new OnTouchListener() {
//                
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    // TODO Auto-generated method stub
//                    return true;
//                }
//            }) ;
            
        	ImageView ivThumb = (ImageView)mView.findViewById(R.id.storyThumb);

			Media[] medias = mActivity.mMPM.mScene.getMediaAsArray();
			if (medias.length > 0) {
				Bitmap bitmap = Media.getThumbnail(mActivity, medias[0], mActivity.mMPM.mProject);
				if (bitmap != null) {
					ivThumb.setImageBitmap(bitmap);
				}
			}
			
			// FIXME figure out what spec we need to try to fetch for preview... could be audio or video
			mMatchingRenderJob = (new JobTable()).getMatchingFinishedJob(getActivity(), mActivity.mMPM.mProject.getId(), "render", "video", mActivity.mMPM.mProject.getUpdatedAt());
			if (mMatchingRenderJob != null) {
			    mFileLastExport = new File(mMatchingRenderJob.getResult());
			}

            mProgressText = (TextView) mView.findViewById(R.id.textViewProgress);
            mProgressText.setText("");
            
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            		  mActivity, R.array.story_sections, android.R.layout.simple_spinner_item );
            		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            mButtonPlay = (ToggleImageButton) mView.findViewById(R.id.btnPlay);
            mButtonPlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    playClicked();
                }
            });
            
            mButtonUpload = (ToggleImageButton) mView.findViewById(R.id.btnUpload);
            mButtonUpload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    uploadClicked();
                }
            });
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(PublishService.ACTION_PUBLISH_SUCCESS);
        f.addAction(PublishService.ACTION_PUBLISH_FAILURE);
        f.addAction(PublishService.ACTION_JOB_SUCCESS);
        f.addAction(PublishService.ACTION_JOB_FAILURE);
        f.addAction(PublishService.ACTION_PROGRESS);
        getActivity().registerReceiver(publishReceiver, f);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(publishReceiver);
    }

    private BroadcastReceiver publishReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(PublishService.INTENT_EXTRA_PUBLISH_JOB_ID)) {
                int publishJobId = intent.getIntExtra(PublishService.INTENT_EXTRA_PUBLISH_JOB_ID, -1);
                if (publishJobId != -1) {
                    PublishJob publishJob = (PublishJob) (new PublishJobTable()).get(getActivity().getApplicationContext(), publishJobId);
            
                    if (intent.getAction().equals(PublishService.ACTION_PUBLISH_SUCCESS)) {
                        publishSucceeded(publishJob);
                    } else if (intent.getAction().equals(PublishService.ACTION_PUBLISH_FAILURE)) {
                        int errorCode = intent.getIntExtra(PublishService.INTENT_EXTRA_ERROR_CODE, -1);
                        String errorMessage = intent.getStringExtra(PublishService.INTENT_EXTRA_ERROR_MESSAGE);
                        publishFailed(publishJob, errorCode, errorMessage);
                    } else if (intent.getAction().equals(PublishService.ACTION_JOB_SUCCESS)) {
                        int jobId = intent.getIntExtra(PublishService.INTENT_EXTRA_JOB_ID, -1);
                        Job job = (Job) (new JobTable()).get(getActivity().getApplicationContext(), jobId); // FIXME should we check for -1?
                        jobSucceeded(job);
                    } else if (intent.getAction().equals(PublishService.ACTION_JOB_FAILURE)) {
                        int jobId = intent.getIntExtra(PublishService.INTENT_EXTRA_JOB_ID, -1);
                        Job job = (Job) (new JobTable()).get(getActivity().getApplicationContext(), jobId); // FIXME should we check for -1?
                        int errorCode = intent.getIntExtra(PublishService.INTENT_EXTRA_ERROR_CODE, -1);
                        String errorMessage = intent.getStringExtra(PublishService.INTENT_EXTRA_ERROR_MESSAGE);
                        jobFailed(job, errorCode, errorMessage);
                    } else if (intent.getAction().equals(PublishService.ACTION_PROGRESS)) {
                        float progress = intent.getFloatExtra(PublishService.INTENT_EXTRA_PROGRESS, -1);
                        String message = intent.getStringExtra(PublishService.INTENT_EXTRA_PROGRESS_MESSAGE);
                        publishProgress(publishJob, progress, message);
                    }
                }
            }
        }
    };

    Handler handlerUI = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (mFileLastExport != null && mFileLastExport.exists()) { // FIXME replace this with a check to make sure render is suitable
				Button btnPlay = (Button) mView.findViewById(R.id.btnPlay);
				// Button btnShare = (Button)mView.findViewById(R.id.btnShare);
				// btnShare.setEnabled(true);
				btnPlay.setEnabled(true);
			}
		}
	};
	
	private void launchStoryInfoEditMode() {
        Intent intent = new Intent(getActivity(), StoryInfoEditActivity.class);
        intent.putExtra("pid", mActivity.getProject().getId());
        startActivity(intent);
	}

    private void showPlaySpinner(boolean vis) {
        mButtonPlay.setEnabled(vis);
        mButtonPlay.setChecked(vis);
        mButtonPlay.setImageResource(vis ? R.drawable.spinner_play : R.drawable.ic_comp_play);
        mButtonUpload.setEnabled(!vis);
        mButtonUpload.setChecked(!vis);
        mProgressText.setVisibility(vis ? View.VISIBLE : View.GONE);
        mProgressText.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void showUploadSpinner(boolean vis) {
//        if (vis) { 
//            mButtonPlay.setImageResource(R.drawable.spinner_play);
//            mButtonUpload.setImageResource(R.drawable.ic_comp_upload);
//        } else {
//            mButtonPlay.setImageResource(R.drawable.ic_comp_play);
//        }
        mButtonUpload.setEnabled(vis);
        mButtonUpload.setChecked(vis);
        mButtonUpload.setImageResource( vis ? R.drawable.spinner_upload : R.drawable.ic_comp_upload);
        mButtonPlay.setEnabled(!vis);
        mButtonPlay.setChecked(!vis);
        mProgressText.setVisibility(vis ? View.VISIBLE : View.GONE);
        mProgressText.setTextColor(getResources().getColor(android.R.color.white));
    }
    
    private void showPlayAndUpload(boolean vis) {
        mButtonPlay.setVisibility(vis ? View.VISIBLE : View.GONE);
        mButtonPlay.setEnabled(true);
        mButtonPlay.setChecked(false);
        mButtonPlay.setImageResource(R.drawable.ic_comp_play);
        
        mButtonUpload.setVisibility(vis ? View.VISIBLE : View.GONE);
        mButtonUpload.setEnabled(true);
        mButtonUpload.setChecked(false);
        mButtonUpload.setImageResource(R.drawable.ic_comp_upload);
        
        mProgressText.setVisibility(View.GONE);
    }

//    private void showRenderingSpinner() {
////        mRenderStateWidget.showNext();
////        mRenderStateWidget.setInAnimation(mHorizExpand);
////        mRenderStateWidget.setOutAnimation(mFadeOut);
//        showRenderingSpinner(true);
//        showPlayAndUpload(false);
//        showRender(false);
//    }
//    
    private void showError(int code, String message) {
        if (mPlaying) {
            mButtonPlay.setImageResource(R.drawable.ic_comp_fail);
            mButtonUpload.setImageResource(R.drawable.ic_comp_upload);
        } else if (mUploading) {
            mButtonUpload.setImageResource(R.drawable.ic_comp_fail);
            mButtonPlay.setImageResource(R.drawable.ic_comp_play);
        }
        mButtonPlay.setEnabled(false);
        mButtonUpload.setEnabled(false);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText("Error #" + code + ": " + message);
        mProgressText.setTextColor(getResources().getColor(R.color.red));
    }
    
    private void purgePublishTables() {
        net.sqlcipher.database.SQLiteDatabase db = new StoryMakerDB(getActivity().getBaseContext()).getWritableDatabase("foo");
        (new PublishJobTable(db)).debugPurgeTable();
        (new JobTable(db)).debugPurgeTable();
        db.close();
    }
    
//    private String setUploadAccount() {
//       
//
//        mMediaUploadAccountKey = null;
//        
//        if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
//                || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
//                )
//        {
//        	mMediaUploadAccountKey = "youTubeUserName";
//        	mMediaUploadAccount = mSettings.getString(mMediaUploadAccountKey, null);
//        }
//        else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
//        {
//        	mMediaUploadAccountKey = "soundCloudUserName";
//        	mMediaUploadAccount = mSettings.getString(mMediaUploadAccountKey, null);
//        }
//         
//
//        if (mMediaUploadAccountKey != null && (mMediaUploadAccount == null || mMediaUploadAccount.length() == 0)) {
//        
//        	AccountManager accountManager = AccountManager.get(mActivity.getBaseContext());
//            final Account[] accounts = accountManager.getAccounts();
//
//            if (accounts.length > 0) {
//            	
//                String[] accountNames = new String[accounts.length];
//
//                for (int i = 0; i < accounts.length; i++) {
//                    accountNames[i] = accounts[i].name + " (" + accounts[i].type + ")";
//                }
//                
//                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//                builder.setTitle(R.string.choose_account_for_youtube_upload);
//                builder.setItems(accountNames, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int item) {
//                        mMediaUploadAccount = accounts[item].name;
//                        
//                        Editor editor = mSettings.edit();
//                        
//                        editor.putString(mMediaUploadAccountKey, mMediaUploadAccount);
//                        editor.commit();
//                        
//                        doPublish();
//                        
//
//                    }
//                }).show();
//                
//              
//            }
//            else
//            {
//            	Toast.makeText(mActivity,R.string.err_you_need_at_least_one_account_configured_on_your_device,Toast.LENGTH_LONG).show();
//            }
//            
//        }
//        else
//        {
//        	 doPublish();
//        }
//        
//        return mMediaUploadAccount;
//    }

    
//    private void handlePublish(final boolean doYouTube, final boolean doStoryMaker, final boolean doOverwrite) {
//        
//    	initFragment();
//    	
//        EditText etTitle = (EditText) mView.findViewById(R.id.etStoryTitle);
//        EditText etDesc = (EditText) mView.findViewById(R.id.editTextDescribe);
//        EditText etLocation = (EditText)  mView.findViewById(R.id.editTextLocation);
//        
//		Spinner s = (Spinner) mView.findViewById( R.id.spinnerSections );
//
//		//only one item can be selected
//		ArrayList<String> alCats = new ArrayList<String>();
//		if (s.getSelectedItem() != null)
//			alCats.add((String)s.getSelectedItem());
//		
//		//now support location with comma in it and set each one as a place category
//		StringTokenizer st = new StringTokenizer(etLocation.getText().toString());
//		while (st.hasMoreTokens())
//		{
//			alCats.add(st.nextToken());
//		}
//		
//		//now add story type to categories: event, breaking-news, issue, feature.
//		String catTag = mActivity.mMPM.mProject.getTemplateTag();
//		if (catTag != null)
//			alCats.add(catTag);
//		
//		String[] cattmp = new String[alCats.size()];
//		int i = 0;
//		for (String catstring: alCats)
//			cattmp[i++] = catstring;
//		
//		final String[] categories = cattmp;
//
//        final String title = etTitle.getText().toString();
//        final String desc = etDesc.getText().toString();
//        
//        String ytdesc = desc;
//        if (ytdesc.length() == 0) {
//            ytdesc = getString(R.string.default_youtube_desc); // can't
//                                                                             // leave
//                                                                             // the
//                                                                             // description
//                                                                             // blank
//                                                                             // for
//                                                                             // YouTube
//        }
//        
//        ytdesc += "\n\n" + getString(R.string.created_with_storymaker_tag);
//
//        if (doYouTube)
//        {
//        	mYouTubeClient = new YouTubeSubmit(null, title, ytdesc, new Date(),
//                mActivity, mHandlerPub, mActivity.getBaseContext());
//			mYouTubeClient.setDeveloperKey(getString(R.string.dev_key,Locale.US));
//        
//	        mThreadYouTubeAuth = new Thread() {
//	            public void run() {
//	
//	
//	        		Account account = mYouTubeClient.setYouTubeAccount(mMediaUploadAccount);
//	
//		    			mYouTubeClient.getAuthTokenWithPermission(new AuthorizationListener<String>() {
//		                    @Override
//		                    public void onCanceled() {
//		                    }
//		
//		                    @Override
//		                    public void onError(Exception e) {
//		                  	  Log.d("YouTube","error on auth",e);
//		                  	 Message msgErr = new Message();
//		                     msgErr.what = -1;
//		                     msgErr.getData().putString("err", e.getLocalizedMessage());
//		                     mHandlerPub.sendMessage(msgErr);
//		                  	  
//		                    }
//		
//		                    @Override
//		                    public void onSuccess(String result) {
//		                    	mYouTubeClient.setClientLoginToken(result);
//		                      
//		                      Log.d("YouTube","got client token: " + result);
//		                      mThreadPublish.start();
//		                      
//	
//		                    }});
//	            	
//	            	 
//	            }
//	            
//	        	};
//        }
//            
//        mThreadPublish = new Thread() {
//
//            public void run ()
//            {
//            	
//                mHandlerPub.sendEmptyMessage(999);
//   
//                Message msg = mHandlerPub.obtainMessage(888);
//                msg.getData().putString("status",
//                        getActivity().getString(R.string.rendering_clips_));
//                mHandlerPub.sendMessage(msg);
//
//                try {
//                    
//                	mFileLastExport = mActivity.mMPM.getExportMediaFile();
//
//                    boolean compress = mSettings.getBoolean("pcompress",false);//compress video?
//                    
//                    mActivity.mdExported = mActivity.mMPM.doExportMedia(mFileLastExport, compress, doOverwrite);
//
//                    // FIXME NPE if we ran out of space and Exported is null
//                    File mediaFile = new File(mActivity.mdExported.path);
//
//                    if (mediaFile.exists()) {
//
//                        Message message = mHandlerPub.obtainMessage(777);
//                        message.getData().putString("fileMedia", mActivity.mdExported.path);
//                        message.getData().putString("mime", mActivity.mdExported.mimeType);
//
//                        if (doYouTube) {
//
//                            String mediaEmbed = "";
//                            
//                            String medium = null;
//                            String mediaService = null;
//                            String mediaGuid = null;
//
//                            if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
//                                    || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
//                                    
//                                    ) {
//                            	
//                            	
//                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_VIDEO;
//                            	
//                                msg = mHandlerPub.obtainMessage(888);
//                                msg.getData().putString("statusTitle",
//                                        getActivity().getString(R.string.uploading));
//                                msg.getData().putString("status", getActivity().getString(
//                                        R.string.connecting_to_youtube_));
//                                mHandlerPub.sendMessage(msg);
//
//                                mYouTubeClient.setVideoFile(mediaFile, mActivity.mdExported.mimeType);
//                                mYouTubeClient.upload(YouTubeSubmit.RESUMABLE_UPLOAD_URL);
//                                
//                                while (mYouTubeClient.videoId == null) {
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (Exception e) {
//                                    	Log.e(AppConstants.TAG,"unable to sleep during youtube upload",e);
//                                    }
//                                }
//
//                                mediaEmbed = "[youtube]" + mYouTubeClient.videoId + "[/youtube]";
//                                mediaService = "youtube";
//                                mediaGuid = mYouTubeClient.videoId;
//                                
//                                message.getData().putString("youtubeid", mYouTubeClient.videoId);
//                            }
//                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
//                            	/*
//                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_AUDIO;
//                            	
//                                boolean installed = SoundCloudUploader
//                                        .isCompatibleSoundCloudInstalled(mActivity.getBaseContext());
//
//                                if (installed) {
//                                	
//                                
//                                
//                                    String scDesc = desc + "\n\n" + getString(R.string.created_with_storymaker_tag);;
//                                    
//                                    SoundCloudUploader scu = new SoundCloudUploader();
//                                    
//                                    String scurl = scu.uploadSound(mediaFile, title, scDesc,
//                                            REQ_SOUNDCLOUD, mActivity, mHandlerPub);
//
//                                    if (scurl != null)
//                                    {
//		                                mediaEmbed = "[soundcloud]" + scurl + "[/soundcloud]";
//		
//		                                mediaService = "soundcloud";
//		                                mediaGuid = scurl;
//                                    }
//                                    else
//                                    {
//                                    	throw new IOException("SoundCloud upload failed");
//                                    }
//                                }
//                                else {
//                                    SoundCloudUploader.installSoundCloud(mActivity);
//                                }*/
//                            }
//                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
//                            {
//                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_PHOTO;
//
//
//                                ServerManager sm = StoryMakerApp.getServerManager();
//                                sm.setContext(mActivity.getBaseContext());
//                                
//                                String murl = sm.addMedia(mActivity.mdExported.mimeType, mediaFile);
//                                mediaEmbed = "<img src=\"" + murl + "\"/>";
//                                
//                            }
//                            
//
//                            if (doStoryMaker) {
//                            
//                            	String postUrl = postToStoryMaker (title, desc, mediaEmbed, categories, medium, mediaService, mediaGuid);
//
//                                message.getData().putString("urlPost", postUrl);
//
//                            	
//                            }
//                            
//                        }
//                        
//
//                        handlerUI.sendEmptyMessage(0);
//
//                        mHandlerPub.sendMessage(message);
//                        
//                    }
//                    else {
//                        Message msgErr = new Message();
//                        msgErr.what = -1;
//                        msgErr.getData().putString("err", "Media export failed");
//                        mHandlerPub.sendMessage(msgErr);
//                    }
//                        
//                        
//                } catch (XmlRpcFault e) {
//                    Message msgErr = new Message();
//                    msgErr.what = -1;
//                    msgErr.getData().putString("err", e.getLocalizedMessage());
//                    mHandlerPub.sendMessage(msgErr);
//                    Log.e(AppConstants.TAG, "error posting", e);
//                }
//                catch (Exception e) {
//                    Message msgErr = new Message();
//                    msgErr.what = -1;
//                    msgErr.getData().putString("err", e.getLocalizedMessage());
//                    mHandlerPub.sendMessage(msgErr);
//                    Log.e(AppConstants.TAG, "error posting", e);
//                }
//            }
//        };
//        
//
//	   	 if ((mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
//	                || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
//	               &&  doYouTube 
//	                ) {
//	   		
//	   		 //if do youtube, get the auth token!
//	   		 
//	   		 mUseOAuthWeb = mSettings.getBoolean("pyoutubewebauth", false);
//	   		 
//	   		 if (mUseOAuthWeb)
//	   		 {
//	   			 Intent intent = new Intent(mActivity.getApplicationContext(),OAuthAccessTokenActivity.class);
//	   		 
//	   			 mActivity.startActivityForResult(intent,EditorBaseActivity.REQ_YOUTUBE_AUTH);
//	   		 }
//	   		 else
//	   		 {
//			 			mThreadYouTubeAuth.start();
//	   		 }
//	   	 }
//	   	 else
//	   	 {
//	   		 mThreadPublish.start();
//	   	 }
//    }
    
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
    
    private void playClicked() {
        // FIXME grab the last acceptable render and use it instead of this mFileLastExport junk
        if (mFileLastExport != null && mFileLastExport.exists()) { // FIXME replace this with a check to make sure render is suitable
            mActivity.mMPM.mMediaHelper.playMedia(mFileLastExport, null);
        } else {
            mUploading = false;
            mPlaying = true;
//            purgePublishTables(); // FIXME DEBUG disable this once we fix the publish table bugs
            // TODO default to a video spec render and kick it off
            // create a dummy publishjob with no sites
            showPlaySpinner(true);
            startRender(mActivity.mProject, new String[] {"preview"}, false, false);
        }
    }
    
    private void uploadClicked() {
        launchChooseAccountsDialog();
    }

    private void launchChooseAccountsDialog() {
        Intent intent = new Intent(mActivity, AccountsActivity.class);
        intent.putExtra("isDialog", true);
        intent.putExtra("inSelectionMode", true);
        getActivity().startActivityForResult(intent, ChooseAccountFragment.ACCOUNT_REQUEST_CODE);
    }
    
    public void onChooseAccountDialogResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d("PublishFragment", "Choose Accounts dialog return ok");
            if (intent.hasExtra(ChooseAccountFragment.EXTRAS_ACCOUNT_KEYS)) {
                ArrayList<String> siteKeys = intent.getStringArrayListExtra(ChooseAccountFragment.EXTRAS_ACCOUNT_KEYS);
                if (!siteKeys.isEmpty()) {
                    Log.d(TAG, "selected sites: " + siteKeys);
                    mSiteKeys = siteKeys.toArray(new String[siteKeys.size()]);
                    
                    boolean useTor = intent.getBooleanExtra(ChooseAccountFragment.EXTRAS_USE_TOR, false);
                    boolean publishToStoryMaker = intent.getBooleanExtra(ChooseAccountFragment.EXTRAS_PUBLISH_TO_STORYMAKER, false);
                    
                    showUploadSpinner(true);
                    mUploading = true;
                    mPlaying = false;
//                    if (mFileLastExport != null && mFileLastExport.exists()) { // FIXME replace this with a check to make sure render is suitable
                    if (mMatchingRenderJob != null) {
                        // FIXME i think we need to add that render job to this publishJob here
                        PublishJob publishJob = PublishController.getMatchingPublishJob(getActivity().getApplicationContext(), mActivity.mMPM.mProject, mSiteKeys, useTor, publishToStoryMaker);
                        Job newJob = JobTable.cloneJob(getActivity().getApplicationContext(), mMatchingRenderJob);
                        newJob.setPublishJobId(publishJob.getId());
                        newJob.save();
                        mMatchingRenderJob = newJob;
                        startUpload(mActivity.mMPM.mProject, mSiteKeys, useTor, publishToStoryMaker);
                    } else {
                        startRender(mActivity.mMPM.mProject, mSiteKeys, useTor, publishToStoryMaker);
                    }
                } else {
                    Utils.toastOnUiThread(mActivity, "No site selected."); // FIXME move to strings.xml
                }
            } else {
                Utils.toastOnUiThread(mActivity, "No site selected."); // FIXME move to strings.xml
            }
        } else {
            Log.d("PublishFragment", "Choose Accounts dialog canceled");
//            Utils.toastOnUiThread(mActivity, "Choose Accounts dialog canceled!"); // FIXME move to strings.xml
            showPlayAndUpload(true);
        }
    }
    
    private void startRender(Project project, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        Intent i = new Intent(getActivity(), PublishService.class);
        i.setAction(PublishService.ACTION_RENDER);
        i.putExtra(PublishService.INTENT_EXTRA_PROJECT_ID, project.getId());
        i.putExtra(PublishService.INTENT_EXTRA_USE_TOR, useTor);
        i.putExtra(PublishService.INTENT_EXTRA_PUBLISH_TO_STORYMAKER, publishToStoryMaker);
        i.putExtra(PublishService.INTENT_EXTRA_SITE_KEYS, siteKeys);
        startService(i, publishToStoryMaker);
    }
    
    private void startUpload(Project project, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        Intent i = new Intent(getActivity(), PublishService.class);
        i.setAction(PublishService.ACTION_UPLOAD);
        i.putExtra(PublishService.INTENT_EXTRA_PROJECT_ID, project.getId());
        i.putExtra(PublishService.INTENT_EXTRA_USE_TOR, useTor);
        i.putExtra(PublishService.INTENT_EXTRA_PUBLISH_TO_STORYMAKER, publishToStoryMaker);
        i.putExtra(PublishService.INTENT_EXTRA_SITE_KEYS, siteKeys);
        startService(i, publishToStoryMaker);
    }
    
    private void startService(Intent i, boolean publishToStoryMaker) {
        if(publishToStoryMaker) {
        	attachedActivity = getActivity();
        	serviceIntent = i;
        	
        	ServerManager sm = StoryMakerApp.getServerManager();
            sm.setContext(mActivity.getBaseContext());
            
            if(!sm.hasCreds()) {
            	Intent connectSMIntent = new Intent(mActivity, ConnectAccountActivity.class);
            	connectSMIntent.putExtra("isPublishPending", true);
            	getActivity().startActivity(connectSMIntent);
            }
        }
    	
    	getActivity().startService(i);
    }
    
    public void continuePublishService() {	
    	if(null != serviceIntent) {
    		attachedActivity.startService(serviceIntent);
    	}    	
    }

    @Override
    public void publishSucceeded(PublishJob publishJob) {
            showUploadSpinner(false); 
            showPlaySpinner(false);
            Utils.toastOnUiThread(mActivity, "Publish succeeded!");
        
////        if (publishJob.isFinished()) {
//            showUploadSpinner(false); // FIXME we should detect which stage of the job just finished
//            showPlaySpinner(false); // FIXME we should detect which stage of the job just finished
//            
//            String path = publishJob.getLastRenderFilePath(); // FIXME this can be null
//            if (path != null) { // FIXME this won't work when a upload job succeeds
//                mFileLastExport = new File(path);
//                Handler handlerTimer = new Handler();
//                mProgressText.setText("Complete!");
//                handlerTimer.postDelayed(new Runnable(){
//                    public void run() {
//                        showPlayAndUpload(true);
//                    }
//                }, 200);
//            } else {
//                Log.d(TAG, "last rendered path is empty!");
//            }
//            
////            if (job != null) {
////                
////            }
//            if (mPlaying) {
//                if (mFileLastExport != null && mFileLastExport.exists()) { // FIXME replace this with a check to make sure render is suitable
//                    mActivity.mMPM.mMediaHelper.playMedia(mFileLastExport, null);
//                } 
//                mPlaying = false;
//            } else if (mUploading) {
//                startUpload(mActivity.mMPM.mProject, mSiteKeys, publishJob.getUseTor(), publishJob.getPublishToStoryMaker());
//            }
////        }
    }
    
    @Override
    public void publishFailed(PublishJob publishJob, int errorCode, String errorMessage) {
        Utils.toastOnUiThread(getActivity(), "Publish failed :'( ... " + publishJob); // FIXME move to strings.xml
        showError(errorCode, errorMessage);
    }

    @Override
    public void publishProgress(PublishJob publishJob, float progress, String message) {
//        Utils.toastOnUiThread(getActivity(), "Progress at " + (progress / 10000) + "%: " + message);
        int prog = Math.round(progress * 100);
        String txt = message + ((prog > 0) ? " " + prog + "%" : "");
        mProgressText.setText(txt);
        Log.d(TAG, txt);
    }

    @Override
    public void jobSucceeded(Job job) {
        PublishJob publishJob = job.getPublishJob();
        if (job.getType().equals(JobTable.TYPE_RENDER)) {
            if (mPlaying) {
                showUploadSpinner(false); 
                showPlaySpinner(false);
                
                String path = publishJob.getLastRenderFilePath(); // FIXME this can be null
                if (path != null) { // FIXME this won't work when a upload job succeeds
                    mFileLastExport = new File(path);
                    Handler handlerTimer = new Handler();
                    mProgressText.setText("Complete!");
                    handlerTimer.postDelayed(new Runnable(){
                        public void run() {
                            showPlayAndUpload(true);
                        }
                    }, 200);
                } else {
                    Log.d(TAG, "last rendered path is empty!");
                }                
                
                if (mFileLastExport != null && mFileLastExport.exists()) { // FIXME replace this with a check to make sure render is suitable
                    mActivity.mMPM.mMediaHelper.playMedia(mFileLastExport, null);
                } 
                mPlaying = false;
            } if (mUploading) { 
                startUpload(mActivity.mMPM.mProject, mSiteKeys, publishJob.getUseTor(), publishJob.getPublishToStoryMaker());
            }
        } else if (job.getType().equals(JobTable.TYPE_UPLOAD)) {
            // FIXME ???
        }
    }

    @Override
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        // TODO Auto-generated method stub
        
    }
}