package org.storymaker.app;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.util.ArrayList;

import org.storymaker.app.media.MediaProjectManager;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.Scene;

import io.scal.secureshareui.lib.ChooseAccountFragment;
import scal.io.liger.model.FullMetadata;


public class PublishActivity extends EditorBaseActivity {
    private PublishFragment mPublishFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        ArrayList<Parcelable> parcelables = getIntent().getParcelableArrayListExtra("export_metadata");
        mProject = new Project(this, 1);

        // FIXME this should be split into a method, probably in the model.Project class?
        mProject = new Project(this, 1);
        mProject.setTitle("export from liger");
        mProject.setTemplatePath("");
        final String medium = ((FullMetadata) parcelables.get(0)).getMedium(); // until we iron out multi medium, we just tied export medium to the medium of the first clip
        if (medium.equals("photo")) {
            mProject.setStoryType(Project.STORY_TYPE_ESSAY);
        } else if (medium.equals("audio")) {
            mProject.setStoryType(Project.STORY_TYPE_AUDIO);
        } else if (medium.equals("video")) {
            mProject.setStoryType(Project.STORY_TYPE_VIDEO);
        }
        mProject.save();
        Scene scene = new Scene(this, parcelables.size());
        scene.setTitle("ligerscene1");
        scene.setProjectId(mProject.getId());
        scene.setProjectIndex(0);
        scene.save();

        // FIXME convert export into project
        int i = 0;
        for (Parcelable p: parcelables) {
            // index, cliptype, path, mimetype
            FullMetadata m = ((FullMetadata) p);
            scene.setMedia(i, m.getFilePath(), m.getFilePath(), "video/mp4");
            i++;
        }
        scene.save();
        // FIXME load project
        mMPM = new MediaProjectManager(this, getApplicationContext(), mHandlerPub, mProject, null);
        mMPM.initProject();

        if (savedInstanceState == null) {
            mPublishFragment = new PublishFragment();
            Bundle args = new Bundle();
//            args.putInt(AddClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
            args.putInt("layout", R.layout.fragment_complete_story);
            args.putInt("scene",0);
            mPublishFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPublishFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_publish, container, false);
//            return rootView;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ChooseAccountFragment.ACCOUNT_REQUEST_CODE) {
            mPublishFragment.onChooseAccountDialogResult(resultCode, intent);
        }
    }
}
