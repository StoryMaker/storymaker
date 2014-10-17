package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.template.Clip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * AddClipsThumbnailFragment
 */
@SuppressLint("ValidFragment")
public class AddClipsThumbnailFragment extends Fragment {
    
    private Clip clip;
    private int mClipIndex;
    private Media mMedia;
    private EditorBaseActivity mActivity;
    
    public AddClipsThumbnailFragment(){}
    
    public AddClipsThumbnailFragment(Clip clip, int clipIndex, Media media, EditorBaseActivity activity) {
        this.clip = clip;
        mClipIndex = clipIndex;
        mMedia = media;
        mActivity = activity;
    }

    public static final String ARG_CLIP_TYPE_ID = "clip_type_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_clips_page, null);

        try {

            ImageView ivImageClip = (ImageView) view.findViewById(R.id.clipTypeImage);
            ImageView ivAddClip = (ImageView) view.findViewById(R.id.ivAddGalleryClip);
            ImageView ivRemoveClip = (ImageView) view.findViewById(R.id.ivRemoveClip);
            ImageView ivRecordClip = (ImageView) view.findViewById(R.id.ivRecordClip);

            if (mMedia != null) {

                Bitmap thumb = Media.getThumbnail(mActivity,mMedia,mActivity.mMPM.mProject);
                ivImageClip.setImageBitmap(thumb);
                
                ivAddClip.setVisibility(View.GONE);
                ivRecordClip.setVisibility(View.GONE);

            }
            else {
            	ivRemoveClip.setVisibility(View.GONE);
            	
                if (clip.mShotType != -1) {
                    TypedArray drawableIds = getActivity().getResources().obtainTypedArray(R.array.cliptype_thumbnails);
                    int drawableId = drawableIds.getResourceId(clip.mShotType, 0);

                    ivImageClip.setImageResource(drawableId);
                }
                else if (clip.mArtwork != null){
                    ivImageClip.setImageBitmap(BitmapFactory.decodeStream(getActivity().getAssets().open(clip.mArtwork)));
                }
            }

            ((TextView) view.findViewById(R.id.clipTypeTitle)).setText(clip.mTitle);
            
            if (clip.mShotSize != null)
                ((TextView) view.findViewById(R.id.clipTypeShotSize)).setText(clip.mShotSize);
            else
                ((TextView) view.findViewById(R.id.clipTypeShotSize)).setVisibility(View.GONE);
            
            ((TextView) view.findViewById(R.id.clipTypeGoal)).setText(clip.mGoal);
            ((TextView) view.findViewById(R.id.clipTypeDescription)).setText(clip.mDescription);
            //((TextView) view.findViewById(R.id.clipTypeGoalLength)).setText(clip.mLength);
            ((TextView) view.findViewById(R.id.clipTypeTip)).setText(clip.mTip);
            //((TextView) view.findViewById(R.id.clipTypeSecurity)).setText(clip.mSecurity);

            ivAddClip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SceneEditorActivity) mActivity).addMediaFromGallery();
                }
            });
            
            ivRemoveClip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) { 
                	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(getActivity().getResources().getString(R.string.delete_clip))
                            .setIcon(getActivity().getResources().getDrawable(R.drawable.ic_action_warning))
                            .setMessage(getActivity().getResources().getString(R.string.delete_warning))
                            .setCancelable(false)
                            .setPositiveButton(getActivity().getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ((SceneEditorActivity) mActivity).deleteCurrentShot();
                                }
                            })
                            .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();       
                }
            });
            
            ivRecordClip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SceneEditorActivity) mActivity).openCaptureMode(clip.mShotType, mClipIndex);
                }
            });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return view;
    }
}