package fi.aalto.kutsuplus;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import fi.aalto.kutsuplus.sms.SMSMessage;
import fi.aalto.kutsuplus.sms.SMSParser;

public class SMSNotificationActivity extends Activity {
	private BroadcastReceiver mIntentReceiver;
	TextView timerText;
	WebView sms_message;
	private ProgressBar progressBar;
	static Boolean timeOut = true;
	CountDownTimer counter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smsnotification);

		timerText = (TextView) findViewById(R.id.SW_TimeRemainigTv);
		progressBar = (ProgressBar) findViewById(R.id.SW_progressBar);
		progressBar.setVisibility(View.VISIBLE);
		sms_message= (WebView) findViewById(R.id.sms_message);
		//sms_message.setWebViewClient(new myWebClient());
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
	
	//http://stackoverflow.com/questions/4066438/android-webview-how-to-handle-redirects-in-app-instead-of-opening-a-browser
	/* public class myWebClient extends WebViewClient
	    {
	        @Override
	        public void onPageStarted(WebView view, String url, Bitmap favicon) {
	            // TODO Auto-generated method stub
	            super.onPageStarted(view, url, favicon);
	        }
	 
	        public boolean shouldOverrideUrlLoading(WebView view, String url){
	            // do your handling codes here, which url is the requested url
	            // probably you need to open that url rather than redirect:
	            //view.loadUrl(url);
	            return false; // then it is not handled by default action
	       }
	    }*/
	
    public void checkOldSMSs()  {
    	final long one_day=1000*60*60*24;
        Uri uriSMSURI = Uri.parse("content://sms/inbox");
        Date filterDateStart=new Date();        
        String filter = "date>=" + (filterDateStart.getTime()-one_day);
        Cursor c = getContentResolver().query(uriSMSURI, null, filter, null,null);
        while (c.moveToNext()) {

        	// Only Inbox messages
            if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                
                SMSMessage read_sms = new SMSMessage(c.getString(c.getColumnIndexOrThrow("_id")), c.getString(c.getColumnIndexOrThrow("address")), c.getString(c.getColumnIndexOrThrow("body")),c.getString(c.getColumnIndexOrThrow("date")));
                //sms_message.setText(read_sms.getId());
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
				
				String body = msg.substring(msg.indexOf(":") + 1);
				//String pNumber = msg.substring(0, msg.lastIndexOf(":"));				
                //sms_message.setText(body);
				SMSParser smsparser=new SMSParser();
				String[] response=smsparser.parse(body);
				if(smsparser.isTicket())
				{
					String template="<body><img src=\"ticket.jpg\"><BR>(1)</body>";
					String body2 = body.replace("\n", "<BR>");
					String html=template.replace("(1)", body2);
					int uripos=html.indexOf("http");
					if(uripos>-1)
					{
						String first=html.substring(0, uripos)+"<A HREF=\"";						
						String end=html.substring(uripos);
						String end2=end.replace("<BR>","\"> link <A><BR>");
						html=first+end2;
						System.out.println("uhtml2!!"+html);
					}
	                sms_message.loadDataWithBaseURL("file:///android_asset/",html,"text/html", "utf-8", null);
				}
				else
				{
					String template="<body>(1)</body>";
					String html=template.replace("(1)", body);
					int uripos=html.indexOf("http");
					if(uripos>-1)
					{
						String first=html.substring(0, uripos)+"<A HREF=\"";
						String end=html.substring(uripos);
						String end2=end.replace("</body>","\"> link <A></body>");
						end=end2.replace("Kutsuplus.fi/sms","");
						html=first+end;
						System.out.println("html2!!"+html);
					}
	                sms_message.loadDataWithBaseURL("file:///android_asset/",html,"text/html", "utf-8", null);
				}
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
