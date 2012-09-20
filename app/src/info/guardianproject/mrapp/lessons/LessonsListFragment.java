package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.db.LessonsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class LessonsListFragment extends ListFragment {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    String[] projection = { StoryMakerDB.Schema.Lessons.ID, StoryMakerDB.Schema.Lessons.COL_TITLE };
	    String[] uiBindFrom = { StoryMakerDB.Schema.Lessons.COL_TITLE };
	   // int[] uiBindTo = { R.id.title };
	    Cursor tutorials = getActivity().managedQuery(
	            LessonsProvider.CONTENT_URI, projection, null, null, null);
	  
	    //CursorAdapter adapter = new SimpleCursorAdapter(getActivity()
	        //    .getApplicationContext(), R.layout.list_item, tutorials,
	      //      uiBindFrom, uiBindTo);
	    //setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	    String projection[] = { StoryMakerDB.Schema.Lessons.COL_URL };
	    Cursor tutorialCursor = getActivity().getContentResolver().query(
	            Uri.withAppendedPath(LessonsProvider.CONTENT_URI,
	                    String.valueOf(id)), projection, null, null, null);
	    if (tutorialCursor.moveToFirst()) {
	        String tutorialUrl = tutorialCursor.getString(0);
	        
	        //tutSelectedListener.onTutSelected(tutorialUrl);
	    }
	    tutorialCursor.close();
	}
}
