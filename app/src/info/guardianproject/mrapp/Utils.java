package info.guardianproject.mrapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Toast;

import android.support.v4.app.FragmentActivity;

public class Utils {
    public static void toastOnUiThread(Activity activity, String message) {
        final Activity _activity = activity;
        final String _msg = message;
        activity.runOnUiThread(new Runnable() {
                public void run() {
                        Toast.makeText(_activity.getApplicationContext(), _msg, Toast.LENGTH_SHORT).show();
                }
        });
    }
    
    public static void toastOnUiThread(FragmentActivity fragmentActivity, String message) {
        final FragmentActivity _activity = fragmentActivity;
        final String _msg = message;
        fragmentActivity.runOnUiThread(new Runnable() {
                public void run() {
                        Toast.makeText(_activity.getApplicationContext(), _msg, Toast.LENGTH_SHORT).show();
                }
        });
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
}
