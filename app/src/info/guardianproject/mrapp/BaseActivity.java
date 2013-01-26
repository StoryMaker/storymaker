package info.guardianproject.mrapp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.holoeverywhere.app.Activity;
//import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

public class BaseActivity extends SlidingActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup drawer
        setBehindContentView(R.layout.fragment_drawer);
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
//        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setBehindWidthRes(R.dimen.slidingmenu_offset);
        
        final Activity activity = this;
        
        ImageButton btnDrawerNewProject = (ImageButton) findViewById(R.id.btnDrawerNewProject);
        ImageButton btnDrawerNewLesson = (ImageButton) findViewById(R.id.btnDrawerNewLesson);
        ImageButton btnDrawerQuickCapture = (ImageButton) findViewById(R.id.btnDrawerQuickCapture);
        Button btnDrawerHome = (Button) findViewById(R.id.btnDrawerHome);
        Button btnDrawerProjects = (Button) findViewById(R.id.btnDrawerProjects);
        Button btnDrawerLessons = (Button) findViewById(R.id.btnDrawerLessons);
        Button btnDrawerProfile = (Button) findViewById(R.id.btnDrawerProfile);

        btnDrawerNewProject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, StoryNewActivity.class);
                activity.startActivity(i);
            }
        });
        btnDrawerNewLesson.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, LessonsActivity.class);
                activity.startActivity(i);
            }
        });
        btnDrawerQuickCapture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Quick Capture coming soon...", Toast.LENGTH_SHORT).show();
            }
        });
//        btnDrawerHome.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(activity, "Coming soon...", Toast.LENGTH_SHORT).show();
//            }
//        });
        btnDrawerProjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, HomeActivity.class);
                activity.startActivity(i);
            }
        });
        btnDrawerLessons.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, HomeActivity.class);
                activity.startActivity(i);
            }
        });
        btnDrawerProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, SimplePreferences.class);
                activity.startActivity(i);
            }
        });
    }
}
