package info.guardianproject.mrapp;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class ObscuraConstants {
	public final static String TAG = "************ OBSCURA ***********";
	
	public final static int CAMERA_RESULT = 0;
	public final static int GALLERY_RESULT = 1;
	public final static int IMAGE_EDITOR = 2;
	public final static int VIDEO_EDITOR = 3;
	public final static int REVIEW_MEDIA = 4;
	
	public final static int ABOUT = 0;
	public final static int PREFS = 1;
	public final static int LOGOUT = 2;
	
	public final static String CAMERA_TMP_FILE = "ssctmp.jpg";
	public final static String MIME_TYPE_JPEG = "image/jpeg";
	
	public final static String CAMCORDER_TMP_FILE = "ssctmp.mp4";
	public final static String MIME_TYPE_MP4 = "video/mp4";
	public final static String MIME_TYPE_MKV = "video/mkv";
	
	public final static class MimeTypes {
		public final static String MP4 = "mp4";
		public final static String JPEG = "jpg";
		public static final String MKV = "mkv";
	}
	
	public static final int NONE = 0;
	public static final int DRAG = 1;
	public static final int ZOOM = 2;
	public static final int TAP = 3;
	
	
	
	// Maximum zoom scale
	public static final float MAX_SCALE = 10f;
	
	// Constant for autodetection dialog
	public static final int DIALOG_DO_AUTODETECTION = 0;
	
	// Colors for region squares
	public final static int DRAW_COLOR = 0x00000000;
	public final static int DETECTED_COLOR = 0x00000000;
	public final static int OBSCURED_COLOR = 0x00000000;
	
	// Constants for the menu items, currently these are in an XML file (menu/image_editor_menu.xml, strings.xml)
	public final static int ABOUT_MENU_ITEM = 0;
	public final static int DELETE_ORIGINAL_MENU_ITEM = 1;
	public final static int SAVE_MENU_ITEM = 2;
	public final static int SHARE_MENU_ITEM = 3;
	public final static int NEW_REGION_MENU_ITEM = 4;
	
	// Constant for temp filename
	public final static String TMP_FILE_NAME_IMAGE = "tmp.jpg";
	public static final String TMP_FILE_NAME_VIDEO = "tmp.mp4";
	
	public final static String TMP_FILE_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.witness.ssc/files/";
	
	public final static class MediaScanner {
		public final static String SCANNED = "mediaScanned";
		public final static String URI = "scannedUri";
	}
	
	// for saving images
    public final static String EXPORT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String OBSCURED_IMAGE_URI = "obscuredImageUri";
	
	public final static class Preferences {
		public final static class Keys {
			public final static String LANGUAGE = "obscura.language";
			public final static String SHOW_HINTS = "obscura.showHints";
		}
	}
	
	public final static class ImageRegion {
		public final static String PROPERTIES = "mProps";
	}
	
	public final static class VideoRegion {
		public final static String PROPERTIES = ImageRegion.PROPERTIES;
		public static final int CORNER_NONE = 0;
		public static final int CORNER_UPPER_LEFT = 1;
		public static final int CORNER_LOWER_LEFT = 2;
		public static final int CORNER_UPPER_RIGHT = 3;
		public static final int CORNER_LOWER_RIGHT = 4;
		
		public final static float REGION_CORNER_SIZE = 26;    
		public final static long FACE_TIME_BUFFER = 2000;
		
		public static final int NONE = 0;
		public static final int DRAG = 1;

	}
	
	public final static class VideoEditor {
		public final static int PLAY = 1;
		public final static int STOP = 2;
		public final static int PROCESS = 3;
		
		public final static class Breakpoints {
			public final static String IN = "Breakpoint.in";
			public final static String OUT = "Breakpoint.out";
			public final static String LEFT = "Breakpoint.left";
			public final static String RIGHT = "Breakpoint.right";
			public final static String TOTAL_TIME = "Breakpoint.totalTime";
			public final static String TOTAL_WIDTH = "Breakpoint.totalWidth";
			public final static String DURATION = "Breakpoint.duration";
			public final static String FILTER = "Breakpoint.filter";
		}
	}
	
	public final static class ExifValues {
		public final static String DESCRIPTION = "InformaCam image";
		public final static String TITLE = "Image taken with InformaCam";
		public final static String CONTENT_TYPE = "MIME_TYPE_JPEG";
		public final static float GEO = 0.0f;
	}
	
	public final static class Filters {
		public final static String PIXELIZE = "p";
		public final static String INFORMA_TAGGER = "t";
		public final static String CROWD_PIXELIZE = "i";
		public final static String SOLID = "s";
		public final static String VIDEO_PIXELIZE = "pixel";
	}
	
	public static void makeToast(Context c, String m) {
		Toast.makeText(c, m, Toast.LENGTH_LONG).show();
	}
}
