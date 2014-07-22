package org.codeforafrica.timby.listeningpost.ui;

import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.AppConstants;
import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.media.MediaClip;
import org.codeforafrica.timby.listeningpost.media.MediaManager;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.DrawTextVideoFilter;
import org.ffmpeg.android.filters.FadeVideoFilter;
import org.ffmpeg.android.filters.VideoFilter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MediaView extends BigImageLabelView implements OnClickListener, OnDismissListener, ShellCallback {

	private MediaManager mMediaManager;
	private MediaClip mMediaClip;
	private Dialog mDialog;
	private EditText mEditTitle, mEditStartTime, mEditEndTime, mEditFadeIn, mEditFadeOut;
	private Button mBtnApply;
	private Context mContext;
	private String mTitle;
	
    private int current, total;

	public MediaView(Context context, MediaManager mediaManager, MediaClip mediaClip, String title, Bitmap image,
			int fontColor, int bgColor) {
		super(context, title, image, fontColor, bgColor);

		mTitle = title;
		mMediaManager = mediaManager;
		mMediaClip = mediaClip;
		mContext = context;
		
		setOnClickListener (this);
	}

	public void showSettingsDialog ()
	{
		if (mDialog == null)
		{
			mDialog = new Dialog(mContext, R.style.CustomDialogTheme);
			mDialog.setContentView(R.layout.dialog_media_view);
			mDialog.setOnDismissListener(this);
			
			
	
		}
		
		mDialog.show();
		
		mEditTitle = (EditText)mDialog.findViewById(R.id.editTitle);
		mEditStartTime = (EditText)mDialog.findViewById(R.id.editStartTime);
		mEditEndTime = (EditText)mDialog.findViewById(R.id.editEndTime);
		mEditFadeIn = (EditText)mDialog.findViewById(R.id.editFadeIn);
		mEditFadeOut = (EditText)mDialog.findViewById(R.id.editFadeOut);
		mBtnApply = (Button)mDialog.findViewById(R.id.btnApply);
		mBtnApply.setOnClickListener(new OnClickListener ()
        {

            @Override
            public void onClick(View v) {
                updateSettings();
                try {
                    mDialog.dismiss();
                    mDialog = null;
                } catch (Exception e) {
                    // ignore: http://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager
                }
            }

        });
	}

	private void updateSettings ()
	{
		ArrayList<VideoFilter> list = new ArrayList<VideoFilter>();
		
		if (mEditTitle.getText().length()>0)
		{
			DrawTextVideoFilter vfTitle = new DrawTextVideoFilter(mEditTitle.getText().toString());
			list.add(vfTitle);
		}
		
		if (mEditStartTime.getText().length()>0)
		{
			mMediaClip.mMediaDescOriginal.startTime = mEditStartTime.getText().toString();
			
		}
		
		if (mEditEndTime.getText().length()>0)
		{
			mMediaClip.mMediaDescOriginal.duration = mEditEndTime.getText().toString();
			
		}
		
		if (mEditFadeIn.getText().length()>0)
		{
	    	FadeVideoFilter vfFadeIn = new FadeVideoFilter("in",0,Integer.parseInt(mEditFadeIn.getText().toString()));
			list.add(vfFadeIn);
		}
		
		if (mEditFadeOut.getText().length()>0)
		{
	    	FadeVideoFilter vfFadeOut = new FadeVideoFilter("out",-1,Integer.parseInt(mEditFadeOut.getText().toString()));
			list.add(vfFadeOut);
		}
			
		if (list.size() > 0)
		{
			mMediaClip.mMediaDescOriginal.videoBitrate = 1200;
			mMediaClip.mMediaDescOriginal.videoFilter = VideoFilter.format(list);
		}
		
		current = 0;
		total = 0; 
	}
	
	@Override
	public void onClick(View v) {
		showSettingsDialog();
		
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		
		//updateSettings ();
		
		
	}
	
	
	@Override
	public void shellOut(String line) {
		
		if (!line.startsWith("frame"))
			Log.d(AppConstants.TAG, line);
		
		int idx1;
		String newStatus = null;
		int progress = 0;
		
		if ((idx1 = line.indexOf("Duration:"))!=-1)
		{
			int idx2 = line.indexOf(",", idx1);
			String time = line.substring(idx1+10,idx2);
			
			int hour = Integer.parseInt(time.substring(0,2));
			int min = Integer.parseInt(time.substring(3,5));
			int sec = Integer.parseInt(time.substring(6,8));
			
			total = (hour * 60 * 60) + (min * 60) + sec;
			
			newStatus = line;
			progress = 0;
		}
		else if ((idx1 = line.indexOf("time="))!=-1)
		{
			int idx2 = line.indexOf(" ", idx1);
			String time = line.substring(idx1+5,idx2);
			newStatus = line;
			
			int hour = Integer.parseInt(time.substring(0,2));
			int min = Integer.parseInt(time.substring(3,5));
			int sec = Integer.parseInt(time.substring(6,8));
			
			current = (hour * 60 * 60) + (min * 60) + sec;
			
			progress = (int)( ((float)current) / ((float)total) *100f );
		}
		
		String status = (mTitle);
		Message msg = mHandler.obtainMessage(0);
		msg.getData().putString("status",mTitle + " " + progress + "%");
        mHandler.sendMessage(msg);
	}

	@Override
	public void processComplete(int exitValue) {
		
		String status = (mTitle);
		Message msg = mHandler.obtainMessage(0);
		msg.getData().putString("status",status);
        mHandler.sendMessage(msg);
		
	}
	
	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
		            	setText(status);
	                 break;
	              
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	};
	
}
