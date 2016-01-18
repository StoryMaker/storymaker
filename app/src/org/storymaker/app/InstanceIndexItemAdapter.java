package org.storymaker.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import scal.io.liger.Constants;
import scal.io.liger.StorageHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.sqlbrite.BaseIndexItem;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItemDao;
import timber.log.Timber;

//import scal.io.liger.IndexManager;
//import scal.io.liger.model.BaseIndexItem;
//import scal.io.liger.model.ExpansionIndexItem;
//import scal.io.liger.model.InstanceIndexItem;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class InstanceIndexItemAdapter extends RecyclerView.Adapter<InstanceIndexItemAdapter.ViewHolder> {

    public List<BaseIndexItem> mDataset;
    private BaseIndexItemSelectedListener mListener;
    private InstalledIndexItemDao installedDao;
    private InstanceIndexItemDao instanceDao;

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
                                    @Nullable BaseIndexItemSelectedListener listener,
                                    InstalledIndexItemDao installedDao, InstanceIndexItemDao instanceDao) {
        mDataset = myDataset;
        mListener = listener;

        this.installedDao = installedDao;
        this.instanceDao = instanceDao;

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

            /*
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onStorySelected(instanceItem);
                    }
                }
            });
            */

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
            HashMap<String, ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(context, installedDao);
            holder.title.setText(baseItem.getTitle());

            // show the content pack version info for help debugging upgrade issues
            if (BuildConfig.DEBUG) {
                int patchVer = !TextUtils.isEmpty(expansionIndexItem.getPatchFileVersion()) ? Integer.parseInt(expansionIndexItem.getPatchFileVersion()) : 0;
                String ver = expansionIndexItem.getExpansionFileVersion() + ((patchVer > 0) ? ("." + patchVer) : "");
                description = "v" + ver + ": " + description;
            }

            // need to verify that content pack containing thumbnail actually exists
            File contentCheck = new File(StorymakerIndexManager.buildFilePath(expansionIndexItem, context), StorymakerIndexManager.buildFileName(expansionIndexItem, Constants.MAIN));

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
                        File file = StorymakerIndexManager.copyThumbnail(context, expansionIndexItem.getThumbnailPath());
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

        holder.card.setOnLongClickListener(new DeleteListener(context, baseItem));

        holder.description.setText(description);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private boolean CheckIfInstalled(BaseIndexItem itemToCheck) {

        if (itemToCheck instanceof ExpansionIndexItem) {
            final String expansionId = ((ExpansionIndexItem) itemToCheck).getExpansionId();

            final ArrayList<String> installedList = new ArrayList<String>();

            installedDao.getInstalledIndexItems().subscribe(new Action1<List<InstalledIndexItem>>() {

                @Override
                public void call(List<InstalledIndexItem> installedIndexItems) {

                    //ArrayList<scal.io.liger.model.sqlbrite.ExpansionIndexItem> indexList = new ArrayList<scal.io.liger.model.sqlbrite.ExpansionIndexItem>();

                    for (InstalledIndexItem item : installedIndexItems) {
                        //indexList.add(item);
                        //returnList.add(item.getExpansionId());
                        if (item.getExpansionId().equals(expansionId)) {
                            installedList.add(expansionId);
                            break;
                        }
                    }


                }
            });

            if (installedList.size() == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    private class DeleteListener implements View.OnLongClickListener {

        Context context;
        BaseIndexItem item;
        int safePosition;

        public DeleteListener(Context context, BaseIndexItem item) {
            this.context = context;
            this.item = item;
        }

        private void sendDeleteCompleteMessage(String expansionId) {

            Intent intent = new Intent("delete-complete");
            intent.putExtra("expansionid", expansionId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        @Override
        public boolean onLongClick(View v) {

            safePosition = InstanceIndexItemAdapter.this.mDataset.indexOf(item);

            if (item instanceof InstanceIndexItem) {

                new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.delete_story))
                    .setMessage(item.getTitle() + " " + sdf.format(new Date(((InstanceIndexItem) item).getStoryCreationDate()))) // FIXME we need to move this logic into the model
                    // using negative button to account for fixed order
                    .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Timber.d("DELETING FILES FOR " + ((InstanceIndexItem) item).getTitle());
                            ((InstanceIndexItem) item).deleteAssociatedFiles(context, false);

                            //Log.d("InstanceIndexItemAdapter", "deleting story "+instanceDao.toString());

                            StorymakerIndexManager.instanceIndexRemoveFromDB(context, (InstanceIndexItem) item, instanceDao);

                            mDataset.remove(safePosition);
                            notifyItemRemoved(safePosition);

                            sendDeleteCompleteMessage(((InstanceIndexItem) item).getInstanceFilePath());

                        }
                    })
                    .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();
            } else if (item instanceof ExpansionIndexItem) {

                // let users delete content packs too
                
                //Only allow content pack deletion from Catalog Activity
                if (context instanceof CatalogActivity) {

                    // check if item is installed? -- only show delete dialog if it's possible to delete
                    if (CheckIfInstalled(item)) {

                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.delete_content_pack))
                                .setMessage(item.getTitle())
                                .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // check installed index
                                        installedDao.getInstalledIndexItemByKey(((ExpansionIndexItem) item).getExpansionId()).take(1).subscribe(new Action1<List<InstalledIndexItem>>() {

                                            @Override
                                            public void call(List<InstalledIndexItem> expansionIndexItems) {

                                                // only one item expected

                                                if (expansionIndexItems.size() != 1) {
                                                    Timber.e("LONG PRESS: UNEXPECTED NUMBER OF RECORDS FOUND FOR " + ((ExpansionIndexItem) item).getExpansionId() + "(" + expansionIndexItems.size() + ")");
                                                    return;

                                                }

                                                InstalledIndexItem installedItem = expansionIndexItems.get(0);

                                                File fileDirectory = StorageHelper.getActualStorageDirectory(context);
                                                WildcardFileFilter fileFilter = new WildcardFileFilter(installedItem.getExpansionId() + ".*");
                                                for (File foundFile : FileUtils.listFiles(fileDirectory, fileFilter, null)) {
                                                    Timber.d("LONG PRESS: FOUND " + foundFile.getPath() + ", DELETING");
                                                    FileUtils.deleteQuietly(foundFile);
                                                }

                                                // remove from installed index
                                                Timber.d("LONG PRESS: REMOVING " + installedItem.expansionId + ", FROM DB");
                                                StorymakerIndexManager.installedIndexRemove(context, installedItem, installedDao);

                                                // need to clear saved threads
                                                sendDeleteCompleteMessage(installedItem.getExpansionId());

                                                //if (context instanceof HomeActivity) {
                                                //    Timber.d("LONG PRESS: REMOVING THREADS FOR " + installedItem.expansionId);
                                                //HomeActivity home = (HomeActivity) context;
                                                //home.removeThreads(installedItem.expansionId);
                                                //} else {
                                                //    Timber.e("LONG PRESS: UNEXPECTED CONTEXT");
                                                //}
                                            }
                                        });


                                    }

                                })
                                .setNegativeButton(context.getString(R.string.cancel), null)
                                .show();
                    }
                }
            } else {
                // no-op
            }

            return true;
        }
    }
}
