package org.storymaker.app;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

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
        onView(withId(R.id.menu_new_project)).perform(click());

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