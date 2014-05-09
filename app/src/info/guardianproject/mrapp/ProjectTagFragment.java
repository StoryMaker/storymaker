package info.guardianproject.mrapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;

/**
 * Fragment for displaying a String[] of tags with 
 * optional parameters to specify whether tags may be removed from the pool
 * by clicking
 *
 */
public class ProjectTagFragment extends Fragment {
    private static final String TAG = "ProjectTagFragment";

    private Project mProject;
	private ViewGroup mContainerProjectTagsView;
	private OnClickListener mInternalOnTagClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mAllowTagRemoval) {
                mContainerProjectTagsView.removeView(v);
                mProject.removeTag(getStringTagFromTagButton(v));
            }
            if (mUserOnTagClickListener != null) {
                mUserOnTagClickListener.onClick(v);
            }
        }
	    
	};
	private OnClickListener mUserOnTagClickListener;
	private boolean mAllowTagRemoval;
	
	 /** Public API **/
	
	 /**
     * Assign a {@link OnClickListener} to be notified when a tag is clicked
     */
    public void setOnTagClickListener(OnClickListener listener) {
        mUserOnTagClickListener = listener;
    }
    
    /**
     * Add a new tag to this fragment's tag pool and the corresponding Project.
     * 
     * Note all Project tags are automatically added on this Fragment's 
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    public void addTag(String tag) {
        if (tag != null && !tag.equals("")) {
            mProject.addTag(tag);
            displayTag(tag);
        }
    }
    
    /** End Public API **/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_project_tag_fragment, null);
		
		Bundle bundle = getArguments();
		int projectId = bundle.getInt("pid");  
		mAllowTagRemoval = bundle.getBoolean("allowTagRemoval", false);

		mProject = (Project) (new ProjectTable()).get(getActivity().getApplicationContext(), projectId);
		mContainerProjectTagsView = (ViewGroup) view.findViewById(R.id.project_tag_container);	
		initialize();
		
		return view;
	}

	
	private void initialize() {
	    if (mProject == null) {
	        throw new IllegalStateException("No project specified by initialize(). Did you bundle a \"pid\" (Project Id) Integer argument?");
	    }
		String[] projectTags = mProject.getTagsAsStringArray();
		for (String tag : projectTags) {
			displayTag(tag);
		}	
	}
	
	/**
	 * Create a Button for each tag with text
	 * equal to "#" + tag, and View tag equal to tag's String value.
	 * 
	 * Does not add tag to the Project represented by this Fragment
	 */
	private void displayTag(String tag) {
		
		Button btnTag = new Button(getActivity());
		btnTag.setEnabled(mAllowTagRemoval);
		btnTag.setText("#" + tag);
		btnTag.setTag(tag);
		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btnTag.setOnClickListener(mInternalOnTagClickListener);
		
		mContainerProjectTagsView.addView(btnTag, 0);
    }
	
	private String getStringTagFromTagButton(View v) {
	    return (String) v.getTag();
	}
	
}
