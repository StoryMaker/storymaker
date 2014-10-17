package info.guardianproject.mrapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;

/**
 * Fragment for displaying a Project's tags and optionally allowing
 * tag removal based on configuration via {@link #newInstance(int, boolean)}
 * 
 * If this Fragment is displayed with editable set false in {@link #newInstance(int, boolean)}
 * and the provided Project has no tags, R.string.no_tags.msg will be 
 * displayed in the tag area.
 * 
 * This Fragment has a public API for interaction with it's host Activity:
 * See {@link #setOnTagClickListener(OnClickListener)}
 * See {@link #addTag(String)}
 *
 */
public class ProjectTagFragment extends Fragment {
    private static final String TAG = "ProjectTagFragment";
    
    // Fragment Initialization Bundle Keys
    protected static final String ARG_PID = "pid";
    protected static final String ARG_EDITABLE = "editable";

    protected int mProjectId;
    protected Project mProject;
    private TextView mSpecialMessageTv;
	private ViewGroup mContainerProjectTagsView;
	private OnClickListener mUserOnTagClickListener;
	protected boolean mEditable;
	private OnClickListener mInternalOnTagClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mEditable) {
                mContainerProjectTagsView.removeView(v);
                mProject.removeTag(getStringTagFromTagButton(v));
                if (mContainerProjectTagsView.getChildCount() == 0) {
                    displayMessage(getActivity().getString(R.string.no_tags_msg));
                }
            }
            if (mUserOnTagClickListener != null) {
                mUserOnTagClickListener.onClick(v);
            }
        }
	    
	};
	
	 /** Public API **/
	
	/**
	 * Preferred method of this Fragment's construction
	 * 
	 * @param pid the Id of the Project this Fragment represents
	 */
	public static ProjectTagFragment newInstance(int pid, boolean editable) {
	    ProjectTagFragment fragment = new ProjectTagFragment();
	    setFragmentTagArguments(fragment, pid, editable);
        return fragment;
    }
	
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
    
    protected static void setFragmentTagArguments(Fragment fragment, int pid, boolean editable) {
        Bundle args = new Bundle();
        args.putInt(ARG_PID, pid);
        args.putBoolean(ARG_EDITABLE, editable);
        fragment.setArguments(args);
    }
    
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle bundle = getArguments();
        mProjectId = bundle.getInt(ARG_PID);  
        mEditable = bundle.getBoolean(ARG_EDITABLE, false);        
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_project_tags, null);
		mContainerProjectTagsView = (ViewGroup) view.findViewById(R.id.project_tag_container);	
		return view;
	}
	
    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }
	
	/**
	 * Set the ViewGroup to be used as the tag container.
	 * 
	 * Useful for subclasses of this fragment that provide a different
	 * layout xml in their {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)
	 * @param vg
	 */
	protected void setContainerProjectTagsView(ViewGroup vg) {
	    mContainerProjectTagsView = vg;
	}

	
	/**
	 * Initialize the UI based on {@link mProject}
	 * 
	 * {@link mProjectId} must be set before this method is called
	 * 
	 * Subclasses must call super() in their implementations
	 */
	protected void initialize() {
        mProject = (Project) (new ProjectTable()).get(getActivity().getApplicationContext(), mProjectId);
	    if (mProject == null) {
	        throw new IllegalStateException("No project specified by initialize(). Did you bundle a \"pid\" (Project Id) Integer argument?");
	    }
		String[] projectTags = mProject.getTagsAsStringArray();
		mContainerProjectTagsView.removeAllViews();
		if (projectTags.length == 0) {
		    displayMessage(getActivity().getString(R.string.no_tags_msg));
		} else {
    		for (String tag : projectTags) {
    			displayTag(tag);
    		}	
		}
	}
	
	/**
	 * Display a tag with text equal to "#" + tag, and View#tag equal 
	 * to tag's String value.
	 * 
	 * Does not add tag to the Project represented by this Fragment
	 */
	private void displayTag(String tag) {
		
		Button btnTag = new Button(getActivity());
		btnTag.setEnabled(mEditable);
		btnTag.setText("#" + tag);
		btnTag.setTag(tag);
		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btnTag.setOnClickListener(mInternalOnTagClickListener);
		
		if (mSpecialMessageTv != null) {
		    mContainerProjectTagsView.removeView(mSpecialMessageTv);
		}
		mContainerProjectTagsView.addView(btnTag, 0);
    }
	
	/**
	 * Display a message in a view that spans the entire
	 * tag pool
	 */
	private void displayMessage(String message) {
	    TextView textView = new TextView(getActivity());
	    textView.setText(message);
	    textView.setGravity(Gravity.CENTER);
	    textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    mSpecialMessageTv = textView;
	    mContainerProjectTagsView.addView(textView, 0);
	}
	
	private String getStringTagFromTagButton(View v) {
	    return (String) v.getTag();
	}
	
}
