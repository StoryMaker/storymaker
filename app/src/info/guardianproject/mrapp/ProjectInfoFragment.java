
package info.guardianproject.mrapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import org.holoeverywhere.widget.EditText;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;

/**
 * Fragment for displaying metadata of a Project. Includes Title, description,
 * tags, category and location. Currently only handles read-only display.
 * 
 * This fragment should be constructed via {@link #newInstance(int, boolean)}
 */
public class ProjectInfoFragment extends ProjectTagFragment {
    private static final String TAG = "ProjectTagFragment";
    
    // Fragment Initialization Bundle Keys
    protected static final String ARG_SHOW_LOC_SEC= "showlocsec";
    protected static final String ARG_EDITABLE= "editable";

    EditText mStoryTitle;
    EditText mStoryDesc;
    TextView mTvStorySection;
    TextView mTvStoryLocation;
    
    private boolean mShowLocationAndSection;
    private boolean mEditable;
    
    /** Public API **/
    
    /**
     * Preferred method of this Fragment's construction
     * 
     * @param pid the Id of the Project this Fragment represents
     */
    public static ProjectInfoFragment newInstance(int pid, boolean editable, boolean showSectionAndLocation) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
        setFragmentTagArguments(fragment, pid, editable);
        fragment.getArguments().putBoolean(ARG_SHOW_LOC_SEC, showSectionAndLocation);
        fragment.getArguments().putBoolean(ARG_EDITABLE, editable);
        return fragment;
    }
    
    /** End Public API **/
    
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowLocationAndSection = getArguments().getBoolean(ARG_SHOW_LOC_SEC, true);
        mEditable = getArguments().getBoolean(ARG_EDITABLE, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, null);
        mStoryTitle = (EditText) view.findViewById(R.id.etStoryTitle);
        mStoryDesc = (EditText) view.findViewById(R.id.etStoryDescription);
        mTvStorySection = (TextView) view.findViewById(R.id.tv_story_section);
        mTvStoryLocation = (TextView) view.findViewById(R.id.tv_story_location);
        setContainerProjectTagsView((ViewGroup) view.findViewById(R.id.project_tag_container));
        
        if (!mEditable) {
            mStoryTitle.setEnabled(false);
            mStoryDesc.setEnabled(false);
        }

        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    protected void initialize() {
        super.initialize();

        mStoryTitle.setText(mProject.getTitle());
        String desc = mProject.getDescription();
        if (desc != null && !desc.isEmpty())
            mStoryDesc.setText(desc);

        if (mShowLocationAndSection) {
            mTvStorySection.setText(mProject.getSection());
            mTvStoryLocation.setText(mProject.getLocation());
        } else {
            getActivity().findViewById(R.id.locationAndSectionContainer).setVisibility(View.GONE);
        }
    }
}
