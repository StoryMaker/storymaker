package info.guardianproject.mrapp.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import info.guardianproject.mrapp.R;

public class ReportCard extends Card {

	private String mDesc = "";
	private String status = "";
	private String date = "";
	private int vids = 0;
	private int pics = 0;
	private int auds = 0;
	private View mView;
	private TextView mTextViewTitle;
	private TextView mTextViewStat;
	private TextView mTextViewDate;
	private TextView mTextViewpic;
	private TextView mTextViewvid;
	private TextView mTextViewaud;
	private int mId = -1;
	private int mIcon = -1;
	private Drawable mImage = null;
	
	public ReportCard(String title, String desc, String status, String date, int vids, int pics, int auds){
		super(title);
		mDesc = desc;
		this.status = status;
		this.date = date;
		this.vids = vids;
		this.pics = pics;
		this.auds = auds;
	}

	public void setIcon (int icon)
	{
		mIcon = icon;
	}
	
	public void setImage (Drawable image)
	{
		mImage = image;
	}

	private OnClickListener mListener;
	
	@Override
	public void setOnClickListener(OnClickListener listener) {
		
		mListener = listener;
		
		super.setOnClickListener(mListener);
	}

	@Override
	public View getCardContent(Context context) {
		
		mView = LayoutInflater.from(context).inflate(R.layout.report_card_picture, null);

		mTextViewTitle =((TextView) mView.findViewById(R.id.title));
		mTextViewTitle.setText(title);
		
		mTextViewStat = ( (TextView) mView.findViewById(R.id.status));
		mTextViewStat.setText(status);
		mTextViewDate = ( (TextView) mView.findViewById(R.id.date));
		mTextViewDate.setText(date);
		mTextViewpic = ( (TextView) mView.findViewById(R.id.textPic));
		mTextViewpic.setText(String.valueOf(pics));
		mTextViewvid = ( (TextView) mView.findViewById(R.id.textVid));
		mTextViewvid.setText(String.valueOf(vids));
		mTextViewaud = ( (TextView) mView.findViewById(R.id.textAud));
		mTextViewaud.setText(String.valueOf(auds));

		if (mId != -1)
		{
			mView.setId(mId);
			mView.setOnClickListener(mListener);
			mTextViewTitle.setId(mId);
			mTextViewDate.setId(mId);
			mTextViewpic.setId(mId);
			mTextViewvid.setId(mId);
			mTextViewaud.setId(mId);
			
			mView.setOnClickListener(mListener);
		}
		
		if (mImage != null)
		{
			ImageView iv = ((ImageView)mView.findViewById(R.id.imageView1));
			iv.setImageDrawable(mImage);
			
			if (mId != -1)
			{
				iv.setId(mId);
				iv.setOnClickListener(mListener);
			}
		}
		
		if (mIcon != -1)
		{
			ImageView iv = ((ImageView)mView.findViewById(R.id.cardIcon));
			iv.setImageResource(mIcon);
			
			if (mId != -1)
			{
				iv.setId(mId);
				iv.setOnClickListener(mListener);
			}
		}
		
		return mView;
	}
	
	
	public void setId (int id)
	{
		mId = id;
	}

	
	
	
}
