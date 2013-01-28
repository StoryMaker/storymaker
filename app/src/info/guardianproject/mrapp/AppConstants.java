package info.guardianproject.mrapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.media.ExifInterface;
import android.os.Environment;
import android.widget.Toast;

public class AppConstants {
	
public final static String TAG = "StoryMaker";
	
	public final static int IMAGE_EDITOR = 2;
	public final static int VIDEO_EDITOR = 3;
	public final static int REVIEW_MEDIA = 4;
	
	public final static int ABOUT = 0;
	public final static int PREFS = 1;
	public final static int LOGOUT = 2;
	

	//TODO switch this to HTTPS!
	public static String DEFAULT_STORYMAKER_CONTENT_SERVER = "https://guardianproject.info/";//"https://storymaker.cc/";
	public static String DEFAULT_STORYMAKER_WORDPRESS_SERVER = "https://storymaker.cc/";
	
	public final static String DEFAULT_STORYMAKER_SERVER_RSS = DEFAULT_STORYMAKER_WORDPRESS_SERVER + "feed/";
	
	public final static String TOR_PROXY_HOST = "localhost";
	public final static int TOR_PROXY_PORT = 9050;
	
	
	public final static class MimeTypes {
		public final static String THREEGPP_AUDIO = "audio/3gpp";
		public final static String MP3 = "audio/mpeg";
		public final static String MP4 = "video/mp4";
		public final static String JPEG = "image/jpeg";
		//public static final String MKV = "mkv";
	}
	
	public static final int NONE = 0;
	public static final int DRAG = 1;
	public static final int ZOOM = 2;
	public static final int TAP = 3;
	
	public static final String FILE_MEDIAFOLDER_NAME = "StoryMaker";
	
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
	
	//public final static String TMP_FILE_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.witness.ssc/files/";
	
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
		public final static String DESCRIPTION = "MRApp image";
		public final static String TITLE = "Image taken with MRApp";
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
	
	public final static String READOUT = "******************* INFORMA READOUT ******************";
	public final static String SUCKER_TAG = "******************** SUCKER SERVICE ******************";
	public final static String VIDEO_LOG = "******************** FFMPEG WRAPPER ******************";
	public final static String PW_EXPIRY = "**EXPIRED**";
	public final static int FROM_INFORMA_WIZARD = 3;
	public final static int FROM_INFORMA_TAGGER = 4;
	public final static int FROM_TRUSTED_DESTINATION_CHOOSER = 5;
	public final static int FROM_ENCRYPTION_SERVICE = 6;
	public final static int FROM_REGISTRATION_IMAGE = 7;
	public final static int BLOB_MAX = 1048576;
	
	public final static String TMP_FILE_NAME = "tmp_.jpg";
	public final static String TMP_VIDEO_DATA_FILE_NAME = "tmp_.txt";
	public final static String NOT_INCLUDED = "NOT INCLUDED IN THIS VERSION";
	
	public final static String REPO = "https://j3m.info/repo/?repo=";
	// one day we will have a way of handling this

	public final static class Keys {
		public final static String USER_CANCELED_EVENT = "userCanceledEvent";
		public final static String ENCRYPTED_IMAGES = "encryptedImages";
		
		public final static class Settings {
			public static final String INFORMA = "informa";
			public static final String SETTINGS_VIEWED = "informa.SettingsViewed";
			public static final String HAS_DB_PASSWORD = "informa.PasswordSet";
			public static final String DB_PASSWORD_CACHE_TIMEOUT = "informa.PasswordCacheTimeout";
			public static final String DEFAULT_IMAGE_HANDLING = "informa.DefaultImageHandling";
			public static final String EULA_ACCEPTED = "informa.EulaAccepted";
			public static final String WITH_ENCRYPTION = "informa.EncryptMetadata";
		}
		
		public final static class Uploader {
			public static final String AUTH_TOKEN = "auth_token";
			public static final String A_OK = "A_OK";
			public static final String FAIL = "FAIL";
			public static final String POSTPONE = "POSTPONE";
			public static final String BOUNDARY = "---------------------------InformaCamv1***924sggo2jbs924qabasfbwrthw9g4";
			public static final String LINE_END = "\r\n";
			public static final String HYPHENS = "--";
			
			public static final class Entities {
				public static final String USER_PGP = "user_pgp";
				public static final String AUTH_TOKEN = Keys.Uploader.AUTH_TOKEN;
				public static final String BYTES_EXPECTED = "bytes_expected";
				public static final String MEDIA_TYPE = "media_type";
				public static final String TIMESTAMP_CREATED = "timestamp_created";
				public static final String MEDIA_UPLOAD = "media_upload";
			}
		}
		
