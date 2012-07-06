package info.guardianproject.mrapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.ffmpeg.android.FFMPEGWrapper;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    public void testFFMPEG () throws FileNotFoundException, IOException, Exception
    {
    	FFMPEGWrapper ffmpegw = new FFMPEGWrapper (this);
    	
    	ArrayList<MediaDesc> mdlist = new ArrayList<MediaDesc>();
    	
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = "somefile.mp4";
    	mdesc.startTime = "00:00:03";
    	mdesc.duration = 10;
    	mdlist.add(mdesc);
    	
    	mdesc = new MediaDesc ();
    	mdesc.path = "someotherfile.mp4";
    	mdesc.startTime = "00:00:00";
    	mdesc.duration = 5;
    	mdlist.add(mdesc);
    	
    	MediaDesc mdout = new MediaDesc ();
    	mdesc.path = "allthefilestogether.mp4";
    	mdlist.add(mdesc);
    	
    	ffmpegw.concatAndTrimFiles(mdlist, mdout, new ShellCallback() {

			@Override
			public void shellOut(char[] msg) {
				
				System.out.println(msg);
				
			}
    	});
    
    }
    
}