package org.storymaker.app.ui;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class AudioActivity extends Activity implements OnClickListener {
/** Called when the activity is first created. */
private Button sampleButton;
private FileObserver mFileObserver;
private Vector<String> audioFileNames;

@Override
public void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
   audioFileNames = new Vector<String>();
   LinearLayout finalContainer = new LinearLayout(this);
   sampleButton = new Button(this);
   sampleButton.setOnClickListener(this);
   sampleButton.setText("Start Audio Intent");
   finalContainer.addView(sampleButton);
   setContentView(finalContainer);
   addObserver();

}

private void addObserver() {
   this.mFileObserver = new FileObserver("/sdcard/Sounds/") {
       @Override
       public void onEvent(int event, String path) {
           if (event == FileObserver.CREATE) {
               if (path != null) {
                   int index = path.indexOf("tmp");
                   String tempFileName = (String) path.subSequence(0,
                           index - 1);
                   audioFileNames.add(tempFileName);

               }
           } else if (event == FileObserver.DELETE) {
               if (path != null) {
                   int index = path.indexOf("tmp");
                   String tempFileName = (String) path.subSequence(0,
                           index - 1);
                   if (audioFileNames.contains(tempFileName)) {
                       audioFileNames.remove(tempFileName);
                   }
               }

           }
       }
   };
}

private void readFile(String fileName) {

   File attachment = new File("/sdcard/Sounds/" + fileName);
   if (attachment.exists()) {
       FileInputStream fis;
       try {
           fis = new FileInputStream(attachment);
           byte[] bytes = new byte[(int) attachment.length()];
           try {
               fis.read(bytes);
               fis.close();

               attachment.delete();

               saveMedia("Test" + fileName, bytes);

           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }

       } catch (FileNotFoundException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
   }
}

@Override
protected void onResume() {
   // TODO Auto-generated method stub
   super.onResume();
   mFileObserver.startWatching();
}

public void saveMedia(String fileName, byte[] data) {

   String imagePath = "/sdcard/sam/";
   System.out.println("Inside Folder");


   File file = new File(imagePath, fileName);
   System.out.println("File Created");

   FileOutputStream fileOutputStream = null;
   try {
       fileOutputStream = new FileOutputStream(file);
       DataOutputStream dataOutputStream = new DataOutputStream(
               fileOutputStream);
       System.out.println("Writting File");
       dataOutputStream.write(data, 0, data.length);
       System.out.println("Finished writting File");
       dataOutputStream.flush();
       dataOutputStream.close();
   } catch (FileNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
   } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
   }
}

public void onClick(View arg0) {
   // TODO Auto-generated method stub
   Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
   startActivityForResult(intent, 2); 
}

@Override
protected void onActivityResult(int requestCode, int resultCode,
       Intent intent) {
   // TODO Auto-generated method stub
   if (requestCode == 2) {
       if (mFileObserver != null) {
           mFileObserver.stopWatching();
       }
       Enumeration<String> audioFileEnum = audioFileNames.elements();
       while (audioFileEnum.hasMoreElements()) {
           readFile((String) audioFileEnum.nextElement());
       }
   }
}}
