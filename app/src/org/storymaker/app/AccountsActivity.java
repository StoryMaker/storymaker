
package org.storymaker.app;

import timber.log.Timber;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import io.scal.secureshareui.controller.SiteController.OnEventListener;
import io.scal.secureshareui.lib.ChooseAccountFragment;
import io.scal.secureshareui.model.Account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class AccountsActivity extends BaseActivity {

	private ChooseAccountFragment caFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getBooleanExtra("isDialog", false)) {
            setTheme(android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
		}
        setContentView(R.layout.activity_accounts);

        boolean isUserLoggedIntoSM = false;
        Auth storymakerAuth = (new AuthTable()).getAuthDefault(getApplicationContext(), Auth.SITE_STORYMAKER);
        if (storymakerAuth != null) { // FIXME we should check a little more carefully if the auth credentials are valid
        	isUserLoggedIntoSM = true;
        }
		
		Bundle bundle = null;
		Intent intent = getIntent();
		bundle = new Bundle();
		bundle.putBoolean("isDialog", intent.getBooleanExtra("isDialog", false));
		bundle.putBoolean("inSelectionMode", intent.getBooleanExtra("inSelectionMode", false));
		bundle.putBoolean("isUserLoggedIntoSM", isUserLoggedIntoSM);
		
		addChooseAccountFragment(bundle);
	}

	public void addChooseAccountFragment(Bundle bundle) {
	    FragmentManager fragManager = getSupportFragmentManager();
	    FragmentTransaction fragTrans = fragManager.beginTransaction();

	    List<Account> accounts = new ArrayList<>();
	    final AuthTable authTable = new AuthTable();
	    String[] siteAvailableNames = Account.CONTROLLER_SITE_NAMES;
	    String[] siteAvailableKeys = Account.CONTROLLER_SITE_KEYS;
	    Auth auth;
		
		for(int i = 0; i < siteAvailableKeys.length; i++) {
			auth = authTable.getAuthDefault(this, siteAvailableKeys[i]);
			
			if(auth == null) {
				accounts.add(new Account(-1, siteAvailableNames[i], siteAvailableKeys[i], "", "", null, false, false));
			}
			else {
				accounts.add(auth.convertToAccountObject());
			}
		}
		
		caFragment = new ChooseAccountFragment(); 
		caFragment.setArguments(bundle);
		caFragment.setLoginIntent(new Intent(this, ConnectAccountActivity.class));
		caFragment.setAccountsList(accounts);  // FIXME we should probably make Account object parcelable and pass this through the bundle
		caFragment.setOnEventListener(new OnEventListener() {

			@Override
			public void onSuccess(Account account) {
				Auth auth = authTable.getAuthDefault(getApplicationContext(), account.getSite());
				
				//if auth doesn't exist in db
				if(auth == null) {
					auth = new Auth(getApplicationContext(), -1, account.getName(), account.getSite(), null, null, null, null, null);
					auth.insert();
				}

				//set id of account based on returned id of auth insert
				account.setId(auth.getId());
				
				auth.setCredentials(account.getCredentials());
				auth.setData(account.getData());
				auth.setUserName(account.getUserName());
				auth.setExpires(null);
				authTable.updateLastLogin(getApplicationContext(), account.getSite(), auth.getUserName());	
				auth.update();
			}

			@Override
			public void onFailure(Account account, String failureMessage) {
				Auth auth = authTable.getAuthDefault(getApplicationContext(), account.getSite());

				if(auth != null) {
					//TODO set variables here
					auth.setCredentials(account.getCredentials());
					auth.setUserName(account.getName());
					auth.setData(account.getData());
					auth.setExpires(new Date()); // FIXME this is a hack to get isValid to fail, probably should be a setFailed() in auth that marks that we are busted
					auth.update();
				}
			}

            @Override
            public void onRemove(Account account) {
                authTable.delete(getApplicationContext(), account.getId());
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
