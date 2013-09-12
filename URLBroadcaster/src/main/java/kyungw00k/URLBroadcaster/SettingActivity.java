package kyungw00k.URLBroadcaster;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.Map;

public class SettingActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final int PICK_ACCOUNT_REQUEST = 1;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initializePreference();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Preference pref = findPreference(key);

        if ( pref != null ) {
            Object value = sp.getAll().get(key);

            if ( pref instanceof ListPreference ) {
                ListPreference list_pref = (ListPreference) pref;
                if ( value instanceof String ) {
                    int index = list_pref.findIndexOfValue((String) value);

                    if ( index >= 0 ) {
                        pref.setSummary(list_pref.getEntries()[index]);
                    }
                } else {
                    pref.setSummary("" + value);
                }
            } else {
                if ( value instanceof String ) {
                    pref.setSummary("" + value);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this); // Add this method.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes

        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);
        addPreferencesFromResource(R.xml.setting);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.PREF_ACCOUNT, Activity.MODE_PRIVATE);

        if ( pref.getString(Consts.PREF_ACCOUNT, null) == null ) {
            initializeAccount();
        }
    }

    public void initializePreference() {
        Map<String, ?> data = getPreferenceManager().getSharedPreferences()
                .getAll();

        for ( String key : data.keySet() ) {
            onSharedPreferenceChanged(getPreferenceManager()
                    .getSharedPreferences(), key);
        }

        Preference accountPreference = (Preference) findPreference(Consts.PREF_ACCOUNT);
        accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                initializeAccount();
                return false;
            }
        });

        Preference siteLink = (Preference) findPreference("site");
        siteLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUri("http://url-broadcaster.appspot.com/");
                return false;
            }
        });


        Preference githubLink = (Preference) findPreference("github");
        githubLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUri("https://github.com/kyungw00k/URLBroadcasterProject");
                return false;
            }
        });

        Preference mailMeLink = (Preference) findPreference("mailMe");
        mailMeLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUri("mailto:parksama@gmail.com");
                return false;
            }
        });
    }

    private void openUri(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void initializeAccount() {
        SharedPreferences pref = getSharedPreferences(Consts.PREF_ACCOUNT, Activity.MODE_PRIVATE);
        String selectedAccountName = pref.getString(Consts.PREF_ACCOUNT, null);

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        Account selectedAccount = null;
        for ( Account account : list ) {
            if ( account.type.equalsIgnoreCase("com.google") && account.name.equals(selectedAccountName) ) {
                selectedAccount = account;
            }
        }
        Intent googlePicker = AccountPicker.newChooseAccountIntent(selectedAccount, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
        googlePicker.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(googlePicker, PICK_ACCOUNT_REQUEST);
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {

        if ( requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK ) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if ( accountName == null ) {
                Util.toastMessage(this, "There's no account.", Toast.LENGTH_SHORT);
                finish();
            }

            SharedPreferences pref = getSharedPreferences(Consts.PREF_ACCOUNT, Activity.MODE_PRIVATE);

            String previousAccount = pref.getString(Consts.PREF_ACCOUNT, null);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Consts.PREF_ACCOUNT, accountName);
            editor.commit();

            if ( previousAccount != null ) {
                if ( previousAccount.equals(accountName) == false ) {
                    GCMIntentService.unregister(getApplicationContext());
                    Util.toastMessage(this, "Account has been changed! Turn on the switch!", Toast.LENGTH_SHORT);
                    finish();
                }
            } else {
                Util.toastMessage(this, "Now, ready to use!", Toast.LENGTH_SHORT);
            }
        }
    }
}
