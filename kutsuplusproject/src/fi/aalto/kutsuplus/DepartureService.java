package fi.aalto.kutsuplus;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class DepartureService extends IntentService {
	int mId = 1;

	/** 
	* A constructor is required, and must call the super IntentService(String)
	* constructor with a name for the worker thread.
	*/
	
	public DepartureService() {
		super("DepartureService");
	}
	
	/**
	* The IntentService calls this method from the default worker thread with
	* the intent that started the service. When this method returns, IntentService
	* stops the service, as appropriate.
	*/

	@Override
	protected void onHandleIntent(Intent intent) {
	
		NotificationCompat.Builder mBuilder = null;
		if (intent.getFlags() == 1) {		
			mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.kp_icon)
		        .setContentTitle("ÄlyBussi")
		        .setContentText(getString(R.string.departure))
		        .setAutoCancel(true);
		}
		else {
			mBuilder =
				new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.kp_icon)
		        .setContentTitle("ÄlyBussi")
		        .setContentText(intent.getStringExtra("fi.aalto.kutsuplus.walkTime")
		        		+ getString(R.string.departure_short))
				.setAutoCancel(true);
		}
		
		// Creates an explicit intent for an Activity
		Intent resultIntent = new Intent(this, MainActivity.class);
		
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// the application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = 
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
						);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());
		
		stopService(intent);
	}
}
