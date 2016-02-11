package org.storymaker.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.storymaker.app.ui.SimpleSectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 12/11/15.
 */
public class StoryListFragment extends Fragment{

    public static final String ARG_OBJECT = "object";
    public final static String EXTRA_MESSAGE = "org.storymaker.app.MESSAGE";
    public static final String LIST_COUNT = "org.storymaker.app.LIST_COUNT";
    public static final String LIST_NAME = "org.storymaker.app.LIST_NAME";
    public static final String HOME_FLAG = "org.storymaker.app.HOME_FLAG";
    public static final String SECTION_FLAG = "org.storymaker.app.SECTION_FLAG";
    public static final String HOME_TAB_FLAG = "org.storymaker.app.HOME_TAB_FLAG";
    public static final String HOME_TAB_INSTANCE_COUNT = "org.storymaker.app.HOME_TAB_INSTANCE_COUNT";
    public static final String HOME_TAB_INSTALLATION_COUNT = "org.storymaker.app.HOME_TAB_INSTALLATION_COUNT";

    private InstanceIndexItemAdapter myInstanceIndexItemAdapter;
    private Integer myListLength;
    private String myListName;
    private Boolean myHomeFlag;
    private Boolean mySectionFlag;
    private Boolean myHomeTabFlag;
    private Integer myHomeTabInstanceCount;
    private Integer myHomeTabInstallationCount;

    public StoryListFragment() {


    }

    public void setMyInstanceIndexItemAdapter(InstanceIndexItemAdapter myInstanceIndexItemAdapter) {
        this.myInstanceIndexItemAdapter = myInstanceIndexItemAdapter;
    }

//    public static final String ARG_OBJECT = "object";

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
//        Bundle args = getArguments();
//        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                Integer.toString(args.getInt(ARG_OBJECT)));
//        return rootView;
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        myListLength = args.getInt(LIST_COUNT);
        myListName = args.getString(LIST_NAME);
        myHomeFlag = args.getBoolean(HOME_FLAG);
        mySectionFlag = args.getBoolean(SECTION_FLAG);
        myHomeTabFlag = args.getBoolean(HOME_TAB_FLAG);
        myHomeTabInstanceCount = args.getInt(HOME_TAB_INSTANCE_COUNT);
        myHomeTabInstallationCount = args.getInt(HOME_TAB_INSTALLATION_COUNT);

        if (myListLength > 0) {

            RecyclerView rv = (RecyclerView) inflater.inflate(
                    R.layout.fragment_story_list, container, false);
            setupRecyclerView(rv);
            return rv;

        } else {

            //LinearLayout ll = (LinearLayout) inflater.inflate(
            //        R.layout.fragment_story_list_empty, container, false);
            //return ll;

            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_story_list_empty, null);
            TextView tv = (TextView) ll.findViewById(R.id.tv_text);
            Button linkButton = (Button) ll.findViewById(R.id.fragment_button);

            int id = getResources().getIdentifier("home_empty_" + myListName, "string", "org.storymaker.app");
            //String catalog_name = getResources().getString(id);
            tv.setText(getResources().getString(id));

            int id2 = getResources().getIdentifier("home_empty_btn_" + myListName, "string", "org.storymaker.app");
            linkButton.setText(getResources().getString(id2));

            //Only add Links if it's the Home page
            if (myHomeFlag) {

                linkButton.setVisibility(View.VISIBLE);
                if (myListName.equals("stories")) {
                    //linkButton.setText("New Story");
                    linkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newProject();
                        }
                    });
                } else {
                    linkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openCatalogTab(v);
                        }
                    });
                }

            } else {
                linkButton.setVisibility(View.GONE);
            }
            return ll;
        }
    }

    public void openCatalogTab(View view) {

        Intent intent = new Intent(view.getContext(), CatalogActivity.class);
        intent.putExtra(EXTRA_MESSAGE, myListName);
        startActivity(intent);

    }

    public void newProject() {
        ((HomeActivity)getActivity()).launchNewProject();
    }


    private void setupRecyclerView(RecyclerView recyclerView) {

        if (mySectionFlag && myHomeTabFlag && (myHomeTabInstallationCount > 0 || myHomeTabInstanceCount > 0)) {

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            //This is the code to provide a sectioned list
            List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                    new ArrayList<SimpleSectionedRecyclerViewAdapter.Section>();

            Log.d("StoryListFragment", "counts installation:" + myHomeTabInstallationCount + " instance" + myHomeTabInstanceCount);

            //Sections
            //FIXME - Simplify this code
            if (myHomeTabInstallationCount > 0 && myHomeTabInstanceCount == 0) {
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, getActivity().getString(R.string.home_tab_latest_installation)));
            } else if (myHomeTabInstallationCount == 0 && myHomeTabInstanceCount > 0) {
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, getActivity().getString(R.string.home_tab_latest_stories)));
            } else if (myHomeTabInstallationCount > 0 && myHomeTabInstanceCount > 0) {
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, getActivity().getString(R.string.home_tab_latest_stories)));
                if (myHomeTabInstanceCount == 1) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(1, getActivity().getString(R.string.home_tab_latest_installation)));
                } else if (myHomeTabInstanceCount == 2) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(2, getActivity().getString(R.string.home_tab_latest_installation)));
                }
            }

            //sections.add(new SimpleSectionedRecyclerViewAdapter.Section(2,"Section 2"));

            SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
            SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                    SimpleSectionedRecyclerViewAdapter(getActivity(),R.layout.section,R.id.section_text,myInstanceIndexItemAdapter);
            mSectionedAdapter.setSections(sections.toArray(dummy));

            recyclerView.setAdapter(mSectionedAdapter);

        } else {

            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

            recyclerView.setAdapter(myInstanceIndexItemAdapter);

        }
    }


//    public static class SimpleStringRecyclerViewAdapter
//            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {
//
//        private final TypedValue mTypedValue = new TypedValue();
//        private int mBackground;
//        private List<String> mValues;
//
//        public static class ViewHolder extends RecyclerView.ViewHolder {
//            public String mBoundString;
//
//            public final View mView;
//            // public final ImageView mImageView;
//            public final TextView mTextView;
//
//            public ViewHolder(View view) {
//                super(view);
//                mView = view;
//                //mImageView = (ImageView) view.findViewById(R.id.avatar);
//                mTextView = (TextView) view.findViewById(android.R.id.text1);
//            }
//
//            @Override
//            public String toString() {
//                return super.toString() + " '" + mTextView.getText();
//            }
//        }
//
//        public String getValueAt(int position) {
//            return mValues.get(position);
//        }
//
//        public SimpleStringRecyclerViewAdapter(Context context, List<String> items) {
//            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//            mBackground = mTypedValue.resourceId;
//            mValues = items;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.story_list_item, parent, false);
//            view.setBackgroundResource(mBackground);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            holder.mBoundString = mValues.get(position);
//            holder.mTextView.setText(mValues.get(position));
//
////            holder.mView.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    Context context = v.getContext();
////                    Intent intent = new Intent(context, StoryDetailActivity.class);
////                    intent.putExtra(StoryDetailActivity.EXTRA_NAME, holder.mBoundString);
////
////                    context.startActivity(intent);
////                }
////            });
//
////            Glide.with(holder.mImageView.getContext())
////                    .load(Cheeses.getRandomCheeseDrawable())
////                    .fitCenter()
////                    .into(holder.mImageView);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mValues.size();
//        }
//    }
}
