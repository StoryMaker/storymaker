package org.codeforafrica.timby.listeningpost;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.ui.MyCard;

import com.fima.cardsui.views.CardUI;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class AboutActivity extends BaseActivity{
    private CardUI mCardView;

	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	    
	    	super.onCreate(savedInstanceState);
	    	
	        setContentView(R.layout.activity_about);
	        
	        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
	      
	        
	        String title = "About ListeningPost";
	        String desc = getString(R.string.about);
	        
	        //init CardView
			mCardView = (CardUI) findViewById(R.id.cardsview);
			mCardView.setSwipeable(false);
			
	        MyCard androidViewsCard = new MyCard(title, desc);
			mCardView.addCard(androidViewsCard);
			
			MyCard androidViewsCard2 = new MyCard(title, desc);
			mCardView.addCard(androidViewsCard2);
			
			mCardView.refresh();
	  }

}
