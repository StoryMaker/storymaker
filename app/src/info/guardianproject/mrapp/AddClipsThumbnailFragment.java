package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.template.Clip;
import info.guardianproject.mrapp.model.Media;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * AddClipsThumbnailFragment
 */
public class AddClipsThumbnailFragment extends Fragment {
    
    private Clip clip;
    private int mClipIndex;
    private Media mMedia;
    private EditorBaseActivity mActivity;
    
    public AddClipsThumbnailFragment(Clip clip, int clipIndex, Media media, EditorBaseActivity activity) {
        this.clip = clip;
        mClipIndex = clipIndex;
        mMedia = media;
        mActivity = activity;
    }

    public static final String ARG_CLIP_TYPE_ID = "clip_type_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_clips_page, null);

        try {

            ImageView iv = (ImageView) view.findViewById(R.id.clipTypeImage);

            if (mMedia != null) {

                Bitmap thumb = mActivity.getThumbnail(mMedia);
                iv.setImageBitmap(thumb);

            } else {
                if (clip.mShotType != -1)
                {
                    TypedArray drawableIds = getActivity().getResources().obtainTypedArray(
                            R.array.cliptype_thumbnails);

                    int drawableId = drawableIds.getResourceId(clip.mShotType, 0);

                    iv.setImageResource(drawableId);
                }
                else if (clip.mArtwork != null)
                {
                    iv.setImageBitmap(BitmapFactory.decodeStream(getActivity().getAssets()
                            .open(clip.mArtwork)));
                }
            }


            ((TextView) view.findViewById(R.id.clipTypeTitle)).setText(clip.mTitle);
            
            if (clip.mShotSize != null)
                ((TextView) view.findViewById(R.id.clipTypeShotSize)).setText(clip.mShotSize);
            else
                ((TextView) view.findViewById(R.id.clipTypeShotSize)).setVisibility(View.GONE);
            
            ((TextView) view.findViewById(R.id.clipTypeGoal)).setText(clip.mGoal);
            ((TextView) view.findViewById(R.id.clipTypeDescription)).setText(clip.mDescription);
            ((TextView) view.findViewById(R.id.clipTypeGoalLength)).setText(clip.mLength);
            ((TextView) view.findViewById(R.id.clipTypeTip)).setText(clip.mTip);
            ((TextView) view.findViewById(R.id.clipTypeSecurity)).setText(clip.mSecurity);

            iv.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v) {
                    ViewPager vp = (ViewPager) v.getParent().getParent().getParent()
                            .getParent();
                    mActivity.mMPM.mClipIndex = vp.getCurrentItem();

                    ((SceneEditorActivity) mActivity).openCaptureMode(clip, mClipIndex);

                }

            });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return view;
    }
}