		public final static class OpenPGP {
			public static final class Entities {
				public static final String BEGIN_PGP_PUBLIC_KEY_BLOCK = "-----BEGIN PGP PUBLIC KEY BLOCK-----";
				public static final String END_PGP_PUBLIC_KEY_BLOCK = "-----END PGP PUBLIC KEY BLOCK-----";
			}
		}
		
		public final static class Service {
			public final static String STOP_SERVICE = "stopService";
			public final static String SET_CURRENT = "setCurrent";
			public final static String SEAL_LOG = "sealLog";
			public final static String GENERATE_IMAGE = "generateImage";
			public final static String IMAGES_GENERATED = "imagesGenerated";
			public final static String SET_EXIF = "setExif";
			public final static String FINISH_ACTIVITY = "finishActivity";
			public final static String START_SERVICE = "startService";
			public final static String LOCK_LOGS = "lockLogs";
			public final static String UNLOCK_LOGS = "unlockLogs";
			public final static String INFLATE_VIDEO_TRACK = "inflateDataForVideoTrack";
			public final static String ENCRYPT_METADATA = "encryptMetadata";
			public final static String CLONE_PATH = "clonePath";
			public final static String START_UPLOADER = "startUploaderService";
			public final static String UPLOADER_AVAILABLE = "uploaderAvailable";
		}
		
		public final static class Informa {
			public final static String INTENT = "intent";
			public final static String GENEALOGY = "genealogy";
			public final static String DATA = "data";
		}
		
		public final static class CaptureEvent {
			public final static String TYPE = "captureEvent";
			public final static String MATCH_TIMESTAMP = "matchTimestamp";
			public final static String TIMESTAMP = Image.TIMESTAMP;
			public final static String ON_VIDEO_START = "timestampOnVideoStart";
			public final static String MEDIA_CAPTURE_COMPLETE = "mediaCapturedComplete";
			public final static String VIDEO_TRACK = Video.VIDEO_TRACK;
			public final static String EVENT = Events.CAPTURE_EVENT;
		}
		
		public final static class VideoRegion {
			public final static String INDEX = ImageRegion.INDEX;
			public final static String TIMESTAMP = ImageRegion.TIMESTAMP;
			public final static String LOCATION = ImageRegion.LOCATION;
			public final static String TAGGER_RETURN = ImageRegion.TAGGER_RETURN;
			public final static String FILTER = ImageRegion.FILTER;
			public final static String COORDINATES = ImageRegion.COORDINATES;
			public final static String DURATION = "region_duration";
			public final static String START_TIME = "startTime";
			public final static String END_TIME = "endTime";
			public final static String CHILD_REGIONS = "region_children";
			public final static String TRAIL = "trail";
			
			public final static class Subject {
				public final static String PSEUDONYM = ImageRegion.Subject.PSEUDONYM;
				public final static String INFORMED_CONSENT_GIVEN = ImageRegion.Subject.INFORMED_CONSENT_GIVEN;
				public final static String PERSIST_FILTER = ImageRegion.Subject.PERSIST_FILTER;
			}
			
			public final static class Child {
				public final static String COORDINATES = "regionCoordinates";
				public final static String DIMENSIONS = "regionDimensions";
				public final static String WIDTH = "region_width";
				public final static String HEIGHT = "region_height";
				public final static String TOP = "region_top";
				public final static String LEFT = "region_left";
			}
		}
		
		public final static class ImageRegion {
			public final static String INDEX = "regionIndex";
			public final static String THUMBNAIL = "regionThumbnail";
			public static final String DATA = "region_data";
			public final static String TIMESTAMP = "timestampOnGeneration";
			public final static String LOCATION = "locationOnGeneration";
			public final static String TAGGER_RETURN = "taggerReturned";
			public final static String FILTER = "obfuscationType";
			public final static String COORDINATES = "regionCoordinates";
			public final static String DIMENSIONS = "regionDimensions";
			public final static String WIDTH = "region_width";
			public final static String HEIGHT = "region_height";
			public final static String TOP = "region_top";
			public final static String LEFT = "region_left";
			public final static String UNREDACTED_DATA = "unredactedRegionData";
			public final static String BASE = "base";
			
			public final static class Data {
				public final static String UNREDACTED_HASH = "unredactedRegionHash";
				public final static String LENGTH = "dataLength";
				public final static String POSITION = "byteStart";
				public final static String BYTES = "byteArray";
			}
			
			public final static class Subject {
				public final static String PSEUDONYM = "subject_pseudonym";
				public final static String INFORMED_CONSENT_GIVEN = "subject_informedConsentGiven";
				public final static String PERSIST_FILTER = "subject_persistFilter";
			}
		}
		
