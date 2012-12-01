package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.AppConstants;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
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
	
	private LessonManagerListener mListener;
	
	private Context mContext;
	
	private String mSubFolder;
	
	public LessonManager (Context context, String remoteRepoUrl, File localStorageRoot)
	{
		mContext = context;
		
		mUrlRemoteRepo = remoteRepoUrl;
		if (!mUrlRemoteRepo.endsWith("/"))
			mUrlRemoteRepo = mUrlRemoteRepo + "/";
		
		mLocalStorageRoot = localStorageRoot;
		mLocalStorageRoot.mkdir();
	}
	
	public void setSubFolder (String subFolder)
	{
		mSubFolder = subFolder;
	}
	
	public void setListener (LessonManagerListener listener)
	{
		mListener = listener;
	}
	
	public void updateLessonsFromRemote ()
	{
		new Thread(this).start();
	}
	
	public ArrayList loadLessonList ()
	{
		ArrayList<Lesson> lessons = new ArrayList<Lesson>();
		
		File lessonFolder = mLocalStorageRoot;
		if (mSubFolder != null)
			lessonFolder = new File(mLocalStorageRoot, mSubFolder);
		
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
					
				
					if (fileLessonJson.exists())
					{
						
						Lesson lesson = Lesson.parse(IOUtils.toString(new FileInputStream(fileLessonJson)));
						File fileIdx = new File(fileLesson,lesson.mResourcePath);
						
						lesson.mTitle = fileLesson.getName() + ": " + lesson.mTitle;
						lesson.mResourcePath = "file://" + fileIdx.getAbsolutePath();
						lessons.add(lesson);
						
						updateResource(fileIdx);
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
            	
            	Integer aTitle = Integer.parseInt(lessonA.mTitle.substring(2,lessonA.mTitle.indexOf(":")));
            	Integer bTitle = Integer.parseInt(lessonB.mTitle.substring(2,lessonB.mTitle.indexOf(":")));
            	
                return aTitle.compareTo(bTitle);
            }
        });
	  
		return lessons;
	}
	
	private void updateResource (File fIndex) throws IOException
	{
			  InputStream is = mContext.getAssets().open("template/index.html");
			  OutputStream os = new java.io.FileOutputStream(fIndex);
			  IOUtils.copyLarge(is, os);
		
	}
	public void run ()
	{
		try
		{
			File lessonFolder = mLocalStorageRoot;
			if (mSubFolder != null)
				lessonFolder = new File(mLocalStorageRoot, mSubFolder);
		
			// open URL and download file listing
			StrongHttpsClient httpClient = new StrongHttpsClient(mContext);
			
			String urlString = mUrlRemoteRepo + "index.json";
			
			if (mSubFolder != null)
				urlString = mUrlRemoteRepo + mSubFolder + "/" + "index.json";
			
			HttpGet request = new HttpGet(urlString);
			HttpResponse response = httpClient.execute(request);

			long conLen = response.getEntity().getContentLength();
			
			if (conLen > -1)
			{
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
						File fileZip = new File(lessonFolder,fileName);
						
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
						Log.e(AppConstants.TAG,"error loading lesson from server: " + i,ioe);
						if (mListener != null)
							mListener.errorLoadingLessons(ioe.getLocalizedMessage());
						
					}
				}
				
				if (mListener != null)
					mListener.lessonsLoadedFromServer();
			}
			else
			{

				Log.w(AppConstants.TAG,"lesson json not available on server");
				if (mListener != null)
					mListener.errorLoadingLessons("Lesson data not yet available on server");	
			}
		}
		catch (Exception ioe)
		{
			Log.e(AppConstants.TAG,"error loading lessons from server",ioe);
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
