package info.guardianproject.mrapp.export;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.HomePanelsActivity;
import info.guardianproject.mrapp.encryption.Encryption;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;

import com.google.common.io.Files;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Export2SDService extends Service {
	private ArrayList<Report> mListReports;
	private ArrayList<Project> mListProjects;
	String data = "";
	String ext;
	int BUFFER = 2048;
	String delete_after_export;
	int reportscount = 0;
	String eI;
	SharedPreferences pref;
	String zipName;
	public File newFolder;
	@Override
    public void onCreate() {
          super.onCreate();
          
	}
	@SuppressWarnings("deprecation")
	
	@Override
	  public void onStart(Intent intent, int startId) {
	      super.onStart(intent, startId);
	       Bundle extras = intent.getExtras(); 
	       eI = extras.getString("includeExported");
	       
	       showNotification("Exporting to SD...");
	       
	        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	      	delete_after_export = pref.getString("delete_after_export",null);
	      	new export2SD().execute();
	}
	private void showNotification(String message) {
   	 CharSequence text = message;
   	 Notification notification = new Notification(R.drawable.ic_menu_send, text, System.currentTimeMillis());
   	 PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, HomePanelsActivity.class), 0);
   	 notification.setLatestEventInfo(this, "Export to SD", text, contentIntent);
   	NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.notify("service started", 1, notification);
		}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getIMEI(Context context){

	    TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE); 
	    String imei = mngr.getDeviceId();
	    return imei;
	}
	class export2SD extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}
		protected String doInBackground(String... args) {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userid = prefs.getString("user_id", "0");
			            
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
            String timestamp = s.format(new Date());
            
            if(eI.equals("0")){
            	mListReports = Report.getAllAsList_EI(getApplicationContext(), eI);
            }else{
            	mListReports = Report.getAllAsList(getApplicationContext());
            }
		 	
            reportscount = mListReports.size();

			zipName = "timby"+userid+"-"+String.valueOf(reportscount)+"-"+timestamp;
			//create folder and transfer all content here
			newFolder = new File(Environment.getExternalStorageDirectory(), zipName);
			newFolder.mkdirs();
						
			ext = String.valueOf(Environment.getExternalStorageDirectory());
		 	//ext += "/"+AppConstants.TAG;
		 	
			//Begin "XML" file
		 	data += "<?xml version='1.0' encoding='UTF-8'?>\n";
		 	data += "<reports>\n";
		 	data += "<imei>"+getIMEI(getApplicationContext())+"</imei>";
		 	data += "<user_id>"+userid+"</user_id>";
		 			 	
			 
			 for (int i = 0; i < reportscount; i++) {
				 	//check if report actually exists
				 	if(mListReports.get(i)!=null){
					 	
					 	data += "<report>\n";
					 	
					 	Report report = mListReports.get(i);
					 	
					 	//TODO: use xml output to set exported
					 	report.setExported("1");
					 	report.save();
					 	
					 	data += "<id>"+String.valueOf(report.getId())+"</id>\n";
					 	
					 	//create report folder
					 	File rFolder = new File(newFolder, "/"+String.valueOf(report.getId()));
					 	rFolder.mkdirs();
					 	
					 	data += "<report_title>"+report.getTitle()+"</report_title>\n";
					 	
					 	String issue = report.getIssue();
					 	if(issue.equals("0")){
					 		issue = "1";
					 	}
					 	data += "<category>"+issue+"</category>\n";
					 	
					 	String category = report.getSector();
					 	if(category.equals("0")){
					 		category = "1";
					 	}
					 	data += "<sector>"+category+"</sector>\n";
					 	
					 	data += "<entity>"+report.getEntity()+"</entity>\n";
					 	data += "<location>"+report.getLocation()+"</location>\n";
					 	data += "<report_date>"+report.getDate()+"</report_date>\n";
					 	data += "<description>"+report.getDescription()+"</description>\n";
					 	data += "<report_objects>\n";
					 	
					 	mListProjects = Project.getAllAsList(getApplicationContext(), report.getId());
					 	for (int j = 0; j < mListProjects.size(); j++) {
					 		Project project = mListProjects.get(j);
						 	
						 	
						 	Log.d("I'm here", "I'm here - 1");
						 	Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
						 	for (Media media: mediaList){
						 		
						 		if(media!=null){
							 		data += "<object>\n";
								 	data += "<object_id>"+media.getId()+"</object_id>\n";
								 	data += "<object_title>"+project.getTitle()+"</object_title>\n";
								 	
							 		String path = media.getPath();
								 	Log.d("I'm here", "I'm here " + path);
	
							 		String file = path;
							 		
							 		String filename = (new File(path)).getName();
							 		
									String outfile = rFolder.getPath().toString()+ "/" +filename;
									
									Log.d("out, in", "out:" + outfile + " in: " + file);
									
							 		//Decrypt file if encrypted
									if(media.getEncrypted()!=0){
								 		Cipher cipher;
										try {
											cipher = Encryption.createCipher(Cipher.DECRYPT_MODE);
											Encryption.applyCipher(file, outfile, cipher);
										}catch (Exception e) {
											// TODO Auto-generated catch block
											Log.e("Encryption error", e.getLocalizedMessage());
											e.printStackTrace();
										}
									}else{
										//copy file to new destination
										try {
											Files.copy(new File(file), new File(outfile));
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								 	data += "<object_media>"+path+"</object_media>\n";
									//}
							 		data += "<object_type>"+media.getMimeType()+"</object_type>\n";
							 		data += "</object>\n";
						 		}
						 	}
							
					 	}
					 	data += "</report_objects>\n";
					 	data += "</report>\n";
					 	
				 	}
				}
			 data += "</reports>";
			 
			 writeToFile(data);
			 
			 	
			//Now create new zip
			zipFileAtPath(newFolder.getAbsolutePath(), String.valueOf(getSD())+"/"+zipName+".zip");
			
			//encrypt zip file
	    	String encrypt_zip_files = prefs.getString("encrypt_zip_files","0");
	    	if(encrypt_zip_files.equals("1")){
	    		
	    		String file = String.valueOf(getSD())+"/"+zipName+".zip";
				
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
	    	}
			
	    	//delete zip source
	    	File zipsource = new File(newFolder.getAbsolutePath());
			DeleteRecursive(zipsource);
	    	
			
			return null;
		}
			
	protected void onPostExecute(String file_url) {
			showNotification("Exported Successfully!");
			if(delete_after_export.equals("1")){
				deleteReports();
			}
			endExporting();
		}
	}
	
	void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	 public void deleteReports(){
			for(int i = 0; i<mListReports.size(); i++){
				if(mListReports.get(i)!=null){
				 	mListReports.get(i).delete();
				 	ArrayList<Project> mListProjects;
					mListProjects = Project.getAllAsList(getApplicationContext(), i);
				 	for (int j = 0; j < mListProjects.size(); j++) {
				 		mListProjects.get(j).delete();
				 	}
				}
			}
		}

	 public void endExporting(){
    	  this.stopSelf();
      }
	 
	public void writeToFile(final String fileContents) {
		try {
	            FileWriter out = new FileWriter(new File(newFolder, "/db.xml"));
	            out.write(fileContents);
	            out.close();
        	}catch (IOException e){
        		Log.d("Write Error!", e.getLocalizedMessage());
        	}

    }

	public boolean zipFileAtPath(String sourcePath, String toLocation) {
	    // ArrayList<String> contentList = new ArrayList<String>();
	    File sourceFile = new File(sourcePath);
	    try {
	        BufferedInputStream origin = null;
	        FileOutputStream dest = new FileOutputStream(toLocation);
	        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
	                dest));
	        if (sourceFile.isDirectory()) {
	            zipSubFolder(out, sourceFile, sourceFile.getParent().length());
	        }else {
	            byte data[] = new byte[BUFFER];
	            FileInputStream fi = new FileInputStream(sourcePath);
	            origin = new BufferedInputStream(fi, BUFFER);
	            ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
	            out.putNextEntry(entry);
	            int count;
	            while ((count = origin.read(data, 0, BUFFER)) != -1) {
	                out.write(data, 0, count);
	            }
	        }
	        out.close();
	    }catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    return true;
	}

	private void zipSubFolder(ZipOutputStream out, File folder,
	        int basePathLength) throws IOException {
	    File[] fileList = folder.listFiles();
	    BufferedInputStream origin = null;
	    for (File file : fileList) {
	        if (file.isDirectory()) {
	            zipSubFolder(out, file, basePathLength);
	        } else {
	            byte data[] = new byte[BUFFER];
	            String unmodifiedFilePath = file.getPath();
	            String relativePath = unmodifiedFilePath
	                    .substring(basePathLength);
	            Log.i("ZIP SUBFOLDER", "Relative Path : " + relativePath);
	            FileInputStream fi = new FileInputStream(unmodifiedFilePath);
	            origin = new BufferedInputStream(fi, BUFFER);
	            ZipEntry entry = new ZipEntry(relativePath);
	            out.putNextEntry(entry);
	            int count;
	            while ((count = origin.read(data, 0, BUFFER)) != -1) {
	                out.write(data, 0, count);
	            }
	            origin.close();
	        }
	    }
	}

	public String getLastPathComponent(String filePath) {
	    String[] segments = filePath.split("/");
	    String lastPathComponent = segments[segments.length - 1];
	    return lastPathComponent;
	}
	
	public File getSD(){
		File extStorage = null;
		
		if(android.os.Build.VERSION.SDK_INT >= 19){
			extStorage = Environment.getExternalStorageDirectory();
		}else{
			if((new File("/mnt/external_sd/")).exists()){
				extStorage = new File("/mnt/external_sd/");
			}else if((new File("/mnt/extSdCard/")).exists()){
				extStorage = new File("/mnt/extSdCard/");
			}else{
				extStorage = Environment.getExternalStorageDirectory();
			}
		}
		
		return extStorage;
	}
}
