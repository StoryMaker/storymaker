package org.storymaker.app;

import org.storymaker.app.server.ServerManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
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

public class BaseActivity extends FragmentActivity {

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
        if (serverManager.hasCreds()) {
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
    
    @Override
    protected void onResume() {
        super.onResume();
        updateSlidingMenuWithUserState();
    }

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
