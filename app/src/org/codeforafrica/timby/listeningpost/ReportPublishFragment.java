package org.codeforafrica.timby.listeningpost;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.api.SyncService;
import org.codeforafrica.timby.listeningpost.location.GPSTracker;
import org.codeforafrica.timby.listeningpost.model.Report;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.animoto.android.views.DraggableGridView;

@SuppressLint("ValidFragment")
public class ReportPublishFragment extends Fragment {
    View mView = null;
 
    private EditorBaseActivity mActivity;
    private SharedPreferences mSettings = null;
    private EditText editTextTitle;

	private EditText editTextDesc;
	
	private int rid;
	private String title;
	private String description;
	private String location;

	private Button done;
	private TextView gpsInfo;
	private GPSTracker gpsT; 
	
    private Dialog dialog;
    private Dialog dialog_save;
    private Dialog dialog_publish;
    
    private ToggleButton toggleGPS;
    
    boolean new_report = false;
    
    protected DraggableGridView mOrderClipsDGV;

    private void initFragment ()
    {
    	mActivity = (EditorBaseActivity)getActivity();
    	
        mSettings = PreferenceManager
        .getDefaultSharedPreferences(getActivity().getApplicationContext());
	
    }
    
    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	initFragment ();
    	
    	int layout = getArguments().getInt("layout");
    	
        mView = inflater.inflate(layout, null);
        if (layout == R.layout.fragment_report_publish) {
        	
        	//initialize views
        	editTextTitle = (EditText)mView.findViewById(R.id.editTextStoryName);
        	editTextDesc = (EditText)mView.findViewById(R.id.editTextDescription);
        	done = (Button)mView.findViewById(R.id.done);
        	gpsInfo = (TextView)mView.findViewById(R.id.textViewLocation);
        	toggleGPS = (ToggleButton)mView.findViewById(R.id.toggleButton1);
        	
        	//update edit view
        	rid = getArguments().getInt("rid");
        	if(rid!=-1){
        		setValues(rid);
        	}else{
        		setLocation();
            	new_report = true;
        	}
        	
        	
        	done.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                	
                	report_save();
                	
                }
            });
        	
            toggleGPS.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                	                	
                    if(toggleGPS.isChecked()){
                    	setLocation();
                    }
                    else{
                    	gpsInfo.setText("0, 0");
                    }
                }
            });
        }
        return mView;
    }
    
    public void setValues(int rid){
    	Report r = Report.get(mActivity.getApplicationContext(), rid);
    	
    	location = r.getLocation();
    	
        if(location.equals("0, 0")){
    		location = "Location not set";
    	}
        
        editTextTitle.setText(r.getTitle());
        
        editTextDesc.setText(r.getDescription());
        
        gpsInfo.setText(r.getLocation());
    }
    public void setLocation(){
		gpsT = new GPSTracker(mActivity); 
		  
        // check if GPS enabled 
        if(gpsT.canGetLocation()){ 

            double latitude = gpsT.getLatitude(); 
            double longitude = gpsT.getLongitude(); 

            // \n is for new line 
            gpsInfo.setText(latitude+", "+longitude); 
           /* GeoPoint myGeoPoint = new GeoPoint( 
                  (int)(latitude*1000000), 
                  (int)(longitude*1000000)); 
          	CenterLocatio(myGeoPoint); */
            if(String.valueOf(latitude).equals("0")){
                gpsT.showSettingsAlert(); 
            }
        }else{ 
            // can't get location 
            // GPS or Network is not enabled 
            // Ask user to enable GPS/network in settings 
            gpsT.showSettingsAlert(); 
        } 
    }
    
    public void report_save(){
    	
			launchProject(editTextTitle.getText().toString(), editTextDesc.getText().toString(),gpsInfo.getText().toString(), true);		
    	    	
    }
    private void launchProject(String title, String pDesc, String pLocation, boolean update) {
    	
    	new_report = false;
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String currentdate = dateFormat.format(new Date());
    	
    	if(pLocation.equals("Location not set")){
    		pLocation = "0, 0";
    	}
    	
    	if (title == null || title.length() == 0)
    	{
    		title = "Captured at "+currentdate;
    	}
    	
    	    
    	
    	Report report;
        if(rid==-1){
        	report = new Report (mActivity.getApplicationContext(), 0, title, "", "", "", pDesc, pLocation, "0", currentdate, "0", "0");
         }else{
        	report = Report.get(mActivity.getApplicationContext(), rid);
        	report.setTitle(title);
        	report.setDescription(pDesc);
        	report.setEntity("");
        	report.setIssue("");
        	report.setSector("");
        	report.setLocation(pLocation);        	
        }
        report.save();
        
        rid = report.getId();
                
        if(update == false){
        	/*
	        Intent intent = new Intent(mActivity.getBaseContext(), StoryNewActivity.class);
	        intent.putExtra("storymode", resultMode);
	        intent.putExtra("rid", report.getId());
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(intent);
	        */
	        
        }else{
        	if(pLocation.equals("0, 0")){
        		Toast.makeText(mActivity.getApplicationContext(), "Trouble finding location. Try again later!", Toast.LENGTH_LONG).show();
        	}else{
	    		
	        	Toast.makeText(mActivity.getBaseContext(), String.valueOf(rid)+" Updated successfully!", Toast.LENGTH_LONG).show();
	        	report_close();
        	}      	
        }
         
    }
    public void report_close(){
    	dialog_publish = new Dialog(mActivity.getApplicationContext());
    	dialog_publish.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog_publish.setContentView(R.layout.dialog_publish);
    	dialog_publish.findViewById(R.id.button_publish).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//TODO: check if sync or encrypt is running
				Intent i = new Intent(mActivity.getApplicationContext(),SyncService.class);
				i.putExtra("rid", rid);
  	        	mActivity.startService(i);
  	        	
            	do_report_close();
				dialog_publish.dismiss();
			}        	
        });
    	dialog_publish.findViewById(R.id.button_skip).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
            	do_report_close();
				dialog_publish.dismiss();
			}        	
        });
    	dialog_publish.show();
    }

   public void do_report_close(){ 	
    	//Hide keyboard
        InputMethodManager inputManager = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE); 
        inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),      
        		    InputMethodManager.HIDE_NOT_ALWAYS);
        //NavUtils.navigateUpFromSameTask(this);
        Intent i = new Intent(mActivity.getBaseContext(), HomePanelsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        mActivity.finish();
    }
}