package info.guardianproject.mrapp;

import info.guardianproject.mrapp.ObscuraConstants.MediaScanner;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class InformaMediaScanner implements MediaScannerConnectionClient {
	private MediaScannerConnection msc;
	private File f;
	private Context c;
	
	public InformaMediaScanner(Context c, File f) {
		Log.d(InformaConstants.TAG, "Calling function has deferred to the Media Scanner before continuing");
		Log.d(InformaConstants.TAG, "file: " + f.getAbsolutePath());
		this.f = f;
		this.c = c;
		msc = new MediaScannerConnection(c, this);
		msc.connect();
	}
	
	@Override
	public void onMediaScannerConnected() {
		msc.scanFile(f.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		msc.disconnect();
		Log.d(InformaConstants.TAG, "new path: " + path + "\nnew uri for path: " + uri.toString());
		c.sendBroadcast(new Intent()
			.setAction(MediaScanner.SCANNED)
			.putExtra(MediaScanner.URI, uri));
	}

}
