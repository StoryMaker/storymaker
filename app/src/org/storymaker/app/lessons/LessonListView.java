
package org.storymaker.app.lessons;

import org.storymaker.app.AppConstants;
import org.storymaker.app.LessonsActivity;
import org.storymaker.app.R;
import org.storymaker.app.StoryMakerApp;
import org.storymaker.app.model.Lesson;
import org.storymaker.app.model.LessonGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LessonListView extends ListView implements LessonManagerListener {

    private LessonManager mLessonManager;
    private ArrayList<Lesson> mListLessons;
    private ArrayList<LessonGroup> mLessonGroups;

    private String mSubFolder = null;
    private LessonsActivity mActivity;

    private Locale mLocale = null;

    public LessonListView(Context context) {
        super(context);
    }

    public LessonListView(Context context, LessonsActivity activity) {
        super(context);

        setDivider(null);
        setDividerHeight(0);

        mActivity = activity;
        mLocale = StoryMakerApp.getCurrentLessonsLocale();

        mLessonManager = StoryMakerApp.getLessonManager();
        mLessonManager.setLessonManagerListener(this);

        new Thread() {
            public void run() {

                Message msg = mHandler.obtainMessage(0);
                mHandler.sendMessage(msg);
                loadData();
                mHandler.sendEmptyMessage(3);
            }
        }.start();
    }

    private void loadData() {
        if (mSubFolder == null) {
            showLessonGroups();
        } else {
            changeLessonFolder(mSubFolder);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext()
                .getApplicationContext());

        final boolean requireComplete = !settings.getBoolean("plessondebug", false);

        setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(android.widget.AdapterView<?> aview,
                    android.view.View view, int selIdx, long arg3) {

                if (mSubFolder == null) {
                    mSubFolder = selIdx + 1 + "";
                    changeLessonFolder(mSubFolder);
                }
                else if (selIdx < mListLessons.size()) {
                    if ((!requireComplete) || selIdx == 0
                            || mListLessons.get(selIdx - 1).mStatus == Lesson.STATUS_COMPLETE) {
                        Lesson lesson = mListLessons.get(selIdx);
                        accessLesson(lesson);
                    }
                }
            }
        });
    }

    private void showLessonGroups() {
        // show lesson categories
        String[] lessonSections = getResources().getStringArray(R.array.lesson_sections);
        String[] lessonSectionsFolder = getResources().getStringArray(
                R.array.lesson_sections_folder);

        mLessonGroups = new ArrayList<LessonGroup>();
        int idx = 0;

        for (String folder : lessonSections) {
            LessonGroup lg = new LessonGroup();
            lg.mTitle = folder;

            String subFolder = lessonSectionsFolder[idx++];

            ArrayList<Lesson> lessons = LessonManager.loadLessonList(getContext(),
                    mLessonManager.getLessonRoot(), subFolder, mLocale.getLanguage(), -1);

            int lessonsComplete = 0;

            for (Lesson lesson : lessons) {
                if (lesson.mStatus == Lesson.STATUS_COMPLETE) {
                    lessonsComplete++;
                }
            }

            if (lessons.size() == 0) {
                lg.mStatus = getContext().getString(
                        R.string.no_lessons_available_tap_to_load_);
            }
            else {
                lg.mStatus = String.format("%d" + getContext().getString(R.string._of_) + "%d"
                        + getContext().getString(R.string._lesson_complete), lessonsComplete,
                        lessons.size());
            }
            mLessonGroups.add(lg);
        }

    }

    public boolean handleBack() {
        if (mSubFolder != null) {
            mSubFolder = null;

            showLessonGroups();
            mHandler.sendEmptyMessage(3);

            return true;
        }

        return false;
    }

    public void changeLessonFolder(String subFolder) {
        mSubFolder = subFolder;
        mLessonManager.setSubFolder(mSubFolder);

        Log.d(AppConstants.TAG, "loading lesson from folder: " + subFolder);
        mListLessons = mLessonManager.loadLessonList(getContext(), mLocale.getLanguage());

        if (mListLessons.size() == 0) {
            mActivity.updateLessons();
        }

        loadLessonListAdapter();
    }

    private void accessLesson(Lesson lesson) {
        // if yes, then display here!
        if (lesson.mStatus != Lesson.STATUS_COMPLETE) {
            try {
                mLessonManager.updateLessonStatus(lesson.mLocalPath.getAbsolutePath(),
                        Lesson.STATUS_IN_PROGRESS);
            } catch (Exception e) {
                Log.e(AppConstants.TAG, "erorr updating lesson status", e);
            }
        }

        try {
            LessonManager.updateLessonResource(getContext(), lesson, mLocale.getLanguage());

            Intent intent = new Intent(getContext(), LessonViewActivity.class);
            intent.putExtra("title", lesson.mTitle);
            intent.putExtra("url", lesson.mResourcePath);
            intent.putExtra("lessonPath", lesson.mLocalPath.getAbsolutePath());
            mActivity.startActivityForResult(intent, 1);
        } catch (IOException e) {
            Log.e(AppConstants.TAG, "error updating lesson", e);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    if (msg.getData().containsKey("status")) {
                        Toast.makeText(getContext(), msg.getData().getString("status"),
                                Toast.LENGTH_SHORT).show();
                    }
                    Object adapter = getAdapter();

                    if (adapter != null) {
                        if (adapter instanceof ArrayAdapter) {
                            ((ArrayAdapter) adapter).notifyDataSetChanged();
                        }
                    }
                    break;

                case 1:
                    mListLessons = mLessonManager.loadLessonList(getContext(),
                            mLocale.getLanguage());
                    loadLessonListAdapter();
                    break;
                
                case 2:
                    if (mActivity != null && mActivity.mProgressLoading != null) {
                        mActivity.mProgressLoading.cancel();
                    }
                    break;

                case 3: // update group list

                    setAdapter(new LessonGroupArrayAdapter(getContext(), R.layout.list_lesson_row,
                            mLessonGroups));

                    break;

                case 4: // error

                    if (mActivity != null && mActivity.mProgressLoading != null) {
                        mActivity.mProgressLoading.cancel();
                    }

                    Toast.makeText(getContext(), msg.getData().getString("err"), Toast.LENGTH_LONG)
                            .show();

                    break;
                case 5: // status update
                    if (mActivity != null && mActivity.mProgressLoading != null) {
                        mActivity.updateLessonProgress(msg.getData().getString("status"));
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public void refreshList() {
        changeLessonFolder(mSubFolder);
        loadLessonListAdapter();
    }

    private void loadLessonListAdapter() {
        Log.d(AppConstants.TAG, "Lesson list adapter loading");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext()
                .getApplicationContext());

        boolean requireComplete = !settings.getBoolean("plessondebug", false);

        setAdapter(new LessonArrayAdapter(getContext(),
                R.layout.list_lesson_row, mListLessons, requireComplete));
    }

    @Override
    public void lessonsLoadedFromServer() {
        Log.d(AppConstants.TAG, "Lessons loaded from server");

        mListLessons = mLessonManager.loadLessonList(getContext(), mLocale.getLanguage());
        mHandler.sendEmptyMessage(1);
        mHandler.sendEmptyMessage(2);

    }

    @Override
    public void lessonLoadingStatusMessage(String status) {
        Message msg = mHandler.obtainMessage(5);
        msg.getData().putString("status", status);
        mHandler.sendMessage(msg);
    }

    @Override
    public void loadingLessonFromServer(String subFolder, String lessonTitle) {
        mHandler.sendEmptyMessage(0);
        mHandler.sendEmptyMessage(1);

    }

    @Override
    public void errorLoadingLessons(String errMsg) {
        Message msg = mHandler.obtainMessage(4);
        msg.getData().putString("err", errMsg);
        mHandler.sendMessage(msg);
    }
}
