package org.codeforafrica.listeningpost;

import org.codeforafrica.listeningpost.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class QuickStoryWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {

    // Get all ids
    ComponentName thisWidget = new ComponentName(context,
    		QuickStoryWidgetProvider.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
    for (int widgetId : allWidgetIds) {

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
          R.layout.widget_quickstory_layout);
      // Set the text
      //remoteViews.setTextViewText(R.id.update, String.valueOf(number));

      Intent intentHome = new Intent(context, HomePanelsActivity.class);
      PendingIntent pendingIntentHome = PendingIntent.getActivity(context,
    		  intentHome.hashCode(), intentHome, 0);
      remoteViews.setOnClickPendingIntent(R.id.btnWidgetHome, pendingIntentHome);
      
      // Register an onClickListener
      
      Intent intentStoryAudio = new Intent(context, StoryNewActivity.class);
      intentStoryAudio.putExtra("story_name", "Quick Story");
      intentStoryAudio.putExtra("storymode", 1);
      intentStoryAudio.putExtra("quickstory", 1);
      intentStoryAudio.putExtra("auto_capture", true);
      
      remoteViews.setOnClickPendingIntent(R.id.btnWidgetAudio,  PendingIntent.getActivity(context, intentStoryAudio.hashCode(), intentStoryAudio, 0));
      

      Intent intentStoryPhoto = new Intent(context, StoryNewActivity.class);
      intentStoryPhoto.putExtra("story_name", "Quick Story");
      intentStoryPhoto.putExtra("storymode", 2);
      intentStoryPhoto.putExtra("auto_capture", true);
      intentStoryAudio.putExtra("quickstory", 1);

      remoteViews.setOnClickPendingIntent(R.id.btnWidgetPhoto, PendingIntent.getActivity(context, intentStoryPhoto.hashCode(), intentStoryPhoto, 0));
      

      Intent intentStoryVideo = new Intent(context, StoryNewActivity.class);
      intentStoryVideo.putExtra("story_name", "Quick Story");
      intentStoryVideo.putExtra("storymode", 0);
      intentStoryVideo.putExtra("auto_capture", true);
      intentStoryAudio.putExtra("quickstory", 1);

      remoteViews.setOnClickPendingIntent(R.id.btnWidgetVideo, PendingIntent.getActivity(context, intentStoryVideo.hashCode(), intentStoryVideo, 0));
      
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
  }
} 