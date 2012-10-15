package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.MediaAppConstants;
import info.guardianproject.mrapp.model.Lesson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class LessonManager implements Runnable {
	
	private String mUrlRemoteRepo;
	private File mLocalStorageRoot;
	
	private ArrayList<Lesson> mListLessons;
	
	private LessonManagerListener mListener;
	
	public LessonManager (String remoteRepoUrl, File localStorageRoot)
	{
		mLocalStorageRoot = localStorageRoot;
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
			URL urlRemoteIndex = new URL(mUrlRemoteRepo + "index.json");
			URLConnection uConn = urlRemoteIndex.openConnection();
			byte[] buffer = new byte[uConn.getContentLength()];
			
			IOUtils.readFully(urlRemoteIndex.openStream(),buffer);
			
			JSONArray jarray = new JSONArray(new String(buffer));
			
			for (int i = 0; i < jarray.length(); i++)
			{
				JSONObject jobj = jarray.getJSONObject(i);
				
				String title = jobj.getString("title");
				String lessonUrl = jobj.getJSONObject("resource").getString("url");
				
				//this should be a zip file
				URL urlLesson = new URL(mUrlRemoteRepo + lessonUrl);
				
				File fileZip = new File(mLocalStorageRoot,urlLesson.getFile());
				
				FileOutputStream fos = new FileOutputStream(fileZip);
				IOUtils.copyLarge(new URL(lessonUrl).openStream(),fos);
				
				File fileZipUnpack = new File(mLocalStorageRoot,urlLesson.getFile()+"-dir");
				fileZipUnpack.mkdirs();
				
				unpack(fileZip,fileZipUnpack);
				
			}
			
			if (mListener != null)
				mListener.lessonsLoadedFromServer();
			
		}
		catch (Exception ioe)
		{
			Log.e(MediaAppConstants.TAG,"error loading lessons from server",ioe);
		}
	}
	
	/** Unpacks the give zip file using the built in Java facilities for unzip. */
	@SuppressWarnings("unchecked")
	public final static void unpack(File zipFile, File rootDir) throws IOException
	{
	  ZipFile zip = new ZipFile(zipFile);
	  Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
	  while(entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();
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
