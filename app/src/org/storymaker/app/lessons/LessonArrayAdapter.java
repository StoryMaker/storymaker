
package org.storymaker.app.lessons;

import java.util.ArrayList;

import org.storymaker.app.R;
import org.storymaker.app.model.Lesson;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LessonArrayAdapter extends ArrayAdapter {

    int mLayoutResourceId;
    boolean mRequireLessonComplete = true;

    public LessonArrayAdapter(Context context, int layoutResourceId, ArrayList<Lesson> lessons,
            boolean requireComplete) {
        super(context, layoutResourceId, lessons);

        mLayoutResourceId = layoutResourceId;
        mRequireLessonComplete = requireComplete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Lesson lesson = ((Lesson) getItem(position));

        TextView tvTitle;
        TextView tvStatus;

        boolean isEnabled = true;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        tvTitle = (TextView) row.findViewById(R.id.title);
        tvTitle.setText(lesson.mTitle);

        tvStatus = (TextView) row.findViewById(R.id.description);
        tvStatus.setText(R.string.lesson_status_have_not_started);

        if (lesson.mStatus == Lesson.STATUS_IN_PROGRESS) {
            tvStatus.setText(R.string.lesson_status_in_progress);
        } else if (lesson.mStatus == Lesson.STATUS_COMPLETE) {
            tvStatus.setText(R.string.lesson_status_complete);
        } else if (mRequireLessonComplete && position > 0
                && ((Lesson) getItem(position - 1)).mStatus != Lesson.STATUS_COMPLETE) {
            isEnabled = false;
        }

        if (!isEnabled) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        row.setEnabled(isEnabled);
        tvTitle.setEnabled(isEnabled);
        tvStatus.setEnabled(isEnabled);

        return row;
    }
}
