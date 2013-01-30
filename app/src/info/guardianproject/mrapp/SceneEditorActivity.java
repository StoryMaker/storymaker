
package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.media.OverlayCameraActivity;
import info.guardianproject.mrapp.model.Clip;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Template;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.server.SoundCloudUploader;
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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;

public class SceneEditorActivity extends BaseActivity implements ActionBar.TabListener {
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	private final static int REQ_OVERLAY_CAM = 888; //for resp handling from overlay cam launch
	private final static int REQ_SOUNDCLOUD = 999;
	
    protected boolean templateStory = false; 
    protected Menu mMenu = null;
    private Context mContext = null;
    private String templateJsonPath = null;
    private int mStoryMode = Project.STORY_TYPE_VIDEO;;
    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    private MediaProjectManager mMPM;
    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;
    public PublishFragment mPublishFragment;
	private PreviewVideoView mPreviewVideoView = null;
	private ImageView mImageViewMedia;
	
	private String mMediaUploadAccount = null;

    
    private ProgressDialog mProgressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        templateStory = intent.hasExtra("template_story");
        templateJsonPath = getIntent().getStringExtra("template_path");
        mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id

        mContext = getBaseContext();

        if (pid != -1)
        {
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, pid);
        }
        else
        {
            int clipCount = 5;

            String title = intent.getStringExtra("title");
        
            Project project = new Project(mContext, clipCount);
            project.setTitle(title);
            project.save();
            
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, project);

        }
        
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

    private Handler mHandlerPub = new Handler()
    {

		@Override
		public void handleMessage(Message msg) {
			
			String statusTitle = msg.getData().getString("statusTitle");
			String status = msg.getData().getString("status");

  	        String error = msg.getData().getString("error");
  	        if (error == null)
  	        	error = msg.getData().getString("err");
  	        
  	        int progress = msg.getData().getInt("progress");
  	        
  	        if (mProgressDialog != null && progress > 0)
  	        	mProgressDialog.setProgress(progress);
			
			switch (msg.what)
			{
				case 0:
				case 1:
					
					if (status != null)
					{
						if (mProgressDialog != null)
						{
							if (statusTitle != null)
								mProgressDialog.setTitle(statusTitle);
							
							mProgressDialog.setMessage(status);
						}
						else
						{
							Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
							
						}
					}
				break;
				
				case 999:
					
						mProgressDialog = new ProgressDialog(SceneEditorActivity.this);
	          		    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	          		    mProgressDialog.setTitle(getString(R.string.rendering));
	          		    mProgressDialog.setMessage(getString(R.string.rendering_project_));
	          		    mProgressDialog.setCancelable(true);
	          		    mProgressDialog.show();
					
				break;
				
				case 888:
	          		  mProgressDialog.setMessage(status);
	            break;
				case 777:
					
		  	        String videoId = msg.getData().getString("youtubeid");
		  	        String url = msg.getData().getString("urlPost");
		  	        String localPath = msg.getData().getString("fileMedia");
		  	        String mimeType = msg.getData().getString("mime");
		  	        
					mProgressDialog.dismiss();
					mProgressDialog = null;
					
					showPublished(url,new File(localPath),videoId,mimeType);
					
					
				break;
				case -1:
					Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
					if (mProgressDialog != null)
					{
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
					 
				break;
				default:
				
					
			}
			
			
		}
    	
    };

    public void showPublished(final String postUrl, final File localMedia, final String youTubeId,
            final String mimeType)
    {
        if (youTubeId != null || postUrl != null)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:

                            String urlOnline = postUrl;

                            if (youTubeId != null)
                                urlOnline = "https://www.youtube.com/watch?v=" + youTubeId;

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(urlOnline));
                            startActivity(i);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                            mMPM.mMediaHelper.playMedia(localMedia, mimeType);

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.view_published_media_online_or_local_copy_)
                    .setPositiveButton(R.string.youtube, dialogClickListener)
                    .setNegativeButton(R.string.local, dialogClickListener).show();
        }
        else
        {

            mMPM.mMediaHelper.playMedia(localMedia, mimeType);

        }

    }

    private void showHelp() {

        Toast.makeText(this, getString(R.string.help_clip_select), Toast.LENGTH_LONG).show();
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
        super.onCreateOptionsMenu(menu);

        getSupportMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        mMenu = menu;
        
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
                if (idx < 2) {
                    getSupportActionBar().setSelectedNavigationItem(Math.min(2, idx + 1));
                } else {
                    mPublishFragment.doPublish();
                }
                return true;
            case R.id.addFromGallery:
                addMediaFromGallery();
            
                return true;
            case R.id.addNewShot:
             
                addShotToScene();
                
                return true;
                
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void addShotToScene ()
    {
        try
        {
            Clip tClip = new Clip();
            tClip.setDefaults();
            AddClipsFragment acf = ((AddClipsFragment)mFragmentTab0);
            acf.addTemplateClip(tClip);
        }
        catch (Exception e)
        {
            Log.e(AppConstants.TAG,"error adding new clip",e);
        }
    }

    private void addMediaFromGallery()
    {
        mMPM.mMediaHelper.openGalleryChooser("*/*");
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().hide(mLastTabFrag).commit();
    }

    // protected void setupAddClipsFragment() {
    // FragmentManager fm = getSupportFragmentManager();
    //
    // try {
    // mFragmentTab0 = new SceneChooserFragment(R.layout.fragment_add_clips, fm,
    // templateJsonPath);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (JSONException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, show the tab contents in the
        // container
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
                    mFragmentTab0 = new AddClipsFragment(layout, fm, templateJsonPath);

                    Bundle args = new Bundle();
                    args.putInt(AddClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mFragmentTab0.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);
                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);
                }

                fm.beginTransaction()
                        .add(R.id.container, mFragmentTab0, layout + "")
                        .commit();

            } else {
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
                    mFragmentTab1 = new OrderClipsFragment(layout);

                    Bundle args = new Bundle();
                    args.putInt(OrderClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mFragmentTab1.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);

                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);

                }

                fm.beginTransaction()
                        .add(R.id.container, mFragmentTab1, layout + "")
                        .commit();

            } else {

                ((OrderClipsFragment)mFragmentTab1).loadMedia();
                
                fm.beginTransaction()
                        .show(mFragmentTab1)
                        .commit();
            }

            mLastTabFrag = mFragmentTab1;

        } else if (tab.getPosition() == 2) {
            layout = R.layout.fragment_story_publish;
            
            if (mPublishFragment == null)
            {
                try {
                    mPublishFragment = new PublishFragment(layout);

                    Bundle args = new Bundle();
                    args.putInt(PublishFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mPublishFragment.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);
                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);
                }
                fm.beginTransaction()
                        .add(R.id.container, mPublishFragment, layout + "")
                        .commit();

            } else {

                fm.beginTransaction()
                        .show(mPublishFragment)
                        .commit();
            }

            mLastTabFrag = mPublishFragment;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class AddClipsFragment extends Fragment {
        private final static String TAG = "AddClipsFragment";
        int layout;
        public ViewPager mAddClipsViewPager;
        View mView = null;
        public AddClipsPagerAdapter mAddClipsPagerAdapter;
        private FragmentManager mFm;
        private String mTemplatePath;
        private Template mTemplate;

        public AddClipsFragment(int layout, FragmentManager fm, String templatePath)
                throws IOException, JSONException {
            this.layout = layout;
            mFm = fm;
            mTemplatePath = templatePath;
            
            initTemplate();

            mAddClipsPagerAdapter = new AddClipsPagerAdapter(fm, mTemplate);
        }
        
        public void initTemplate ()  throws IOException, JSONException 
        {
            int count = mMPM.mProject.getClipCount();
            
            mTemplate = new Template();
            mTemplate.parseAsset(mContext, mTemplatePath);
            
            while (mTemplate.getClips().size() < count)
            {
                Clip clip = new Clip();
                clip.setDefaults();
                mTemplate.addClip(clip);
            }
        }
        
        public Template getTemplate ()
        {
            return mTemplate;
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        public void reloadClips() throws IOException, JSONException {
            
            int cItemIdx = mAddClipsViewPager.getCurrentItem();
            
//            initTemplate();

            mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplate);
            mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
            
            mAddClipsViewPager.setCurrentItem(cItemIdx);
        }
        
        public void addTemplateClip (Clip clip) throws IOException, JSONException
        {
            mTemplate.addClip(clip);
            mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplate);
            mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
            
            mAddClipsViewPager.setCurrentItem(mTemplate.getClips().size()-1);
            mMPM.mClipIndex = mTemplate.getClips().size()-1;
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
                //mAddClipsViewPager.setOffscreenPageLimit(5);
                
                mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
                
                mAddClipsViewPager.setOnPageChangeListener(new OnPageChangeListener()
                {
                    int mDragAtEnd = 0;
                    
                    @Override
                    public void onPageScrollStateChanged(int state) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        
                        if (((position+1) == mTemplate.getClips().size()) && positionOffset == 0 & positionOffsetPixels == 0)
                        {
                            mDragAtEnd++;
                            
                            if (mDragAtEnd > 5)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SceneEditorActivity.this);
                                builder.setMessage(R.string.add_new_clip_to_the_scene_)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                addShotToScene();
                                                
                                            }
                                        })
                                        .setNegativeButton(R.string.no, null).show();
                                
                               
                                mDragAtEnd = 0;
                            }
                        }
                        else
                        {
                            mDragAtEnd = 0;
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        mMPM.mClipIndex = position;
                    }
                });
            }
            return view;
        }

        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding
         * to the clips we are editing
         */
        public class AddClipsPagerAdapter extends FragmentStatePagerAdapter {
            private Template sTemplate;

            public AddClipsPagerAdapter(FragmentManager fm, Template template) throws IOException,
                    JSONException {
                super(fm);
                sTemplate = template;
                        
              
            }

        
            @Override
            public Fragment getItem(int i) {
                
                
                Clip clip = sTemplate.getClips().get(i);

                ArrayList<Media> lMedia = mMPM.mProject.getMediaAsList();
                Media media = null;

                if (lMedia.size() > i)
                {
                    media = lMedia.get(i);
                }
                
                Fragment fragment = new AddClipsThumbnailFragment(clip, i, media);
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
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class OrderClipsFragment extends Fragment {
        private final static String TAG = "OrderClipsFragment";
        int layout;
        public ViewPager mAddClipsViewPager;
        View mView = null;

        /**
         * The sortable grid view that contains the clips to reorder on the
         * Order tab
         */
        protected DraggableGridView mOrderClipsDGV;

        public OrderClipsFragment(int layout)
                throws IOException, JSONException {
            this.layout = layout;
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View view = inflater.inflate(layout, null);
            
            mOrderClipsDGV = (DraggableGridView) view.findViewById(R.id.DraggableGridView01);
            mImageViewMedia = (ImageView) view.findViewById(R.id.imageView1);

            mPreviewVideoView = (PreviewVideoView) view.findViewById(R.id.previewVideoView);
            
            Button playButton = (Button) view.findViewById(R.id.buttonPlay);
            playButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    
                    if (mPreviewVideoView.isPlaying())
                    {
                 //       mPreviewVideoView.pause();

                        mPreviewVideoView.stopPlayback();
                    }
                    else
                    {
                     // TODO hide thumbnail
                        mImageViewMedia.setVisibility(View.GONE);
                        mPreviewVideoView.setVisibility(View.VISIBLE);
                        // play
                        String[] pathArray = mMPM.mProject.getMediaAsPathArray();
                        mPreviewVideoView.setMedia(pathArray);
                        mPreviewVideoView.play();
                    }
                    
                    // FIXME need to detect which clip user last clicked on
                    // and start from there
                    // FIXME need to know when mPreviewVideoView is done
                    // playing so we can return the thumbnail
                }
            });

            mPreviewVideoView.setCompletionCallback(new Runnable() {
                @Override
                public void run() {
                    mImageViewMedia.setVisibility(View.VISIBLE);
                    mPreviewVideoView.setVisibility(View.GONE);
                }
            });
            
           
            loadMedia();
            
            return view;
        }
        
        public void loadMedia ()
        {
            mOrderClipsDGV.removeAllViews();
            
            Media[] sceneMedias = mMPM.mProject.getMediaAsArray();

            for (int i = 0; i < sceneMedias.length; i++)
            {
                ImageView iv = new ImageView(getActivity());
                if (sceneMedias[i] != null) {
                    iv.setImageBitmap(getThumbnail(sceneMedias[i]));
                } 
                else
                {
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.thumb_incomplete));
                }
                
                mOrderClipsDGV.addView(iv);
            }
            
            mOrderClipsDGV.setOnRearrangeListener(new OnRearrangeListener() {

                @Override
                public void onRearrange(int oldIndex, int newIndex) {
                    mMPM.mProject.swapMediaIndex(oldIndex, newIndex);
                 
                }
            });

            mOrderClipsDGV.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "item clicked");
                    Media[] medias = mMPM.mProject.getMediaAsArray();
                    if (medias[position] != null) {
                        
                        mImageViewMedia.setVisibility(View.GONE);
                        mPreviewVideoView.setVisibility(View.VISIBLE);
                        // play
                        mPreviewVideoView.stopPlayback();
                        String[] pathArray = {medias[position].getPath()};
                        mPreviewVideoView.setMedia(pathArray);
                        mPreviewVideoView.play();

                    }

                }
            });
            
          
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class PublishFragment extends Fragment {
        private final static String TAG = "PublishFragment";
        int layout;
        public ViewPager mAddClipsViewPager;
        View mView = null;

        /**
         * The sortable grid view that contains the clips to reorder on the
         * Order tab
         */
        protected DraggableGridView mOrderClipsDGV;

        public PublishFragment(int layout)
                throws IOException, JSONException {
            this.layout = layout;
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View view = inflater.inflate(layout, null);
            if (this.layout == R.layout.fragment_story_publish) {
                EditText etTitle = (EditText) view.findViewById(R.id.etStoryTitle);
                EditText etDesc = (EditText) view.findViewById(R.id.editTextDescribe);

                etTitle.setText(mMPM.mProject.getTitle());

                ToggleButton tbYouTube = (ToggleButton) view.findViewById(R.id.toggleButtonYoutube);

                tbYouTube.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {

                        if (isChecked) {
                            checkYouTubeAccount();
                        }

                    }

                });

                ToggleButton tbStoryMaker = (ToggleButton) view
                        .findViewById(R.id.toggleButtonStoryMaker);

                tbStoryMaker.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {

                        if (isChecked)
                        {
                            ServerManager sm = StoryMakerApp.getServerManager();
                            sm.setContext(SceneEditorActivity.this);

                            if (!sm.hasCreds())
                                showLogin();
                        }

                    }

                });

                final PublishFragment that = this;
                Button btn = (Button) view.findViewById(R.id.btnPublish);
                btn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        that.doPublish();
                    }
                });
            }
            return view;
        }
        
        public void doPublish() {
            ServerManager sm = StoryMakerApp.getServerManager();
            sm.setContext(SceneEditorActivity.this);

            if (sm.hasCreds()) {
                handlePublish();
            }
            else {
                showLogin();
            }
        }

        private void showLogin() {
            startActivity(new Intent(mContext, LoginActivity.class));
        }

        private void checkYouTubeAccount() {
            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(SceneEditorActivity.this);
            mMediaUploadAccount = settings.getString("youTubeUserName", null);

            if (mMediaUploadAccount == null) {
                AccountManager accountManager = AccountManager.get(mContext);
                final Account[] accounts = accountManager.getAccounts();

                if (accounts.length > 0) {
                    String[] accountNames = new String[accounts.length];
                    for (int i = 0; i < accounts.length; i++) {
                        accountNames[i] = accounts[i].name + " (" + accounts[i].type + ")";
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(SceneEditorActivity.this);
                    builder.setTitle(R.string.choose_account_for_youtube_upload);
                    builder.setItems(accountNames, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            mMediaUploadAccount = accounts[item].name;
                            // SharedPreferences settings =
                            // PreferenceManager.getDefaultSharedPreferences(SceneEditorNoSwipeActivity.this);
                            // settings.edit().putString("youTubeUserName",
                            // mYouTubeUsername);
                            // settings.edit().commit();
                        }
                    }).show();
                }
            }
        }

        private String processTitle(String title) {
            String result = title;
            result = result.replace(' ', '_');
            result = result.replace('!', '_');
            result = result.replace('/', '_');
            result = result.replace('!', '_');
            result = result.replace('#', '_');
            result = result.replace('"', '_');
            result = result.replace('\'', '_');
            return result;
        }

        private void handlePublish() {
            EditText etTitle = (EditText) findViewById(R.id.etStoryTitle);
            EditText etDesc = (EditText) findViewById(R.id.editTextDescribe);

            ToggleButton tbYouTube = (ToggleButton) findViewById(R.id.toggleButtonYoutube);

            ToggleButton tbStoryMaker = (ToggleButton) findViewById(R.id.toggleButtonStoryMaker);

            // final String exportFileName =
            // processTitle(mMPM.mProject.getTitle()) + "-export-" + new
            // Date().getTime();
            final String exportFileName = mMPM.mProject.getId() + "-export-" + new Date().getTime();

            final boolean doYouTube = tbYouTube.isChecked();
            final boolean doStoryMaker = tbStoryMaker.isChecked();

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
                    SceneEditorActivity.this, mHandlerPub);

            Thread thread = new Thread() {
                public void run() {
                    ServerManager sm = StoryMakerApp.getServerManager();
                    sm.setContext(SceneEditorActivity.this);

                    Message msg = mHandlerPub.obtainMessage(888);
                    msg.getData().putString("status",
                            getActivity().getString(R.string.rendering_clips_));
                    mHandlerPub.sendMessage(msg);

                    try {
                        mMPM.doExportMedia(exportFileName, doYouTube);
                        MediaDesc mdExported = mMPM.getExportMedia();
                        File mediaFile = new File(mdExported.path);

                        if (mediaFile.exists()) {

                            Message message = mHandlerPub.obtainMessage(777);
                            message.getData().putString("fileMedia", mdExported.path);
                            message.getData().putString("mime", mdExported.mimeType);

                            if (doYouTube) {

                                String mediaEmbed = "";

                                if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO) {
                                    msg = mHandlerPub.obtainMessage(888);
                                    msg.getData().putString("statusTitle",
                                            getActivity().getString(R.string.uploading));
                                    msg.getData().putString("status", getActivity().getString(
                                            R.string.connecting_to_youtube_));
                                    mHandlerPub.sendMessage(msg);

                                    yts.setVideoFile(mediaFile, mdExported.mimeType);
                                    yts.getAuthTokenWithPermission(mMediaUploadAccount);
                                    // yts.upload(mYouTubeUsername,new
                                    // File(mdExported.path));

                                    while (yts.videoId == null) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (Exception e) {
                                        }
                                    }

                                    mediaEmbed = "[youtube]" + yts.videoId + "[/youtube]";

                                    message.getData().putString("youtubeid", yts.videoId);
                                }
                                else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
                                    boolean installed = SoundCloudUploader
                                            .isCompatibleSoundCloudInstalled(mContext);

                                    if (installed) {
                                        String scurl = SoundCloudUploader.buildSoundCloudURL(
                                                mMediaUploadAccount, mediaFile, title);
                                        mediaEmbed = "[soundcloud]" + scurl + "[/soundcloud]";

                                        SoundCloudUploader.uploadSound(mediaFile, title, desc,
                                                REQ_SOUNDCLOUD, SceneEditorActivity.this);

                                    }
                                    else {
                                        SoundCloudUploader.installSoundCloud(mContext);
                                    }
                                }
                                else {
                                    String murl = sm.addMedia(mdExported.mimeType, mediaFile);
                                    mediaEmbed = murl;
                                }

                                if (doStoryMaker) {
                                    String descWithMedia = desc + "\n\n" + mediaEmbed;
                                    String postId = sm.post(title, descWithMedia);
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

    /**
     * ClipThumbnailFragment
     */
    public class AddClipsThumbnailFragment extends Fragment {
    	
    	private Clip clip;
    	private int mClipIndex;
    	private Media mMedia;
    	
        public AddClipsThumbnailFragment(Clip clip, int clipIndex, Media media) {
        	this.clip = clip;
        	mClipIndex = clipIndex;
        	mMedia = media;
        }

        public static final String ARG_CLIP_TYPE_ID = "clip_type_id";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_add_clips_page, null);

            try {

                ImageView iv = (ImageView) view.findViewById(R.id.clipTypeImage);

                if (mMedia != null) {

                    Bitmap thumb = getThumbnail(mMedia);
                    iv.setImageBitmap(thumb);

                } else {
                    if (clip.mShotType != -1)
                    {
                        TypedArray drawableIds = getActivity().getResources().obtainTypedArray(
                                R.array.cliptype_thumbnails);

                        int drawableId = drawableIds.getResourceId(clip.mShotType, 0);

                        iv.setImageResource(drawableId);
                    }
                    else if (clip.mArtwork != null)
                    {
                        iv.setImageBitmap(BitmapFactory.decodeStream(getActivity().getAssets()
                                .open(clip.mArtwork)));
                    }
                }


                ((TextView) view.findViewById(R.id.clipTypeTitle)).setText(clip.mTitle);
                
                if (clip.mShotSize != null)
                    ((TextView) view.findViewById(R.id.clipTypeShotSize)).setText(clip.mShotSize);
                else
                    ((TextView) view.findViewById(R.id.clipTypeShotSize)).setVisibility(View.GONE);
                
                ((TextView) view.findViewById(R.id.clipTypeGoal)).setText(clip.mGoal);
                ((TextView) view.findViewById(R.id.clipTypeDescription)).setText(clip.mDescription);
                ((TextView) view.findViewById(R.id.clipTypeGoalLength)).setText(clip.mLength);
                ((TextView) view.findViewById(R.id.clipTypeTip)).setText(clip.mTip);
                ((TextView) view.findViewById(R.id.clipTypeSecurity)).setText(clip.mSecurity);

                iv.setOnClickListener(new OnClickListener()
                {

                    @Override
                    public void onClick(View v) {
                        ViewPager vp = (ViewPager) v.getParent().getParent().getParent()
                                .getParent();
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
            try {
                ((AddClipsFragment) mFragmentTab0).reloadClips();
            } 
            catch (Exception e) {
                Log.e(AppConstants.TAG, "error reloading clips", e);
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

            // mMPM.mMediaHelper.openGalleryChooser("*/*");
            // mMPM.mMediaHelper.captureVideo(mContext.getExternalFilesDir(null));

            Intent i = new Intent(mContext, OverlayCameraActivity.class);
            i.putExtra("group", clip.mShotType);
            i.putExtra("mode", mStoryMode);
            mMPM.mClipIndex = clipIndex;
            startActivityForResult(i, REQ_OVERLAY_CAM);
        }
    }

    private File mCapturePath;

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent) {

        if (resCode == RESULT_OK)
        {
            if (reqCode == REQ_OVERLAY_CAM)
            {
                File fileMediaFolder = getExternalFilesDir(null);

                if (mStoryMode == Project.STORY_TYPE_VIDEO)
                {
                    mCapturePath = mMPM.mMediaHelper.captureVideo(fileMediaFolder);

                }
                else if (mStoryMode == Project.STORY_TYPE_PHOTO)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }
                else if (mStoryMode == Project.STORY_TYPE_ESSAY)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }

            }
            else
            {
                mMPM.handleResponse(intent, mCapturePath);

                refreshClipPager();
                
            }

        }
    }

    public Bitmap getThumbnail(Media media)
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
                return BitmapFactory.decodeFile(fileThumb.getAbsolutePath(), options);
            }
            else
            {
                Bitmap bmp = MediaUtils.getVideoFrame(path, -1);
                try {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, new FileOutputStream(fileThumb));
                } catch (FileNotFoundException e) {
                    Log.e(AppConstants.TAG, "could not cache video thumb", e);
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

    @Override
    protected void onResume() {
        super.onResume();
        
        
    }
}