		public final static class Data {
			public final static String IMAGE_REGIONS = "imageRegions";
			public final static String VIDEO_REGIONS = "videoRegions";
			public final static String EVENTS = "events";
			public final static String MEDIA_HASH = "mediaHash";
			public final static String EXIF = "exif";
			public final static String LOCATIONS = "location";
			public final static String CORROBORATION = "corroboration";
			public final static String CAPTURE_TIMESTAMPS = "captureTimestamp";
		}
		
		public final static class Genealogy {
			public final static String LOCAL_MEDIA_PATH = "localMediaPath";
			public final static String DATE_CREATED = "dateCreated";
			public final static String MEDIA_ORIGIN = "mediaOrigin";
		}
		
		public final static class Location {
			public final static String TYPE = "locationType";
			public final static String DATA = "locationData";
			public final static String COORDINATES = "location_gpsCoordinates";
			public final static String CELL_ID = Suckers.Phone.CELL_ID;
		}
		
		public final static class CaptureTimestamp {
			public final static String TYPE = "timestampType";
		}
		
		public final static class Intent {
			public final static String ENCRYPT_LIST = "encryptList";
			public final static String INTENDED_DESTINATION = "intendedDestination";
			public final static class Destination {
				public final static String EMAIL = "destinationEmail";
				public final static String DISPLAY_NAME = "displayName";
			}
		}
		
		public final static class Events {
			public final static String EVENT_DATA = "eventData";
			public final static String CAPTURE_EVENT = CaptureEvent.TYPE;
			public final static String TYPE = "eventType";
			public final static String TIMESTAMP = CaptureEvent.TIMESTAMP;
		}
		
		public final static class TrustedDestinations {
			public final static String EMAIL = Intent.Destination.EMAIL;
			public final static String KEYRING_ID = "keyringId";
			public final static String DISPLAY_NAME = Intent.Destination.DISPLAY_NAME;
			public final static String DESTO = "tdDestination";
			public final static String HOOKUPS = "hookups";
			public final static String CERT = "cert";
			public final static String DATE_UPDATED = "dateUpdated";
			public final static String ENCRYPTION_KEY = "encryptionKey";
		}
		
		public final static class Media {
			public final static String MEDIA_TYPE = "source_type";
			public final static String UNREDACTED_HASH = Image.UNREDACTED_IMAGE_HASH;
			public final static String REDACTED_HASH = Image.REDACTED_IMAGE_HASH;
			public final static String SHARE_VECTOR = "shareVector";
			public final static String ALIAS = "mediaAlias";
			public final static String AUTH_TOKEN = Keys.Uploader.AUTH_TOKEN;
			public final static String KEY_HASH = Keys.Device.PUBLIC_KEY_HASH;
			
			public final static String UPLOAD_ATTEMPTS = "uploadAttempts";
			public final static String STATUS = "status";
			
			public final static class Manager {
				public final static String SHARE_BASE = "shareBase";
				public final static String VIEW_IMAGE_URI = "viewImageUri";
				public final static String SHARE_IMAGE_URI = "shareImageUri";
				public final static String MESSAGE_URL = "messageUrl";
			}
		}
		
		public final static class Image {
			public static final String METADATA = "source_metadata";
			public static final String CONTAINMENT_ARRAY = "source_containmentArray";
			public static final String UNREDACTED_IMAGE_HASH = "source_unredactedImageHash";
			public static final String REDACTED_IMAGE_HASH = "source_redactedImageHash";
			public final static String LOCAL_MEDIA_PATH = Genealogy.LOCAL_MEDIA_PATH;
			public final static String TIMESTAMP = "timestamp";
			public final static String LOCATION_OF_ORIGINAL = "source_locationOfOriginal";
			public final static String LOCATION_OF_OBSCURED_VERSION = "source_locationOfObscuredVersion";
			public final static String LOCATION_OF_CLONE = "source_locationOfClone";
			public final static String EXIF = Data.EXIF;
			public final static String TRUSTED_DESTINATION = Intent.Destination.EMAIL;
			public final static String SHARED_SECRET = "source_sharedSecret";
		}
		
		public final static class Video {
			public final static String FIRST_TIMESTAMP = CaptureEvent.ON_VIDEO_START;
			public final static String DURATION = Exif.DURATION;
			public final static String VIDEO_TRACK = "videoTrack";
		}
		
		public final static class Ass {
			public final static String TEMP = "ass.ass";
			public final static String VROOT = "%vroot";
			public final static String BLOCK_START = "%blockstart";
			public final static String BLOCK_END = "%blockend";
			public final static String BLOCK_DATA = "%mdload";
		}
		
