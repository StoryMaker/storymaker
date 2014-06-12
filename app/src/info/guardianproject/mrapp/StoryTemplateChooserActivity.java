package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;
import info.guardianproject.mrapp.model.template.Template;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.CirclePageIndicator;

public class StoryTemplateChooserActivity extends BaseActivity {
    private String mTemplatePath;
    private Template mTemplate;
    private String mProjectName;
    private int mStoryMode;
    private String mStoryModeTemplate;
    private int rid;

	private ViewPager mPager = null;
	private MyAdapter mAdapter = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_template_chooser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        mProjectName = intent.getStringExtra("project_title");
        mStoryMode = intent.getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);
        mStoryModeTemplate = intent.getStringExtra("story_mode_template");
        rid = intent.getIntExtra("rid", 0);
        
        int[] title = {
        	R.string.story_type_event_news_title,
        	R.string.story_type_event_feature_title,
        	R.string.story_type_event_breaking_title,
        	R.string.story_type_issue_social_title,
        	R.string.story_type_issue_political_title,
        	R.string.story_type_issue_economic_title,
        	R.string.story_type_profile_public_title,
        	R.string.story_type_profile_professional_title,
        	R.string.story_type_profile_personal_title
        };
        
        int[] messages = {
        	R.string.story_type_event_news_desc,
        	R.string.story_type_event_feature_desc,
        	R.string.story_type_event_breaking_desc,
        	R.string.story_type_issue_social_desc,
        	R.string.story_type_issue_political_desc,
        	R.string.story_type_issue_economic_desc,
        	R.string.story_type_profile_public_desc,
        	R.string.story_type_profile_professional_desc,
        	R.string.story_type_profile_personal_desc
        };
        
        mAdapter = new MyAdapter(getSupportFragmentManager(), title, messages);
		mPager = ((ViewPager)findViewById(R.id.pager));
		mPager.setId((int)(Math.random()*10000));
		mPager.setOffscreenPageLimit(5);
		
		 mPager.setAdapter(mAdapter);
		 
		//Bind the title indicator to the adapter
         CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.circles);
         indicator.setViewPager(mPager);
         indicator.setSnap(true);
         
         final float density = getResources().getDisplayMetrics().density;
         
         indicator.setRadius(5 * density);
         indicator.setFillColor(0xFFFF0000);
         indicator.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);
        
         loadTemplateSummary ("event","basic", mStoryMode);
       
    }
    
    private void loadTemplateSummary(String name, String type, int mode) {
        try {
            String lang = StoryMakerApp.getCurrentLocale().getLanguage();
            mTemplatePath = "story/templates/" + lang + '/' + name + '/' + name + '_' + type + ".json";

            // if templates folder for this lang don't exist, fallback to english
            if (!Utils.assetExists(getApplicationContext(), mTemplatePath)) {
                mTemplatePath = "story/templates/en/" + name + '/' + name + '_' + type + ".json";
            }

            if (mStoryModeTemplate != null) {
                mTemplate = Template.parseAsset(this, mTemplatePath, mStoryModeTemplate);
            } else {
                mTemplate = Template.parseAsset(this, mTemplatePath);
            }

        } catch (Exception e) {
            Log.e(AppConstants.TAG, "error loading template", e);
        }
    }
    
    // FIXME display template's scenes in list

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_choose_template, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.itemForward:
            	showTemplateEditor();
            	
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showTemplateEditor ()
    {
    	int pageIdx = mPager.getCurrentItem();
    	String templateLevel = "basic";
    	
    	RadioGroup view = ((RadioGroup)findViewById(R.id.radioGroupStoryLevel));
    	if (view.getCheckedRadioButtonId() == R.id.radioStoryType1)
    		templateLevel = "expert";
    	
    	String templateType = null;
    	
    	switch (pageIdx)
		{
			case 0:
				templateType = "event_news";
			break;
			case 1:
				templateType = "event_feature";
			break;
			case 2: 
				templateType = "event_breaking";
			break;
			case 3: 
				templateType = "issue_social";
			break;
			case 4: 
				templateType = "issue_political";
			break;
			case 5: 
				templateType = "issue_economic";
			break;
			case 6: 
				templateType = "profile_public";
			break;
			case 7: 
				templateType = "profile_professional";
			break;
			case 8: 
				templateType = "profile_personal";
			break;
		}
    	
    	loadTemplateSummary (templateType, templateLevel, mStoryMode);
    	
        // FIXME this should be split into a method, probably in the model.Project class?
        int sceneCount = mTemplate.getScenes().size(); 
        Project project = new Project(this, sceneCount);
        project.setTitle(mProjectName);
        project.setTemplatePath(mTemplatePath);
        project.setStoryType(mStoryMode);
        project.setReport_Id(rid);
        project.save();
        int i = 0;
        for (info.guardianproject.mrapp.model.template.Scene s : mTemplate.getScenes()) {
            Scene scene = new Scene(this, s.getClips().size());
            scene.setTitle(s.mTitle);
            scene.setProjectId(project.getId());
            scene.setProjectIndex(i);
            scene.save();
            i++;
        }
        Intent intent = new Intent(getBaseContext(), StoryTemplateActivity.class);
        intent.putExtra("pid", project.getId());
        intent.putExtra("template_path", mTemplatePath);
        
        startActivity(intent);
        finish();
    }
    
    public class MyAdapter extends FragmentPagerAdapter {
		 
		 int[] mMessages;
		 int[] mTitles;
		 
	        public MyAdapter(FragmentManager fm, int[] titles, int[] messages) {
	            super(fm);
	            mTitles = titles;
	            mMessages = messages;
	        }

	        @Override
	        public int getCount() {
	            return mMessages.length;
	        }

	        @Override
	        public Fragment getItem(int position) {
	        	Bundle bundle = new Bundle();
	        	bundle.putString("title",getString(mTitles[position]));
	        	bundle.putString("msg", getString(mMessages[position]));
	        	
	        	Fragment f = new MyFragment();
	        	f.setArguments(bundle);
	        	
	            return f;
	        }
	    }
	
	@SuppressLint("ValidFragment")
	public class MyFragment extends Fragment {
	
		String mMessage;
		String mTitle;
		
		 /**
        * When creating, retrieve this instance's number from its arguments.
        */
       @Override
       public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);

           mTitle = getArguments().getString("title");
           mMessage = getArguments().getString("msg");
       }

       /**
        * The Fragment's UI is just a simple text view showing its
        * instance number.
        */
       @Override
       public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
           
           ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
           
           ((TextView)root.findViewById(R.id.title)).setText(mTitle);
           
           ((TextView)root.findViewById(R.id.description)).setText(mMessage);
           
           return root;
       }
	
	}

}
