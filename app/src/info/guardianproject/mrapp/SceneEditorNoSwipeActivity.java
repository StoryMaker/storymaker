package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.media.OverlayCameraActivity;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Template;
import info.guardianproject.mrapp.model.Template.Clip;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.server.YouTubeSubmit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import net.micode.soundrecorder.SoundRecorder;

import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.ToggleButton;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;

public class SceneEditorNoSwipeActivity extends org.holoeverywhere.app.Activity implements ActionBar.TabListener {
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private final static int REQ_OVERLAY_CAM = 888; //for resp handling from overlay cam launch
    protected boolean templateStory = false; 
    protected Menu mMenu = null;
    private Context mContext = null;
    private String templateJsonPath = null;
    private int mStoryMode = Project.STORY_TYPE_VIDEO;;
    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    private MediaProjectManager mMPM;
    public SceneChooserFragment mFragmentTab0, mFragmentTab1, mFragmentTab2, mLastTabFrag;
	private PreviewVideoView mPreviewVideoView = null;
	private ImageView mImageViewMedia;
	
	private String mYouTubeUsername = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().hasExtra("template_story")) {
        	templateStory = true;
        }
        
        if (getIntent().hasExtra("template_path")) {
        	templateJsonPath = getIntent().getStringExtra("template_path");
        }
        
        if (getIntent().hasExtra("story_mode"))
        {
        	mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);
        }
        
        mContext = getBaseContext();
        
        mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub);

        setContentView(R.layout.activity_scene_editor_no_swipe);
        
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_publish).setTabListener(this));
        
        showHelp();
    }
    
    private ProgressDialog dialog = null;
    
    private Handler mHandlerPub = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			String status = msg.getData().getString("status");

  	        String error = msg.getData().getString("error");
  	        if (error == null)
  	        	error = msg.getData().getString("err");
  	        
  	        int progress = msg.getData().getInt("progress");
  	        
  	        if (dialog != null && progress > 0)
  	        	dialog.setProgress(progress);
			
			switch (msg.what)
			{
				case 0:
				case 1:
					
					if (status != null)
					{
						if (dialog != null)
							dialog.setMessage(status);
						else
						{
							Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
							
						}
					}
				break;
				
				case 999:
					
						dialog = new ProgressDialog(SceneEditorNoSwipeActivity.this);
	          		    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	          		    dialog.setTitle("Publishing");
	          		    dialog.setMessage("Rendering project...");
	          		    dialog.setCancelable(true);
	          		    dialog.show();
					
				break;
				
				case 888:
	          		  dialog.setMessage(status);
	            break;
				case 777:
					
		  	        String videoId = msg.getData().getString("youtubeid");
		  	        String url = msg.getData().getString("urlPost");
		  	        String localPath = msg.getData().getString("fileVideo");
		
					dialog.dismiss();
					dialog = null;
					
					showPublished(url,new File(localPath),videoId);
					
					
				break;
				case -1:
					 dialog.setMessage(error);
				break;
				default:
				
					
			}
			
			
		}
    	
    };

    public void showPublished (final String postUrl, final File localVideo, final String youTubeId)
    {
    	if (youTubeId != null)
    	{
	    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	
	    	        switch (which){
	    	        case DialogInterface.BUTTON_POSITIVE:
	    	        	String youTubeUrl = "https://www.youtube.com/watch?v=" + youTubeId;
	    	        	
	    	        	Intent i = new Intent(Intent.ACTION_VIEW);
	    	        	i.setData(Uri.parse(youTubeUrl));
	    	        	startActivity(i);
	    	            break;
	
	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	        	
	    	        	mMPM.mMediaHelper.playMedia(localVideo, "video/mp4");
	    	        	
	    	            break;
	    	        }
	    	    }
	    	};
	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Watch video on YouTube or local copy?").setPositiveButton("YouTube", dialogClickListener)
	    	    .setNegativeButton("Local", dialogClickListener).show();
    	}
    	else
    	{
        	mMPM.mMediaHelper.playMedia(localVideo, "video/mp4");

    	}
    	
    }
    
    private void showHelp (){
    	
    	Toast.makeText(this, getString(R.string.help_clip_select),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mMenu = menu;
        getSupportMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	if (templateStory) {
            		NavUtils.navigateUpTo(this, new Intent(this, StoryTemplateActivity.class));
            	} else {
            		NavUtils.navigateUpFromSameTask(this);
            	}
                return true;
            case R.id.itemForward:
            	int idx = getSupportActionBar().getSelectedNavigationIndex();
            	getSupportActionBar().setSelectedNavigationItem(Math.min(2, idx+1));
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().hide(mLastTabFrag).commit();
    }
    
//    protected void setupAddClipsFragment() {
//    	FragmentManager fm = getSupportFragmentManager();
//    
//		 try {
//			mFragmentTab0 = new SceneChooserFragment(R.layout.fragment_add_clips, fm, templateJsonPath);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, show the tab contents in the container
        int layout = R.layout.fragment_add_clips;
        FragmentManager fm = getSupportFragmentManager();
        
        if (mMenu != null) {
	        mMenu.findItem(R.id.itemInfo).setVisible(false);
	        mMenu.findItem(R.id.itemTrim).setVisible(false);
        }


        
        if (tab.getPosition() == 0) {
        	if (mMenu != null) {
        		mMenu.findItem(R.id.itemForward).setEnabled(true);
        	}
        	layout = R.layout.fragment_add_clips;
        	
        	if (mFragmentTab0 == null)
        	{
        		 try {
        			 mFragmentTab0 = new SceneChooserFragment(layout, fm, templateJsonPath);
        			 
     	            Bundle args = new Bundle(); 
     	            args.putInt(SceneChooserFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
     	            mFragmentTab0.setArguments(args);
     	            
     			} catch (IOException e) {
     				Log.e("SceneEditr","IO erorr", e);
     				
     			} catch (JSONException e) {
     				Log.e("SceneEditr","json error", e);
     				
     			}
        		 
        		 

        	        fm.beginTransaction()
        	        .add(R.id.container, mFragmentTab0, layout+"")
        	        .commit();

        	}
        	else
        	{

                fm.beginTransaction()
                .show(mFragmentTab0)
                .commit();
        	}
        	mLastTabFrag = mFragmentTab0;
        	
        } else if (tab.getPosition() == 1) {
            layout = R.layout.fragment_order_clips;

        	if (mMenu != null) {
	            mMenu.findItem(R.id.itemInfo).setVisible(true);
	            mMenu.findItem(R.id.itemTrim).setVisible(true);
		        mMenu.findItem(R.id.itemForward).setEnabled(true);
        	}
        	
        	if (mFragmentTab1 == null)
        	{
        		 try {
        			 mFragmentTab1 = new SceneChooserFragment(layout, fm, templateJsonPath);

     	            Bundle args = new Bundle(); 
     	            args.putInt(SceneChooserFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
     	            mFragmentTab1.setArguments(args);
     	            
     			} catch (IOException e) {
     				Log.e("SceneEditr","IO erorr", e);
     				
     			} catch (JSONException e) {
     				Log.e("SceneEditr","json error", e);
     				
     			}
        		 

        	        fm.beginTransaction()
        	        .add(R.id.container, mFragmentTab1, layout+"")
        	        .commit();
        		 
        	}
        	else
        	{

                fm.beginTransaction()
                .show(mFragmentTab1)
                .commit();
        	}
        	
        	mLastTabFrag = mFragmentTab1;

        } else if (tab.getPosition() == 2) {
            layout = R.layout.fragment_story_publish;
            mMenu.findItem(R.id.itemForward).setEnabled(false);
            
            if (mFragmentTab2 == null)
        	{
        		 try {
        			 mFragmentTab2 = new SceneChooserFragment(layout, fm, templateJsonPath);

     	            Bundle args = new Bundle(); 
     	            args.putInt(SceneChooserFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
     	            mFragmentTab2.setArguments(args);
     	            
     			} catch (IOException e) {
     				Log.e("SceneEditr","IO erorr", e);
     				
     			} catch (JSONException e) {
     				Log.e("SceneEditr","json error", e);
     				
     			}
        		 

        	        fm.beginTransaction()
        	        .add(R.id.container, mFragmentTab2, layout+"")
        	        .commit();

        	}
            else
            {

                fm.beginTransaction()
                .show(mFragmentTab2)
                .commit();
            }
            
        	mLastTabFrag = mFragmentTab2;

            
        }
        


        
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public class SceneChooserFragment extends Fragment {
    	private final static String TAG = "SceneChooserFragment";
        int layout;
        public ViewPager mAddClipsViewPager;
        View mView = null;
        public AddClipsPagerAdapter mAddClipsPagerAdapter;
        private FragmentManager mFm;
        private String mTemplatePath;
        
        /**
         * The sortable grid view that contains the clips to reorder on the Order tab
         */
        protected DraggableGridView mOrderClipsDGV;
        
        public SceneChooserFragment(int layout, FragmentManager fm, String templatePath) throws IOException, JSONException {
            this.layout = layout;
            mFm = fm;
            mTemplatePath = templatePath;
            
            mAddClipsPagerAdapter = new AddClipsPagerAdapter(fm, templatePath);
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        public void reloadClips ()  throws IOException, JSONException
        {
        	mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplatePath);
        	mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
            
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
            View view = inflater.inflate(layout, null);
            if (this.layout == R.layout.fragment_add_clips) {
            	
              // Set up the AddClips ViewPager with the AddClips adapter.
              mAddClipsViewPager = (ViewPager) view.findViewById(R.id.viewPager);
              mAddClipsViewPager.setPageMargin(-75);
              mAddClipsViewPager.setPageMarginDrawable(R.drawable.ic_action_forward_gray);
              mAddClipsViewPager.setOffscreenPageLimit(5);
              mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);

              
            } else if (this.layout == R.layout.fragment_order_clips) {
            	mOrderClipsDGV = (DraggableGridView) view.findViewById(R.id.DraggableGridView01);
            	mImageViewMedia = (ImageView) view.findViewById(R.id.imageView1);
            	
            	mPreviewVideoView = (PreviewVideoView) view.findViewById(R.id.previewVideoView);
            	final ImageView imageViewMedia = (ImageView) view.findViewById(R.id.imageView1);
            	
            	Media[] sceneMedias = mMPM.mProject.getMediaAsArray();

            	ImageView iv = new ImageView(getActivity());
            	if (sceneMedias[0] != null) {
						iv.setImageBitmap(getThumbnail(sceneMedias[0]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_close));
            	}
            	mOrderClipsDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[1] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[1]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_detail));
            	}
            	mOrderClipsDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[2] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[2]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_long));
            	}
            	mOrderClipsDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[3] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[3]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_medium));
            	} 
            	mOrderClipsDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[4] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[4]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_wide));
            	}
            	mOrderClipsDGV.addView(iv);
        		
            	mOrderClipsDGV.setOnRearrangeListener(new OnRearrangeListener() {
					
					@Override
					public void onRearrange(int oldIndex, int newIndex) {
						mMPM.mProject.swapMediaIndex(oldIndex, newIndex);
						//((SceneEditorNoSwipeActivity)mActivity).refreshClipPager();
						Log.d(TAG, "grid rearranged");
					}
				});
            	
            	mOrderClipsDGV.setOnItemClickListener(new OnItemClickListener() {
            		
            		@Override
        			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            			Log.d(TAG, "item clicked");
            			Media[] medias = mMPM.mProject.getMediaAsArray();
            			if (medias[position] != null) {
            				Bitmap bm = MediaUtils.getVideoFrame(medias[position].getPath(), -1);
            				imageViewMedia.setImageBitmap(bm);
            			} else {
            				TypedArray drawableIds = getActivity().getResources().obtainTypedArray(R.array.cliptype_thumbnails);
            				imageViewMedia.setImageResource(drawableIds.getResourceId(position, 0));
            			}
            			
            		}
				});
            	
            	Button playButton = (Button) view.findViewById(R.id.buttonPlay);
            	playButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO hide thumbnail
						mImageViewMedia.setVisibility(View.GONE);
						mPreviewVideoView.setVisibility(View.VISIBLE);
						// play
						String[] pathArray = mMPM.mProject.getMediaAsPathArray();
						mPreviewVideoView.setMedia(pathArray);
						mPreviewVideoView.play();
						
						// FIXME need to detect which clip user last clicked on and start from there
						// FIXME need to know when mPreviewVideoView is done playing so we can return the thumbnail
					}
				} );
            	
            	mPreviewVideoView.setCompletionCallback(new Runnable() {
					@Override
					public void run() {
						mImageViewMedia.setVisibility(View.VISIBLE);
						mPreviewVideoView.setVisibility(View.GONE);
					}
				});
            	
            } else if (this.layout == R.layout.fragment_story_publish) {
            	
            	EditText etTitle = (EditText)view.findViewById(R.id.etStoryTitle);
    			EditText etDesc = (EditText)view.findViewById(R.id.editTextDescribe);

    			etTitle.setText(mMPM.mProject.getTitle());
    			
            	Button btn = (Button)view.findViewById(R.id.btnPublish);
            	btn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						
	    				ServerManager sm = StoryMakerApp.getServerManager();
	    				sm.setContext(SceneEditorNoSwipeActivity.this);
	    				
	    				if (sm.hasCreds())
	    					handlePublish ();
	    				else
	    				{
	    					showLogin();
	    				}
					}
            		
            	});
            }
            return view;
        }
        
        private void showLogin ()
        {
        	startActivity(new Intent(mContext,LoginActivity.class));
        }

        /*
        @Override
        public void onResume() {
            super.onResume();
            if (this.layout == R.layout.fragment_add_clips) {
    
            } else if (this.layout == R.layout.fragment_order_clips) {
            } else if (this.layout == R.layout.fragment_story_publish) {
            }
        }*/
        
        
        private void handlePublish ()
    	{
			EditText etTitle = (EditText)findViewById(R.id.etStoryTitle);
			EditText etDesc = (EditText)findViewById(R.id.editTextDescribe);

			ToggleButton tbYouTube = (ToggleButton)findViewById(R.id.toggleButtonYoutube);
			ToggleButton tbStoryMaker = (ToggleButton)findViewById(R.id.toggleButtonStoryMaker);
			
			
			
			final boolean doYouTube = tbYouTube.isChecked();
			final boolean doStoryMaker = tbStoryMaker.isChecked();
			
			mHandlerPub.sendEmptyMessage(999);
			
			final String title = etTitle.getText().toString();
			final String desc = etDesc.getText().toString();
			String ytdesc = desc;
			if (ytdesc.length() == 0)
			{
				ytdesc = getActivity().getString(R.string.default_youtube_desc); //can't leave the description blank for YouTube
			}
			
			
			final YouTubeSubmit yts = new YouTubeSubmit(null, title, ytdesc, new Date(),SceneEditorNoSwipeActivity.this, mHandlerPub);
			
    		Thread thread = new Thread ()
    		{
    			public void run ()
    			{
    				
    				ServerManager sm = StoryMakerApp.getServerManager();
    				sm.setContext(SceneEditorNoSwipeActivity.this);
    				
    				Message msg = mHandlerPub.obtainMessage(888);
    				msg.getData().putString("status", "prerendering clips...");
    				mHandlerPub.sendMessage(msg);
    				
    				try {
    				
	    				mMPM.doExportMedia();
	    				
	    				MediaDesc mdExported = mMPM.getExportMedia();
	    				File videoFile = new File(mdExported.path);
	    				
	    				Message message = mHandlerPub.obtainMessage(777);
						message.getData().putString("fileVideo",mdExported.path);
						
	    				if (doYouTube)
	    				{
	    					yts.setVideoFile(videoFile,mdExported.mimeType);
	    					yts.getAuthTokenWithPermission(mYouTubeUsername);
	    					//yts.upload(mYouTubeUsername,new File(mdExported.path));
	    					
	    					while (yts.videoId == null)
	    					{
	    						try { Thread.sleep(1000); } catch (Exception e){}
	    					}
	    					
	    					if (doStoryMaker)
	    					{
	    						String descWithVideo = desc + "\n\n [youtube]" + yts.videoId + "[/youtube]";
	    					
	    						String postId = sm.post(title, descWithVideo);
							
	    						String urlPost = sm.getPostUrl(postId);
	    				
	    						message.getData().putString("urlPost", urlPost);
	    					}
	    					
							message.getData().putString("youtubeid", yts.videoId);
							
	    				}
    					
						mHandlerPub.sendMessage(message);
						
						
					} catch (XmlRpcFault e) {
						
						Message msgErr = new Message();
						msgErr.what = e.getErrorCode();
						msgErr.getData().putString("err", e.getLocalizedMessage());
						mHandlerPub.sendMessage(msgErr);
						Log.e(AppConstants.TAG,"error posting",e);
						
					}
    				catch (Exception e) {
						
						Message msgErr = new Message();
						msgErr.what = -1;
						msgErr.getData().putString("err", e.getLocalizedMessage());
						mHandlerPub.sendMessage(msgErr);
						Log.e(AppConstants.TAG,"error posting",e);
						
					}
    				
    			}
    		};
    		
    		thread.start();
    	}
        
       
        

        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the clips we are editing
         */
        public class AddClipsPagerAdapter extends FragmentStatePagerAdapter {


            private Template sTemplate;
            
            public AddClipsPagerAdapter(FragmentManager fm, String path) throws IOException, JSONException {
                super(fm);
              
                loadStoryTemplate(path);
            }


            private void loadStoryTemplate (String path) throws IOException, JSONException
            {
            	sTemplate = new Template();
            	sTemplate.parseAsset(mContext, path);
            	
            	
            	
            }
            
            @Override
            public Fragment getItem(int i) {
            	
            	Template.Clip clip = sTemplate.getClips().get(i);
            	
            	ArrayList<Media> lMedia = mMPM.mProject.getMediaAsList();
            	Media media = null;
            	
            	if (lMedia.size()>i)
            	{
            		media = lMedia.get(i);
            		
            	}
            	
            	Fragment fragment = new ClipThumbnailFragment(clip, i, media);
        		return fragment;
            }

            @Override
            public int getCount() {

                return sTemplate.getClips().size();
            }
            
            @Override 
            public int getItemPosition(Object object) {
            	return POSITION_NONE;
            }
        }
    }
    
    
    
    /**
     * ClipThumbnailFragment 
     */
    public class ClipThumbnailFragment extends Fragment {
    	
    	private Template.Clip clip;
    	private int mClipIndex;
    	private Media mMedia;
    	
        public ClipThumbnailFragment(Template.Clip clip, int clipIndex, Media media) {
        	this.clip = clip;
        	mClipIndex = clipIndex;
        	mMedia = media;
        }

        public static final String ARG_CLIP_TYPE_ID = "clip_type_id";

        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            
        	View view = inflater.inflate(R.layout.fragment_add_clips_page, null);

        	try {
        		
        		ImageView iv = (ImageView)view.findViewById(R.id.clipTypeImage);
	            
        		if (mMedia != null) {
        			
        			Bitmap thumb = getThumbnail(mMedia);
        			iv.setImageBitmap(thumb);
        			
        		} else {
	        		if (clip.mShotType != -1)
	        		{
	        			TypedArray drawableIds = getActivity().getResources().obtainTypedArray(R.array.cliptype_thumbnails);
		            
	        			int drawableId = drawableIds.getResourceId(clip.mShotType, 0); 
		            
	        			iv.setImageResource(drawableId);
	        		}
	        		else if (clip.mArtwork != null)
	        		{
	        			iv.setImageBitmap(BitmapFactory.decodeStream(getActivity().getAssets().open(clip.mArtwork)));
	        		}
        		}
        		
	            if (clip.mShotSize != null)
	            	((TextView)view.findViewById(R.id.clipTypeShotSize)).setText(clip.mShotSize);
	            
	            ((TextView)view.findViewById(R.id.clipTypeGoal)).setText(clip.mGoal);
	            ((TextView)view.findViewById(R.id.clipTypeDescription)).setText(clip.mDescription);
	            ((TextView)view.findViewById(R.id.clipTypeGoalLength)).setText(clip.mLength);
	            ((TextView)view.findViewById(R.id.clipTypeTip)).setText(clip.mTip);
	            ((TextView)view.findViewById(R.id.clipTypeSecurity)).setText(clip.mSecurity);
	            
	            iv.setOnClickListener(new OnClickListener()
	            {
	
					@Override
					public void onClick(View v) {
						ViewPager vp = (ViewPager) v.getParent().getParent().getParent().getParent();
						mMPM.mClipIndex = vp.getCurrentItem();
						
						openCaptureMode(clip, mClipIndex);
						
					}
	          	  
	            });
            
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            
            return view;
        }
    }

    public void refreshClipPager() {
    	if (mFragmentTab0 != null) {
    		try
    		{
    			mFragmentTab0.reloadClips();
    		}
    		catch (Exception e)
    		{
    			Log.e(AppConstants.TAG,"error reloading clips",e);
    		}
    	}
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void openCaptureMode (Clip clip, int clipIndex)
	{

		
		
		if (mStoryMode == Project.STORY_TYPE_AUDIO)
		{
			Intent i = new Intent(mContext, SoundRecorder.class);
			i.setType(CAPTURE_MIMETYPE_AUDIO);
			i.putExtra("mode", mStoryMode);
			mMPM.mClipIndex = clipIndex;
			startActivityForResult(i,mStoryMode);

		}
		else
		{
			
			//mMPM.mMediaHelper.openGalleryChooser("*/*");
			//mMPM.mMediaHelper.captureVideo(mContext.getExternalFilesDir(null));
			
			Intent i = new Intent(mContext, OverlayCameraActivity.class);
			i.putExtra("group", clip.mShotType);
			i.putExtra("mode", mStoryMode);
			mMPM.mClipIndex = clipIndex;
			startActivityForResult(i,REQ_OVERLAY_CAM);
		}
	}


	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent intent) {
		
		if (resCode == RESULT_OK)
		{
			//figure out what kind of media is being returned and add it to the project
			if (reqCode == REQ_OVERLAY_CAM)
			{
	    		File fileMediaFolder = getExternalFilesDir(null);
	    		
	    		if (mStoryMode == Project.STORY_TYPE_VIDEO)
	    		{
	    			mMPM.mMediaHelper.captureVideo(fileMediaFolder);
	    			
	    		}
	    		else if (mStoryMode == Project.STORY_TYPE_PHOTO)
	    		{
	    			mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
	    		}
	    		else if (mStoryMode == Project.STORY_TYPE_ESSAY)
	    		{
	    			mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
	    		}
		    	
			}
			else if (reqCode == Project.STORY_TYPE_AUDIO)
			{
				Uri uriAudio = intent.getData(); 
				mMPM.handleResponse(intent);
			}
			else
			{
				mMPM.handleResponse(intent);

			}
			
			this.refreshClipPager();
		}
	}
	
	public Bitmap getThumbnail (Media media)
	{
		String path = media.getPath();
		
		if (media.getMimeType() == null)
		{
			return null;
		}
		else if (media.getMimeType().startsWith("video"))
		{
			File fileThumb = new File(path + ".jpg");
			if (fileThumb.exists())
			{

				 final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
				return BitmapFactory.decodeFile(fileThumb.getAbsolutePath(),options);
			}
			else
			{
				Bitmap bmp = MediaUtils.getVideoFrame(path, -1);
			    try {
					bmp.compress(Bitmap.CompressFormat.JPEG, 70, new FileOutputStream(fileThumb));
				} catch (FileNotFoundException e) {
					Log.e(AppConstants.TAG,"could not cache video thumb",e);
				}
	
				return bmp;
			}
		}
		else if (media.getMimeType().startsWith("image"))
		{
			 final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			  
			return BitmapFactory.decodeFile(path, options);
		}
		else
		{
			return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_complete);
		}
	}
}
