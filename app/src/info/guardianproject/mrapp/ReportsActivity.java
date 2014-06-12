package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ReportsActivity extends BaseActivity {
	ListView mListView;
	private ArrayList<Report> mListReports;
	private ReportArrayAdapter aaReports;
	ProgressDialog pDialog;
	getThumbnail get_thumbnail=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        
        // action bar stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        //getSupportActionBar().setTitle("View Reports");

        //TextView title2 = (TextView) getWindow().getDecorView().findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));
        //title2.setTextColor(getResources().getColor(R.color.soft_purple));
	     
        
        mListView = (ListView)findViewById(R.id.projectslist);
        /*pDialog = new ProgressDialog(ReportsActivity.this);
		pDialog.setMessage("Getting thumbnails...");
		pDialog.setCancelable(false);
		pDialog.show(); 
        
        */
        //Create decryption folder
        File mThumbsDir = new File(Environment.getExternalStorageDirectory(), AppConstants.TAG+"/decrypts");
	    if (!mThumbsDir.exists()) {
	        if (!mThumbsDir.mkdirs()) {
	            Log.e("TIMBY: ", "Problem creating thumbnails folder");
	        }
	    }else{
	    	DeleteRecursive(mThumbsDir);
	    }
	    
        initListView(mListView);
        
        Toast.makeText(getApplicationContext(), "Thumbnails might take a while to display", Toast.LENGTH_LONG).show();
        
        int delay = 3000; // delay for 1 sec. 
		int period = 3000; // repeat every 10 sec. 
		final Timer timer = new Timer(); 
		timer.scheduleAtFixedRate(new TimerTask(){ 
		        public void run() 
		        { 
		        	if(checkTasks()<1){
		        		Handler refresh = new Handler(Looper.getMainLooper());
						refresh.post(new Runnable() {
						    public void run()
						    {
		        			  //pDialog.dismiss();
		        			  timer.cancel();
		        			  //finish();
		        		      //Toast.makeText(getApplicationContext(), "Something", Toast.LENGTH_SHORT).show();    
		        		   }
		        		}); 
		             }
		        } 
		    }, delay, period); 
		
    }
    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }
    
    public int checkTasks(){
		int tasks = 0;
		if((get_thumbnail!=null)){
			if(get_thumbnail.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}
		
		if(tasks==0){
	        File mThumbsDir = new File(Environment.getExternalStorageDirectory(), AppConstants.TAG+"/decrypts");

			DeleteRecursive(mThumbsDir);
		}
		
		Log.d("Tasks", String.valueOf(tasks));
		return tasks;
	}
    class showList extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}
		protected String doInBackground(String... args) {
			
			mListReports = Report.getAllAsList(ReportsActivity.this);
	        aaReports = new ReportArrayAdapter(ReportsActivity.this, R.layout.list_report_row, mListReports);
	         
	       //  mListView.setAdapter(aaReports);
			return null;
		}
	protected void onPostExecute(String file_url) {
			mListView.setAdapter(aaReports);
			
		}
	}
    /*
    @Override
	public void onResume() {
		super.onResume();
		refreshReports();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_reports, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
         case android.R.id.home:

	        	NavUtils.navigateUpFromSameTask(this);
	        	
             return true;
     }
 		
     return super.onOptionsItemSelected(item);
  
	}
    
	*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent i = new Intent(getBaseContext(), HomePanelsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPreferences ()	{
    	
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
	
    }

    
    
 

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		
		super.onActivityResult(arg0, arg1, arg2);
		

		boolean changed = ((StoryMakerApp)getApplication()).checkLocale();
		if (changed){
			startActivity(new Intent(this,ReportsActivity.class));
			
			finish();
			
		}
	}

	
    public void initListView (ListView list) {
    
    	list.setOnItemLongClickListener(new OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            final Report report = mListReports.get(arg2);
            /*
            AlertDialog.Builder builder = new AlertDialog.Builder(ReportsActivity.this);
            builder.setMessage("Delete report?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteReport (report);
                        }
                        
                    })
                    .setNegativeButton(R.string.no, null).show();
            
            
              */
            final Dialog dialog = new Dialog(ReportsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_delete);
            dialog.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					deleteReport (report);
					dialog.dismiss();
				}
            	
            });
            dialog.findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
            	
            dialog.show();
                return false;
            }
    	    
    	});
    	list.setOnItemClickListener(new OnItemClickListener ()
        {

			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				Report report = mListReports.get(position);
				Intent intent = null;
				intent = new Intent(ReportsActivity.this, ReportActivity.class);
				intent.putExtra("title",report.getTitle());
				intent.putExtra("issue", report.getIssue());
                intent.putExtra("sector", report.getSector());
                intent.putExtra("description", report.getDescription());
                intent.putExtra("entity", report.getEntity());
                intent.putExtra("location", report.getLocation());
                intent.putExtra("rid", report.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        startActivity(intent);
			}
        	
        });
       
        
       refreshReports();
        
    }
    
    public void refreshReports ()
    {    	
    	 new showList().execute();
    }
    
    private void deleteReport (Report report)
    {
    	mListReports.remove(report);
        aaReports.notifyDataSetChanged();
        
    	report.delete();
    	
		 	ArrayList<Project> mListProjects;
			mListProjects = Project.getAllAsList(getApplicationContext(), report.getId());
		 	for (int j = 0; j < mListProjects.size(); j++) {
		 		if(mListProjects.get(j)!=null){
					mListProjects.get(j).delete();
				}
		 	}
 
    	//should we delete report folders here too?
    }
    
    class ReportArrayAdapter extends ArrayAdapter {
    	
    	Context context; 
        int layoutResourceId;    
        ArrayList<Report> reports;
        
        public ReportArrayAdapter(Context context, int layoutResourceId,ArrayList<Report> reports) {
            super(context, layoutResourceId, reports);        
            
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.reports = reports;
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
            Report report = reports.get(position);
            
            //Set status
            tv = (TextView)row.findViewById(R.id.status);
            
            String status = "";
            String exported = "";
            String synced = "";
            
            if(report.getServerId().equals("0")){
            	synced = "Not synced";
            }else{
            	synced = "Synced";
            }
            
            if(report.getExported().equals("0")){
            	exported = "Not exported";
            }else{
            	exported = "Exported";
            }
            
            status = exported+" | "+synced;
        	tv.setText(status);
            
            
            tv = (TextView)row.findViewById(R.id.date);            
            tv.setText(report.getDate());
                  
            //Thumbnail Business
            ArrayList<Project> mListProjects;
    		mListProjects = Project.getAllAsList(getApplicationContext(), report.getId());
    	 	Log.d("id", "Projects:"+mListProjects.size());

    		if(mListProjects.size()>0){
	    		Project project = mListProjects.get(0);
	    	 	ImageView ivIcon = (ImageView)row.findViewById(R.id.imageView1);
	            
	            // FIXME default to use first scene
	            Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
	            
	            if (mediaList != null && mediaList.length > 0)    
	            {
	            	for (Media media: mediaList)
	            		if (media != null)
	            		{
	            			GetThumbnailParams params = new GetThumbnailParams(ReportsActivity.this,media,project, ivIcon);
	            		 	get_thumbnail = new getThumbnail();
	            		 	get_thumbnail.execute(params);	
	            			//Bitmap bmp = Media.getThumbnail(ProjectsActivity.this,media,project);
	            			//if (bmp != null)
	            			//	ivIcon.setImageBitmap(bmp);
	            			break;
	            		}
	            }
	            int vids =0;
	            int pics =0;
	            int auds =0;
	            TextView tvVids = (TextView)row.findViewById(R.id.textVid);
	            ImageView imVids = (ImageView)row.findViewById(R.id.imageVid);
	            
	            TextView tvAuds = (TextView)row.findViewById(R.id.textAud);
	            ImageView imAuds = (ImageView)row.findViewById(R.id.imageAud);
	            
	            TextView tvPics = (TextView)row.findViewById(R.id.textPic);
	            ImageView imPics = (ImageView)row.findViewById(R.id.imageCam);
	            //Get MIME types
	        	for (int j = 0; j < mListProjects.size(); j++) {
	    	 		Project project2 = mListProjects.get(j);
	    	 		
	    	 		Media[] mediaList2 = project2.getScenesAsArray()[0].getMediaAsArray();
	    	 		Media media = mediaList2[0];
	    	 		
	    		 	String ptype = media.getMimeType();
	    		 	if(ptype.contains("image")){
	    		 		pics++;
	    		 		tvPics.setText(String.valueOf(pics));
	    		 		imPics.setImageResource(R.drawable.pics_);
	    		 		
	    		 	}else if(ptype.contains("video")){
	    		 		vids++;
	    		 		tvVids.setText(String.valueOf(vids));
	    		 		imVids.setImageResource(R.drawable.vids_);
	    		 		
	    		 	}else if(ptype.contains("audio")){
	    		 		auds++;
	    		 		tvAuds.setText(String.valueOf(auds));
	    		 		imAuds.setImageResource(R.drawable.auds_);
	    		 	}
	        	}
	        	tvAuds.setText(String.valueOf(mListProjects.size()));
    		}
            return row;
        }
        
    }
 
    private static class GetThumbnailParams {
    	Context context;
    	Media media;
    	Project project;
    	ImageView ivIcon;
	    
	    GetThumbnailParams(Context context, Media media, Project project,ImageView ivIcon) {
	        this.context = context;
	        this.media = media;
	        this.project = project;
	        this.ivIcon = ivIcon;
	    }
	}
    class getThumbnail extends AsyncTask<GetThumbnailParams, String, String> {

		@Override
        protected void onPreExecute() {
            super.onPreExecute();
			
        }
        protected String doInBackground(GetThumbnailParams... params) {
        	Context context = params[0].context;
        	final Media media = params[0].media;
        	final Project project = params[0].project;
        	final ImageView ivIcon = params[0].ivIcon;
        	//
     	   final Bitmap bmp = Media.getThumbnail(ReportsActivity.this,media,project);
     	   
            ReportsActivity.this.runOnUiThread(new Runnable() 
                  {
                       public void run() 
                       {
                    	   
               				if (bmp != null)
               					ivIcon.setImageBitmap(bmp);
               				/*
               				String file = Environment.getExternalStorageDirectory()+"/"+AppConstants.TAG+"/thumbs/"+media.getId()+".jpg";

            		 		Cipher cipher;
            				try {
            					cipher = Encryption.createCipher(Cipher.ENCRYPT_MODE);
            					Encryption.applyCipher(file, file+"_", cipher);
            				}catch (Exception e) {
            					// TODO Auto-generated catch block
            					Log.e("Encryption error", e.getLocalizedMessage());
            					e.printStackTrace();
            				}
            				//Then delete original file
            				File oldfile = new File(file);
            				oldfile.delete();
            				
            				//Then remove _ on encrypted file
            				File newfile = new File(file+"_");
            				newfile.renameTo(new File(file));
            				*/
                       	  }
                       });
            
        	return null;
        }
       
        protected void onPostExecute(String ppath) {
            
        }
	}

}
