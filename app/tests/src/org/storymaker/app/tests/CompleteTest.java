package org.storymaker.app.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.espresso.NoMatchingViewException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.AccountsActivity;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;
import org.storymaker.app.SceneEditorActivity;
import org.storymaker.app.StoryInfoEditActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import io.scal.secureshareui.login.SoundCloudLoginActivity;
import scal.io.liger.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

//import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
/*
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
/*/
//*/

/**
 * Created by mnbogner on 1/15/15.
 */
public class CompleteTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mHomeActivity;

    private Instrumentation.ActivityMonitor mVideoActivityMonitor;
    private Instrumentation.ActivityMonitor mAudioActivityMonitor;
    private Instrumentation.ActivityMonitor mPhotoActivityMonitor;
    private Instrumentation.ActivityMonitor mPassThroughMonitor1;
    private Instrumentation.ActivityMonitor mPassThroughMonitor2;
    private Instrumentation.ActivityMonitor mPassThroughMonitor3;
    private Instrumentation.ActivityMonitor mPassThroughMonitor4;
    private Instrumentation.ActivityMonitor mPassThroughMonitor5;

    private String testDirectory;

    public CompleteTest() {
        super(HomeActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mHomeActivity = getActivity();

        String sampleVideoName = "TEST_SAMPLE.mp4";
        String sampleAudioName = "TEST_SAMPLE.mp3";
        String samplePhotoName = "TEST_SAMPLE.jpg";

        // create references to sample files for dummy responses
        // sample files assumed to be present (copied by test setup script)
        // NOTE: can these be refactored into uri's like "content://media/external/video/media/1258"
        String packageName = mHomeActivity.getApplicationContext().getPackageName();
        File root = Environment.getExternalStorageDirectory();
        testDirectory = root.toString() + "/Android/data/" + packageName + "/files/";
        String sampleVideo = testDirectory + sampleVideoName;
        String sampleAudio = testDirectory + sampleAudioName;
        String samplePhoto = testDirectory + samplePhotoName;

        // copy sample file from assets
        InputStream assetIn = null;
        OutputStream assetOut = null;

        AssetManager assetManager = mHomeActivity.getApplicationContext().getAssets();

        File sampleVideoFile = new File(sampleVideo);

        try {
            assetIn = assetManager.open(sampleVideoName);

            assetOut = new FileOutputStream(sampleVideoFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = assetIn.read(buffer)) != -1) {
                assetOut.write(buffer, 0, read);
            }
            assetIn.close();
            assetIn = null;
            assetOut.flush();
            assetOut.close();
            assetOut = null;
        } catch (IOException ioe) {
            Log.e("AUTOMATION", "COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED");
            return;
        }

        // check for zero-byte files
        if (sampleVideoFile.exists() && (sampleVideoFile.length() == 0)) {
            Log.e("AUTOMATION", "COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED (FILE WAS ZERO BYTES)");
            sampleVideoFile.delete();
        }

        // seems necessary to create activity monitor to prevent activity from being caught by other monitors
        mPassThroughMonitor1 = new Instrumentation.ActivityMonitor(MainActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor1);

        mPassThroughMonitor2 = new Instrumentation.ActivityMonitor(AccountsActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor2);

        mPassThroughMonitor3 = new Instrumentation.ActivityMonitor(StoryInfoEditActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor3);

        mPassThroughMonitor4 = new Instrumentation.ActivityMonitor(SoundCloudLoginActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor4);

        mPassThroughMonitor5 = new Instrumentation.ActivityMonitor(SceneEditorActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor5);

        // create activity monitors to intercept media capture requests
        IntentFilter videoFilter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent videoIntent = new Intent();
        Uri videoUri = Uri.parse(sampleVideo);
        videoIntent.setData(videoUri);
        Instrumentation.ActivityResult videoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, videoIntent);
        mVideoActivityMonitor = new Instrumentation.ActivityMonitor(videoFilter, videoResult, true);
        getInstrumentation().addMonitor(mVideoActivityMonitor);

        IntentFilter photoFilter = new IntentFilter(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent photoIntent = new Intent();
        Uri photoUri = Uri.parse(samplePhoto);
        photoIntent.setData(photoUri);
        Instrumentation.ActivityResult photoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, photoIntent);
        mPhotoActivityMonitor = new Instrumentation.ActivityMonitor(photoFilter, photoResult, true);
        getInstrumentation().addMonitor(mPhotoActivityMonitor);

        IntentFilter audioFilter = new IntentFilter(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        Intent audioIntent = new Intent();
        Uri audioUri = Uri.parse(sampleAudio);
        audioIntent.setData(audioUri);
        Instrumentation.ActivityResult audioResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, audioIntent);
        mAudioActivityMonitor = new Instrumentation.ActivityMonitor(audioFilter, audioResult, true);
        getInstrumentation().addMonitor(mAudioActivityMonitor);

        // clear out files from previous tests
        cleanup(testDirectory);
    }

    public void testAaEula() {

        onView(withId(R.id.btnTos)).perform(click());

        onView(withText("Accept")).perform(click());

        onView(withId(R.id.btnNoThanks)).perform(click());

        // just pass, this is mostly for setting up the real test
        assertTrue(true);
    }

    public void testBbEverything() {

        boolean testFlag = true;

        String[] firstOption = {
                "Video" //,
                // NEED TO DETERMINE FILE LOCATION "Photo",
                // NO CAPTURE ACTIVITY TO INTERCEPT "Audio"
        };

        String[] secondOption = {
                "SoundCloud"
                // don't know how to fake logins for these sites
                //"Internet Archive",
                //"YouTube",
                //"Flickr",
                //"Facebook",
                //"Private Server (SSH)"
        };

        for (int i = 0; i < firstOption.length; i++) {

            String firstSelection = firstOption[i];

            for (int j = 0; j < secondOption.length; j++) {

                String secondSelection = secondOption[j];

                Log.d("AUTOMATION", " *** TESTING " + firstSelection + "/" + secondSelection + " ****************************** ");

                testFlag = (testFlag && doTest(firstSelection, secondSelection));

                // restart app
                mHomeActivity.finish();
                mHomeActivity.startActivity(mHomeActivity.getIntent());

                // allow time for restart
                stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
            }
        }

        assertTrue(testFlag);
    }

    public boolean doTest(String mediaString, String accountString) {

        // obb file assumed to be present (test requires network access)

        // select "new" option
        onView(withText("New")).perform(click());

        // first selection
        onView(withText("An Event")).perform(click());

        // second selection
        onView(withText("Show the best moments.")).perform(click());

        // third selection
        onView(withText(mediaString)).perform(click());

        // media capture
        swipe(2);
        stall(500, "WAIT FOR SCROLLING");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());
        stall(500, "WAIT FOR MEDIA CAPTURE UPDATE");

        // scroll to bottom, check for publish button
        swipe(24);

        // begin publish/upload steps
        try {
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).perform(click());
            Log.d("AUTOMATION", "CLICKED PUBLISH BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO PUBLISH BUTTON FOUND (FAIL)");
            return false;
        }

        // enter metadata
        onView(withId(R.id.fl_info_container)).perform(click());
        Log.d("AUTOMATION", "CLICKED METADATA FIELD");

        //get time for unique id
        Calendar now = new GregorianCalendar();

        onView(withId(R.id.et_story_info_title)).perform(clearText()).perform(typeText(mediaString.toUpperCase() + "/" + accountString.toUpperCase() + "/" + now.get(Calendar.HOUR) + ":" + now.get(Calendar.MINUTE)));
        Log.d("AUTOMATION", "ENTERED TITLE TEXT");

        onView(withId(R.id.et_story_info_description)).perform(clearText()).perform(typeText(mediaString + "/" + accountString));
        Log.d("AUTOMATION", "ENTERED DESCRIPTION TEXT");

        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(mediaString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED FIRST TAG");

        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(accountString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED SECOND TAG");

        // these seem problematic, will troubleshoot later
        /*
        onView(withId(R.id.sp_story_section)).perform(click());
        onView(withText("Travel")).perform(click());
        Log.d("AUTOMATION", "SELECTED SECTION");

        onView(withId(R.id.sp_story_location)).perform(click());
        onView(withText("Czech Republic")).perform(click());
        Log.d("AUTOMATION", "SELECTED LOCATION");
        */

        onView(withText("Save")).perform(click());
        Log.d("AUTOMATION", "SAVED INFO");

        // select account and upload
        onView(withId(R.id.btnUpload)).perform(click());
        Log.d("AUTOMATION", "CLICKED UPLOAD BUTTON");

        // scroll to account
        onView(withText(accountString)).perform(scrollTo(), click());
        Log.d("AUTOMATION", "SCROLLED TO " + accountString + " BUTTON");

        // get name/password from xml file (add more later)
        String accountName = "";
        String accountPass = "";

        if (accountString.equals("SoundCloud")) {
            accountName = mHomeActivity.getApplicationContext().getString(R.string.soundcloud_name);
            accountPass = mHomeActivity.getApplicationContext().getString(R.string.soundcloud_pass);
        }

        // enter name/password
        onView(withId(R.id.etUsername)).perform(clearText()).perform(typeText(accountName));
        Log.d("AUTOMATION", "ENTERED USER NAME");

        onView(withId(R.id.etPassword)).perform(clearText()).perform(typeText(accountPass));
        Log.d("AUTOMATION", "ENTERED USER PASSWORD");

        onView(withId(R.id.btnSignIn)).perform(click());
        Log.d("AUTOMATION", "CLICKED SIGN IN BUTTON");

        // after login, account is checked by default?
        /*
        onView(withText(accountString)).perform(click());
        Log.d("AUTOMATION", "CLICKED " + accountString + " BUTTON");
        */

        try {
            onView(withText("Continue")).perform(click());
            Log.d("AUTOMATION", "CLICKED CONTINUE BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO CONTINUE BUTTON FOUND (FAIL)");
            return false;
        }

        // TODO: how to verify successful upload? (failure indicated by visible message)
        stall(30000, "WAITING FOR UPLOAD");
        Log.d("AUTOMATION", "TEST RUN COMPLETE (PASS)");
        return true;
    }

    private void stall(long milliseconds, String message) {
        try {
            Log.d("AUTOMATION", "SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void swipe(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUpLess());
        }
    }

    private void cleanup(String directory) {
        WildcardFileFilter oldFileFilter = new WildcardFileFilter("*instance*");
        for (File oldFile : FileUtils.listFiles(new File(directory), oldFileFilter, null)) {
            Log.d("AUTOMATION", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
            FileUtils.deleteQuietly(oldFile);
        }
    }
}