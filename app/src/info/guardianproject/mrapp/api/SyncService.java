package info.guardianproject.mrapp.api;

import info.guardianproject.mrapp.ConnectionDetector;
import info.guardianproject.mrapp.HomePanelsActivity;
import info.guardianproject.mrapp.encryption.Encryption;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;
import info.guardianproject.mrapp.model.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncService extends Service {
	String token;
	String user_id;
	private static String KEY_SUCCESS = "status";
	private static String KEY_ID = "report_id";
	ProgressBar pDialog;
	
	createReport create_Report=null;
	//updateReport update_Report=null;
	
	createObject create_Object=null;
	//updateObjet update_Object=null;
	
	private ArrayList<Report> mListReports;
		
 	Button done;
 	TextView log;
 	AsyncTask<String, String, String> check_token;
 	
 	//Connection detector class
    ConnectionDetector cd;
    //flag for Internet connection status
    Boolean isInternetPresent = false;
    Timer timer;
    SharedPreferences prefs;
    String delete_after_sync;
    @Override
    public IBinder onBind(Intent arg0) {
          return null;
    }
    @Override
    public void onCreate() {
          super.onCreate();
  	    
          showNotification("Syncing...");
          cd = new ConnectionDetector(getApplicationContext());
        	mListReports = Report.getAllAsList(getApplicationContext());

        //get Internet status
        //  isInternetPresent = cd.isConnectingToInternet();
          
         // if(!isInternetPresent){
         // 	Toast.makeText(this, "You have no connection!", Toast.LENGTH_LONG).show();
          //}else{
          	check_token = new checkToken().execute();
         // }
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	delete_after_sync = prefs.getString("delete_after_sync","0");

  		int delay = 3000; // delay for 1 sec. 
  		int period = 1000; // repeat every 10 sec. 
  		
  		
  		timer = new Timer(); 
  		timer.scheduleAtFixedRate(new TimerTask(){ 
  		        public void run() 
  		        { 
  		        	checkTasks();
  		        } 
  		    }, delay, period); 
          
    }
    
    @Override
    public void onDestroy() {
          super.onDestroy();
          //Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
    }
    private void showNotification(String message) {
    	 CharSequence text = message;
    	 Notification notification = new Notification(R.drawable.ic_menu_upload, text, System.currentTimeMillis());
    	 PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
    	                new Intent(this, HomePanelsActivity.class), 0);
    	notification.setLatestEventInfo(this, "Sync",
    	      text, contentIntent);
    	NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.notify("service started", 0, notification);
		}
    public void deleteReports(){
		for(int i = 0; i<mListReports.size(); i++){
			if(mListReports.get(i)!=null){
			 	mListReports.get(i).delete();
			 	ArrayList<Project> mListProjects;
				mListProjects = Project.getAllAsList(getApplicationContext(), i);
			 	for (int j = 0; j < mListProjects.size(); j++) {
					if(mListProjects.get(j)!=null){
						mListProjects.get(j).delete();
					}
			 	}
			}
		}
	}

    public void loopReports(){
    	for (int i = 0; i < mListReports.size(); i++) {
		 	if(mListReports.get(i)!=null){
			Report report = mListReports.get(i);
			String location = report.getLocation();
			
			String lat = "0";
			String lon = "0";
			
			if(location.contains(", ")){
				String[] locations = location.split(", ");
				lat = locations[0];
				lon = locations[1];
			}
						
			String title = report.getTitle();
			
			int issue_int;
			if(Integer.parseInt(report.getIssue())==0){
				issue_int=1;
			}else{
				issue_int = Integer.parseInt(report.getIssue());
			}
			
			int sector_int;
			if(Integer.parseInt(report.getSector())==0){
				sector_int=1;
			}else{
				sector_int = Integer.parseInt(report.getSector());
			}
			
			String issue = String.valueOf(issue_int);
			String sector = String.valueOf(sector_int);
			
			String entity = report.getEntity();
			
			String date = report.getDate();
			int rid = report.getId();
			String description = report.getDescription();
			int serverID = Integer.parseInt(report.getServerId());
			if(serverID==0){
				//report is not created yet, create and grab server id
				//new createReport().execute();
				ReportTaskParams params = new ReportTaskParams(rid, serverID, title, issue, sector, entity, lat, lon, date, description);
			 	createReport myTask = new createReport();
			 	myTask.execute(params);		 
			}else{
				/*
				//update report
				//get report servid
				//new updateReport().execute();
				ReportTaskParams params = new ReportTaskParams(rid, serverID, title, issue, sector, entity, lat, lon, date, description);
			 	update_Report = new updateReport();
			 	update_Report.execute(params);	
			 	*/	 
			}
		  }
		}
	}
    private static class ReportTaskParams {
	    String title;
	    String lat;
	    String lon;
	    String date;
	    String description;
	    int rid;
	    int serverID;
	    
	    ReportTaskParams(int rid, int serverID, String title, String issue, String sector, String entity, String lat, String lon, String date, String description) {
	        this.title = title;
	        this.lat = lat;
	        this.lon=lon;
	        this.date=date;
	        this.description=description;
	        this.rid = rid;
	        this.serverID=serverID;
	    }
	}
	
	private static class MyTaskParams {
	    String ppath;
	    String ptype;
	    String optype;
	    String ptitle;
	    String pid;
	    String preportid;
	    MyTaskParams(String ppath, String ptype, String optype, String ptitle, String pid, String preportid) {
	        this.ppath = ppath;
	        this.ptype = ptype;
	        this.optype = optype;
	        this.pid = pid;
	        this.preportid=preportid;
	        this.ptitle=ptitle;
	    }
	}
	
	public void uploadMedia(int rid, int serverid){
		ArrayList<Project> mListProjects;
		mListProjects = Project.getAllAsList(this, rid);
	 	for (int j = 0; j < mListProjects.size(); j++) {
	 		Project project = mListProjects.get(j);
	 		
	 		Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
	 		Media media = mediaList[0];
	 		String ppath = media.getPath();
		 	String ptype = media.getMimeType();
		 	
		 	String optype = "video";
		 	if(ptype.contains("image")){
		 		optype = "image";
		 	}else if(ptype.contains("video")){
		 		optype = "video";
		 	}else if(ptype.contains("audio")){
		 		optype = "audio";
		 	}
		 	String file = ppath;
	 		Cipher cipher;
			try {				
				cipher = Encryption.createCipher(Cipher.DECRYPT_MODE);
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
			
		 	String ptitle = project.getTitle();
		 	String pid = String.valueOf(project.getId());
		 	String preportid = String.valueOf(serverid);
		 	
		 	//new createObject().execute();		
		 	MyTaskParams params = new MyTaskParams(ppath, ptype, optype, ptitle, pid, preportid);
		 	create_Object = new createObject();
		 	create_Object.execute(params);	
	 	}
	}
	/*
	public void updateMedia(int rid, int serverid){
		
		ArrayList<Project> mListProjects;
		mListProjects = Project.getAllAsList(this, rid);
	 	for (int j = 0; j < mListProjects.size(); j++) {
	 		Project project = mListProjects.get(j);
	 		
	 		Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
	 		
	 		Media media = mediaList[0];
	 		
	 		String ppath = media.getPath();
	 		
	 		String ptype = media.getMimeType();
		 	
		 	String optype = "video";
		 	if(ptype.contains("image")){
		 		optype = "image";
		 	}else if(ptype.contains("video")){
		 		optype = "video";
		 	}else if(ptype.contains("audio")){
		 		optype = "video";
		 	}
		 	
		 	String ptitle = project.getTitle();
		 	String pid = String.valueOf(project.getObjectID());
		 	String psequence = String.valueOf(j+1);
		 	String preportid = String.valueOf(serverid);
		 	
		 	if(String.valueOf(project.getObjectID())!=""){
			 	//new updateObject().execute();	
			 	MyTaskParams params = new MyTaskParams(ppath, ptype, optype, ptitle, pid, preportid);
			 	update_Object = new updateObject();
			 	update_Object.execute(params);	
		 	}else{
		 		//file has not been uploaded yet
		 		String file = ppath;
		 		Cipher cipher;
				try {
					cipher = Encryption.createCipher(Cipher.DECRYPT_MODE);
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
				
			 	//new createObject().execute();		
			 	MyTaskParams params = new MyTaskParams(ppath, ptype, optype, ptitle, pdate, pid, psequence, preportid);
			 	create_Object = new createObject();
			 	create_Object.execute(params);
		 	}
		 }
	 	
	}
	
	private static class MyTaskParams {
	    String ppath;
	    String ptype;
	    String optype;
	    String ptitle;
	    String pdate;
	    String pid;
	    String psequence;
	    String preportid;
	    MyTaskParams(String ppath, String ptype, String optype, String ptitle, String pdate, String pid, String psequence,String preportid) {
	        this.ppath = ppath;
	        this.pdate = pdate;
	        this.ptype = ptype;
	        this.optype = optype;
	        this.pid = pid;
	        this.pdate=pdate;
	        this.psequence=psequence;
	        this.preportid=preportid;
	        this.ptitle=ptitle;
	    }
	}
	class updateObject extends AsyncTask<MyTaskParams, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             //log.append("---Updating media: \n");
            //pDialog.setVisibility(View.VISIBLE); 
        }
        protected String doInBackground(MyTaskParams... params) {
        	String ppath = params[0].ppath;
        	String optype = params[0].optype;
        	String ptype = params[0].ptype;
    	    String ptitle = params[0].ptitle;;

    	    String pid = params[0].pid;;

    	    String preportid = params[0].preportid;;
    	    
        	APIFunctions apiFunction = new APIFunctions();
        	Log.d("j", String.valueOf(ppath));
			JSONObject json = apiFunction.updateObject(token, user_id, ptitle, psequence, preportid, ptype, optype, pid, pdate, ppath, getApplicationContext());
		
			try {
				String res = json.getString(KEY_SUCCESS); 
					if(res.equals("OK")){
						JSONObject json_report = json.getJSONObject("message");
						String objectid = json_report.getString(KEY_ID);
						
					}else{
						//Some error message. Not sure what yet.
						
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			
			
        	return ppath;
        }
       
        protected void onPostExecute(String ppath) {
           //log.append("Updated media object "+ppath+"\n");
           checkTasks();
        }
	}
	*/
	class createObject extends AsyncTask<MyTaskParams, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute(); 
            //log.append("---Updating media: \n");
            //pDialog.setVisibility(View.VISIBLE); 
        }
        protected String doInBackground(MyTaskParams... params) {
        	String ppath = params[0].ppath;
        	String optype = params[0].optype;
        	String ptype = params[0].ptype;
    	    String ptitle = params[0].ptitle;;
 
    	    String pid = params[0].pid;;

    	    String preportid = params[0].preportid;;
    	    
    	    Log.d("optype", optype);
    	    Log.d("ptype", ptype);
        	
    	    APIFunctions apiFunction = new APIFunctions();
			JSONObject json = apiFunction.newObject(token, user_id, ptitle, preportid, ptype, optype, pid, ppath);
			
			try {
				String res = json.getString(KEY_SUCCESS); 
					if(res.equals("OK")){
						
						JSONObject json_report = json.getJSONObject("message");
						
						//JSONObject details = json_report.getJSONObject(0);//json_report.getString(KEY_ID);
						int object_id = Integer.parseInt(json_report.getString("object_id"));
						
						//TODO: Get rid of sequence id as object property | new API
						String sequence_id = "0";//String.valueOf(json_report.getString("sequence_id"));
						
						//Update object id and sequence id
						Project project= (Project)(new ProjectTable()).get(getApplicationContext(),Integer.parseInt(pid));
						project.setObjectID(object_id);
						project.save();
						/*
						//Re-encrypt
						Intent startMyService= new Intent(getApplicationContext(), EncryptionService.class);
				        startMyService.putExtra("filepath", ppath);
				        startMyService.putExtra("mode", Cipher.ENCRYPT_MODE);
				        startService(startMyService);
				        */
						String file = ppath;
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
					}else{
						//Some error message. Not sure what yet.
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			
			
        	return ppath;
        }
        protected void onPostExecute(String ppath) {
        	//Delete decrypted file and 
        	//log.append("Uploaded media object "+ppath+"\n");
        	checkTasks();
        }
	}
	
	class createReport extends AsyncTask<ReportTaskParams, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pDialog.setVisibility(View.VISIBLE); 
        }
        
        protected String doInBackground(ReportTaskParams... params) {
        	String title = params[0].title;
    	    String lat = params[0].lat;;
    	    String lon = params[0].lon;;
    	    String date = params[0].date;;
    	    String description = params[0].description;;
    	    int rid = params[0].rid;
        	APIFunctions apiFunction = new APIFunctions();
			JSONObject json = apiFunction.newReport(token, user_id, title, lat, lon, date, description);
			//Log.d("Values passed", "Token: "+token+" user id: "+user_id+" title "+title+" issue "+issue+" sector "+sector+" entity "+entity+" lat "+lat+" lon "+lon+" date "+date+" description "+description);
			try {
				String res = json.getString(KEY_SUCCESS); 
					if(res.equals("OK")){
						JSONObject json_report = json.getJSONObject("message");//json.getJSONObject("message");
						//JSONObject serverid = json_report.getJSONObject("id");//json_report.getString(KEY_ID);
						String srid = String.valueOf(json_report.getString(KEY_ID));
						//Update report with server id 
						Report report = Report.get(getApplicationContext(), rid);
						report.setServerId(srid);
						report.save();
						
						//if(!isInternetPresent){
				        //	Toast.makeText(getApplicationContext(), "You have no connection!", Toast.LENGTH_LONG).show();
				       // }else{
							//Upload corresponding media files :O
							//uploadEntities(report.getId(), Integer.parseInt(srid));
							//uploadMedia(report.getId(), Integer.parseInt(srid));
				        //}
					}else{
						//Some error message. Not sure what yet.
						
					}
					
				}catch(JSONException e){
					e.printStackTrace();
				}
			//Add to log
			
			return title;
        }
        

        protected void onPostExecute(String title) {
        	//log.append("Created report "+title+"\n");
        	checkTasks();
        }
	}
	/*
	class updateReport extends AsyncTask<ReportTaskParams, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pDialog.setVisibility(View.VISIBLE); 
        }
        
        protected String doInBackground(ReportTaskParams... params) {
        	String title = params[0].title;
        	String issue = params[0].issue;
        	String sector= params[0].sector;
    	    String entity = params[0].entity;;
    	    String lat = params[0].lat;;
    	    String lon = params[0].lon;;
    	    String date = params[0].date;;
    	    String description = params[0].description;
    	    int rid = params[0].rid;
    	    int serverID = params[0].serverID;
        	APIFunctions apiFunction = new APIFunctions();
			JSONObject json = apiFunction.updateReport(token, user_id, title, issue, sector, entity, lat, lon, date, description, String.valueOf(serverID), getApplicationContext());
			
			try {
				String res = json.getString(KEY_SUCCESS); 
					if(res.equals("OK")){
						JSONObject json_report = json.getJSONObject("message");//json.getJSONObject("message");
						//JSONObject serverid = json_report.getJSONObject("id");//json_report.getString(KEY_ID);
						String severID = String.valueOf(json_report.getString("report_id"));
						
							updateMedia(rid, serverID);
				        //}
					}else{
						//Some error message. Not sure what yet.
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			//Add to log
        	return title;
        }
        

        protected void onPostExecute(String title) {
        	//log.append("Updated report "+title+"\n");
        	checkTasks();
        }
	}
	*/
	class checkToken extends AsyncTask<String, String, String> {
		 
  
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pDialog.setVisibility(View.VISIBLE);
            
        }
        protected String doInBackground(String... args) {
        	/*
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        token = settings.getString("token",null);
	        user_id = settings.getString("user_id",null);
	        
	        if(token==null){
	        	showNotification("Token expired! Log in with internet and try again!");
				//Toast.makeText(getApplicationContext(), "Token expired! Login and try syncing again.", Toast.LENGTH_LONG).show();

	        	Intent login = new Intent(getApplicationContext(), LoginPreferencesActivity.class);
				login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(login);
				
	        }else{
		        APIFunctions apiFunction = new APIFunctions();
		        JSONObject json = apiFunction.checkTokenValidity(user_id, token, getApplicationContext());
		        
		        try{
		        	String res = json.getString(KEY_SUCCESS); 
					if(res.equals("OK")){
					*/	loopReports();
					/*}else{
						showNotification("Token Expired");

					}
		        }catch (JSONException e) {
					e.printStackTrace();
				}
	        }*/
	        return null;
        }
        

        protected void onPostExecute(String file_url) {

        }
	}
	
	public void checkTasks(){
		int tasks = 0;
		if((create_Report!=null)){
			if(create_Report.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}/*
		if(update_Report!=null){
			if(update_Report.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}
		*/
		if(create_Object!=null){
			if(create_Object.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}
		/*
		if(update_Object!=null){
			if(update_Object.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}
		
		if(check_token!=null){
			if(check_token.getStatus() == AsyncTask.Status.RUNNING){
				tasks++;
			}
		}
		*/
		if(tasks<1){
			//End
			showNotification("Syncing complete!");
			if(delete_after_sync.equals("1")){
				//delete all reports
				deleteReports();
			}
			if(timer!=null){
				timer.cancel();
			}
			this.stopSelf();
		}
	}
}
