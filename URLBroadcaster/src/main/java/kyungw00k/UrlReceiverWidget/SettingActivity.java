package kyungw00k.UrlReceiverWidget;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import net.daum.adam.publisher.AdInterstitial;
import net.daum.adam.publisher.AdView;

import java.util.Map;

public class SettingActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final int PICK_ACCOUNT_REQUEST = 1;

    private AdView adView;
    private AdInterstitial adInterstitial;

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
            // 계정 설정
            initializeAccount();
        }

        // 광고 설정
        initializeAd();
    }

    public void initializePreference() {
        Map<String, ?> data = getPreferenceManager().getSharedPreferences()
                .getAll();

        for ( String key : data.keySet() ) {
            onSharedPreferenceChanged(getPreferenceManager()
                    .getSharedPreferences(), key);
        }

        Preference accountPreference = (Preference)findPreference(Consts.PREF_ACCOUNT);
        accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){

            @Override
            public boolean onPreferenceClick(Preference preference) {
                initializeAccount();
                return false;
            }
        });


        Preference githubLink = (Preference)findPreference("github");
        githubLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Link to GitHub Pages
                return false;
            }
        });

        Preference mailMeLink = (Preference)findPreference("mailMe");
        mailMeLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Link to GitHub Pages
                return false;
            }
        });
    }

    private void initializeAd() {
        // Ad@m sdk 초기화 시작
        adView = (AdView) findViewById(R.id.adview);
        adView.setClientId("5571Z2FT1410076d76b");
        adView.setAnimationType(AdView.AnimationType.FADE);
        adView.setRequestInterval(12);
        adView.setAdUnitSize("320x50");
        adView.setVisibility(View.VISIBLE);


        adInterstitial = new AdInterstitial(this);
        adInterstitial.setClientId("559aZ2YT1410a6e4ec4");
        adInterstitial.loadAd();

    }

    private void initializeAccount() {
        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null,
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
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Consts.PREF_ACCOUNT, accountName);

            if ( !editor.commit() ) {
                Log.e("", "Couldn't save account name!");
            } else {
                Util.toastMessage(this, "Now, ready to use!", Toast.LENGTH_SHORT);
            }
        }
        if ( adView != null ) {
            adView.destroy();
        }
        finish();
    }

    @Override
    protected void onDestroy() {

        if ( adInterstitial != null ) {
            adInterstitial = null;
        }

        if ( adView != null ) {
            adView.destroy();
        }

        super.onDestroy();
    }
}
