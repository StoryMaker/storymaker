package org.storymaker.app;

import timber.log.Timber;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import timber.log.Timber;

public class Utils {
    public static void toastOnUiThread(Activity activity, String message) {
    	toastOnUiThread(activity, message, false);
    }
    
    public static void toastOnUiThread(final Activity activity, String message, final boolean isLongToast) {
        final String _msg = message;
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(activity.getApplicationContext(), _msg, isLongToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
				}
			});
		}
    }
    
    public static void toastOnUiThread(FragmentActivity fragmentActivity, String message) {
    	toastOnUiThread(fragmentActivity, message, false);
    }
    
    public static void toastOnUiThread(FragmentActivity fragmentActivity, String message, final boolean isLongToast) {
        final FragmentActivity _activity = fragmentActivity;
        final String _msg = message;
        fragmentActivity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(_activity.getApplicationContext(), _msg, isLongToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
			}
		});
    }

	public static String getAppName(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String url = settings.getString("pserver", Constants.DEFAULT_SERVER_URL);
		String appName = context.getString(R.string.app_name);
		if(url.contains("beta")) {
			appName = "BETA MODE: " + appName;
		}
		return appName;
	}

    public static boolean assetExists(Context context, String path) {
        AssetManager mg = context.getResources().getAssets();

        try {
            mg.open(path);
            return true;
        } catch (IOException e) {
            Timber.d(e.getLocalizedMessage());
            return false;
        }
    }
    
    public static boolean isActivity(Context context) {
        return (context instanceof Activity);
    }
    
    /**
     * 
     * @param timeString in 00:00:00.000 format (hour:min:second.ms)
     * @return time in ms
     */
    public static int convertTimeStringToInt(String timeString) {
        int duration = 0;
        try {
            duration = (int) (new SimpleDateFormat("hh:mm:ss.SS").parse(timeString)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return duration;
    }
    
    /**
     * 
     * This converts a ActionbarSherlock MenuItem into a stock Android MenuItem so you can pass it into 
     * ActionBarDrawerToggle.onOptionsItemSelected method
     * 
     * @param item an ActionbarSherlock MenuItem
     * @return
     */
    public static android.view.MenuItem convertABSMenuItemToStock(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean expandActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public View getActionView() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getGroupId() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Drawable getIcon() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Intent getIntent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getNumericShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getOrder() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCheckable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChecked() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {
				// TODO Auto-generated method stub

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
    
    public static boolean stringNotBlank(String string) {
        return (string != null) && !string.equals("");
    }
    
    public static String stringArrayToCommaString(String[] strings) {
        if (strings.length > 0) {
            StringBuilder nameBuilder = new StringBuilder();

            for (String n : strings) {
                nameBuilder.append(n.replaceAll("'", "\\\\'")).append(",");
            }

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            return nameBuilder.toString();
        } else {
            return "";
        }
    }
    
    public static String[] commaStringToStringArray(String string) {
        if (string != null) {
            return string.split(",");
        } else {
            return null;
        }
    }

    public static class Proc {
        // various console cmds
        public final static String SHELL_CMD_CHMOD = "chmod";
        public final static String SHELL_CMD_KILL = "kill -9";
        public final static String SHELL_CMD_RM = "rm";
        public final static String SHELL_CMD_PS = "ps";
        public final static String SHELL_CMD_PIDOF = "pidof";

        public static void killZombieProcs(Context context) throws Exception {
            int killDelayMs = 300;
            int procId = -1;
            File fileCmd = new File(context.getDir("bin", Context.MODE_WORLD_READABLE), "ffmpeg");

            while ((procId = findProcessId(fileCmd.getAbsolutePath())) != -1) {
				Timber.w("Found Tor PID=" + procId + " - killing now...");

                String[] cmd = {
                    SHELL_CMD_KILL + ' ' + procId + ""
                };

                StringBuilder log = new StringBuilder();
                doShellCommand(cmd, log, false, false);
                try {
                    Thread.sleep(killDelayMs);
                } catch (Exception e) {
                }
            }
        }

        public static int findProcessId(String command) {
            int procId = -1;

            try {
                procId = findProcessIdWithPidOf(command);

                if (procId == -1) {
                    procId = findProcessIdWithPS(command);
                }
            } catch (Exception e) {
                try {
                    procId = findProcessIdWithPS(command);
                } catch (Exception e2) {
					Timber.w(e2, "Unable to get proc id for: " + command);
                }
            }

            return procId;
        }

        // use 'pidof' command
        public static int findProcessIdWithPidOf(String command) throws Exception {
            int procId = -1;
            Runtime r = Runtime.getRuntime();
            Process procPs = null;
            String baseName = new File(command).getName();
            // fix contributed my mikos on 2010.12.10
            procPs = r.exec(new String[] {
                    SHELL_CMD_PIDOF, baseName
            });
            // procPs = r.exec(SHELL_CMD_PIDOF);

            BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                try {
                    // this line should just be the process id
                    procId = Integer.parseInt(line.trim());
                    break;
                } catch (NumberFormatException e) {
                    Timber.e("unable to parse process pid: " + line, e);
                }
            }

            return procId;
        }

        // use 'ps' command
        public static int findProcessIdWithPS(String command) throws Exception {
            int procId = -1;
            Runtime r = Runtime.getRuntime();
            Process procPs = null;
            procPs = r.exec(SHELL_CMD_PS);

            BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (line.indexOf(' ' + command) != -1) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken(); // proc owner
                    procId = Integer.parseInt(st.nextToken().trim());
                    break;
                }
            }

            return procId;
        }

        public static int doShellCommand(String[] cmds, StringBuilder log, boolean runAsRoot, boolean waitFor) throws Exception {
            Process proc = null;
            int exitCode = -1;

            if (runAsRoot) {
                proc = Runtime.getRuntime().exec("su");
            } else {
                proc = Runtime.getRuntime().exec("sh");
            }

            OutputStreamWriter out = new OutputStreamWriter(proc.getOutputStream());

            for (int i = 0; i < cmds.length; i++) {
                out.write(cmds[i]);
                out.write("\n");
            }

            out.flush();
            out.write("exit\n");
            out.flush();

            if (waitFor) {
                final char buf[] = new char[10];

                // Consume the "stdout"
                InputStreamReader reader = new InputStreamReader(proc.getInputStream());
                int read = 0;
                while ((read = reader.read(buf)) != -1) {
                    if (log != null) {
                        log.append(buf, 0, read);
                    }
                }

                // Consume the "stderr"
                reader = new InputStreamReader(proc.getErrorStream());
                read = 0;
                while ((read = reader.read(buf)) != -1) {
                    if (log != null) {
                        log.append(buf, 0, read);
                    }
                }

                exitCode = proc.waitFor();
            }

            return exitCode;
        }
    }

    public static class Files {
        public static boolean isExternalStorageReady() {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Something else is wrong. It may be one of many other states,
                // but all we need to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }

			// adding logging to clarify errors
			if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
				if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					Timber.e("EXTERNAL STORAGE IS READ ONLY");
				}
				if (!mExternalStorageAvailable) {
					Timber.e("EXTERNAL STORAGE IS NOT AVAILABLE (STATE: " + state + ")");
				}
				if (!mExternalStorageWriteable) {
					Timber.e("EXTERNAL STORAGE IS NOT WRITEABLE (STATE: " + state + ")");
				}
			}

            return mExternalStorageAvailable && mExternalStorageWriteable;
        }
    }
    
    public static void sendLogcatToSupport(Activity activity) {
        String logcat = Utils.getLogCat();
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(activity);
        builder.setType("message/rfc822");
        builder.addEmailTo("support@storymaker.org");
        builder.setSubject(activity.getString(R.string.app_name));
        builder.setChooserTitle("logcat from StoryMaker");
        builder.startChooser();
    }

    public static String getLogCat() {
        String description = "";

        try {
            Process process = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append(System.getProperty("line.separator"));
            }
            bufferedReader.close();

            description = log.toString();
        } catch (IOException e) {
        }

        return description;
    }
}
