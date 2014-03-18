package fi.aalto.kutsuplus.testserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Copied from:
	// http://stackoverflow.com/questions/2856407/android-how-to-get-access-to-raw-resources-that-i-put-in-res-folder
	private String readTxt(int resource_id) {
		InputStream raw = getResources().openRawResource(resource_id);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try {
			i = raw.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = raw.read();
			}
			raw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArrayOutputStream.toString();

	}

	private String makeUPMessage() {
		Random randomGenerator = new Random(System.currentTimeMillis());
		int delay = randomGenerator.nextInt(5);
		try {
			Thread.sleep(delay * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int answer_case = randomGenerator.nextInt(12);
		String txt,txt2;
		switch (answer_case) {
		case 0:
			txt = readTxt(R.raw.broken_message);
			return txt.replace("{{unknown-parameter}}", "123");
		case 1:
			return readTxt(R.raw.destination_stop_missing);
		case 2:
			txt = readTxt(R.raw.order_expence);
			txt2=txt.replace("{{max-price}}", "123");
			return txt2.replace("{{trip-price}}", "123");
		case 3:
			txt = readTxt(R.raw.order_failure);
			txt2=txt.replace("{{departure-stop}}", "123");	
			return txt2.replace("{{arrival-stop}}", "456");
		case 4:
			txt = readTxt(R.raw.out_of_operating_hours);
			txt2=txt.replace("{{service-date}}", "12.3");
			return txt2.replace("{{service-time}}", "6:30");
		case 5:
			return readTxt(R.raw.syntax_error);
		case 6:
			txt = readTxt(R.raw.unknown_stop);
			return txt.replace("{{stop-id}}", "123");			 
		default:
			return readTxt(R.raw.normal);
		}

	}

	private void sendSMS(String receiver, String message) {

		PendingIntent sentPI;
		String SENT = "SMS_SENT";

		sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(receiver, "5556", message, sentPI, null);
	}

	private BroadcastReceiver mIntentReceiver;

	protected void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
		mIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String msg = intent.getStringExtra("get_msg");

				// Process the sms format and extract body &amp; phoneNumber
				msg = msg.replace("\n", "");
				String pNumber = msg.substring(0, msg.lastIndexOf(":"));
				String new_message = makeUPMessage();
				sendSMS(pNumber, new_message);
			}

		};
		this.registerReceiver(mIntentReceiver, intentFilter);
	}

	protected void onPause() {

		super.onPause();
		this.unregisterReceiver(this.mIntentReceiver);
	}

}
