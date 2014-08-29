package info.guardianproject.mrapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PressSecureActivity extends Activity {
    public static final int PRESSSECURE_REQUEST_CODE = 1022783278;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_press_secure);
		Button button = (Button) findViewById(R.id.buttonSubmit);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_OK, getIntent());
				finish();
				// FIXME stuff presssecure stuff into the extras
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.press_secure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
