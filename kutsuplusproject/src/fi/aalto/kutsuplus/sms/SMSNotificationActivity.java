package fi.aalto.kutsuplus.sms;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import fi.aalto.kutsuplus.R;

public class SMSNotificationActivity extends Activity {
	private BroadcastReceiver mIntentReceiver;
	TextView timerText;
	TextView sms_message;
	private ProgressBar progressBar;
	static Boolean timeOut = true;
	CountDownTimer counter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smsnotification);

		timerText = (TextView) findViewById(R.id.SW_TimeRemainigTv);
		progressBar = (ProgressBar) findViewById(R.id.SW_progressBar);
		progressBar.setVisibility(View.VISIBLE);
		sms_message= (TextView) findViewById(R.id.sms_message);
		// show 30 second time count down
		counter=new CountDownTimer(30000, 1000) {

			public void onTick(long millisUntilFinished) {
				timerText.setText(getString(R.string.sms_waiting)+" " + millisUntilFinished
						/ 1000);
			}

			public void onFinish() {
				timerText.setText(getString(R.string.sms_timeout));
			}
		}.start();
		 checkOldSMSs();

	}
	
    public void checkOldSMSs()  {
    	final long one_day=1000*60*60*24;
    	TextView view = new TextView(this);
        Uri uriSMSURI = Uri.parse("content://sms/inbox");
        Date filterDateStart=new Date();        
        String filter = "date>=" + (filterDateStart.getTime()-one_day);
        Cursor c = getContentResolver().query(uriSMSURI, null, filter, null,null);
        while (c.moveToNext()) {

        	// Only Inbox messages
            if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                
                SMSMessage read_sms = new SMSMessage(c.getString(c.getColumnIndexOrThrow("_id")), c.getString(c.getColumnIndexOrThrow("address")), c.getString(c.getColumnIndexOrThrow("body")),c.getString(c.getColumnIndexOrThrow("date")));
                sms_message.setText(read_sms.getId());
            } 

            c.moveToNext();
        }
        return;
    }
	
	

	protected void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
		mIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String msg = intent.getStringExtra("get_msg");

				// Process the sms format and extract body &amp; phoneNumber
				msg = msg.replace("\n", "");
				String body = msg.substring(msg.lastIndexOf(":") + 1,
						msg.length());
				String pNumber = msg.substring(0, msg.lastIndexOf(":"));				
                sms_message.setText(body);
                counter.cancel();
                timerText.setText(getString(R.string.sms_ticket_ok));
                progressBar.setVisibility(View.GONE);
                

			}
		};
		this.registerReceiver(mIntentReceiver, intentFilter);
	}

	protected void onPause() {

		super.onPause();
		this.unregisterReceiver(this.mIntentReceiver);
	}

}
