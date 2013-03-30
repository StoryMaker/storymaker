package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.server.SoundCloudUploader;
import info.guardianproject.mrapp.server.YouTubeSubmit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.ToggleButton;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.animoto.android.views.DraggableGridView;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
@SuppressLint("ValidFragment")
public class PublishFragment extends Fragment {
    private final static String TAG = "PublishFragment";
    private final static int REQ_SOUNDCLOUD = 999;
    
    int layout;
    public ViewPager mAddClipsViewPager;
    View mView = null;
    private EditorBaseActivity mActivity;
    private Handler mHandlerPub;
    private String mMediaUploadAccount = null;

    /**
     * The sortable grid view that contains the clips to reorder on the
     * Order tab
     */
    protected DraggableGridView mOrderClipsDGV;

    public PublishFragment(int layout, EditorBaseActivity activity)
            throws IOException, JSONException {
        this.layout = layout;
        mActivity = activity;
        mHandlerPub = activity.mHandlerPub;
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(layout, null);
        if (this.layout == R.layout.fragment_story_publish) {
        	
        	ImageView ivThumb = (ImageView)view.findViewById(R.id.storyThumb);

            Media[] medias = mActivity.mMPM.mScene.getMediaAsArray();
            if (medias.length > 0)
            {
                Bitmap bitmap = mActivity.getThumbnail(medias[0]);
            	if (bitmap != null) ivThumb.setImageBitmap(bitmap);
            }
        	
            EditText etTitle = (EditText) view.findViewById(R.id.etStoryTitle);
            EditText etDesc = (EditText) view.findViewById(R.id.editTextDescribe);

            etTitle.setText(mActivity.mMPM.mProject.getTitle());
            
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            		  mActivity, R.array.story_sections, android.R.layout.simple_spinner_item );
            		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            		
			Spinner s = (Spinner) view.findViewById( R.id.spinnerSections );
			s.setAdapter( adapter );

            Button btnRender = (Button) view.findViewById(R.id.btnRender);
            btnRender.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0) {
                    
                	File fileExport = mActivity.mMPM.getExportMediaFile();
                	if (fileExport.exists())
                		fileExport.delete();
                	
                    handlePublish(false, false);
                }
                
            });
            
            Button btn = (Button) view.findViewById(R.id.btnPublish);
            btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    

                    
                    doPublish(); 

                }
            });
        }
        return view;
    }
    
    public void doPublish() {
    	
    	setUploadAccount();
        
        ServerManager sm = StoryMakerApp.getServerManager();
        sm.setContext(mActivity.getBaseContext());

        if (!sm.hasCreds())
            showLogin();
        else
        {
        	handlePublish(true, true);
        }
        
    }

    private void showLogin() {
        startActivity(new Intent(mActivity, LoginActivity.class));
    }

    private void setUploadAccount() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(mActivity);

        String projectType = null;
        
        if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
                || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                )
        	projectType = "video";
        else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
        	projectType = "audio";
        else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
        	projectType = "image";
        
        
        mMediaUploadAccount = settings.getString("youTubeUserName", null);

        
        if (mMediaUploadAccount == null) {
        
        	AccountManager accountManager = AccountManager.get(mActivity.getBaseContext());
            Account[] accounts = accountManager.getAccounts();

          
            if (accounts.length > 0) {
            	
            	for (Account account : accounts)
            	{
            		if (account.type != null)
	            		if (projectType.startsWith("audio") && account.type.contains("com.soundcloud"))
	            		{
	            			mMediaUploadAccount = account.name;
	            			break;
	            		}
	            		else if (projectType.startsWith("video") && account.type.contains("com.google"))
	            		{
	            			mMediaUploadAccount = account.name;
	            			break;
	            		}
            	}
                
              
            }
        }
    }

    private void handlePublish(final boolean doYouTube, final boolean doStoryMaker) {
        
        EditText etTitle = (EditText) mActivity.findViewById(R.id.etStoryTitle);
        EditText etDesc = (EditText) mActivity.findViewById(R.id.editTextDescribe);
        EditText etLocation = (EditText)  mActivity.findViewById(R.id.editTextLocation);
        
		Spinner s = (Spinner) mActivity.findViewById( R.id.spinnerSections );

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
		
		String[] cattmp = new String[alCats.size()];
		int i = 0;
		for (String catstring: alCats)
			cattmp[i++] = catstring;
		
		final String[] categories = cattmp;
		
        mHandlerPub.sendEmptyMessage(999);

        final String title = etTitle.getText().toString();
        final String desc = etDesc.getText().toString();
        String ytdesc = desc;
        if (ytdesc.length() == 0) {
            ytdesc = getActivity().getString(R.string.default_youtube_desc); // can't
                                                                             // leave
                                                                             // the
                                                                             // description
                                                                             // blank
                                                                             // for
                                                                             // YouTube
        }

        final YouTubeSubmit yts = new YouTubeSubmit(null, title, ytdesc, new Date(),
                mActivity, mHandlerPub, mActivity.getBaseContext());


        Thread thread = new Thread() {
            public void run() {

                yts.getAuthTokenWithPermission(mMediaUploadAccount);
                
                ServerManager sm = StoryMakerApp.getServerManager();
                sm.setContext(mActivity.getBaseContext());

                Message msg = mHandlerPub.obtainMessage(888);
                msg.getData().putString("status",
                        getActivity().getString(R.string.rendering_clips_));
                mHandlerPub.sendMessage(msg);

                try {
                    
                    File fileExport = mActivity.mMPM.getExportMediaFile();

                    SharedPreferences settings = PreferenceManager
                            .getDefaultSharedPreferences(mActivity);
                    
                    boolean compress = settings.getBoolean("pcompress",true);//compress video?
                    boolean overwrite = false;
                    
                    mActivity.mMPM.doExportMedia(fileExport, compress, overwrite);
                    
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

                                yts.setVideoFile(mediaFile, mActivity.mdExported.mimeType);
                                yts.upload();
                                
                                while (yts.videoId == null) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                    }
                                }

                                mediaEmbed = "[youtube]" + yts.videoId + "[/youtube]";
                                mediaService = "youtube";
                                mediaGuid = yts.videoId;
                                
                                message.getData().putString("youtubeid", yts.videoId);
                            }
                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
                            	
                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_AUDIO;
                            	
                                boolean installed = SoundCloudUploader
                                        .isCompatibleSoundCloudInstalled(mActivity.getBaseContext());

                                if (installed) {
                                	
                                	String scTitle = title + " " + new Date().getTime();
                                	
                                    String scurl = SoundCloudUploader.buildSoundCloudURL(
                                            mMediaUploadAccount, mediaFile, scTitle);
                                    mediaEmbed = "[soundcloud]" + scurl + "[/soundcloud]";

                                    SoundCloudUploader.uploadSound(mediaFile, scTitle, desc,
                                            REQ_SOUNDCLOUD, mActivity);

                                    mediaService = "soundcloud";
                                    mediaGuid = scurl;
                                }
                                else {
                                    SoundCloudUploader.installSoundCloud(mActivity.getBaseContext());
                                }
                            }
                            else if (sm.hasCreds()) //must be photo
                            {
                            	medium = ServerManager.CUSTOM_FIELD_MEDIUM_PHOTO;

                                String murl = sm.addMedia(mActivity.mdExported.mimeType, mediaFile);
                                mediaEmbed = "<img src=\"" + murl + "\"/>";
                                
                            }

                            if (doStoryMaker) {
                            	
                            	Message msgStatus = mHandlerPub.obtainMessage(888);
                            	msgStatus.getData().putString("status",
                                        getActivity().getString(R.string.uploading_to_storymaker));
                                mHandlerPub.sendMessage(msgStatus);
                            	
                                String descWithMedia = desc + "\n\n" + mediaEmbed;
                                String postId = sm.post(title, descWithMedia, categories, medium, mediaService, mediaGuid);
                                
                                String urlPost = sm.getPostUrl(postId);
                                message.getData().putString("urlPost", urlPost);
                            }
                        }
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

        thread.start();
    }
}