		public final static class Owner {
			public static final String SIG_KEY_ID = "owner_sigKeyID";
			public static final String DEFAULT_SECURITY_LEVEL = "owner_defaultSecurityLevel";
			public static final String OWNERSHIP_TYPE = "owner_ownershipType";
		}
		
		public final static class Device {
			public static final String LOCAL_TIMESTAMP = "device_localTimestamp";
			public static final String PUBLIC_TIMESTAMP = "device_publicTimestamp";
			public static final String IMEI = Suckers.Phone.IMEI;
			public static final String BLUETOOTH_DEVICE_NAME = Suckers.Phone.BLUETOOTH_DEVICE_NAME;
			public static final String BLUETOOTH_DEVICE_ADDRESS = Suckers.Phone.BLUETOOTH_DEVICE_ADDRESS;
			public static final String BASE_IMAGE = "device_baseImage";
			public static final String PUBLIC_KEY = "device_publicKey";
			public static final String PUBLIC_KEY_HASH = "hashed_pgp";
			public static final String PRIVATE_KEY = "device_privateKey";
			public static final String PASSPHRASE = "device_passphrase";
			public static final String TIME_SEEN = "timeSeen";
		}
		
		public final static class Tables {
			public static final String IMAGES = "informaImages";
			public static final String CONTACTS = "informaContacts";
			public static final String SETUP = "informaSetup";
			public static final String IMAGE_REGIONS = "imageRegions";
			public static final String TRUSTED_DESTINATIONS = "trustedDestinations";
			public static final String ENCRYPTED_IMAGES = "encryptedImages";
			public static final String KEYRING = "privateKeyring";
			public static final String KEYSTORE = "tofupopKeystore";
		}
		
		public final static class Suckers {
			public final static String PHONE = "Suckers_Phone";
			public final static String ACCELEROMETER = "Suckers_Accelerometer";
			public final static String GEO = "Suckers_Geo";
						
			public final static class Accelerometer {
				public final static String ACC = "acc";
				public final static String ORIENTATION = "orientation";
				public final static String LIGHT = "lightMeter";
				public final static String X = "acc_x";
				public final static String Y = "acc_y";
				public final static String Z = "acc_z";
				public final static String AZIMUTH = "orientation_azimuth";
				public final static String PITCH = "orientation_pitch";
				public final static String ROLL = "orientation_roll";
				public final static String LIGHT_METER_VALUE = "lightMeter_value";
			}
			
			public final static class Geo {
				public final static String GPS_COORDS = Location.COORDINATES;
			}
			
			public final static class Phone {
				public final static String CELL_ID = "location_cellId";
				public final static String IMEI = "device_imei";
				public final static String BLUETOOTH_DEVICE_NAME = "device_bluetooth_name";
				public final static String BLUETOOTH_DEVICE_ADDRESS = "device_bluetooth_address";
			}
			
			public static final String SIGNATURE = "verifiedSignature";
		}
		
		public final static class Exif {
			public final static String MAKE = ExifInterface.TAG_MAKE;
			public final static String MODEL = ExifInterface.TAG_MODEL;
			public final static String APERTURE = ExifInterface.TAG_APERTURE;
			public final static String FLASH = ExifInterface.TAG_FLASH;
			public final static String EXPOSURE = ExifInterface.TAG_EXPOSURE_TIME;
			public final static String FOCAL_LENGTH = ExifInterface.TAG_FOCAL_LENGTH;
			public final static String IMAGE_WIDTH = ExifInterface.TAG_IMAGE_WIDTH;
			public final static String IMAGE_LENGTH = ExifInterface.TAG_IMAGE_LENGTH;
			public final static String ISO = ExifInterface.TAG_ISO;
			public final static String ORIENTATION = ExifInterface.TAG_ORIENTATION;
			public final static String WHITE_BALANCE = ExifInterface.TAG_WHITE_BALANCE;
			public final static String TIMESTAMP = ExifInterface.TAG_DATETIME;
			public final static String DURATION = "duration";
		}
		
		
	}
	
	public final static class Media {
		public final static class ShareVector {
			public final static int ENCRYPTED_UPLOAD_QUEUE = 1020;
			public final static int ENCRYPTED_BUT_NOT_UPLOADED = 1021;
			public final static int UNENCRYPTED_NOT_UPLOADED = 1022;
			public final static int UNENCRYPTED_UPLOAD_QUEUE = 1023;
		}
		
