
package org.codeforafrica.timby.listeningpost.lessons;

import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.model.LessonGroup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LessonGroupArrayAdapter extends ArrayAdapter {

    int layoutResourceId;

    public LessonGroupArrayAdapter(Context context, int layoutResourceId, ArrayList<LessonGroup> groups) {
        super(context, layoutResourceId, groups);

        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        LessonGroup group = ((LessonGroup) getItem(position));

        TextView tvTitle;
        TextView tvStatus;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        tvTitle = (TextView) row.findViewById(R.id.title);
        tvTitle.setText(group.mTitle);

        tvStatus = (TextView) row.findViewById(R.id.description);
        tvStatus.setText(group.mStatus);

        return row;
    }
}
