package org.codeforafrica.timby.listeningpost.encryption;


import java.io.File;
import java.util.ArrayList;

import javax.crypto.Cipher;

import org.codeforafrica.timby.listeningpost.HomePanelsActivity;
import org.codeforafrica.timby.listeningpost.encryption.Encryption;
import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.Project;
import org.codeforafrica.timby.listeningpost.model.Report;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class EncryptionService extends Service{
	String message;
	String file;

	@Override
    public void onCreate() {
          super.onCreate();
          
          
    }
	  @Override
		public int onStartCommand(Intent intent, int flags, int startId){
			super.onStartCommand(intent, flags, startId);
	       Bundle extras = intent.getExtras(); 
	       file = extras.getString("filepath");
	       message = "Encryption started...";
	       showNotification(message);
	       new encryptFile().execute();
		return startId;
	       
	  }
	  class encryptFile extends AsyncTask<String, String, String> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
			}
			protected String doInBackground(String... args) {
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
		       //  mListView.setAdapter(aaReports);
				return null;
			}
		protected void onPostExecute(String file_url) {

			message = "Encryption completed successfully!";
			showNotification(message);
			endEncryption();
				
			}
		}

	public void endEncryption(){
        
		this.stopSelf();
	}
	
	private void showNotification(String message) {
		
	   	 CharSequence text = message;
	   	 Notification notification = new Notification(R.drawable.ic_secure, text, System.currentTimeMillis());
	   	 PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	   	                new Intent(this, HomePanelsActivity.class), 0);
	   	notification.setLatestEventInfo(this, "LP: Encryption",
	   	      text, contentIntent);
	   	NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			nm.notify("service started", 2, notification);
			
	}
			
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