		public final static class Status {
			public final static int UPLOADING = 1030;
			public final static int UPLOAD_COMPLETE = 1031;
			public final static int UPLOAD_FAILED = 1032;
			public final static int NEVER_SCHEDULED_FOR_UPLOAD = 1033;
			public final static int BASE_IMAGE_REQUIRED = 1034;
		}
	}
	
	public final static class Uploader {
		public final static int FROM_NOTIFICATION_BAR = 30;
		public final static class RequestCodes {
			public final static int A_OK = 200;
			public final static int RETRY = 201;
			public final static int POSTPONE = 202;
		}
		
		public final static class Results {
			public final static String POSTPONE = "{result: \"" + Keys.Uploader.POSTPONE + "\"}";
		}
		
	}
	
	public final static class MediaTypes {
		public final static int PHOTO = 101;
		public final static int VIDEO = 102;
	}
	
	public final static class CaptureEvents {
		public final static int MEDIA_CAPTURED = 5;
		public final static int MEDIA_SAVED = 6;
		public final static int REGION_GENERATED = 7;
		public final static int EXIF_REPORTED = 8;
		public final static int BLUETOOTH_DEVICE_SEEN = 9;
		public final static int VALUE_CHANGE = 4;
		public final static int DURATIONAL_LOG = 3;
	}

	public final static class LocationTypes {
		public final static int ON_MEDIA_CAPTURED = CaptureEvents.MEDIA_CAPTURED;
		public final static int ON_MEDIA_SAVED = CaptureEvents.MEDIA_SAVED;
		public final static int ON_REGION_GENERATED = CaptureEvents.REGION_GENERATED;
		public final static int ON_VIDEO_START = 13;
	}
	
	public final static class CaptureTimestamps {
		public final static int ON_MEDIA_CAPTURED = LocationTypes.ON_MEDIA_CAPTURED;
		public final static int ON_MEDIA_SAVED = LocationTypes.ON_MEDIA_SAVED;
		public final static int ON_REGION_GENERATED = LocationTypes.ON_REGION_GENERATED;
		public final static int ON_VIDEO_START = LocationTypes.ON_VIDEO_START;
	}

	public final static class SecurityLevels {
		public final static int UNENCRYPTED_SHARABLE = 100;
		public final static int UNENCRYPTED_NOT_SHARABLE = 101;
	}
	
	public final static class LoginCache {
		public final static int ALWAYS = 200;
		public final static int AFTER_SAVE = 201;
		public final static int ON_CLOSE = 202;
	}
	
	public final static class OriginalImageHandling {
		public final static int LEAVE_ORIGINAL_ALONE = 300;
		public final static int ENCRYPT_ORIGINAL = 301;
		public final static int DELETE_ORIGINAL = 302;
	}
	
	public final static class Device {
		public final static int IS_SELF = -1;
		public final static int IS_NEIGHBOR = 1;
		public final static String SELF = "_self";
	}
	
	public final static class Owner {
		public final static int INDIVIDUAL = 400;
	}
	
	public final static class Consent {
		public final static int GENERAL = 101;
	}
	
	public final static class Selections {
		public final static String SELECT_ONE = "select_one";
		public final static String SELECT_MULTI = "select_multi";
	}
	
	public final static class Suckers {
		public final static class LogRate {
			public final static long ACC = 500L;
			public final static long PHONE = 5000L;
			public final static long GEO = 10000L;
		}
	}
	
	public final static class Genealogy {
		public final static class MediaOrigin {
			public final static int IMPORT = 400;
			public final static int FROM_INFORMA = 401;
		}
	}
	
	public final static class VideoRegions {
		public final static class Parent {
			public final static int SELF = -1;
		}
	}
	
	public final static int NOT_REPORTED = -1;
	
	public final static long timestampToMillis(String ts) throws ParseException {
		//2012:06:12 10:42:04
		try {
			DateFormat df = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss", Locale.getDefault());
		
			Date d = (Date) df.parse(ts);
			return d.getTime();
		} catch(ParseException e) {
			return Long.parseLong(ts);
		}
	}
	
	public final static String millisecondsToTimestamp(long ms, long max) {
		if(ms > max)
			return millisecondsToTimestamp(max);
		else
			return millisecondsToTimestamp(ms);
	}
	
	public final static String millisecondsToTimestamp(long ms) {
		int s = (int) (ms/1000);
		int hours = s/3600;
		int remainder = s%3600;
		int min = remainder/60;
		int sec = remainder%60;
		
		String ts = ((hours < 10 ? "0" : "") + hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
		if(ts.contains("-"))
			ts = ts.replace("-","0.");
		return ts;
	}

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");//$NON-NLS-1$

}

