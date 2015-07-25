package org.storymaker.app.tests;

import android.support.test.espresso.NoMatchingViewException;
import android.util.Log;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 7/24/15.
 */
public class HookPathsTest extends BaseTest {

    ArrayList<String> brokenPaths = new ArrayList<String>();

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


    public void testHookPaths() {

        Log.d("AUTOMATION", "BEGIN HOOK PATHS TEST");

        // eula
        doEula();

        for (int i = 0; i < firstOption.length; i++) {

            String firstSelection = firstOption[i];

            for (int j = 0; j < secondOption.length; j++) {

                String secondSelection = secondOption[j];

                for (int k = 0; k < thirdOption.length; k++) {

                    String thirdSelection = thirdOption[k];

                    // some selections spawn a fourth set of options
                    if (secondSelection.equals("Collect a set of pictures of people.")) {

                        for (int l = 0; l < fourthOption.length; l++) {

                            String fourthSelection = fourthOption[l];

                            // obb file assumed to be present (copied by test setup script)

                            // select "new" option
                            onView(withText("New")).perform(click());

                            // first selection
                            Log.d("AUTOMATION", "FIRST SELECTION (" + firstSelection + ")");
                            onView(withText(firstSelection)).perform(click());

                            // second selection
                            try {
                                Log.d("AUTOMATION", "SECOND SELECTION (" + secondSelection + ")");
                                onView(withText(secondSelection)).perform(click());

                                // third selection
                                try {
                                    Log.d("AUTOMATION", "THIRD SELECTION (" + thirdSelection + ")");
                                    onView(withText(thirdSelection)).perform(click());

                                    // fourth selection
                                    try {
                                        Log.d("AUTOMATION", "FOURTH SELECTION (" + fourthSelection + ")");
                                        onView(withText(fourthSelection)).perform(click());

                                        // get liger activity
                                        ActivityGetter ag = new ActivityGetter();
                                        getInstrumentation().runOnMainSync(ag);

                                        if (mMainActivity == null) {
                                            Log.e("AUTOMATION", "NO LIGER ACTIVITY ACCESS");
                                            assertTrue(false);
                                        }

                                        // check to see if a story path was loaded
                                        // scroll down, check for capture button
                                        ActivityScroller as = new ActivityScroller(5); // UPDATE IF STORY PATH CHANGES
                                        mMainActivity.runOnUiThread(as);

                                        try {
                                            stall(500, "CAPTURE BUTTON");
                                            onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).check(matches(isDisplayed()));

                                            // pause before next loop
                                            stall(500, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection + " COMPLETE)");
                                        } catch (NoMatchingViewException nmve) {
                                            // implies no button was found (failure)
                                            Log.d("AUTOMATION", "NO CAPTURE BUTTON FOUND IN " + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                            // return;
                                            brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                        }
                                    } catch (NoMatchingViewException nmve) {
                                        // some options do not support all questions (not a failure state)
                                        Log.d("AUTOMATION", "SELECTION " + fourthSelection + " NOT AVAILABLE");
                                    }
                                } catch (NoMatchingViewException nmve) {
                                    // some options do not support all media types (not a failure state)
                                    Log.d("AUTOMATION", "SELECTION " + thirdSelection + " NOT AVAILABLE");
                                }
                            } catch (NoMatchingViewException nmve) {
                                // some options do not support all formats (not a failure state???)
                                Log.d("AUTOMATION", "SELECTION " + secondSelection + " NOT AVAILABLE");
                            }

                            // restart app
                            pressBack();
                            mHomeActivity.finish();
                            mHomeActivity.startActivity(mHomeActivity.getIntent());

                            // allow time for restart
                            stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                        }

                        continue;
                    }

                    if (secondSelection.equals("Ask the same question to many people.")) {

                        for (int l = 0; l < fifthOption.length; l++) {

                            String fourthSelection = fifthOption[l];

                            // obb file assumed to be present (copied by test setup script)

                            // select "new" option
                            onView(withText("New")).perform(click());

                            // first selection
                            Log.d("AUTOMATION", "FIRST SELECTION (" + firstSelection + ")");
                            onView(withText(firstSelection)).perform(click());

                            // second selection
                            try {
                                Log.d("AUTOMATION", "SECOND SELECTION (" + secondSelection + ")");
                                onView(withText(secondSelection)).perform(click());

                                // third selection
                                try {
                                    Log.d("AUTOMATION", "THIRD SELECTION (" + thirdSelection + ")");
                                    onView(withText(thirdSelection)).perform(click());

                                    // fourth selection
                                    try {
                                        Log.d("AUTOMATION", "FOURTH SELECTION (" + fourthSelection + ")");
                                        onView(withText(fourthSelection)).perform(click());

                                        // get liger activity
                                        ActivityGetter ag = new ActivityGetter();
                                        getInstrumentation().runOnMainSync(ag);

                                        if (mMainActivity == null) {
                                            Log.e("AUTOMATION", "NO LIGER ACTIVITY ACCESS");
                                            assertTrue(false);
                                        }

                                        // check to see if a story path was loaded
                                        // scroll down, check for capture button
                                        ActivityScroller as = new ActivityScroller(5); // UPDATE IF STORY PATH CHANGES
                                        mMainActivity.runOnUiThread(as);

                                        try {
                                            stall(500, "CAPTURE BUTTON");
                                            onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).check(matches(isDisplayed()));

                                            // pause before next loop
                                            stall(500, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection + " COMPLETE)");
                                        } catch (NoMatchingViewException nmve) {
                                            // implies no button was found (failure)
                                            Log.d("AUTOMATION", "NO CAPTURE BUTTON FOUND IN " + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                            // return;
                                            brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                        }
                                    } catch (NoMatchingViewException nmve) {
                                        // some options do not support all questions (not a failure state)
                                        Log.d("AUTOMATION", "SELECTION " + fourthSelection + " NOT AVAILABLE");
                                    }
                                } catch (NoMatchingViewException nmve) {
                                    // some options do not support all media types (not a failure state)
                                    Log.d("AUTOMATION", "SELECTION " + thirdSelection + " NOT AVAILABLE");
                                }
                            } catch (NoMatchingViewException nmve) {
                                // some options do not support all formats (not a failure state???)
                                Log.d("AUTOMATION", "SELECTION " + secondSelection + " NOT AVAILABLE");
                            }

                            // restart app
                            pressBack();
                            mHomeActivity.finish();
                            mHomeActivity.startActivity(mHomeActivity.getIntent());

                            // allow time for restart
                            stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                        }

                        continue;
                    }

                    // obb file assumed to be present (copied by test setup script)

                    // select "new" option
                    onView(withText("New")).perform(click());

                    // first selection
                    Log.d("AUTOMATION", "FIRST SELECTION (" + firstSelection + ")");
                    onView(withText(firstSelection)).perform(click());

                    // second selection
                    try {
                        Log.d("AUTOMATION", "SECOND SELECTION (" + secondSelection + ")");
                        onView(withText(secondSelection)).perform(click());

                        // third selection
                        try {
                            Log.d("AUTOMATION", "THIRD SELECTION (" + thirdSelection + ")");
                            onView(withText(thirdSelection)).perform(click());

                            // get liger activity
                            ActivityGetter ag = new ActivityGetter();
                            getInstrumentation().runOnMainSync(ag);

                            if (mMainActivity == null) {
                                Log.e("AUTOMATION", "NO LIGER ACTIVITY ACCESS");
                                assertTrue(false);
                            }

                            // check to see if a story path was loaded
                            // scroll down, check for capture button
                            ActivityScroller as = new ActivityScroller(4); // UPDATE IF STORY PATH CHANGES
                            mMainActivity.runOnUiThread(as);

                            try {
                                stall(500, "CAPTURE BUTTON");
                                onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).check(matches(isDisplayed()));

                                // pause before next loop
                                stall(500, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " COMPLETE)");
                            } catch (NoMatchingViewException nmve) {
                                // implies no button was found (failure)
                                Log.d("AUTOMATION", "NO CAPTURE BUTTON FOUND IN " + firstSelection + " > " + secondSelection + " > " + thirdSelection);
                                // return;
                                brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection);
                            }
                        } catch (NoMatchingViewException nmve) {
                            // some options do not support all media types (not a failure state)
                            Log.d("AUTOMATION", "SELECTION " + thirdSelection + " NOT AVAILABLE");
                        }
                    } catch (NoMatchingViewException nmve) {
                        // some options do not support all formats (not a failure state???)
                        Log.d("AUTOMATION", "SELECTION " + secondSelection + " NOT AVAILABLE");
                    }

                    // restart app
                    pressBack();
                    mHomeActivity.finish();
                    mHomeActivity.startActivity(mHomeActivity.getIntent());

                    // allow time for restart
                    stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                }
            }
        }

        for (String brokenPath : brokenPaths) {
            Log.d("AUTOMATION", "BROKEN PATH: " + brokenPath);
        }

        assertEquals(brokenPaths.size(), 0);

        Log.d("AUTOMATION", "HOOK PATHS TEST COMPLETE");
    }
}
