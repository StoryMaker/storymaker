package org.storymaker.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import scal.io.liger.model.InstanceIndexItem;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class InstanceIndexItemAdapter extends RecyclerView.Adapter<InstanceIndexItemAdapter.ViewHolder> {

    public List<InstanceIndexItem> mDataset;
    private InstanceIndexItemSelectedListener mListener;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static interface InstanceIndexItemSelectedListener {
        public void onStorySelected(InstanceIndexItem selectedItem);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public ImageView thumb;
        public TextView description;
        public TextView title;

        public ViewHolder(View v) {
            super(v);
            card        = (CardView) v;
            title       = (TextView) v.findViewById(R.id.title);
            description = (TextView) v.findViewById(R.id.description);
            thumb       = (ImageView) v.findViewById(R.id.thumbnail);
        }
    }

    public InstanceIndexItemAdapter(@NonNull List<InstanceIndexItem> myDataset,
                                    @Nullable InstanceIndexItemSelectedListener listener) {
        mDataset = myDataset;
        mListener = listener;
    }

    @Override
    public InstanceIndexItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_picture, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Context context = holder.card.getContext();

        final InstanceIndexItem instance = mDataset.get(position);

        holder.title.setText(String.format("%s %s",
                !TextUtils.isEmpty(instance.getStoryTitle()) ?
                        instance.getStoryTitle() :
                        context.getString(R.string.no_title),
                instance.getStoryCreationDate() == 0 ?
                        "" :
                        sdf.format(new Date(instance.getStoryCreationDate()))));

        int mediumStringResId;
        String storyType = TextUtils.isEmpty(instance.getStoryType()) ?
                           "?" : instance.getStoryType();

        String description;

        if (storyType.equals("learningGuide")) {
            description = context.getString(R.string.learning_guide_description);
        } else {
            switch (storyType) {
                case "video":
                    mediumStringResId = R.string.lbl_video;
                    break;
                case "audio":
                    mediumStringResId = R.string.lbl_audio;
                    break;
                case "photo":
                    mediumStringResId = R.string.lbl_photo;
                    break;
                default:
                    mediumStringResId = R.string.no_medium;
                    break;

            }

            description = context.getString(mediumStringResId)
                    + ". "
                    + context.getString(org.storymaker.app.R.string.last_modified)
                    + ": ";

            if (!TextUtils.isEmpty(instance.getInstanceFilePath()))
                description += sdf.format(new Date(new File(instance.getInstanceFilePath()).lastModified()));
        }

        holder.description.setText(description);

        if (!TextUtils.isEmpty(instance.getStoryThumbnailPath())) {
            Picasso.with(context)
                    .load(new File(instance.getStoryThumbnailPath()))
                    .into(holder.thumb);
        } else {
            Picasso.with(context)
                    .load(R.drawable.no_thumbnail)
                    .into(holder.thumb);
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onStorySelected(instance);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}