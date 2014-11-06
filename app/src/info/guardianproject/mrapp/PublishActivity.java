package info.guardianproject.mrapp;

import android.app.Activity;
import android.app.ActionBar;
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

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;
import scal.io.liger.model.FullMetadata;


public class PublishActivity extends EditorBaseActivity {

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
        mProject.setStoryType(Project.STORY_TYPE_VIDEO);
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
            Fragment fragment = new PublishFragment();
            Bundle args = new Bundle();
//            args.putInt(AddClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
            args.putInt("layout", R.layout.fragment_complete_story);
            args.putInt("scene",0);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
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
}
