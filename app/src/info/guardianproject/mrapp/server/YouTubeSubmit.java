/* Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.guardianproject.mrapp.server;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.server.Authorizer.AuthorizationListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class YouTubeSubmit {
  private static final String LOG_TAG = AppConstants.TAG;

  private static final String INITIAL_UPLOAD_URL =
      "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";

  private static final String CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
  private static final String DEFAULT_VIDEO_CATEGORY = "News";
  private static final String DEFAULT_VIDEO_TAGS = "mobile, storymaker";

  private static final int DIALOG_LEGAL = 0;

  private static final int MAX_RETRIES = 5;
  private static final int BACKOFF = 4; // base of exponential backoff

  

  public String videoId = null;
  
  private String ytdDomain = null;
  private String assignmentId = null;
  //private Uri videoUri = null;
  private File videoFile = null;
  private String clientLoginToken = null;
  private String youTubeName = null;
  private String title = null;
  private String description = null;
  private Date dateTaken = null;
  private Authorizer authorizer = null;
  private Location videoLocation = null;
  private String tags = null;
  private LocationListener locationListener = null;
  private LocationManager locationManager = null;
  private SharedPreferences preferences = null;
  private TextView domainHeader = null;
  // TODO - replace these counters with a state variable
  private double currentFileSize = 0;
  private double totalBytesUploaded = 0;
  private int numberOfRetries = 0;

  private Activity activity;

  private String videoContentType = "video/mp4";
  
  private Handler handler;
  
  static class YouTubeAccountException extends Exception {
    public YouTubeAccountException(String msg) {
      super(msg);
    }
  }
  
  
  public YouTubeSubmit(File videoFile, String title, String description, Date dateTaken, Activity activity, Handler handler) {
      this.authorizer = new GlsAuthorizer.GlsAuthorizerFactory().getAuthorizer(activity,
        GlsAuthorizer.YOUTUBE_AUTH_TOKEN_TYPE);

    this.videoFile = videoFile;
    this.activity = activity;
    this.title = title;
    this.description = description;
    this.dateTaken = dateTaken;
    this.handler = handler;
  }
  
  public void setVideoFile (File videoFile, String contentType)
  {
	  this.videoFile = videoFile;
	  this.videoContentType = contentType;
  }

  public void upload(String youTubeName, File videoFile, String contentType) {
   
	this.youTubeName = youTubeName;
    this.videoFile = videoFile;
    
    asyncUpload(videoFile,contentType);
  }

  public void asyncUpload(final File videoFile, final String contentType) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        msg.setData(bundle);
        msg.what = 888;
        
        int submitCount=0;
        try {
          while (submitCount<=MAX_RETRIES && videoId == null) {
            try {
              submitCount++;
              videoId = startUpload(videoFile, contentType);
              assert videoId!=null;
            } catch (Internal500ResumeException e500) { // TODO - this should not really happen
              if (submitCount<MAX_RETRIES) {
                Log.w(LOG_TAG, e500.getMessage());
                Log.d(LOG_TAG, String.format("Upload retry :%d.",submitCount));
              } else {
                Log.d(LOG_TAG, "Giving up");
                Log.e(LOG_TAG, e500.getMessage());
                throw new IOException(e500.getMessage());
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          bundle.putString("error", e.getMessage());
          msg.what = -1;
          
          handler.sendMessage(msg);
          return;
        } catch (YouTubeAccountException e) {
          e.printStackTrace();
          msg.what = -1;
          
          bundle.putString("error", e.getMessage());
          handler.sendMessage(msg);
          return;
        } catch (SAXException e) {
          e.printStackTrace();
          msg.what = -1;
          
          bundle.putString("error", e.getMessage());
          handler.sendMessage(msg);
        } catch (ParserConfigurationException e) {
          e.printStackTrace(); 
          msg.what = -1;
          
          bundle.putString("error", e.getMessage());
          handler.sendMessage(msg);
        }

        bundle.putString("videoId", videoId);
        handler.sendMessage(msg);
      }
    }).start();
  }

 
  private String startUpload(File file, String contentType) throws IOException, YouTubeAccountException, SAXException, ParserConfigurationException, Internal500ResumeException {

    if (this.clientLoginToken == null) {
      // The stored gmail account is not linked to YouTube
      throw new YouTubeAccountException(this.youTubeName + " is not linked to a YouTube account.");
    }

    String slug = file.getName();
    String uploadUrl = uploadMetaData(slug, contentType, file.length(), true);

    Log.d(LOG_TAG, "uploadUrl=" + uploadUrl);
    Log.d(LOG_TAG, String.format("Client token : %s ",this.clientLoginToken));

    this.currentFileSize = file.length();
    this.totalBytesUploaded = 0;
    this.numberOfRetries = 0;

    int uploadChunk = 1024 * 1024 * 3; // 3MB

    int start = 0;
    int end = -1;

    String videoId = null;
    double fileSize = this.currentFileSize;
    while (fileSize > 0) {
      if (fileSize - uploadChunk > 0) {
        end = start + uploadChunk - 1;
      } else {
        end = start + (int) fileSize - 1;
      }
      String status = String.format("start=%s end=%s total=%s", start, end, file.length());
      Message msg = handler.obtainMessage(888);
      msg.getData().putString("status", status);
      handler.sendMessage(msg);
      
      Log.d(LOG_TAG, status);
      try {
        videoId = gdataUpload(file, uploadUrl, start, end);
        fileSize -= uploadChunk;
        start = end + 1;
        this.numberOfRetries = 0; // clear this counter as we had a succesfull upload
      } catch (IOException e) {
        Log.d(LOG_TAG,"Error during upload : " + e.getMessage());
        ResumeInfo resumeInfo = null;
        do {
          if (!shouldResume()) {
            Log.d(LOG_TAG, String.format("Giving up uploading '%s'.", uploadUrl));
            throw e;
          }
          try {
            resumeInfo = resumeFileUpload(uploadUrl,file);
          } catch (IOException re) {
            // ignore
            Log.d(LOG_TAG, String.format("Failed retry attempt of : %s due to: '%s'.", uploadUrl, re.getMessage()));
          }
        } while (resumeInfo == null);
        Log.d(LOG_TAG, String.format("Resuming stalled upload to: %s.", uploadUrl));
        if (resumeInfo.videoId != null) { // upload actually complted despite the exception
          videoId = resumeInfo.videoId;
          Log.d(LOG_TAG, String.format("No need to resume video ID '%s'.", videoId));          
          break;
        } else {
          int nextByteToUpload = resumeInfo.nextByteToUpload;
          Log.d(LOG_TAG, String.format("Next byte to upload is '%d'.", nextByteToUpload));
          this.totalBytesUploaded = nextByteToUpload; // possibly rolling back the previosuly saved value
          fileSize = this.currentFileSize - nextByteToUpload;
          start = nextByteToUpload;
        }
      }
    }

    if (videoId != null) {
      return videoId;
    }

    return null;
  }

  private String uploadMetaData(String slug, String contentType, long contentLength, boolean retry) throws IOException {
    String uploadUrl = INITIAL_UPLOAD_URL;

    HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl, slug);
    urlConnection.setRequestMethod("POST");
    urlConnection.setDoOutput(true);
    
    urlConnection.setRequestProperty("X-Upload-Content-Type",contentType);
    urlConnection.setRequestProperty("X-Upload-Content-Length",contentLength+"");
    
    
    String atomData;

    
    String category = DEFAULT_VIDEO_CATEGORY;
    this.tags = DEFAULT_VIDEO_TAGS;

	
  String template = Util.readFile(activity, R.raw.gdata).toString();
  atomData = String.format(template, title, description, category, this.tags);
  
      /*
  } else {
      String template = Util.readFile(activity, R.raw.gdata_geo).toString();
      atomData = String.format(template, title, description, category, this.tags,
          videoLocation.getLatitude(), videoLocation.getLongitude());
    }*/
    
    urlConnection.setRequestProperty("Content-Length", atomData.length()+"");

    OutputStreamWriter outStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
    outStreamWriter.write(atomData);
    outStreamWriter.close();

    int responseCode = urlConnection.getResponseCode();
    

	// ERROR LOGGING
	InputStream is = urlConnection.getErrorStream();
	if (is != null) {
		Log.e(LOG_TAG, " Error stream from Youtube available!");
		BufferedReader in = new BufferedReader(
				new InputStreamReader(is));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			Log.d(LOG_TAG, inputLine);
		}
		in.close();

		Map<String, List<String>> hfs = urlConnection.getHeaderFields();
		for (Entry<String, List<String>> hf : hfs.entrySet()) {
			Log.d(LOG_TAG, " entry : " + hf.getKey());
			List<String> vals = hf.getValue();
			for (String s : vals) {
				Log.d(LOG_TAG, "vals:" + s);
			}
		}
	}
    
    
    if (responseCode < 200 || responseCode >= 300) {
      // The response code is 40X
      if ((responseCode + "").startsWith("4") && retry) {
        Log.d(LOG_TAG, "retrying to fetch auth token for " + youTubeName);
        this.clientLoginToken = authorizer.getFreshAuthToken(youTubeName, clientLoginToken);
        // Try again with fresh token
        return uploadMetaData(slug, contentType, contentLength, false);
      } else {
        throw new IOException(String.format("response code='%s' (code %d)" + " for %s",
            urlConnection.getResponseMessage(), responseCode, urlConnection.getURL()));
      }
    }

    return urlConnection.getHeaderField("Location");
  }

  private String gdataUpload(File file, String uploadUrl, int start, int end) throws IOException {
    int chunk = end - start + 1;
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    FileInputStream fileStream = new FileInputStream(file);

    HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl, null);
    // some mobile proxies do not support PUT, using X-HTTP-Method-Override to get around this problem
    if (isFirstRequest()) {
      Log.d(LOG_TAG, String.format("Uploaded %d bytes so far, using POST method.", (int)totalBytesUploaded));
      urlConnection.setRequestMethod("POST");
    } else {
      urlConnection.setRequestMethod("POST");
      urlConnection.setRequestProperty("X-HTTP-Method-Override", "PUT");
      Log.d(LOG_TAG, String.format("Uploaded %d bytes so far, using POST with X-HTTP-Method-Override PUT method.",
        (int)totalBytesUploaded));
    }
    urlConnection.setDoOutput(true);
    urlConnection.setFixedLengthStreamingMode(chunk);
    urlConnection.setRequestProperty("Content-Type", "video/mp4");
    urlConnection.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end,
        file.length()));
    Log.d(LOG_TAG, urlConnection.getRequestProperty("Content-Range"));

    OutputStream outStreamWriter = urlConnection.getOutputStream();

    fileStream.skip(start);

    int bytesRead;
    int totalRead = 0;
    while ((bytesRead = fileStream.read(buffer, 0, bufferSize)) != -1) {
      outStreamWriter.write(buffer, 0, bytesRead);
      totalRead += bytesRead;
      this.totalBytesUploaded += bytesRead;

      double percent = (totalBytesUploaded / currentFileSize) * 99;

      String status = String.format(
    	      "fileSize=%f totalBytesUploaded=%f percent=%f", currentFileSize,
    	      totalBytesUploaded, percent); 
     // Log.d(LOG_TAG, status);

      Message msg = handler.obtainMessage(888);
      msg.getData().putString("status", status);
      msg.getData().putInt("progress", (int)percent);
      handler.sendMessage(msg);

      if (totalRead == (end - start + 1)) {
        break;
      }
    }

    outStreamWriter.close();

    int responseCode = urlConnection.getResponseCode();

    Log.d(LOG_TAG, "responseCode=" + responseCode);
    Log.d(LOG_TAG, "responseMessage=" + urlConnection.getResponseMessage());

    try {
      if (responseCode == 201) {
        String videoId = parseVideoId(urlConnection.getInputStream());

        String latLng = null;
        if (this.videoLocation != null) {
          latLng = String.format("lat=%f lng=%f", this.videoLocation.getLatitude(),
              this.videoLocation.getLongitude());
        }

        /*
        submitToYtdDomain(this.ytdDomain, this.assignmentId, videoId,
            this.youTubeName, YouTubeSubmit.this.clientLoginToken, this.title,
            this.description, this.dateTaken, latLng, this.tags);
        */
        
        Message msg = handler.obtainMessage(888);
        msg.getData().putInt("progress", 100);
        handler.sendMessage(msg);

        return videoId;
      } else if (responseCode == 200) {
        Set<String> keySet = urlConnection.getHeaderFields().keySet();
        String keys = urlConnection.getHeaderFields().keySet().toString();
        Log.d(LOG_TAG, String.format("Headers keys %s.", keys));
        for (String key : keySet) {
          Log.d(LOG_TAG, String.format("Header key %s value %s.", key, urlConnection.getHeaderField(key)));          
        }
        Log.w(LOG_TAG, "Received 200 response during resumable uploading");
        throw new IOException(String.format("Unexpected response code : responseCode=%d responseMessage=%s", responseCode,
              urlConnection.getResponseMessage()));
      } else {
        if ((responseCode + "").startsWith("5")) {
          String error = String.format("responseCode=%d responseMessage=%s", responseCode,
              urlConnection.getResponseMessage());
          Log.w(LOG_TAG, error);
          // TODO - this exception will trigger retry mechanism to kick in
          // TODO - even though it should not, consider introducing a new type so
          // TODO - resume does not kick in upon 5xx
          throw new IOException(error);
        } else if (responseCode == 308) {
          // OK, the chunk completed succesfully 
          Log.d(LOG_TAG, String.format("responseCode=%d responseMessage=%s", responseCode,
              urlConnection.getResponseMessage()));
        } else {
          // TODO - this case is not handled properly yet
          Log.w(LOG_TAG, String.format("Unexpected return code : %d %s while uploading :%s", responseCode,
            urlConnection.getResponseMessage(), uploadUrl));
        }
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }

    return null;
  }

  public boolean isFirstRequest() {
    return totalBytesUploaded==0;
  }

  private ResumeInfo resumeFileUpload(String uploadUrl, File file) throws IOException, ParserConfigurationException, SAXException, Internal500ResumeException {
    HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl, file.getName());
    urlConnection.setRequestProperty("Content-Range", "bytes */*");
    urlConnection.setRequestMethod("POST");
    urlConnection.setRequestProperty("X-HTTP-Method-Override", "PUT");
    urlConnection.setFixedLengthStreamingMode(0);

    HttpURLConnection.setFollowRedirects(false);

    urlConnection.connect();
    int responseCode = urlConnection.getResponseCode();

    if (responseCode >= 300 && responseCode < 400) {
      int nextByteToUpload;
      String range = urlConnection.getHeaderField("Range");
      if (range == null) {
        Log.d(LOG_TAG, String.format("PUT to %s did not return 'Range' header.", uploadUrl));
        nextByteToUpload = 0;
      } else {
        Log.d(LOG_TAG, String.format("Range header is '%s'.", range));
        String[] parts = range.split("-");
        if (parts.length > 1) {
          nextByteToUpload = Integer.parseInt(parts[1]) + 1;
        } else {
          nextByteToUpload = 0;
        }
      }
      return new ResumeInfo(nextByteToUpload);
    } else if (responseCode >= 200 && responseCode < 300) {
      return new ResumeInfo(parseVideoId(urlConnection.getInputStream()));
    } else if (responseCode == 500) {
      // TODO this is a workaround for current problems with resuming uploads while switching transport (Wifi->EDGE)
      throw new Internal500ResumeException(String.format("Unexpected response for PUT to %s: %s " +
      		"(code %d)", uploadUrl, urlConnection.getResponseMessage(), responseCode));
    } else {
      throw new IOException(String.format("Unexpected response for PUT to %s: %s " +
      		"(code %d)", uploadUrl, urlConnection.getResponseMessage(), responseCode));
    }
  }


  private boolean shouldResume() {
    this.numberOfRetries++;
    if (this.numberOfRetries>MAX_RETRIES) {
      return false;
    }
    try {
      int sleepSeconds = (int) Math.pow(BACKOFF, this.numberOfRetries);
      Log.d(LOG_TAG,String.format("Zzzzz for : %d sec.", sleepSeconds));
      Thread.currentThread().sleep(sleepSeconds * 1000);
      Log.d(LOG_TAG,String.format("Zzzzz for : %d sec done.", sleepSeconds));      
    } catch (InterruptedException se) {
      se.printStackTrace();
      return false;
    }
    return true;
  }

  private String parseVideoId(InputStream atomDataStream) throws ParserConfigurationException,
      SAXException, IOException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(atomDataStream);

    NodeList nodes = doc.getElementsByTagNameNS("*", "*");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String nodeName = node.getNodeName();
      if (nodeName != null && nodeName.equals("yt:videoid")) {
        return node.getFirstChild().getNodeValue();
      }
    }
    return null;
  }

  private HttpURLConnection getGDataUrlConnection(String urlString, String slug) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Host", "uploads.gdata.youtube.com");
    
    connection.setRequestProperty("Content-Type", CONTENT_TYPE);
    
    String devKey = activity.getString(R.string.dev_key);
    connection.setRequestProperty("X-GData-Key", String.format("key=%s", devKey));
    
    if (clientLoginToken != null)
		connection.setRequestProperty("Authorization", String.format(
				"GoogleLogin auth=\"%s\"", clientLoginToken));
    
    
    connection.setRequestProperty("GData-Version", "2");
    
    if (slug != null)
    	connection.setRequestProperty("Slug", slug);
        
    return connection;
  }

  public void getAuthTokenWithPermission(String accountName) {
	
	if (accountName == null)
		this.youTubeName = ((GlsAuthorizer)authorizer).getAccount(null).name;
	else
		this.youTubeName = accountName;
	  
    this.authorizer.fetchAuthToken(accountName, activity, new AuthorizationListener<String>() {
      @Override
      public void onCanceled() {
      }

      @Override
      public void onError(Exception e) {
    	  Log.e("YouTube","error on auth",e);
      }

      @Override
      public void onSuccess(String result) {
        YouTubeSubmit.this.clientLoginToken = result;
        Log.d("YouTube","got client token: " + result);
        upload(youTubeName,videoFile, videoContentType);
      }});
  }

