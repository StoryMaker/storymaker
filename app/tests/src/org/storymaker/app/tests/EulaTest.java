package org.storymaker.app.tests;

import timber.log.Timber;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.espresso.NoMatchingViewException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.AccountsActivity;
import org.storymaker.app.FirstStartActivity;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;
import org.storymaker.app.StoryInfoEditActivity;

import java.io.File;

import scal.io.liger.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
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
public class EulaTest extends ActivityInstrumentationTestCase2<FirstStartActivity> {

    private FirstStartActivity mFirstStartActivity;

    public EulaTest() {
        super(FirstStartActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mFirstStartActivity = getActivity();
    }

    public void testAaEula() {

        // assert visibility of button?
        onView(withId(R.id.btnTos)).check(matches(isDisplayed()));

        onView(withId(R.id.btnTos)).perform(click());

        onView(withText("Accept")).perform(click());

        onView(withId(R.id.btnNoThanks)).perform(click());

        // select "new" option
        onView(withText("New")).perform(click());

        try {
            Timber.d("SLEEP " + (5000 / 1000) + " (" + "foo" + ")");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // just pass, this is mostly for setting up the real test
        assertTrue(true);
    }
}