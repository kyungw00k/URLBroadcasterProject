package kyungw00k.UrlReceiverWidget;

import kyungw00k.UrlReceiverWidget.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Widget Configuration
 * 
 * @author kyungwook
 */
public class SettingActivity extends PreferenceActivity {
	@SuppressWarnings("unused")
	private static final String TAG = SettingActivity.class.getSimpleName();
	
	SharedPreferences defaultSharedPref = null;	
	String baseUri = null;
	String userName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		baseUri = defaultSharedPref.getString("serverAddress", getText(R.string.remote_server).toString());
		userName = defaultSharedPref.getString("userName", "guest");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	String newUri = defaultSharedPref.getString("serverAddress", getText(R.string.remote_server).toString());
	    	String newUser = defaultSharedPref.getString("userName", "guest");

	    	if ( !(newUri.equals(baseUri) && newUser.equals(userName)) ) {
	    		Intent SERVICE_INTENT = new Intent(ReceiverService.class.getName());
	    		
	    		if ( getBaseContext().stopService(SERVICE_INTENT) ) {
	    			Toast.makeText(this, "Service should be restarted", Toast.LENGTH_LONG).show();
	    		}
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
}