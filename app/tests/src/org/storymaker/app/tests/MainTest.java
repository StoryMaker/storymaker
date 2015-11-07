package org.storymaker.app.tests;

import timber.log.Timber;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.storymaker.app.tests.Util.waitId;

public class MainTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private final boolean TAKE_SCREENSHOTS = false;

    private Screenshooter mScreenshooter;
    private Activity mActivity;

    public MainTest() {
        super(HomeActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        assertNotNull(mActivity);

        mScreenshooter = new Screenshooter(getInstrumentation());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateStory() {
        Log.i("TEST", "Beginning test");
        assertTrue(1 == 1);
        takeScreenshot("HomeActivity");
        //onView(isRoot()).perform(waitId(R.id.menu_new_project, 2 * 1000));
        onView(withId(R.id.menu_new_project)).perform(click());
        onView(withText("New")).perform(click());

        takeScreenshot("StoryPath");
        onView(withText("An Event")).perform(click());
        onView(withText("Talk to people about it.")).perform(click());
        onView(withText("Video")).perform(click());
        // TODO : Complete test.
    }

    private void takeScreenshot(String title) {
        if (TAKE_SCREENSHOTS) {
            mScreenshooter.takeScreenshot(title);

            // When Spoon issue resolved, replace above with:
            // Spoon.screenshot(mActivity, title);
        }
    }

}