package fi.aalto.kutsuplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DepartureAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// Start the notification service
		Intent i = new Intent(context, DepartureService.class);
		i.addFlags(1);
        context.startService(i);
	}

}
