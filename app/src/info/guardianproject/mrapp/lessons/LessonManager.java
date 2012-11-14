package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.MediaAppConstants;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.onionkit.trust.StrongHttpsClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class LessonManager implements Runnable {
	
	private String mUrlRemoteRepo;
	private File mLocalStorageRoot;
	
	private ArrayList<Lesson> mListLessons;
	
	private LessonManagerListener mListener;
	
	private Context mContext;
	
	public LessonManager (Context context, String remoteRepoUrl, File localStorageRoot)
	{
		mContext = context;
		mUrlRemoteRepo = remoteRepoUrl;
		mLocalStorageRoot = new File(localStorageRoot,"lessons");
		mLocalStorageRoot.mkdir();
	}
	
	public void setListener (LessonManagerListener listener)
	{
		mListener = listener;
	}
	
	public void updateLessonsFromRemote ()
	{
		new Thread(this).start();
	}
	
	public ArrayList<Lesson> loadLessonList (boolean refresh)
	{
		if (mListLessons == null || refresh)
		{
			mListLessons = new ArrayList<Lesson>();
			
			if (mLocalStorageRoot.exists())
			{
				
				File[] fileLessons = mLocalStorageRoot.listFiles();
				
				for (File fileLesson : fileLessons)
				{
					try
					{
						File fileLessonJson = new File(fileLesson,"lesson.json");
						
						if (fileLessonJson.exists())
						{
							Lesson lesson = Lesson.parse(IOUtils.toString(new FileInputStream(fileLessonJson)));
							lesson.mResourcePath = "file://" + new File(fileLesson,lesson.mResourcePath).getAbsolutePath();
							mListLessons.add(lesson);
						}
					}
					catch (FileNotFoundException fnfe)
					{
						Log.w(MediaAppConstants.TAG,"lesson json not found: " + fileLesson.getAbsolutePath(),fnfe);
					}
					catch (IOException fnfe)
					{
						Log.w(MediaAppConstants.TAG,"lesson json i/o error on loading: " + fileLesson.getAbsolutePath(),fnfe);
					}
					catch (Exception fnfe)
					{
						Log.w(MediaAppConstants.TAG,"lesson json general exception: " + fileLesson.getAbsolutePath(),fnfe);
					}
				}
				
				
			}
			
			
			
			
		}
		
		return mListLessons;
	}
	
	public void run ()
	{
		try
		{
			// open URL and download file listing
			HttpClient httpClient = new StrongHttpsClient(mContext);
			HttpGet request = new HttpGet(mUrlRemoteRepo + "index.json");
			HttpResponse response = httpClient.execute(request);

			byte[] buffer = new byte[(int)response.getEntity().getContentLength()];
			
			IOUtils.readFully(response.getEntity().getContent(),buffer);
			
			JSONObject jObjMain = new JSONObject(new String(buffer));
			
			JSONArray jarray = jObjMain.getJSONArray("lessons");
			
			for (int i = 0; i < jarray.length() && (!jarray.isNull(i)); i++)
			{
				try
				{
					JSONObject jobj = jarray.getJSONObject(i);
					
					String title = jobj.getString("title");
					String lessonUrl = jobj.getJSONObject("resource").getString("url");
					
					
					//this should be a zip file
					URI urlLesson = new URI(mUrlRemoteRepo + lessonUrl);
					request = new HttpGet(urlLesson);
					response = httpClient.execute(request);
					
					String fileName = urlLesson.getPath();
					fileName = fileName.substring(fileName.lastIndexOf('/')+1);
					File fileZip = new File(mLocalStorageRoot,fileName);
					
					if (fileZip.exists())
					{
						long remoteLen = response.getEntity().getContentLength();
						long localLen = fileZip.length();
						
						if (localLen == remoteLen)
						{
							//same file, leave it be
							continue;							
						}
						else
						{
							//otherwise, delete and download
							fileZip.delete();
						}
					
					}

					if (mListener != null)
						mListener.loadingLessonFromServer(title);
					IOUtils.copyLarge(response.getEntity().getContent(),new FileOutputStream(fileZip));
					
					unpack(fileZip,mLocalStorageRoot);
					
				//	fileZip.delete();
				}
				catch (Exception ioe)
				{
					Log.e(MediaAppConstants.TAG,"error loading lesson from server: " + i,ioe);
					if (mListener != null)
						mListener.errorLoadingLessons(ioe.getLocalizedMessage());
					
				}
			}
			
			if (mListener != null)
				mListener.lessonsLoadedFromServer();
			
		}
		catch (Exception ioe)
		{
			Log.e(MediaAppConstants.TAG,"error loading lessons from server",ioe);
			if (mListener != null)
				mListener.errorLoadingLessons(ioe.getLocalizedMessage());
			
		}
	}
	
	/** Unpacks the give zip file using the built in Java facilities for unzip. */
	@SuppressWarnings("unchecked")
	public final static void unpack(File zipFile, File rootDir) throws IOException
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
