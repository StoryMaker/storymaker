package info.guardianproject.mrapp;

import com.fima.cardsui.views.CardUI;

import info.guardianproject.mrapp.ui.MyCard;
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
			mCardView.refresh();
	  }

}
