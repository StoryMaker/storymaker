package org.codeforafrica.timby.listeningpost.encryption;


import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;

import org.codeforafrica.timby.listeningpost.model.Media;
import org.json.JSONArray;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class EncryptionBackground extends Service {
	String message;
	String file;
	Media media;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Timer timer = new Timer();
        TimerTask updateProfile = new mainTask();
        timer.scheduleAtFixedRate(updateProfile, 0, 1000);
  	}
	
	private class mainTask extends TimerTask
    { 
        public void run() 
        {
        	 new Thread(new Runnable() {
				public void run() {
					try {
						if(!isServiceRunning()){
			        		String filepath = null;
			        		//Find first file
			                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			                JSONArray jsonArray2 = null;
			                JSONArray jsonArray3 = new JSONArray();
			                try {
			                    jsonArray2 = new JSONArray(prefs.getString("eQ", "[]"));
				        		//Log.d("running", "not running"+jsonArray2.length());

			                    if(jsonArray2.length()>0){
			                    	//Get relevant media
			                    	media = Media.get(getApplicationContext(), Integer.parseInt(jsonArray2.getString(0)));
			 
				                    filepath = media.getPath();
				                    
				                  //Remove from list
				                    
				                    for (int i = 0; i < jsonArray2.length(); i++) {
				                        if(i!=0){
				                        	jsonArray3.put(jsonArray2.getString(i));
				                        	media.setEncrypted(1);
				                        	media.save();
				                        }
				                   }
			                    }
			                } catch (Exception e) {
			                    e.printStackTrace();
			                }
			                
			                /*
			                
			                */
			                if(jsonArray2.length()>0){
			                	
			                	
			                		Editor editor = prefs.edit();
			                		editor.putString("eQ", jsonArray3.toString());
					                editor.commit();
			                	
				                
				                
			                	
			                	Intent startMyService= new Intent(getApplicationContext(), EncryptionService.class);
				                startMyService.putExtra("filepath", filepath);
				                startMyService.putExtra("mode", Cipher.ENCRYPT_MODE);
				                startService(startMyService);
			                }
			        	}
					} catch (NullPointerException ex) { 
					    System.out.println("NPE encountered in body"); 
					} catch (Throwable ex) {
					    System.out.println("Regular Throwable: " + ex.getMessage());
					} finally {
					    //System.out.println("Final throwable");
					}
				}}).start();
        	
        }
    }   
	
	public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (EncryptionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
	}
}
