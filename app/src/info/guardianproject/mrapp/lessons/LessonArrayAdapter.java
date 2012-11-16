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
	
	Context context; 
    int layoutResourceId;    
    ArrayList<Lesson> lessons;
    
    public LessonArrayAdapter(Context context, int layoutResourceId,ArrayList<Lesson> lessons) {
        super(context, layoutResourceId, lessons);        
        
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.lessons = lessons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        
        TextView tv;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            
        }
        
        tv = (TextView)row.findViewById(R.id.lessonRowTitle);
        
        tv.setText(lessons.get(position).mTitle);        
        
        
        return row;
    }
    
}