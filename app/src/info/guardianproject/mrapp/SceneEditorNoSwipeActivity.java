package info.guardianproject.mrapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.micode.soundrecorder.SoundRecorder;

import org.ffmpeg.android.MediaUtils;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;

import info.guardianproject.mrapp.media.MediaClip;
import info.guardianproject.mrapp.media.MediaHelper;
import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.media.OverlayCameraActivity;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Template;
import info.guardianproject.mrapp.model.Template.Clip;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.ui.MediaView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

import com.WazaBe.HoloEverywhere.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;

public class SceneEditorNoSwipeActivity extends com.WazaBe.HoloEverywhere.sherlock.SActivity implements ActionBar.TabListener {


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
        
        mMPM = new MediaProjectManager(this, mContext, getIntent());

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
        public ViewPager mClipViewPager;
        View mView = null;
        public ClipPagerAdapter mClipPagerAdapter;
        private FragmentManager mFm;
        private String mTemplatePath;
        
        /**
         * The sortable grid view that contains the clips to reorder on the Order tab
         */
        protected DraggableGridView mDGV;
        
        public SceneChooserFragment(int layout, FragmentManager fm, String templatePath) throws IOException, JSONException {
            this.layout = layout;
            mFm = fm;
            mTemplatePath = templatePath;
            
            mClipPagerAdapter = new ClipPagerAdapter(fm, templatePath);
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        public void reloadClips ()  throws IOException, JSONException
        {
            mClipPagerAdapter = new ClipPagerAdapter(mFm, mTemplatePath);
            mClipViewPager.setAdapter(mClipPagerAdapter);
            
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
            View view = inflater.inflate(layout, null);
            if (this.layout == R.layout.fragment_add_clips) {
            	
              // Set up the clip ViewPager with the clip adapter.
              mClipViewPager = (ViewPager) view.findViewById(R.id.viewPager);
              mClipViewPager.setPageMargin(-75);
              mClipViewPager.setPageMarginDrawable(R.drawable.ic_action_forward_gray);
              mClipViewPager.setOffscreenPageLimit(5);
              mClipViewPager.setAdapter(mClipPagerAdapter);

              
            } else if (this.layout == R.layout.fragment_order_clips) {
            	mDGV = (DraggableGridView) view.findViewById(R.id.DraggableGridView01);
            	
            	Media[] sceneMedias = mMPM.mProject.getMediaAsArray();

            	ImageView iv = new ImageView(getActivity());
            	if (sceneMedias[0] != null) {
						iv.setImageBitmap(getThumbnail(sceneMedias[0]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_close));
            	}
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[1] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[1]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_detail));
            	}
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[2] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[2]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_long));
            	}
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[3] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[3]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_medium));
            	} 
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	if (sceneMedias[4] != null) {
            		iv.setImageBitmap(getThumbnail(sceneMedias[4]));
            	} else { 
            		iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptype_wide));
            	}
            	mDGV.addView(iv);
        		
            	mDGV.setOnRearrangeListener(new OnRearrangeListener() {
					
					@Override
					public void onRearrange(int oldIndex, int newIndex) {
						// TODO Auto-generated method stub
						Log.d(TAG, "grid rearranged");
					}
				});
            	
            	mDGV.setOnItemClickListener(new OnItemClickListener() {
            		
            		@Override
        			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            			Log.d(TAG, "item clicked");
            			
            		}
				});
            } else if (this.layout == R.layout.fragment_story_publish) {
            	
            	Button btn = (Button)view.findViewById(R.id.btnPublish);
            	btn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						
						handlePublish ();
					}
            		
            	});
            }
            return view;
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
    		Thread thread = new Thread ()
    		{
    			public void run ()
    			{
    				EditText et = (EditText)findViewById(R.id.editTextDescribe);
    				ServerManager sm = StoryMakerApp.getServerManager();
    				
    				String title = mMPM.mProject.getTitle();
    				String desc = et.getText().toString();
    				
    				mMPM.doExportMedia();
    				
    			
    				
    				try {
    					
						sm.post(title, desc);
						
						
						mHandlerPub.sendEmptyMessage(0);
						
					} catch (XmlRpcFault e) {
						
						Message msgErr = new Message();
						msgErr.what = e.getErrorCode();
						msgErr.getData().putString("err", e.getLocalizedMessage());
						mHandlerPub.sendMessage(msgErr);
						Log.e(AppConstants.TAG,"error posting",e);
						
					}
    				
    			}
    		};
    		
    		thread.start();
    	}
        
        private Handler mHandlerPub = new Handler ()
        {

			@Override
			public void handleMessage(Message msg) {
				
				switch (msg.what)
				{
					case 0:
						
						((TextView)findViewById(R.id.textViewStatus)).setText("PUBLISHED!");
						((Button)findViewById(R.id.btnPublish)).setEnabled(false);
						
					break;
					
					default:
						
						//err
						Toast.makeText(SceneEditorNoSwipeActivity.this, msg.getData().getString("err"), Toast.LENGTH_LONG).show();
				}
			}
        	
        };
        
        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the clips we are editing
         */
        public class ClipPagerAdapter extends FragmentPagerAdapter {


            private Template sTemplate;
            
            public ClipPagerAdapter(FragmentManager fm, String path) throws IOException, JSONException {
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
            	Media media = mMPM.mProject.getMediaAsArray()[i];
                Fragment fragment = new ClipThumbnailFragment(clip, i, media);
                return fragment;
            }

            @Override
            public int getCount() {
                return 5;
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
					//	int cIdx = mClipViewPager.getCurrentItem();

						ViewPager vp = (ViewPager) v.getParent().getParent().getParent().getParent();
						mMPM.clipIndex = vp.getCurrentItem();
						
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

//    public void addMediaViewToClipPager(int clipIndex, MediaView mv) {
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
			mMPM.clipIndex = clipIndex;
			startActivityForResult(i,mStoryMode);

		}
		else
		{
			
			//mMPM.mMediaHelper.openGalleryChooser("*/*");
			//mMPM.mMediaHelper.captureVideo(mContext.getExternalFilesDir(null));
			
			Intent i = new Intent(mContext, OverlayCameraActivity.class);
			i.putExtra("group", clip.mShotType);
			i.putExtra("mode", mStoryMode);
			mMPM.clipIndex = clipIndex;
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
				return BitmapFactory.decodeFile(fileThumb.getAbsolutePath());
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
