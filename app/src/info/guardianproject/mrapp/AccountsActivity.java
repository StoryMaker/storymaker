
package info.guardianproject.mrapp;

//import io.scal.secureshareui.lib.ChooseAccountFragment;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import io.scal.secureshareui.controller.PublishController.OnPublishEventListener;
import io.scal.secureshareui.lib.ChooseAccountFragment;
import io.scal.secureshareui.model.PublishAccount;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class AccountsActivity extends BaseActivity {

	private ChooseAccountFragment caFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getBooleanExtra("isDialog", false)) {
            setTheme(android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
		}
		setContentView(R.layout.activity_accounts);
		
		Bundle bundle = null;
		Intent intent = getIntent();
		bundle = new Bundle();
		bundle.putBoolean("isDialog", intent.getBooleanExtra("isDialog", false));
		bundle.putBoolean("inSelectionMode", intent.getBooleanExtra("inSelectionMode", false));
		
		addChooseAccountFragment(bundle);
	}

	public void addChooseAccountFragment(Bundle bundle) {
	    FragmentManager fragManager = getSupportFragmentManager();
	    FragmentTransaction fragTrans = fragManager.beginTransaction();

	    List<PublishAccount> accounts = new ArrayList<PublishAccount>();
	    final AuthTable authTable = new AuthTable();
	    String[] siteAvailableNames = PublishAccount.CONTROLLER_SITE_NAMES;
	    String[] siteAvailableKeys = PublishAccount.CONTROLLER_SITE_KEYS;
	    Auth auth;
		
		for(int i = 0; i < siteAvailableKeys.length; i++) {
			auth = authTable.getAuthDefault(this, siteAvailableKeys[i]);
			
			if(auth == null) {
				accounts.add(new PublishAccount(null, siteAvailableNames[i], siteAvailableKeys[i], "", "", false, false));
			}
			else {
				accounts.add(auth.convertToPublishAccountObject());
			}
		}
		
		caFragment = new ChooseAccountFragment(); 
		caFragment.setArguments(bundle);
		caFragment.setPublishAccountsList(accounts);  // FIXME we should probably make AccountInfo parcelable and pass this through the bundle
		caFragment.setOnPublishEventListener(new OnPublishEventListener() {

			@Override
			public void onSuccess(PublishAccount publishAccount) {
				Auth auth = authTable.getAuthDefault(getApplicationContext(), publishAccount.getSite());
				
				//if auth doesn't exist in db
				if(auth == null) {
					auth = new Auth(getApplicationContext(), -1, publishAccount.getName(), publishAccount.getSite(), "", "", null, null);
					auth.insert();
				}

				auth.setCredentials(publishAccount.getCredentials());			
				auth.setUserName(publishAccount.getName());
				auth.setExpires(null);
				authTable.updateLastLogin(getApplicationContext(), publishAccount.getSite(), auth.getUserName());	
				auth.update();
			}

			@Override
			public void onFailure(PublishAccount publishAccount, String failureMessage) {
				Auth auth = authTable.getAuthDefault(getApplicationContext(), publishAccount.getSite());

				if(auth != null) {
					//TODO set variables here
					auth.setCredentials(publishAccount.getCredentials());
					auth.setUserName(publishAccount.getName());
					auth.setExpires(new Date()); // FIXME not sure hardcoding now() makes sense here for all sites?
					auth.update();
				}
			}
		});

		fragTrans.add(R.id.fragmentLayout, caFragment);
		fragTrans.commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		caFragment.onActivityResult(requestCode, resultCode, data);
	} 
}
