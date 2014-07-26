package org.codeforafrica.timby.listeningpost;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.spy.StoryNewService;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.RemoteViews;


public class DiscreetStoryWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {

    // Get all ids
    ComponentName thisWidget = new ComponentName(context,
    		DiscreetStoryWidgetProvider.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
    for (int widgetId : allWidgetIds) {

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
          R.layout.discreet_capture_widget);
      // Set the text      
      remoteViews.setTextViewText(R.id.camera, String.valueOf((new SimpleDateFormat("yyyy-MM-dd")).format(new Date())));
      remoteViews.setTextViewText(R.id.video, String.valueOf((new SimpleDateFormat("HH:mm:ss")).format(new Date())));
      remoteViews.setTextViewText(R.id.audio, String.valueOf((new SimpleDateFormat("EEEE")).format(new Date())));

      // Register an onClickListener      
      Intent intentStoryAudio = new Intent(context, StoryNewService.class);
      intentStoryAudio.putExtra("story_name", "Quick Story");
      intentStoryAudio.putExtra("storymode", 1);
      intentStoryAudio.putExtra("quickstory", 1);
      intentStoryAudio.putExtra("auto_capture", true);
      
      remoteViews.setOnClickPendingIntent(R.id.audio,  PendingIntent.getService(context, intentStoryAudio.hashCode(), intentStoryAudio, 0));
      

      Intent intentStoryPhoto = new Intent(context, StoryNewService.class);
      intentStoryPhoto.putExtra("story_name", "Quick Story");
      intentStoryPhoto.putExtra("storymode", 2);
      intentStoryPhoto.putExtra("auto_capture", true);
      intentStoryAudio.putExtra("quickstory", 1);

      remoteViews.setOnClickPendingIntent(R.id.camera, PendingIntent.getService(context, intentStoryPhoto.hashCode(), intentStoryPhoto, 0));
      

      Intent intentStoryVideo = new Intent(context, StoryNewService.class);
      intentStoryVideo.putExtra("story_name", "Quick Story");
      intentStoryVideo.putExtra("storymode", 0);
      intentStoryVideo.putExtra("auto_capture", true);
      intentStoryAudio.putExtra("quickstory", 1);

      remoteViews.setOnClickPendingIntent(R.id.video, PendingIntent.getService(context, intentStoryVideo.hashCode(), intentStoryVideo, 0));
      
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
  }
} 