/*
  private void getVideoLocation() {
    this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setPowerRequirement(Criteria.POWER_HIGH);
    criteria.setAltitudeRequired(false);
    criteria.setBearingRequired(false);
    criteria.setSpeedRequired(false);
    criteria.setCostAllowed(true);

    String provider = locationManager.getBestProvider(criteria, true);

    this.locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        if (location != null) {
          YouTubeSubmitActivity.this.videoLocation = location;
          double lat = location.getLatitude();
          double lng = location.getLongitude();
          Log.d(LOG_TAG, "lat=" + lat);
          Log.d(LOG_TAG, "lng=" + lng);

          TextView locationText = (TextView) findViewById(R.id.locationLabel);
          locationText.setText("Geo Location: " + String.format("lat=%.2f lng=%.2f", lat, lng));
          locationManager.removeUpdates(this);
        } else {
          Log.d(LOG_TAG, "location is null");
        }
      }

      @Override
      public void onProviderDisabled(String provider) {
      }

      @Override
      public void onProviderEnabled(String provider) {
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {
      }

    };
    
    if (provider != null) {
      locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
    }
  }
*/
  /*
  public void submitToYtdDomain(String ytdDomain, String assignmentId, String videoId,
      String youTubeName, String clientLoginToken, String title, String description,
      Date dateTaken, String videoLocation, String tags) {

    JSONObject payload = new JSONObject();
    try {
      payload.put("method", "NEW_MOBILE_VIDEO_SUBMISSION");
      JSONObject params = new JSONObject();

      params.put("videoId", videoId);
      params.put("youTubeName", youTubeName);
      params.put("clientLoginToken", clientLoginToken);
      params.put("title", title);
      params.put("description", description);
      params.put("videoDate", dateTaken.toString());
      params.put("tags", tags);

      if (videoLocation != null) {
        params.put("videoLocation", videoLocation);
      }

      if (assignmentId != null) {
        params.put("assignmentId", assignmentId);
      }

      payload.put("params", params);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    String jsonRpcUrl = "https://" + ytdDomain + "/jsonrpc";
    String json = Util.makeJsonRpcCall(jsonRpcUrl, payload, activity);

    if (json != null) {
      try {
        JSONObject jsonObj = new JSONObject(json);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }*/

  class ResumeInfo {
    int nextByteToUpload;
    String videoId;
    ResumeInfo(int nextByteToUpload) {
      this.nextByteToUpload = nextByteToUpload;
    }
    ResumeInfo(String videoId) {
      this.videoId = videoId;
    }
  }

  /**
   * Need this for now to trigger entire upload transaction retry
   */
  class Internal500ResumeException extends Exception {
    Internal500ResumeException(String message) {
      super(message);
    }
  }
}