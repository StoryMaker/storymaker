
package info.guardianproject.mrapp;

//import io.scal.secureshareui.lib.ChooseAccountFragment;
import io.scal.secureshareui.lib.ChooseAccountFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class AccountsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        
        addChooseAccountFragment();
    }
    
    public void addChooseAccountFragment() {
        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
        
        
        Fragment caFragment = (Fragment) new ChooseAccountFragment();
        fragTrans.add(R.id.fragmentLayout, caFragment);
        fragTrans.commit();    
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.accounts, menu);
//        return true;
//    }

}
