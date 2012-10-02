package info.guardianproject.mrapp.ui;

import java.util.ArrayList;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.media.MediaClip;

import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.filters.DrawTextVideoFilter;
import org.ffmpeg.android.filters.FadeVideoFilter;
import org.ffmpeg.android.filters.VideoFilter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MediaView extends BigImageLabelView implements OnClickListener, OnDismissListener {

	private MediaClip mMediaClip;
	private Dialog mDialog;
	private EditText mEditTitle, mEditStartTime, mEditEndTime, mEditFadeIn, mEditFadeOut;
	
	private Context mContext;
	
	public MediaView(Context context, MediaClip mediaClip, String title, Bitmap image,
			int fontColor, int bgColor) {
		super(context, title, image, fontColor, bgColor);

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
			
		mMediaClip.mMediaDescOriginal.videoFilter = VideoFilter.format(list);
	}
	
	@Override
	public void onClick(View v) {
		showSettingsDialog();
		
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		
		updateSettings ();
		
		
	}
}
