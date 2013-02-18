package info.guardianproject.mrapp.lessons;

import java.util.ArrayList;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.model.Lesson;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LessonArrayAdapter extends ArrayAdapter {
	
    int layoutResourceId;    
    
    public LessonArrayAdapter(Context context, int layoutResourceId,ArrayList<Lesson> lessons) {
        super(context, layoutResourceId, lessons);        
        
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	View row = convertView;
        Lesson lesson = ((Lesson)getItem(position));
        
        TextView tvTitle;
        TextView tvStatus;
        
        boolean isEnabled = true;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
        }
        
        tvTitle = (TextView)row.findViewById(R.id.lessonRowTitle);
        tvTitle.setText(lesson.mTitle);        
        
        tvStatus = (TextView)row.findViewById(R.id.lessonRowStatus);
        tvStatus.setText(R.string.lesson_status_have_not_started);        
        
        if (lesson.mStatus == Lesson.STATUS_IN_PROGRESS)
        	tvStatus.setText(R.string.lesson_status_in_progress);
        else if (lesson.mStatus == Lesson.STATUS_COMPLETE)
        	tvStatus.setText(R.string.lesson_status_complete);
        else if (position > 0 && ((Lesson)getItem(position-1)).mStatus != Lesson.STATUS_COMPLETE)
    	{
        	isEnabled = false;
    	}
        
    	row.setEnabled(isEnabled);
    	tvTitle.setEnabled(isEnabled);
    	tvStatus.setEnabled(isEnabled);
    
        
        return row;
    }
    
}