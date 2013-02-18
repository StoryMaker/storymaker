package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.server.SoundCloudUploader;
import info.guardianproject.mrapp.server.YouTubeSubmit;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.ToggleButton;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.animoto.android.views.DraggableGridView;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class PublishFragment extends Fragment {
    private final static String TAG = "PublishFragment";
    private final static int REQ_SOUNDCLOUD = 999;
    
    int layout;
    public ViewPager mAddClipsViewPager;
    View mView = null;
    private EditorBaseActivity mActivity;
    private Handler mHandlerPub;
    private String mMediaUploadAccount = null;

    /**
     * The sortable grid view that contains the clips to reorder on the
     * Order tab
     */
    protected DraggableGridView mOrderClipsDGV;

    public PublishFragment(int layout, EditorBaseActivity activity)
            throws IOException, JSONException {
        this.layout = layout;
        mActivity = activity;
        mHandlerPub = ((SceneEditorActivity)mActivity).mHandlerPub;
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(layout, null);
        if (this.layout == R.layout.fragment_story_publish) {
            EditText etTitle = (EditText) view.findViewById(R.id.etStoryTitle);
            EditText etDesc = (EditText) view.findViewById(R.id.editTextDescribe);

            etTitle.setText(mActivity.mMPM.mProject.getTitle());

            ToggleButton tbYouTube = (ToggleButton) view.findViewById(R.id.toggleButtonYoutube);

            tbYouTube.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {

                    if (isChecked) {
                        checkYouTubeAccount();
                    }

                }

            });

            ToggleButton tbStoryMaker = (ToggleButton) view
                    .findViewById(R.id.toggleButtonStoryMaker);

            tbStoryMaker.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {

                    if (isChecked)
                    {
                        ServerManager sm = StoryMakerApp.getServerManager();
                        sm.setContext(mActivity.getBaseContext());

                        if (!sm.hasCreds())
                            showLogin();
                    }

                }

            });
            
            Button btnRender = (Button) view.findViewById(R.id.btnRender);
            btnRender.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0) {
                    
                    handlePublish(false, false);
                }
                
            });
            
            Button btn = (Button) view.findViewById(R.id.btnPublish);
            btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    
                    doPublish(); 

                }
            });
        }
        return view;
    }
    
    public void doPublish() {
        ServerManager sm = StoryMakerApp.getServerManager();
        sm.setContext(mActivity.getBaseContext());

        ToggleButton tbYouTube = (ToggleButton) mActivity.findViewById(R.id.toggleButtonYoutube);
        ToggleButton tbStoryMaker = (ToggleButton) mActivity
                .findViewById(R.id.toggleButtonStoryMaker);
        final boolean doYouTube = tbYouTube.isChecked();
        final boolean doStoryMaker = tbStoryMaker.isChecked();

        if (!sm.hasCreds() && doStoryMaker)
            showLogin();
        else
            handlePublish(doYouTube, doStoryMaker);
    }

    private void showLogin() {
        startActivity(new Intent(mActivity, LoginActivity.class));
    }

    private void checkYouTubeAccount() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
        mMediaUploadAccount = settings.getString("youTubeUserName", null);

        if (mMediaUploadAccount == null) {
            AccountManager accountManager = AccountManager.get(mActivity);
            final Account[] accounts = accountManager.getAccounts();

            if (accounts.length > 0) {
                String[] accountNames = new String[accounts.length];
                for (int i = 0; i < accounts.length; i++) {
                    accountNames[i] = accounts[i].name + " (" + accounts[i].type + ")";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.choose_account_for_youtube_upload);
                builder.setItems(accountNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mMediaUploadAccount = accounts[item].name;
                        // SharedPreferences settings =
                        // PreferenceManager.getDefaultSharedPreferences(SceneEditorNoSwipeActivity.this);
                        // settings.edit().putString("youTubeUserName",
                        // mYouTubeUsername);
                        // settings.edit().commit();
                    }
                }).show();
            }
        }
    }

    private String processTitle(String title) {
        String result = title;
        result = result.replace(' ', '_');
        result = result.replace('!', '_');
        result = result.replace('/', '_');
        result = result.replace('!', '_');
        result = result.replace('#', '_');
        result = result.replace('"', '_');
        result = result.replace('\'', '_');
        return result;
    }

    private void handlePublish(final boolean doYouTube, final boolean doStoryMaker) {
        
        EditText etTitle = (EditText) mActivity.findViewById(R.id.etStoryTitle);
        EditText etDesc = (EditText) mActivity.findViewById(R.id.editTextDescribe);

        // final String exportFileName =
        // processTitle(mMPM.mProject.getTitle()) + "-export-" + new
        // Date().getTime();
        final String exportFileName = mActivity.mMPM.mProject.getId() + "-export-" + new Date().getTime();


        mHandlerPub.sendEmptyMessage(999);

        final String title = etTitle.getText().toString();
        final String desc = etDesc.getText().toString();
        String ytdesc = desc;
        if (ytdesc.length() == 0) {
            ytdesc = getActivity().getString(R.string.default_youtube_desc); // can't
                                                                             // leave
                                                                             // the
                                                                             // description
                                                                             // blank
                                                                             // for
                                                                             // YouTube
        }

        final YouTubeSubmit yts = new YouTubeSubmit(null, title, ytdesc, new Date(),
                mActivity, mHandlerPub, mActivity.getBaseContext());

        Thread thread = new Thread() {
            public void run() {
                ServerManager sm = StoryMakerApp.getServerManager();
                sm.setContext(mActivity.getBaseContext());

                Message msg = mHandlerPub.obtainMessage(888);
                msg.getData().putString("status",
                        getActivity().getString(R.string.rendering_clips_));
                mHandlerPub.sendMessage(msg);

                try {
                    
                    //if (mActivity.mdExported == null)
                       
                	mActivity.mMPM.doExportMedia(exportFileName, doYouTube);
                    
                    mActivity.mdExported = mActivity.mMPM.getExportMedia();
                    File mediaFile = new File(mActivity.mdExported.path);

                    if (mediaFile.exists()) {

                        Message message = mHandlerPub.obtainMessage(777);
                        message.getData().putString("fileMedia", mActivity.mdExported.path);
                        message.getData().putString("mime", mActivity.mdExported.mimeType);

                        if (doYouTube) {

                            String mediaEmbed = "";

                            if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
                                    || mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                                    
                                    ) {
                                msg = mHandlerPub.obtainMessage(888);
                                msg.getData().putString("statusTitle",
                                        getActivity().getString(R.string.uploading));
                                msg.getData().putString("status", getActivity().getString(
                                        R.string.connecting_to_youtube_));
                                mHandlerPub.sendMessage(msg);

                                yts.setVideoFile(mediaFile, mActivity.mdExported.mimeType);
                                yts.getAuthTokenWithPermission(mMediaUploadAccount);
                                // yts.upload(mYouTubeUsername,new
                                // File(mActivity.mdExported.path));

                                while (yts.videoId == null) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                    }
                                }

                                mediaEmbed = "[youtube]" + yts.videoId + "[/youtube]";

                                message.getData().putString("youtubeid", yts.videoId);
                            }
                            else if (mActivity.mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
                                boolean installed = SoundCloudUploader
                                        .isCompatibleSoundCloudInstalled(mActivity.getBaseContext());

                                if (installed) {
                                    String scurl = SoundCloudUploader.buildSoundCloudURL(
                                            mMediaUploadAccount, mediaFile, title);
                                    mediaEmbed = "[soundcloud]" + scurl + "[/soundcloud]";

                                    SoundCloudUploader.uploadSound(mediaFile, title, desc,
                                            REQ_SOUNDCLOUD, mActivity);

                                }
                                else {
                                    SoundCloudUploader.installSoundCloud(mActivity.getBaseContext());
                                }
                            }
                            else if (sm.hasCreds())
                            {
                                String murl = sm.addMedia(mActivity.mdExported.mimeType, mediaFile);
                                mediaEmbed = murl;
                            }

                            if (doStoryMaker) {
                                String descWithMedia = desc + "\n\n" + mediaEmbed;
                                String postId = sm.post(title, descWithMedia);
                                String urlPost = sm.getPostUrl(postId);
                                message.getData().putString("urlPost", urlPost);
                            }
                        }
                        mHandlerPub.sendMessage(message);
                    }
                    else {
                        Message msgErr = new Message();
                        msgErr.what = -1;
                        msgErr.getData().putString("err", "Media export failed");
                        mHandlerPub.sendMessage(msgErr);
                    }
                } catch (XmlRpcFault e) {
                    Message msgErr = new Message();
                    msgErr.what = -1;
                    msgErr.getData().putString("err", e.getLocalizedMessage());
                    mHandlerPub.sendMessage(msgErr);
                    Log.e(AppConstants.TAG, "error posting", e);
                }
                catch (Exception e) {
                    Message msgErr = new Message();
                    msgErr.what = -1;
                    msgErr.getData().putString("err", e.getLocalizedMessage());
                    mHandlerPub.sendMessage(msgErr);
                    Log.e(AppConstants.TAG, "error posting", e);
                }
            }
        };

        thread.start();
    }
}