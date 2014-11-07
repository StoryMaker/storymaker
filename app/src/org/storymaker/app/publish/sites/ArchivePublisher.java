package org.storymaker.app.publish.sites;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;
import org.storymaker.app.server.ServerManager;

import java.util.Locale;

import android.content.Context;
import android.util.Log;

public class ArchivePublisher extends PublisherBase {
	private final String TAG = "ArchivePublisher";

	private static final String ARCHIVE_URL_DOWNLOAD = "https://archive.org/download/";
	private static final String ARCHIVE_API_ENDPOINT = "http://s3.us.archive.org/";

	public ArchivePublisher(Context context, PublishController publishController, PublishJob publishJob) {
		super(context, publishController, publishJob);
	}

	public void startRender() {
		Log.d(TAG, "startRender");
		// TODO should detect if user is directly publishing to youtube so we don't double publish to there
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(),mPublishJob.getId(), JobTable.TYPE_RENDER, null,VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}

	public void startUpload() {
		Log.d(TAG, "startUpload");
		 // FIXME hardcoded to youtube?
		Job newJob = new Job(mContext, mPublishJob.getProjectId(),mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_ARCHIVE,null);
		mController.enqueueJob(newJob);
	}

	public String getEmbed(Job job) {
		if(null == job) {
			return null;
		}
		
		String medium = job.getSpec();
		String fileURL = job.getResult();
		String width = null;
		String height = null;
		String cleanFileURL = null;

		if (medium != null) {
			if (medium.equals(ServerManager.CUSTOM_FIELD_MEDIUM_PHOTO)) {
				// keep default image size
				width = "";
				height = "";
			} else if (medium.equals(ServerManager.CUSTOM_FIELD_MEDIUM_VIDEO)) {
				width = "600";
				height = "480";
			} else if (medium.equals(ServerManager.CUSTOM_FIELD_MEDIUM_AUDIO)) {
				width = "500";
				height = "30";
			}
			
			cleanFileURL = cleanFileURL(fileURL);
		}

		String embed  = null;
		if (null != width && null != height && null != cleanFileURL) {	
			embed = String.format(Locale.US, "[archive %s %s %s]", cleanFileURL, width, height);
			
			/*
			if(isMediumPhoto) {
				embed = String.format(Locale.US, "<img src='%s' alt='Archive Embed'>" ,
													ARCHIVE_URL_DOWNLOAD + cleanFileURL);
			} else {
				embed = String.format(Locale.US, "<iframe " +
												"src='%s' " +
												"width='%s' " +
												"height='%s' " +
												"frameborder='0' " +
												"webkitallowfullscreen='true' " + 
												"mozallowfullscreen='true' allowfullscreen>" +
												"</iframe>",
												ARCHIVE_URL_DOWNLOAD + cleanFileURL, width, height);
			}
			*/
		}

		return embed;
	}

	private String cleanFileURL(String fileURL) {	
		fileURL = fileURL.replace(ARCHIVE_API_ENDPOINT, "");
		return fileURL;
	}
}
