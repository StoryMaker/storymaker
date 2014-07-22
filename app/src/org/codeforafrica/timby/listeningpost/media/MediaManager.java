package org.codeforafrica.timby.listeningpost.media;

import org.ffmpeg.android.MediaDesc;
//import org.ffmpeg.android.ShellUtils.ShellCallback;

public interface MediaManager {

	//public void prerenderMedia(MediaClip mClip, ShellCallback shellCallback);
	
	public void applyExportSettings (MediaDesc mDesc);
}
