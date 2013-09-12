package kyungw00k.URLBroadcaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kyungwook on 13. 9. 5..
 */
public class Util {
    public static void toastMessage(final Context context, final String message, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, duration).show();
            }
        });
    }

    public static boolean checkUserAccount(final Context context) {

        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.PREF_ACCOUNT, Activity.MODE_PRIVATE);

        boolean hasAccount = pref.getString(Consts.PREF_ACCOUNT, null) != null;

        if ( !hasAccount ) {
            Log.e("", "There's no account. Choose one");

            Intent accountPickerIntent = new Intent(context, SettingActivity.class);
            accountPickerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(accountPickerIntent);
        }

        return hasAccount;
    }
}
