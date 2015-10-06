package org.storymaker.app;

import org.storymaker.app.server.ServerManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.FeedbackManager;

//import com.google.analytics.tracking.android.EasyTracker;

// NEW/CACHEWORD
import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

public class BaseActivity extends FragmentActivity implements ICacheWordSubscriber {

    // NEW/CACHEWORD
    protected CacheWordHandler mCacheWordHandler;
    protected String CACHEWORD_UNSET;
    protected String CACHEWORD_FIRST_LOCK;
    protected String CACHEWORD_SET;

    protected ActionBarDrawerToggle mDrawerToggle;
    protected DrawerLayout mDrawerLayout;
    protected ViewGroup mDrawerContainer;
    protected boolean mDrawerOpen;

	@Override
	public void onStart() {
		super.onStart();
//		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
//		EasyTracker.getInstance(this).activityStop(this);
	}

    // NEW/CACHEWORD
    @Override
    protected void onPause() {
        super.onPause();
        mCacheWordHandler.disconnectFromService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // only display notification if the user has set a pin
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            Log.d("CACHEWORD", "pin set, so display notification (base)");
            mCacheWordHandler.setNotification(buildNotification(this));
        } else {
            Log.d("CACHEWORD", "no pin set, so no notification (base)");
        }

        mCacheWordHandler.connectToService();
        updateSlidingMenuWithUserState();
    }

    private Notification buildNotification(Context c) {

        Log.d("CACHEWORD", "buildNotification (base)");

        NotificationCompat.Builder b = new NotificationCompat.Builder(c);
        b.setSmallIcon(R.drawable.ic_menu_key);
        b.setContentTitle(c.getText(R.string.cacheword_notification_cached_title));
        b.setContentText(c.getText(R.string.cacheword_notification_cached_message));
        b.setTicker(c.getText(R.string.cacheword_notification_cached));
        b.setWhen(System.currentTimeMillis());
        b.setOngoing(true);
        b.setContentIntent(CacheWordHandler.getPasswordLockPendingIntent(c));
        return b.build();
    }

    @Override
    public void onCacheWordUninitialized() {

        // if we're uninitialized, default behavior should be to stop
        Log.d("CACHEWORD", "cacheword uninitialized, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordLocked() {

        // if we're locked, default behavior should be to stop
        Log.d("CACHEWORD", "cacheword locked, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordOpened() {

        // if we're opened, check db and update menu status
        Log.d("CACHEWORD", "cacheword opened, activity will continue");
        updateSlidingMenuWithUserState();

    }

    public void setupDrawerLayout() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerContainer = (ViewGroup) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer_white, R.string.open_drawer, R.string.close_drawer) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mDrawerOpen = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mDrawerOpen = true;
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        final Activity activity = this;
        
        RelativeLayout llDrawerLogin = (RelativeLayout) findViewById(R.id.llLogin);
        
        ImageButton btnDrawerQuickCaptureVideo = (ImageButton) findViewById(R.id.btnDrawerQuickCaptureVideo);
        ImageButton btnDrawerQuickCapturePhoto = (ImageButton) findViewById(R.id.btnDrawerQuickCapturePhoto);
        ImageButton btnDrawerQuickCaptureAudio = (ImageButton) findViewById(R.id.btnDrawerQuickCaptureAudio);
        
        Button btnDrawerHome =          (Button) findViewById(R.id.btnDrawerHome);
//        Button btnDrawerProjects =      (Button) findViewById(R.id.btnDrawerProjects);
        //Button btnDrawerAccount = (Button) findViewById(R.id.btnDrawerAccount);
        Button btnDrawerAccounts =      (Button) findViewById(R.id.btnDrawerAccounts);
        Button btnDrawerExports =      (Button) findViewById(R.id.btnDrawerExports);
        Button btnDrawerUploadManager = (Button) findViewById(R.id.btnDrawerUploadManager);
        Button btnDrawerSettings =      (Button) findViewById(R.id.btnDrawerSettings);
        Button btnDrawerFeedback =      (Button) findViewById(R.id.btnDrawerFeedback);
        TextView textViewVersion =      (TextView) findViewById(R.id.textViewVersion);

        // NEW/CACHEWORD
        Button btnDrawerLock = (Button) findViewById(R.id.btnDrawerLock);

        // disable button if the user has set a pin
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            Log.d("CACHEWORD", "pin set, so remove button");
            btnDrawerLock.setVisibility(View.GONE);
        } else {
            Log.d("CACHEWORD", "no pin set, so show button");
        }

        String pkg = getPackageName();
        try {
            String versionName = getPackageManager().getPackageInfo(pkg, 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(pkg, 0).versionCode;
            textViewVersion.setText("v" + versionName + " build " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        updateSlidingMenuWithUserState();
        
        // Set a random profile background
        ImageView imageViewProfileBg = (ImageView) findViewById(R.id.imageViewProfileBg);
        int profileBg = (int) (Math.random() * 2);
        switch (profileBg) {
            case 0:
                imageViewProfileBg.setImageResource(R.drawable.profile_bg1);
                break;
            case 1:
                imageViewProfileBg.setImageResource(R.drawable.profile_bg2);
                break;
            case 2:
                imageViewProfileBg.setImageResource(R.drawable.profile_bg3);
                break;
        }

        llDrawerLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawers();
                
	        	Intent i = new Intent(activity, ConnectAccountActivity.class);
	            activity.startActivity(i);
            }
        });
        
        btnDrawerHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawers();
                
            	 Intent i = new Intent(activity, HomeActivity.class);
                 activity.startActivity(i);
            }
        });
        btnDrawerExports.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawers();
                Intent i = new Intent(activity, ProjectsActivity.class);
                activity.startActivity(i);
            }
        });

        btnDrawerAccounts.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();

                Intent i = new Intent(activity, AccountsActivity.class);
                activity.startActivity(i);
            }
        });
        
        btnDrawerUploadManager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_LONG).show();
