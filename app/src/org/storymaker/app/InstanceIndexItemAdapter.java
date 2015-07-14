package org.storymaker.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.IndexManager;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.BaseIndexItem;
import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.model.InstanceIndexItem;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class InstanceIndexItemAdapter extends RecyclerView.Adapter<InstanceIndexItemAdapter.ViewHolder> {

    public List<BaseIndexItem> mDataset;
    private BaseIndexItemSelectedListener mListener;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static interface BaseIndexItemSelectedListener {
        public void onStorySelected(BaseIndexItem selectedItem);
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

    public InstanceIndexItemAdapter(@NonNull List<BaseIndexItem> myDataset,
                                    @Nullable BaseIndexItemSelectedListener listener) {
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

        final BaseIndexItem baseItem = mDataset.get(position);
        String description = baseItem.getDescription();
        if (baseItem instanceof InstanceIndexItem) {
            final InstanceIndexItem instanceItem = (InstanceIndexItem) baseItem;
            holder.title.setText(String.format("%s %s",
                    !TextUtils.isEmpty(instanceItem.getTitle()) ?
                            instanceItem.getTitle() :
                            context.getString(R.string.no_title),
                    instanceItem.getStoryCreationDate() == 0 ?
                            "" :
                            sdf.format(new Date(instanceItem.getStoryCreationDate()))));

            int mediumStringResId;
            String storyType = TextUtils.isEmpty(instanceItem.getStoryType()) ?
                    "?" : instanceItem.getStoryType();



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

            if (!TextUtils.isEmpty(instanceItem.getInstanceFilePath())) {
                description += sdf.format(new Date(new File(instanceItem.getInstanceFilePath()).lastModified()));
            }

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onStorySelected(instanceItem);
                    }
                }
            });

            String thumbnailPath = baseItem.getThumbnailPath();
            if (!TextUtils.isEmpty(thumbnailPath)) {
                if (thumbnailPath.startsWith("http")) {
                    Picasso.with(context)
                            .load(thumbnailPath)
                            .into(holder.thumb);
                } else {
                    Picasso.with(context)
                            .load(new File(thumbnailPath)) // FIXME leaving for now, but doesnt picasso handle making teh File object iteself?
                            .into(holder.thumb);
                }
            } else {
                Picasso.with(context)
                        .load(R.drawable.no_thumbnail)
                        .into(holder.thumb);
            }
        } else {
            ExpansionIndexItem expansionIndexItem = (ExpansionIndexItem) baseItem;
            // check if this is already installed or waiting to be downloaded to change which picture we show
            HashMap<String, ExpansionIndexItem> installedIds = IndexManager.loadInstalledIdIndex(context);
            holder.title.setText(baseItem.getTitle());

            // need to verify that content pack containing thumbnail actually exists
            File contentCheck = new File(IndexManager.buildFilePath(expansionIndexItem), IndexManager.buildFileName(expansionIndexItem, Constants.MAIN));

            // need to verify that index item has been updated with content pack thumbnail path
            String contentPath = expansionIndexItem.getPackageName() + File.separator + expansionIndexItem.getExpansionId();

            if (installedIds.containsKey(expansionIndexItem.getExpansionId()) &&
                    contentCheck.exists() &&
                    baseItem.getThumbnailPath().startsWith(contentPath)) {
//              ZipHelper.getTempFile((baseItem.getThumbnailPath(), "/sdcard/"
                holder.thumb.setImageBitmap(BitmapFactory.decodeStream(ZipHelper.getFileInputStream(baseItem.getThumbnailPath(), context)));
            } else {
                String thumbnailPath = baseItem.getThumbnailPath();
                if (!TextUtils.isEmpty(thumbnailPath)) {
                    if (thumbnailPath.startsWith("http")) {
                        Picasso.with(context)
                                .load(thumbnailPath)
                                .into(holder.thumb);
                    } else {
                        File file = IndexManager.copyThumbnail(context, expansionIndexItem.getThumbnailPath());
                        Picasso.with(context)
                                .load(file)
//                        .load("file:///android_assets/" + expansionIndexItem.getThumbnailPath())
                                .into(holder.thumb);
                    }
                } else {
                    Picasso.with(context)
                            .load(R.drawable.no_thumbnail)
                            .into(holder.thumb);
                }
                // FIXME desaturate image and overlay the downarrow
            }
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onStorySelected(baseItem);
                }
            }
        });

        holder.description.setText(description);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}