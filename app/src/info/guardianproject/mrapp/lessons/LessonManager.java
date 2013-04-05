package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.onionkit.trust.StrongHttpsClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class LessonManager implements Runnable {
	
	private String mUrlRemoteRepo;
	private File mLocalStorageRoot;
	
	private LessonManagerListener mListener;
	
	private Context mContext;
	
	private String mSubFolder;
	
	public final static String LESSON_METADATA_FILE = "lesson.json";
	public final static String LESSON_STATUS_FILE = "status.txt";
	//public final static String LESSON_INDEX_FILE = "index.json";
	
	public LessonManager (Context context, String remoteRepoUrl, File localStorageRoot)
	{
		mContext = context;
		
		mUrlRemoteRepo = remoteRepoUrl;
		if (!mUrlRemoteRepo.endsWith("/"))
			mUrlRemoteRepo = mUrlRemoteRepo + "/";
		
		mLocalStorageRoot = localStorageRoot;
		mLocalStorageRoot.mkdir();
	}
	
	public File getLessonRoot ()
	{
		return mLocalStorageRoot;
	}
	
	public void setSubFolder (String subFolder)
	{
		mSubFolder = subFolder;
	}
	
	public void setLessonManagerListener (LessonManagerListener listener)
	{
		mListener = listener;
	}
	
	public void updateLessonsFromRemote ()
	{
		new Thread(this).start();
	}
	
	public void updateLessonStatus (String path, int status) throws IOException
	{
		File fileLesson = new File(path);
		if (!fileLesson.isDirectory())
		{
			fileLesson = fileLesson.getParentFile();
		}
		
		String strStatus = status + "";
		
		File fileStatus = new File(fileLesson,LESSON_STATUS_FILE);
		fileStatus.createNewFile();
		FileOutputStream fos = new FileOutputStream(fileStatus);
		fos.write(strStatus.getBytes());
		
	}
	
	public ArrayList<Lesson> loadLessonList (Context context, String lang)
	{
		return loadLessonList(context, mLocalStorageRoot, mSubFolder, lang, -1);
	}
	
	public ArrayList<Lesson> loadLessonList (Context context, String lang, int matchStatus)
	{
		return loadLessonList(context, mLocalStorageRoot, mSubFolder, lang, matchStatus);
	}
	
	public static ArrayList<Lesson> loadLessonList (Context context,File targetFolder, String subFolder, String lang, int matchStatus)
	{
		
		ArrayList<Lesson> lessons = new ArrayList<Lesson>();
		
		File lessonFolder = targetFolder;
		
		if (subFolder != null)
			lessonFolder = new File(targetFolder, subFolder);
		
		if (lessonFolder.exists())
		{
			
			File[] fileLessons = lessonFolder.listFiles();
			
			for (File fileLesson : fileLessons)
			{
				try
				{
					File fileLessonJson = new File(fileLesson,"lesson.json");

					if (!fileLessonJson.exists())
					{
						fileLessonJson = new File(fileLesson,"Lesson.json"); //sometimes people are silly and case matters

					}
				

					File fileStatus = new File(fileLesson,LESSON_STATUS_FILE);
					int status = -1;
					Date statusModified = null;
					
					if (fileStatus.exists())
					{
						byte[] bStatus = new byte[(int)fileStatus.length()];
						IOUtils.readFully(new FileInputStream(fileStatus),bStatus);
						status = Integer.parseInt(new String(bStatus).trim());
						statusModified = new Date(fileStatus.lastModified());
						
					}
					
					if (matchStatus != -1 && matchStatus != status)
						continue; //didn't match so don't add it
					
					if (fileLessonJson.exists())
					{
						
						Lesson lesson = Lesson.parse(IOUtils.toString(new FileInputStream(fileLessonJson)));
						File fileIdx = new File(fileLesson,lesson.mResourcePath);
						
						lesson.mTitle = fileLesson.getName() + ": " + lesson.mTitle;
						lesson.mResourcePath = fileIdx.getAbsolutePath();
						lesson.mStatus = status;
						lesson.mStatusModified = statusModified;
						
						lessons.add(lesson);
						
						lesson.mLocalPath = fileLesson;
						
						File fileImage = new File(fileLesson,"1.png");
						if (fileImage.exists())
							lesson.mImage = fileImage.getAbsolutePath();
						
						lesson.mSortIdx = Integer.parseInt(lesson.mTitle.substring(2,lesson.mTitle.indexOf(":")));
					}
				}
				catch (FileNotFoundException fnfe)
				{
					Log.w(AppConstants.TAG,"lesson json not found: " + fileLesson.getAbsolutePath(),fnfe);
				}
				catch (IOException fnfe)
				{
					Log.w(AppConstants.TAG,"lesson json i/o error on loading: " + fileLesson.getAbsolutePath(),fnfe);
				}
				catch (Exception fnfe)
				{
					Log.w(AppConstants.TAG,"lesson json general exception: " + fileLesson.getAbsolutePath(),fnfe);
				}
			}
			
			
		}
		
	  Collections.sort(lessons,new Comparator<Lesson>() {
            public int compare(Lesson lessonA, Lesson lessonB) {
            	
                return lessonA.mSortIdx.compareTo(lessonB.mSortIdx);
            }
        });
	  
		return lessons;
	}
	
	public static void updateLessonResource (Context context, Lesson lesson, String locale) throws IOException
	{
		  File fileIdx = new File(lesson.mResourcePath);
	
		  InputStream is = context.getAssets().open("template/index.html." + locale);
		  OutputStream os = new java.io.FileOutputStream(fileIdx);
		  IOUtils.copyLarge(is, os);
		
	}
	
	private static StrongHttpsClient mHttpClient;
	
	private synchronized StrongHttpsClient getHttpClientInstance ()
	{
		if (mHttpClient == null)
			mHttpClient = new StrongHttpsClient(mContext);
		
		mHttpClient.getStrongTrustManager().setNotifyVerificationSuccess(true);
		mHttpClient.getStrongTrustManager().setNotifyVerificationFail(true);
		
		return mHttpClient;
	}
	
	public void run ()
	{

		Log.d(AppConstants.TAG,"loading lessons from remote");
		
		String sUrlLesson = null;
		
		try
		{
			File lessonFolder = mLocalStorageRoot;
			if (mSubFolder != null)
				lessonFolder = new File(mLocalStorageRoot, mSubFolder);
		
			
			Log.d(AppConstants.TAG,"current lesson folder: " + lessonFolder.getAbsolutePath());
			
			
			// open URL and download file listing
			StrongHttpsClient httpClient = getHttpClientInstance();
			
			 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

		     boolean useTor = settings.getBoolean("pusetor", false);
		     
			if (useTor)
			{
				httpClient.useProxy(true, "SOCKS", AppConstants.TOR_PROXY_HOST, AppConstants.TOR_PROXY_PORT);
			}
			else
			{
				httpClient.useProxy(false, null, null, -1);

			}
			
			String urlBase = mUrlRemoteRepo;
			if (mSubFolder != null)
				urlBase += mSubFolder + '/';
			
			String urlIndex = urlBase + LESSON_METADATA_FILE;
			
			Log.d(AppConstants.TAG,"Loading lesson index: " + urlIndex);
			
			
			HttpGet request = new HttpGet(urlIndex);
			HttpResponse response = httpClient.execute(request);

			HttpEntity entity = response.getEntity();


			int statusCode = response.getStatusLine().getStatusCode();

			//InputStream isContent = entity.getContent();
			
			long conLen = entity.getContentLength();
								
			boolean isChunked = entity.isChunked();
			
			//if (conLen > -1)
			if (statusCode == 200)
			{
				String jsonData = EntityUtils.toString(entity);
				
				if (!jsonData.contains("]"))
					jsonData += "]}";
				
			//	Log.d(AppConstants.TAG,"got json data: " + jsonData);
				
				JSONObject jObjMain = new JSONObject(jsonData);
				
				JSONArray jarray = jObjMain.getJSONArray("lessons");
				
				
				for (int i = 0; i < jarray.length() && (!jarray.isNull(i)); i++)
				{
					try
					{
						JSONObject jobj = jarray.getJSONObject(i);
						
						String title = jobj.getString("title");
						String lessonUrl = jobj.getJSONObject("resource").getString("url");
						
						//this should be a zip file
						sUrlLesson = urlBase + lessonUrl;
						
						Log.d(AppConstants.TAG,"Loading lesson zip: " + sUrlLesson);
						
						URI urlLesson = new URI(sUrlLesson);
						request = new HttpGet(urlLesson);
						response = httpClient.execute(request);
						
						String fileName = urlLesson.getPath();
						fileName = fileName.substring(fileName.lastIndexOf('/')+1);
						File fileZip = new File(lessonFolder,fileName);
						
						if (fileZip.exists())
						{
							long remoteLen = response.getEntity().getContentLength();
							long localLen = fileZip.length();
							
							if (localLen == remoteLen)
							{
								//same file, leave it be
								Log.d(AppConstants.TAG,"file already exists locally; skipping!");
								response.getEntity().consumeContent();
								continue;							
							}
							else
							{
								Log.d(AppConstants.TAG,"local file is out of date; updating...");

								//otherwise, delete and download
								fileZip.delete();
							}
						
						}
	
						if (mListener != null)
							mListener.loadingLessonFromServer(mSubFolder, title);
						
						fileZip.getParentFile().mkdirs();
						
						BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
						IOUtils.copyLarge(bis,new FileOutputStream(fileZip));
						
						unpack(fileZip,lessonFolder);
						
					//	fileZip.delete();
					}
					catch (Exception ioe)
					{
						Log.e(AppConstants.TAG,"error loading lesson from server: " + sUrlLesson,ioe);
						
						if (response != null)
							response.getEntity().consumeContent();

						if (mListener != null)
							mListener.errorLoadingLessons(ioe.getLocalizedMessage());
						
					}
				}
				
				if (mListener != null)
					mListener.lessonsLoadedFromServer();
			}
			else
			{

				Log.w(AppConstants.TAG,"lesson json not available on server: " + sUrlLesson);
				if (mListener != null)
					mListener.errorLoadingLessons("Lesson data not yet available on server");	
			}
		}
		catch (Exception ioe)
		{
			Log.e(AppConstants.TAG,"error loading lessons from server: " + sUrlLesson,ioe);
			
			if (mListener != null)
				mListener.errorLoadingLessons(ioe.getLocalizedMessage());
			
		}
	}
	
	/** Unpacks the give zip file using the built in Java facilities for unzip. */
	@SuppressWarnings("unchecked")
	public void unpack(File zipFile, File rootDir) throws IOException
	{
		
	  ZipFile zip = new ZipFile(zipFile);
	  Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
	  ZipEntry entry = null;
	  
	  while(entries.hasMoreElements()) {
	    entry = entries.nextElement();
	    
	    java.io.File f = new java.io.File(rootDir, entry.getName());
	    if (entry.isDirectory()) { // if its a directory, create it
	      continue;
	    }

	    if (!f.exists()) {
	      f.getParentFile().mkdirs();
	      f.createNewFile();
	    }

	    InputStream is = zip.getInputStream(entry); // get the input stream
	    OutputStream os = new java.io.FileOutputStream(f);
	    IOUtils.copyLarge(is, os);
	    
	  }
	  
	}
}