//                Intent i = new Intent(activity, AccountsActivity.class);
//                activity.startActivity(i);
            }
        });
        
        btnDrawerSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();

                Intent i = new Intent(activity, SimplePreferences.class);
                activity.startActivity(i);
            }
        });

        btnDrawerFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();

                FeedbackManager.register(activity, AppConstants.HOCKEY_APP_ID, null);
                FeedbackManager.showFeedbackActivity(activity);
            }
        });

        // NEW/CACHEWORD
        btnDrawerLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // if there has been no first lock, set status so user will be prompted to create a pin
                SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
                String cachewordStatus = sp.getString("cacheword_status", "default");
                if (cachewordStatus.equals(CACHEWORD_UNSET)) {
                    SharedPreferences.Editor e = sp.edit();
                    e.putString("cacheword_status", CACHEWORD_FIRST_LOCK);
                    e.commit();
                    Log.d("CACHEWORD", "set cacheword first lock status");
                }
                mCacheWordHandler.lock();
            }
        });
    }
    
    /**
     * Alter the Profile badge of the SlidingMenu with the current user state
     * 
     * e.g: Show username if logged in, prompt to sign up or sign in if not.
     */
    private void updateSlidingMenuWithUserState() {
        ServerManager serverManager = StoryMakerApp.getServerManager();
        TextView textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);
        TextView textViewJoinStorymaker = (TextView) findViewById(R.id.textViewJoinStorymaker);

        if (mCacheWordHandler.isLocked()) {

            // prevent credential check attempt if database is locked
            Log.d("CACHEWORD", "cacheword locked, skipping menu credential check");
            textViewSignIn.setText(R.string.sign_in);
            textViewJoinStorymaker.setVisibility(View.VISIBLE);

        } else if (serverManager.hasCreds()) {
            // The Storymaker user is logged in. Replace Sign/Up language with username
            textViewSignIn.setText(serverManager.getUserName());
            textViewJoinStorymaker.setVisibility(View.GONE);
        } else {
            textViewSignIn.setText(R.string.sign_in);
            textViewJoinStorymaker.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        // NEW/CACHEWORD
        CACHEWORD_UNSET = getText(R.string.cacheword_state_unset).toString();
        CACHEWORD_FIRST_LOCK = getText(R.string.cacheword_state_first_lock).toString();
        CACHEWORD_SET = getText(R.string.cacheword_state_set).toString();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int timeout = Integer.parseInt(settings.getString("pcachewordtimeout", "600"));
        mCacheWordHandler = new CacheWordHandler(this, timeout); // TODO: timeout of -1 represents no timeout (revisit)

        if(!Eula.isAccepted(this)) {
            Intent firstStartIntent = new Intent(this, FirstStartActivity.class);
            firstStartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(firstStartIntent);
        }

        setContentView(R.layout.activity_base);

        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setContentView(int resId) {
        if (!getClass().getSimpleName().equals("BaseActivity")) {

            super.setContentView(R.layout.activity_base);
            if (resId != R.layout.activity_base) {
                setContentViewWithinDrawerLayout(resId);
                setupDrawerLayout();
            }

        } else {
            super.setContentView(resId);
        }
    }

    /**
     * Inflate the given layout into this Activity's DrawerLayout
     *
     * @param resId the layout resource to inflate
     */
    public void setContentViewWithinDrawerLayout(int resId) {
        ViewGroup content = (ViewGroup) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(resId, content, true);
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        updateSlidingMenuWithUserState();
    }
    */

	private void detectCoachOverlay ()
    {
        try {
        	
        	if (this.getClass().getName().contains("SceneEditorActivity"))
        	{
        		showCoachOverlay("images/coach/coach_add.png");
        	}
        	else if (this.getClass().getName().contains("OverlayCameraActivity"))
        	{
        		showCoachOverlay("images/coach/coach_camera_prep.png");
        	}
        		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 
	public void switchContent(final Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}	
**/
    
    private void showCoachOverlay (String path) throws IOException
    {
    	ImageView overlayView = new ImageView(this);
    	
    	overlayView.setOnClickListener(new OnClickListener () 
    	{

			@Override
			public void onClick(View v) {
				getWindowManager().removeView(v);
				
			}
    		
    	});
    	
    	AssetManager mngr = getAssets();
        // Create an input stream to read from the asset folder
           InputStream ins = mngr.open(path);

           // Convert the input stream into a bitmap
           Bitmap bmpCoach = BitmapFactory.decodeStream(ins);
           overlayView.setImageBitmap(bmpCoach);
           
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    	        WindowManager.LayoutParams.MATCH_PARENT,
    	        WindowManager.LayoutParams.MATCH_PARENT,
    	        WindowManager.LayoutParams.TYPE_APPLICATION,
    	        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
    	        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
    	        PixelFormat.TRANSLUCENT);

    	getWindowManager().addView(overlayView, params);
    }

    public void toggleDrawer() {
        if (mDrawerLayout == null) return;

        if (mDrawerOpen) mDrawerLayout.closeDrawer(mDrawerContainer);
        else mDrawerLayout.openDrawer(mDrawerContainer);

    }
}
