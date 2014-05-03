package fi.aalto.kutsuplus.sms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SMSReceiver extends BroadcastReceiver {
	  public void onReceive(Context context, Intent intent) {

          Bundle extras = intent.getExtras();
          if (extras == null)
                 return;
          //http://en.wikipedia.org/wiki/Protocol_data_unit
          Object[] pdus = (Object[]) extras.get("pdus");
          for (int i = 0; i < pdus.length; i++) {
                 SmsMessage SMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                 String sender = SMessage.getOriginatingAddress();
                 String body = SMessage.getMessageBody().toString();

                 // A custom Intent that will used as another Broadcast
                 Intent in = new Intent("SmsMessage.intent.MAIN").putExtra(
                              "get_msg", sender + ":" + body);

                 // You can place your check conditions here(on the SMS or the
                 // sender)
                 // and then send another broadcast
                 
                 context.sendBroadcast(in);
          }
   }

}
