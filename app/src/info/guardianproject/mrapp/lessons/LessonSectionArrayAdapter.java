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

public class LessonSectionArrayAdapter extends ArrayAdapter {
	
    int layoutResourceId;    
    
    public LessonSectionArrayAdapter(Context context, int layoutResourceId,String[] sections) {
        super(context, layoutResourceId, sections);        
        
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	View row = convertView;
        String section = ((String)getItem(position));
        
        TextView tvTitle;
        TextView tvStatus;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
        }
        
        tvTitle = (TextView)row.findViewById(R.id.lessonRowTitle);
        tvTitle.setText(section);        
        
        tvStatus = (TextView)row.findViewById(R.id.lessonRowStatus);
        tvStatus.setText(R.string.lesson_status_have_not_started);        
        
        return row;
    }
    
}