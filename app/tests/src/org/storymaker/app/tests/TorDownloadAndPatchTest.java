package org.storymaker.app.tests;

import timber.log.Timber;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import scal.io.liger.ZipHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by mnbogner on 7/24/15.
 */
public class TorDownloadAndPatchTest extends BaseTest {

    public void testDownloadAndPatch() {

        Timber.d("BEGIN DOWNLOAD AND PATCH TEST");

        // eula
        doEula();

        // tor
        if (!doTorToggle()) {
            Timber.e("TOR TOGGLE FAILED, CAN'T CONTINUE");
            assertTrue(false);
        }

        // scroll to bottom
        // HomeActivityScroller as = new HomeActivityScroller(0);
        // mHomeActivity.runOnUiThread(as);
        // stall(500, "WAIT FOR SCROLLING");

        // initiate download by clicking a menu item.
        Timber.d("SELECTING CONTENT PACK");
        onView(withText("Learning Guide")).perform(click());

        // delay to allow time for downloads
        stall(30000, "WAITING FOR DOWNLOADS");

        // test that clicking again brings up the content index
        Timber.d("SELECTING CONTENT PACK AGAIN");
        onView(withText("Learning Guide")).perform(click());

        Timber.d("CHECKING CONTENT PACK ITEMS");
        onView(withText("Learn the Basic Elements of a Story")).check(matches(isDisplayed()));
        onView(withText("Add More Detail to Your Story")).check(matches(isDisplayed()));

        // setup
        String testFilePath = ZipHelper.getFileFolderName(mHomeActivity);
        File indexFile = new File(testFilePath + "installed_index.json");
        File learningGuideMain = new File(testFilePath + "learning_test.main.1.obb");
        File learningGuidePatch = new File(testFilePath + "learning_test.patch.2.obb");
        File learningGuideMainTemp = new File(testFilePath + "learning_test.main.1.obb.tmp");
        File learningGuidePatchTemp = new File(testFilePath + "learning_test.patch.2.obb.tmp");
        File learningGuideMainPart = new File(testFilePath + "learning_test.main.1.obb.part");
        File learningGuidePatchPart = new File(testFilePath + "learning_test.patch.2.obb.part");

        // verify index file existence (created when first content pack is installed)
        assertTrue(indexFile.exists());
        Timber.d("INDEX FILE EXISTS");

        // verify test file existence
        assertTrue(learningGuideMain.exists());
        assertTrue(learningGuidePatch.exists());
        Timber.d("EXPANSION FILES EXIST");

        // verify test file size
        assertTrue(learningGuideMain.length() > 0);
        assertTrue(learningGuidePatch.length() > 0);
        Timber.d("EXPANSION FILES NON-ZERO");

        // verify test file cleanup
        assertTrue(!learningGuideMainTemp.exists());
        assertTrue(!learningGuidePatchTemp.exists());
        Timber.d("TEMP EXPANSION FILES DELETED");
        assertTrue(!learningGuideMainPart.exists());
        assertTrue(!learningGuidePatchPart.exists());
        Timber.d("PART EXPANSION FILES DELETED");

        // verify test file contents
        try {
            String testString = "";
            InputStream testStream = ZipHelper.getFileInputStream("org.storymaker.app/learning_test/learning_guide_1/learning_guide_1_library.json", mHomeActivity);

            if (testStream != null) {
                int size = testStream.available();
                byte[] buffer = new byte[size];
                testStream.read(buffer);
                testStream.close();
                testString = new String(buffer);
            }

            assertTrue(testString.contains("Learn the Basic Elements of a Story"));
            Timber.d("learning_guide_1_library.json IS OK");
        } catch (IOException ioe) {
            Timber.e("READING JSON FILE " + "org.storymaker.app/learning_test/learning_guide_1/learning_guide_1_library.json" + " FROM ZIP FILE FAILED");
        }

        try {
            String testString = "";
            InputStream testStream = ZipHelper.getFileInputStream("org.storymaker.app/learning_test/learning_guide_2/learning_guide_2_library.json", mHomeActivity);

            if (testStream != null) {
                int size = testStream.available();
                byte[] buffer = new byte[size];
                testStream.read(buffer);
                testStream.close();
                testString = new String(buffer);
            }

            assertTrue(testString.contains("Add More Detail to Your Story"));
            Timber.d("learning_guide_2_library.json IS OK");
        } catch (IOException ioe) {
            Timber.e("READING JSON FILE " + "org.storymaker.app/learning_test/learning_guide_2/learning_guide_2_library.json" + " FROM ZIP FILE FAILED");
        }

        // delete test files so test can be re-run
        if (indexFile.exists()) {
            indexFile.delete();
        }
        if (learningGuideMain.exists()) {
            learningGuideMain.delete();
        }
        if (learningGuidePatch.exists()) {
            learningGuidePatch.delete();
        }
        if (learningGuideMainTemp.exists()) {
            learningGuideMainTemp.delete();
        }
        if (learningGuidePatchTemp.exists()) {
            learningGuidePatchTemp.delete();
        }
        if (learningGuideMainPart.exists()) {
            learningGuideMainPart.delete();
        }
        if (learningGuidePatchPart.exists()) {
            learningGuidePatchPart.delete();
        }

        Timber.d("FINISHED CLEANUP");

        Timber.d("DOWNLOAD AND PATCH TEST COMPLETE");
    }
}
