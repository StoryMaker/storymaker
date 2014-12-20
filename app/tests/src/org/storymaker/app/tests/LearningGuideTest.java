package org.storymaker.app.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;

import java.io.File;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 11/18/14.
 */
public class LearningGuideTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mHomeActivity;
    private RecyclerView mRecyclerView;
    private Instrumentation.ActivityMonitor mVideoActivityMonitor;
    private Instrumentation.ActivityMonitor mAudioActivityMonitor;
    private Instrumentation.ActivityMonitor mPhotoActivityMonitor;

    public LearningGuideTest() {
        super(HomeActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mHomeActivity = getActivity();
        mRecyclerView = (RecyclerView) mHomeActivity.findViewById(R.id.recyclerView);

        // create references to sample files for dummy responses
        // sample files assumed to be present (copied by test setup script)
        // NOTE: can these be refactored into uri's like "content://media/external/video/media/1258"
        String packageName = mHomeActivity.getApplicationContext().getPackageName();
        File root = Environment.getExternalStorageDirectory();
        String directory = root.toString() + "/Android/data/" + packageName + "/files/";
        String sampleVideo = directory + "SAMPLE.mp4";
        String sampleAudio = directory + "SAMPLE.mp3";
        String samplePhoto = directory + "SAMPLE.jpg";

        // create activity monitors to intercept media capture requests
        IntentFilter videoFilter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent videoIntent = new Intent();
        Uri videoUri = Uri.parse(sampleVideo);
        videoIntent.setData(videoUri);
        Instrumentation.ActivityResult videoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, videoIntent);
        mVideoActivityMonitor = new Instrumentation.ActivityMonitor(videoFilter, videoResult, true);
        getInstrumentation().addMonitor(mVideoActivityMonitor);

        IntentFilter audioFilter = new IntentFilter(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        Intent audioIntent = new Intent();
        Uri audioUri = Uri.parse(sampleAudio);
        audioIntent.setData(audioUri);
        Instrumentation.ActivityResult audioResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, audioIntent);
        mAudioActivityMonitor = new Instrumentation.ActivityMonitor(audioFilter, audioResult, true);
        getInstrumentation().addMonitor(mAudioActivityMonitor);

        IntentFilter photoFilter = new IntentFilter(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent photoIntent = new Intent();
        Uri photoUri = Uri.parse(samplePhoto);
        photoIntent.setData(photoUri);
        Instrumentation.ActivityResult photoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, photoIntent);
        mPhotoActivityMonitor = new Instrumentation.ActivityMonitor(photoFilter, photoResult, true);
        getInstrumentation().addMonitor(mPhotoActivityMonitor);
    }

    public void testPreConditions() {
        assertTrue(mHomeActivity != null);
        assertTrue(mRecyclerView != null);
        Log.d("AUTOMATION", "testPreConditions() COMPLETE");
    }

    public void testRecyclerViewExist() {
        assertOnScreen(mHomeActivity.getWindow().getDecorView(), mRecyclerView);
        Log.d("AUTOMATION", "testRecyclerViewExist() COMPLETE");
    }


    public void testVideo() {

        // obb file assumed to be present (copied by test setup script)

        // select file
        stall(500, "SELECT FILE");
        onData(hasToString(equalToIgnoringCase("learning_guide_1_library.json"))).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Video")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        // next
        stall(500, "NEXT");
        swipe(1);
        onView(withText("Next: Add More Detail to Your Story")).perform(click());

        // pause before closing
        stall(2000, "INTERMISSION (LEARNING GUIDE 1 VIDEO COMPLETE)");

        Log.d("AUTOMATION", "testVideo() COMPLETE");
    }

    public void testAudio() {

        // obb file assumed to be present (copied by test setup script)

        // select file
        stall(500, "SELECT FILE");
        onData(hasToString(equalToIgnoringCase("learning_guide_1_library.json"))).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Audio")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        // next
        stall(500, "NEXT");
        swipe(1);
        onView(withText("Next: Add More Detail to Your Story")).perform(click());

        // pause before closing
        stall(2000, "INTERMISSION (LEARNING GUIDE 1 AUDIO COMPLETE)");

        Log.d("AUTOMATION", "testAudio() COMPLETE");
    }

    public void testPhoto() {

        // obb file assumed to be present (copied by test setup script)

        // select file
        stall(500, "SELECT FILE");
        onData(hasToString(equalToIgnoringCase("learning_guide_1_library.json"))).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Photo")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        // next
        stall(500, "NEXT");
        swipe(1);
        onView(withText("Next: Add More Detail to Your Story")).perform(click());

        // pause before closing
        stall(2000, "INTERMISSION (LEARNING GUIDE 1 PHOTO COMPLETE)");

        Log.d("AUTOMATION", "testPhoto() COMPLETE");
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
}