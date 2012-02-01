package kyungw00k.UrlReceiver;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * URL Receiver Service
 * 
 * @author kyungwook
 * 
 */
public class ReceiverService extends Service {
	private static final String TAG = ReceiverService.class.getSimpleName();
	private final Handler mHandler = new Handler(Looper.getMainLooper());

	private String mBaseUri = null;
	private String mUsername = null;

	private boolean mIsChecked = false;
	private boolean mIsError = false;

	private NotificationManager mNotificationManager;

	private final SocketIO socket = new SocketIO();

	private final IOCallback callback = new IOCallback() {

		@Override
		public void onError(SocketIOException socketIOException) {
			Log.d(TAG, "ConnectFailure");
			mIsError = true;
			switchTurnOff(mIsError);
		}

		@Override
		public void onDisconnect() {
			Log.d(TAG, "Disconnect");
			switchTurnOff(mIsError);
		}

		@Override
		public void onConnect() {
			Log.d(TAG, "Socket connection opened");

			try {
				Log.d(TAG, "Send device info to server");
				socket.emit("deviceOn", getDeviceInfo());
			} catch (IOException e) {
				e.printStackTrace();
				switchTurnOff(true);
				return;
			} catch (JSONException e) {
				e.printStackTrace();
				switchTurnOff(true);
				return;
			}
			showNotification(true);
		}

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			if (event.equals(mUsername)) {
				JSONObject jObj = (JSONObject) args[0];
				String url = null;
				String uuid = null;

				try {
					url = jObj.getString("url");
					uuid = jObj.getString("uuid");
					Log.d(TAG, "[" + uuid + "] " + url);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				if (uuid.equals(getDeviceUUID())) {
					displayMessage("Go to " + url);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					if (intent != null) {
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

						PendingIntent p = PendingIntent.getActivity(
								getApplicationContext(), 0, intent, 0);
						try {
							p.send();
						} catch (CanceledException e) {
							e.printStackTrace();

						}
					}
				}
			}
		}

		@Override
		public void onMessage(JSONObject json, IOAcknowledge ack) {
		}

		@Override
		public void onMessage(String data, IOAcknowledge ack) {
		}
	};

	/*
	 * 장치 정보를 반환한다.
	 * 
	 * @return JSONObject
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	private JSONObject getDeviceInfo() throws JSONException,
			UnsupportedEncodingException {
		JSONObject deviceInfo = new JSONObject();
		deviceInfo.put("userName", mUsername);
		deviceInfo.put("uuid", getDeviceUUID());
		deviceInfo.put("devid", URLEncoder.encode(Build.MODEL, "UTF8"));
		deviceInfo.put("osver",
				URLEncoder.encode(Build.VERSION.RELEASE, "UTF8"));
		return deviceInfo;
	}

	/*
	 * Display Toast Message
	 * 
	 * @param msg
	 */
	private void displayMessage(final String msg) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	/*
	 * Disconnecting connection
	 * 
	 * @param error
	 */
	private void switchTurnOff(boolean error) {
		SharedPreferences pref = getSharedPreferences(
				SwitchWidgetProvider.CHECKED, Activity.MODE_PRIVATE);
		mIsChecked = pref.getBoolean(SwitchWidgetProvider.CHECKED, true);

		Log.d(TAG, "SWITCH STATUS : " + mIsChecked);

		if (error) {
			displayMessage("Cannot connect to server");
		}

		// UI Update
		if (mIsChecked) {
			Intent intent = new Intent(SwitchWidgetProvider.CLICK_ACTION);
			intent.putExtra(SwitchWidgetProvider.CHECKED, mIsChecked);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Change Widget Status
			try {
				pendingIntent.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
		if (!error) {
			showNotification(false);
		}
	}

	/*
	 * Return Device UUID
	 * 
	 * @return Device UUID
	 */
	private String getDeviceUUID() {
		final TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

		// TODO UUID Encryption?
		return deviceUuid.toString();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "Service creating");

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Emulator Only
		if (Build.MODEL.indexOf("sdk") > -1) {
			System.setProperty("java.net.preferIPv6Addresses", "false");
		}

		SharedPreferences defaultSharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String serverUrl = getText(R.string.remote_server).toString();

		mBaseUri = defaultSharedPref.getString("serverAddress", serverUrl);
		mUsername = defaultSharedPref.getString("userName", "guest");

		if (mBaseUri.length() < 1) {
			defaultSharedPref.edit().putString("serverAddress", serverUrl).commit();
			mBaseUri = serverUrl;
		}

		if (mUsername.length() < 1) {
			defaultSharedPref.edit().putString("userName", "guest").commit();
			mUsername = "guest";
		}

		try {
			socket.connect(mBaseUri, callback);
		} catch (MalformedURLException e) {
			mIsError = true;
			e.printStackTrace();
		} catch (Exception e) {
			mIsError = true;
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		try {
			socket.emit("deviceOff", getDeviceInfo());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		socket.disconnect();

		// Cancel the persistent notification.
		mNotificationManager.cancel(R.string.remote_service_started);
		mNotificationManager = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void showNotification(boolean bOn) {
		showNotification(bOn, null);
	}

	private void showNotification(boolean bOn, Integer resId) {
		int textId = -1;
		int icon = -1;

		if (bOn) {
			textId = (R.string.remote_service_started);
			icon = R.drawable.on;
		} else {
			textId = (R.string.remote_service_stopped);
			icon = R.drawable.off;
		}

		if (resId != null) {
			textId = resId;
			icon = R.drawable.on;
		}

		Notification notification = new Notification(icon, getText(textId), System.currentTimeMillis());

		Intent intentSetting = new Intent(this, SettingActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intentSetting, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(this,
				getText(R.string.remote_service_label),
				getText(R.string.remote_service_configuration), contentIntent);

		mNotificationManager.notify(R.string.remote_service_started, notification);
	}
}
