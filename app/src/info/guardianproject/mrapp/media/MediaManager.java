package info.guardianproject.mrapp.media;

import org.ffmpeg.android.ShellUtils.ShellCallback;

public interface MediaManager {

	public void prerenderMedia(MediaClip mClip, ShellCallback shellCallback);
}
