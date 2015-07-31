package org.storymaker.app.tests;

import android.support.test.espresso.AmbiguousViewMatcherException;
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
public class EndToEndTest extends BaseTest {

    boolean soundcloudConnected = false;

    public void testEndToEnd() {

        Log.d("AUTOMATION", "BEGIN END-TO-END TEST");

        // eula
        doEula();

        // signup/login
        onView(withText(mHomeActivity.getApplicationContext().getString(R.string.app_name))).perform(click());
        Log.d("AUTOMATION", "OPENED SIDE MENU");

        onView(withId(R.id.llLogin)).perform(click());
        Log.d("AUTOMATION", "SELECTED SIGNUP/LOGIN");

        onView(withId(R.id.btnSignIn)).perform(click());
        Log.d("AUTOMATION", "SELECTED LOGIN");

        // get name/password from xml file
        String accountName = mHomeActivity.getApplicationContext().getString(R.string.storymaker_name);
        String accountPass = mHomeActivity.getApplicationContext().getString(R.string.storymaker_pass);

        // enter name/password
        onView(withId(R.id.login_username)).perform(clearText()).perform(typeText(accountName));
        Log.d("AUTOMATION", "ENTERED USER NAME");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.login_password)).perform(clearText()).perform(typeText(accountPass));
        Log.d("AUTOMATION", "ENTERED USER PASSWORD");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.btnLogin)).perform(click());
        Log.d("AUTOMATION", "CLICKED LOGIN BUTTON");
        stall(5000, "WAIT FOR LOGIN");

        pressBack();

        boolean testFlag = true;

        String[] firstOption = {
                "Video",
                // NEED TO DETERMINE FILE LOCATION "Photo",
                "Audio"
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
                pressBack();
                mHomeActivity.finish();
                mHomeActivity.startActivity(mHomeActivity.getIntent());

                // allow time for restart
                stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
            }
        }

        assertTrue(testFlag);

        Log.d("AUTOMATION", "END-TO-END TEST COMPLETE");
    }

    public boolean doTest(String mediaString, String accountString) {

        // obb file assumed to be present (test requires network access)

        // select "new" option
        onView(withText("New")).perform(click());
        Log.d("AUTOMATION", "CLICKED NEW BUTTON");

        // first selection
        onView(withText("An Event")).perform(click());
        Log.d("AUTOMATION", "CLICKED EVENT BUTTON");

        // second selection
        onView(withText("Show the best moments.")).perform(click());
        Log.d("AUTOMATION", "CLICKED HIGHLIGHTS BUTTON");

        // third selection
        onView(withText(mediaString)).perform(click());
        Log.d("AUTOMATION", "CLICKED MEDIA BUTTON");

        // get liger activity
        ActivityGetter ag = new ActivityGetter();
        getInstrumentation().runOnMainSync(ag);

        if (mMainActivity == null) {
            Log.e("AUTOMATION", "NO LIGER ACTIVITY ACCESS");
            return false;
        }

        // scroll to first capture card
        ActivityScroller as1 = new ActivityScroller(4); // UPDATE IF STORY PATH CHANGES
        mMainActivity.runOnUiThread(as1);
        stall(500, "WAIT FOR SCROLLING");

        if (mediaString.equals("Video")) {
            onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());
            Log.d("AUTOMATION", "CLICKED CAPTURE BUTTON");
            stall(500, "WAIT FOR MEDIA CAPTURE UPDATE");
        } else if (mediaString.equals("Audio")) {
            // audio captured by the card itself, record a 10 second clip
            onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());
            Log.d("AUTOMATION", "CLICKED CAPTURE BUTTON");
            stall(10000, "WAIT FOR 10 SECONDS OF AUDIO");
            onView(allOf(withText("STOP RECORDING"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());
            Log.d("AUTOMATION", "CLICKED STOP BUTTON");
            stall(500, "WAIT FOR MEDIA CAPTURE UPDATE");
        } else {
            Log.d("AUTOMATION", "TEST DOES NOT HANDLE MEDIA TYPE " + mediaString);
            return false;
        }

        // scroll to bottom
        ActivityScroller as2 = new ActivityScroller(18); // UPDATE IF STORY PATH CHANGES
        mMainActivity.runOnUiThread(as2);
        stall(500, "WAIT FOR SCROLLING");

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
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.et_story_info_description)).perform(clearText()).perform(typeText(mediaString + "/" + accountString));
        Log.d("AUTOMATION", "ENTERED DESCRIPTION TEXT");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(mediaString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED FIRST TAG");
        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText(accountString.toLowerCase()));
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED SECOND TAG");
        pressBack();
        stall(500, "PAUSE TO CLEAR KEYBOARD");

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
        Log.d("AUTOMATION", "SAVED METADATA");

        // select account and upload
        onView(withId(R.id.btnUpload)).perform(click());
        Log.d("AUTOMATION", "CLICKED UPLOAD BUTTON");


            // scroll to account
            onView(withText(accountString)).perform(scrollTo(), click());
            Log.d("AUTOMATION", "SCROLLED TO " + accountString + " BUTTON");

        // only login on the first time through the loop
        if ((accountString.equals("SoundCloud")) && (!soundcloudConnected)) {
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
            pressBack();
            stall(500, "PAUSE TO CLEAR KEYBOARD");

            onView(withId(R.id.etPassword)).perform(clearText()).perform(typeText(accountPass));
            Log.d("AUTOMATION", "ENTERED USER PASSWORD");
            pressBack();
            stall(500, "PAUSE TO CLEAR KEYBOARD");

            onView(withId(R.id.btnSignIn)).perform(click());
            Log.d("AUTOMATION", "CLICKED SIGN IN BUTTON");

            soundcloudConnected = true;
        } else {
            Log.d("AUTOMATION", "ALREADY LOGGED IN TO " + accountString);
        }

        //onView(withId(R.id.switchStoryMaker)).perform(click());
        Log.d("AUTOMATION", "CLICKED STORYMAKER PUBLISH SWITCH");

        try {
            // TODO: re-enable once test points to beta server
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
}
