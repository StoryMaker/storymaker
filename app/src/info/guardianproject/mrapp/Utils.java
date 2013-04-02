package info.guardianproject.mrapp;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Toast;

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
}
