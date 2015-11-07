package org.storymaker.app.tests;

import timber.log.Timber;

import android.support.test.espresso.NoMatchingViewException;
import android.util.Log;

import org.storymaker.app.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 7/24/15.
 */
public class EndToEndVideoTest extends BaseTest {

    public void testEndToEnd() {

        Timber.d("BEGIN END-TO-END TEST");

        // eula
        doEula();

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
            // onView(withText("Continue")).perform(click());
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
}
