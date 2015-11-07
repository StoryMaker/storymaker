package org.storymaker.app.tests;

import timber.log.Timber;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.AccountsActivity;
import org.storymaker.app.ConnectAccountActivity;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.ProjectsActivity;
import org.storymaker.app.R;
import org.storymaker.app.SceneEditorActivity;
import org.storymaker.app.SimplePreferences;
import org.storymaker.app.StoryInfoEditActivity;
import org.storymaker.app.server.LoginActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import io.scal.secureshareui.login.SoundCloudLoginActivity;
import scal.io.liger.MainActivity;
import scal.io.liger.StorageHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
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
public class MinimumTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mHomeActivity;
    private MainActivity mMainActivity;

    private Instrumentation.ActivityMonitor mVideoActivityMonitor;
    private Instrumentation.ActivityMonitor mAudioActivityMonitor;
    private Instrumentation.ActivityMonitor mPhotoActivityMonitor;
    private Instrumentation.ActivityMonitor mPassThroughMonitor0;
    private Instrumentation.ActivityMonitor mPassThroughMonitor1;
    private Instrumentation.ActivityMonitor mPassThroughMonitor2;
    private Instrumentation.ActivityMonitor mPassThroughMonitor3;
    private Instrumentation.ActivityMonitor mPassThroughMonitor4;
    private Instrumentation.ActivityMonitor mPassThroughMonitor5;
    private Instrumentation.ActivityMonitor mPassThroughMonitor6;
    private Instrumentation.ActivityMonitor mPassThroughMonitor7;
    private Instrumentation.ActivityMonitor mPassThroughMonitor8;
    private Instrumentation.ActivityMonitor mPassThroughMonitor9;

    private String testDirectory;

    private ArrayList<String> brokenPaths = new ArrayList<String>();

    String[] firstOption = {
            "An Event",
            //"A Person",
            "An Issue"
    };

    String[] secondOption = {
            "Talk to people about it.",
            //"Ask the same question to many people.",
            //"Create an accurate summary.",
            //"Document the steps taken.",
            //"Collect a set of pictures of people.",
            "Show the best moments."
    };

    String[] thirdOption = {
            "Audio",
            //"Video",
            "Photo"
    };

    String[] fourthOption = {
            "Characters",
            //"Actions",
            //"Results",
            //"Places",
            "Signatures"
    };

    String[] fifthOption = {
            "Why is the event important?",
            //"What is your opinion of the event?",
            //"What do you think is the most exciting part of the event?",
            //"Why is the issue important?",
            //"What is your opinion of the issue?",
            //"What do you think should be done about the issue?",
            //"Why is the character important?",
            //"What is your opinion of the character?",
            "What do you think the character should do next?",
    };

    public MinimumTest() {
        super(HomeActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        Timber.d("BEGIN SETUP");

        mHomeActivity = getActivity();

        String sampleVideoName = "TEST_SAMPLE.mp4";
        String sampleAudioName = "TEST_SAMPLE.mp3";
        String samplePhotoName = "TEST_SAMPLE.jpg";

        // create references to sample files for dummy responses
        // NOTE: can these be refactored into uri's like "content://media/external/video/media/1258"

        // String packageName = mHomeActivity.getApplicationContext().getPackageName();
        // File root = Environment.getExternalStorageDirectory();
        // testDirectory = root.toString() + "/Android/data/" + packageName + "/files/";
        testDirectory = StorageHelper.getActualStorageDirectory(mHomeActivity.getApplicationContext()).getPath() + "/";

        String sampleVideo = testDirectory + sampleVideoName;
        String sampleAudio = testDirectory + sampleAudioName;
        String samplePhoto = testDirectory + samplePhotoName;

        // copy test files from assets
        InputStream assetIn = null;
        OutputStream assetOut = null;

        AssetManager assetManager = getInstrumentation().getContext().getAssets();

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
            Timber.d("COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FINISHED");
        } catch (IOException ioe) {
            Timber.e("COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED");
            return;
        }

        // check for zero-byte files
        if (sampleVideoFile.exists() && (sampleVideoFile.length() == 0)) {
            Timber.e("COPYING SAMPLE FILE " + sampleVideoName + " FROM ASSETS TO " + testDirectory + " FAILED (FILE WAS ZERO BYTES)");
            sampleVideoFile.delete();
        }

        // seems necessary to create activity monitors to prevent activities from being caught by other monitors
        mPassThroughMonitor0 = new Instrumentation.ActivityMonitor(HomeActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor0);
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
        mPassThroughMonitor6 = new Instrumentation.ActivityMonitor(ConnectAccountActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor6);
        mPassThroughMonitor7 = new Instrumentation.ActivityMonitor(ProjectsActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor7);
        mPassThroughMonitor8 = new Instrumentation.ActivityMonitor(SimplePreferences.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor8);
        mPassThroughMonitor9 = new Instrumentation.ActivityMonitor(LoginActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor9);

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

        Timber.d("SETUP COMPLETE");
    }

    public void testAaEula() {

        Timber.d("BEGIN EULA TEST");

        onView(withId(R.id.btnTos)).perform(click());
        Timber.d("CLICKED TOS BUTTON");

        onView(withText("Accept")).perform(click());
        Timber.d("CLICKED ACCEPT BUTTON");

        onView(withId(R.id.btnNoThanks)).perform(click());
        Timber.d("CLICKED NO THANKS BUTTON");

        // just pass, this is mostly for setting up the real tests
        assertTrue(true);

        Timber.d("EULA TEST COMPLETE");
    }

    public void testBbDownloadAndPatch() {

        Timber.d("DOWNLOAD TEST NOT IMPLEMENTED IN MinimumTest CLASS");

        assertTrue(true);

    }

    public void testCcEverything() {

        Timber.d("BEGIN END-TO-END TEST");

        // signup/login
        onView(withText(mHomeActivity.getApplicationContext().getString(R.string.app_name))).perform(click());
        Timber.d("OPENED SIDE MENU");

        onView(withId(R.id.llLogin)).perform(click());
        Timber.d("SELECTED SIGNUP/LOGIN");

        onView(withId(R.id.btnSignIn)).perform(click());
        Timber.d("SELECTED LOGIN");

        // get name/password from xml file
        String accountName = mHomeActivity.getApplicationContext().getString(R.string.storymaker_name);
        String accountPass = mHomeActivity.getApplicationContext().getString(R.string.storymaker_pass);

        // enter name/password
        onView(withId(R.id.login_username)).perform(clearText()).perform(typeText(accountName));
        Timber.d("ENTERED USER NAME");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.login_password)).perform(clearText()).perform(typeText(accountPass));
        Timber.d("ENTERED USER PASSWORD");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.btnLogin)).perform(click());
        Timber.d("CLICKED LOGIN BUTTON");
        stall(5000, "WAIT FOR LOGIN");

        pressBack();

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

                Timber.d(" *** TESTING " + firstSelection + "/" + secondSelection + " ****************************** ");

                testFlag = (testFlag && doTest(firstSelection, secondSelection));

                // restart app
                pressBack();
                mHomeActivity.finish();
                mHomeActivity.startActivity(mHomeActivity.getIntent());

                // allow time for restart
                stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
            }
        }

        assertTrue(testFlag);

        Timber.d("END-TO-END TEST COMPLETE");
    }

    public boolean doTest(String mediaString, String accountString) {

        // obb file assumed to be present (test requires network access)

        // select "new" option
        onView(withText("New")).perform(click());
        Timber.d("CLICKED NEW BUTTON");

        // first selection
        onView(withText("An Event")).perform(click());
        Timber.d("CLICKED EVENT BUTTON");

        // second selection
        onView(withText("Show the best moments.")).perform(click());
        Timber.d("CLICKED HIGHLIGHTS BUTTON");

        // third selection
        onView(withText(mediaString)).perform(click());
        Timber.d("CLICKED MEDIA BUTTON");

        // get liger activity
        ActivityGetter ag = new ActivityGetter();
        getInstrumentation().runOnMainSync(ag);

        if (mMainActivity == null) {
            Timber.e("NO LIGER ACTIVITY ACCESS");
            return false;
        }

        // scroll to first capture card
        ActivityScroller as1 = new ActivityScroller(4); // UPDATE IF STORY PATH CHANGES
        mMainActivity.runOnUiThread(as1);
        stall(500, "WAIT FOR SCROLLING");

        // media capture
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());
        Timber.d("CLICKED CAPTURE BUTTON");
        stall(500, "WAIT FOR MEDIA CAPTURE UPDATE");

        // scroll to bottom
        ActivityScroller as2 = new ActivityScroller(18); // UPDATE IF STORY PATH CHANGES
        mMainActivity.runOnUiThread(as2);
        stall(500, "WAIT FOR SCROLLING");

        // begin publish/upload steps
        try {
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).perform(click());
            Timber.d("CLICKED PUBLISH BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Timber.d("NO PUBLISH BUTTON FOUND (FAIL)");
            return false;
        }

        // enter metadata
        onView(withId(R.id.fl_info_container)).perform(click());
        Timber.d("CLICKED METADATA FIELD");

        //get time for unique id
        Calendar now = new GregorianCalendar();

        onView(withId(R.id.et_story_info_title)).perform(clearText()).perform(typeText(mediaString.toUpperCase() + "/" + accountString.toUpperCase() + "/" + now.get(Calendar.HOUR) + ":" + now.get(Calendar.MINUTE)));
        Timber.d("ENTERED TITLE TEXT");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.et_story_info_description)).perform(clearText()).perform(typeText(mediaString + "/" + accountString));
        Timber.d("ENTERED DESCRIPTION TEXT");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(mediaString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Timber.d("ENTERED FIRST TAG");
        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(accountString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Timber.d("ENTERED SECOND TAG");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        // these seem problematic, will troubleshoot later
        /*
        onView(withId(R.id.sp_story_section)).perform(click());
        onView(withText("Travel")).perform(click());
        Timber.d("SELECTED SECTION");

        onView(withId(R.id.sp_story_location)).perform(click());
        onView(withText("Czech Republic")).perform(click());
        Timber.d("SELECTED LOCATION");
        */

        onView(withText("Save")).perform(click());
        Timber.d("SAVED METADATA");

        // select account and upload
        onView(withId(R.id.btnUpload)).perform(click());
        Timber.d("CLICKED UPLOAD BUTTON");

        // scroll to account
        onView(withText(accountString)).perform(scrollTo(), click());
        Timber.d("SCROLLED TO " + accountString + " BUTTON");

        // get name/password from xml file (add more later)
        String accountName = "";
        String accountPass = "";

        if (accountString.equals("SoundCloud")) {
            accountName = mHomeActivity.getApplicationContext().getString(R.string.soundcloud_name);
            accountPass = mHomeActivity.getApplicationContext().getString(R.string.soundcloud_pass);
        }

        // enter name/password
        onView(withId(R.id.etUsername)).perform(clearText()).perform(typeText(accountName));
        Timber.d("ENTERED USER NAME");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.etPassword)).perform(clearText()).perform(typeText(accountPass));
        Timber.d("ENTERED USER PASSWORD");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.btnSignIn)).perform(click());
        Timber.d("CLICKED SIGN IN BUTTON");

        onView(withId(R.id.switchStoryMaker)).perform(click());
        Timber.d("CLICKED STORYMAKER PUBLISH SWITCH");

        try {
            // TODO: re-enable once test points to beta server
            onView(withText("Continue")).perform(click());
            Timber.d("CLICKED CONTINUE BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Timber.d("NO CONTINUE BUTTON FOUND (FAIL)");
            return false;
        }

        // TODO: how to verify successful upload? (failure indicated by visible message)
        stall(30000, "WAITING FOR UPLOAD");
        Timber.d("TEST RUN COMPLETE (PASS)");
        return true;
    }

    public void testDdHookPaths() {

        Timber.d("HOOK TEST NOT IMPLEMENTED IN MinimumTest CLASS");

        assertTrue(true);
    }

    public void testEeSettings() {

        Timber.d("SETTINGS TEST NOT IMPLEMENTED IN MinimumTest CLASS");

        assertTrue(true);
    }

    private void stall(long milliseconds, String message) {
        try {
            Timber.d("SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    private void swipe(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUpLess());
        }
    }

    private void swipeMore(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        }
    }

    private void settingsSwipe(int currentItem) {
        onView(withChild(withChild(withText(mHomeActivity.getApplicationContext().getString(currentItem))))).perform(Util.swipeUp());
    }
    */

    private void cleanup(String directory) {
        WildcardFileFilter oldFileFilter = new WildcardFileFilter("*instance*");
        for (File oldFile : FileUtils.listFiles(new File(directory), oldFileFilter, null)) {
            Timber.d("CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
            FileUtils.deleteQuietly(oldFile);
        }
    }

    private class ActivityGetter implements Runnable
    {
        public void run() {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
            if (resumedActivities.iterator().hasNext()){
                Object currentActivity = resumedActivities.iterator().next();

                if (currentActivity instanceof MainActivity) {
                    Timber.d("GOT MAIN ACTIVITY");
                    mMainActivity = (MainActivity)currentActivity;
                } else {
                    Timber.d("NOT MAIN ACTIVITY");
                }
            }
        }
    }

    private class ActivityScroller implements Runnable
    {
        int position = 0;

        public ActivityScroller (int position) {
            this.position = position;
        }

        public void run() {
            mMainActivity.scroll(position);
        }
    }
}