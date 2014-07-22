package org.codeforafrica.timby.listeningpost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.template.Clip;
import org.codeforafrica.timby.listeningpost.model.template.Template;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 */
@SuppressLint("ValidFragment")
public class AddClipsFragment extends Fragment {
    private final static String TAG = "AddClipsFragment";
    public ViewPager mAddClipsViewPager;
    View mView = null;
    public AddClipsPagerAdapter mAddClipsPagerAdapter;
    private FragmentManager mFm;
    private Template mTemplate;
    private EditorBaseActivity mActivity;
    private int mScene;

    private void initAddClips() throws IOException, JSONException
    {
    	mActivity = (EditorBaseActivity)getActivity();
        
    	mTemplate = mActivity.getTemplate();
        
        mFm = getFragmentManager();
        
        mScene = getArguments().getInt("scene");
        mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplate, mScene);
        
        
    }
    
    
    public Template getTemplate ()
    {
        return mTemplate;
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    public void reloadClips() throws IOException, JSONException {
        
        int cItemIdx = mAddClipsViewPager.getCurrentItem();
        
//        initTemplate();

        mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplate, mScene);
        mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
        
        mAddClipsViewPager.setCurrentItem(cItemIdx);
    }
    
    // only gets called from addShotToScene
    public void addTemplateClip (Clip clip) throws IOException, JSONException
    {
        mTemplate.getScene(mScene).addClip(clip); 
        mAddClipsPagerAdapter = new AddClipsPagerAdapter(mFm, mTemplate, mScene);
        mAddClipsViewPager.setAdapter(mAddClipsPagerAdapter);
        
        mAddClipsViewPager.setCurrentItem(mTemplate.getScene(mScene).getClips().size()-1); 
        mActivity.mMPM.mClipIndex = mTemplate.getScene(mScene).getClips().size()-1; 
        
        mActivity.mdExported = null;
        
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	try
    	{
    		if (mActivity == null)
    			initAddClips();
    	}
    	catch (Exception e)
    	{
    		Log.e(AppConstants.TAG,"error creating add clips view",e);
    		return null;
    	}
    	
    	int layout = getArguments().getInt("layout");
        View view = inflater.inflate(layout, null);
        if (layout == R.layout.fragment_add_clips) {

            // Set up the AddClips ViewPager with the AddClips adapter.
            mAddClipsViewPager = (ViewPager) view.findViewById(R.id.viewPager);
            //mAddClipsViewPager.setPageMargin(-75);
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
                    
                    if (((position+1) == mTemplate.getScene(mScene).getClips().size()) && positionOffset == 0 & positionOffsetPixels == 0)
                    {
                        mDragAtEnd++;
                        
                        if (mDragAtEnd > mTemplate.getScene(mScene).getClips().size())
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                            builder.setMessage(R.string.add_new_clip_to_the_scene_)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ((SceneEditorActivity)mActivity).addShotToScene();
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
                    mActivity.mMPM.mClipIndex = position;
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

        private int mScene;
        
        public AddClipsPagerAdapter(FragmentManager fm, Template template, int scene) throws IOException,
                JSONException {
            super(fm);
            sTemplate = template;
            mScene = scene;
            
            ArrayList<Media> lMedia = mActivity.mMPM.mScene.getMediaAsList();

            while (lMedia.size() > sTemplate.getScene(mScene).getClips().size())
            {
            	Clip tClip = new Clip();
                tClip.setDefaults();
                mTemplate.getScene(mScene).addClip(tClip); 

            }
        }            
        
        @Override
        public Fragment getItem(int i) {
            
            
            Clip clip = sTemplate.getScene(mScene).getClip(i);

            ArrayList<Media> lMedia = mActivity.mMPM.mScene.getMediaAsList();
            Media media = null;

            if (lMedia.size() > i)
            {
                media = lMedia.get(i);
                //add to queue for encryption if not added
                try {
					addToQ(media);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            Fragment fragment = new AddClipsThumbnailFragment(clip, i, media, mActivity);
            return fragment;
        }
        
        @Override
        public int getCount() {
            return sTemplate.getScene(mScene).getClips().size();
            
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
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
        		bitThumb.compress(Bitmap.CompressFormat.JPEG, 30, out);
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


}
