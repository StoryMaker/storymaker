package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.template.Clip;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.template.Template;

import java.io.IOException;
import java.util.ArrayList;

import org.holoeverywhere.app.AlertDialog;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 */
@SuppressLint("ValidFragment")
public class AddClipsFragment extends Fragment {
    private final static String TAG = "AddClipsFragment";
    int layout;
    public ViewPager mAddClipsViewPager;
    View mView = null;
    public AddClipsPagerAdapter mAddClipsPagerAdapter;
    private FragmentManager mFm;
    private Template mTemplate;
    private EditorBaseActivity mActivity;
    private int mScene;

    public AddClipsFragment(int layout, FragmentManager fm, Template template, int scene, EditorBaseActivity activity)
            throws IOException, JSONException {
        this.layout = layout;
        mFm = fm;
        mTemplate = template;
        mActivity = activity;
        mScene = scene;
        
        mAddClipsPagerAdapter = new AddClipsPagerAdapter(fm, mTemplate, scene);
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
                    
                    if (((position+1) == mTemplate.getScene(mScene).getClips().size()) && positionOffset == 0 & positionOffsetPixels == 0)
                    {
                        mDragAtEnd++;
                        
                        if (mDragAtEnd > 5)
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
          
        }

    
        @Override
        public Fragment getItem(int i) {
            
            
            Clip clip = sTemplate.getScene(mScene).getClip(i);

            ArrayList<Media> lMedia = mActivity.mMPM.mScene.getMediaAsList();
            Media media = null;

            if (lMedia.size() > i)
            {
                media = lMedia.get(i);
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
}
