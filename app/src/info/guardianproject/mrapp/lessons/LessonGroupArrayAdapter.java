package info.guardianproject.mrapp.lessons;

import java.util.ArrayList;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.LessonGroup;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LessonGroupArrayAdapter extends ArrayAdapter {
	
    int layoutResourceId;    
    
    public LessonGroupArrayAdapter(Context context, int layoutResourceId,ArrayList<LessonGroup> groups) {
        super(context, layoutResourceId, groups);        
        
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	View row = convertView;
    	LessonGroup group = ((LessonGroup)getItem(position));
        
        TextView tvTitle;
        TextView tvStatus;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
        }
        
        tvTitle = (TextView)row.findViewById(R.id.lessonRowTitle);
        tvTitle.setText(group.mTitle);        
        
        tvStatus = (TextView)row.findViewById(R.id.lessonRowStatus);
        tvStatus.setText(group.mStatus);        
        
        return row;
    }
    
}