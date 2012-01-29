package kyungw00k.UrlReceiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;

/**
 * Switch Widget
 * 
 * @author kyungwook
 * 
 */
public class SwitchWidgetProvider extends AppWidgetProvider {
	@SuppressWarnings("unused")
	private static final String TAG = SwitchWidgetProvider.class.getSimpleName();

	static final String CLICK_ACTION = "kyungw00k.UrlReceiver.SwitchWidgetProvider.CLICK";
	static final String CHECKED = "checked";

	private boolean isChecked = false;
	private static final Intent SERVICE_INTENT = new Intent(ReceiverService.class.getName());

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(CLICK_ACTION) == true) {
			// button is clicked.
			SharedPreferences pref = context.getSharedPreferences(CHECKED, Activity.MODE_PRIVATE);

			// default value is true.
			isChecked = pref.getBoolean(CHECKED, true);
			Editor e = pref.edit();
			e.putBoolean(CHECKED, !isChecked);
			e.commit();
			isChecked = intent.getBooleanExtra(CHECKED, false);

			// toggle button.
			isChecked = !isChecked;

			// Update Widgets.
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			this.onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, SwitchWidgetProvider.class)));
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// For switch icon
			Intent intentClick = new Intent(CLICK_ACTION);
			intentClick.putExtra(CHECKED, isChecked);
			PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_UPDATE_CURRENT);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			
			if (isChecked == true) {
				context.startService(SERVICE_INTENT);
				views.setImageViewResource(R.id.onoff, R.drawable.on);
			} else {
				context.stopService(SERVICE_INTENT);
				views.setImageViewResource(R.id.onoff, R.drawable.off);
			}
			views.setOnClickPendingIntent(R.id.onoff, pendingIntentClick);
			
			// For configuration icon
			Intent intentSetting = new Intent(context, SettingActivity.class);
			intentSetting.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntentSetting = PendingIntent.getActivity(context, 0, intentSetting, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.config, pendingIntentSetting);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

}
