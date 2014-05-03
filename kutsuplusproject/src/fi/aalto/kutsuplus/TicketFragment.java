package fi.aalto.kutsuplus;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fi.aalto.kutsuplus.events.OTTOCommunication;
 
public class TicketFragment extends Fragment {
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	private View rootView;
	private final String LOG_TAG = "kutsuplus" + this.getClass().getName();

	TextView timerText;
	WebView sms_message;
	private ProgressBar progressBar;
	static Boolean timeOut = true;
	CountDownTimer counter;
	boolean delay_animation=false;
	boolean animation_set=false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.ticketfragment, container, false);
		if(delay_animation)
			start_animation();
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
	}


	public void onResume() {
		super.onResume();
	}
	
	/*
	 * showTicket(String body)  shows the received ticket message on the screen
	 */
	public void showTicket(String body)
	{
		String template = "<body><img src=\"ticket.jpg\"  width=\"80%\"><BR>(1)</body>";
		String body2 = body.replace("\n", "<BR>");
		String html = template.replace("(1)", body2);
		int uripos = html.indexOf("http");
		if (uripos > -1) {
			String first = html.substring(0, uripos) + "<A HREF=\"";
			String end = html.substring(uripos);
			String end2 = end.replace("<BR>", "\"> link <A><BR>");
			html = first + end2;
		}
		sms_message.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);	
	}

	/*
	 * In case there is an error message in stead of the ticket at the SMS message
	 * showErrorMessage(String body) format that to be shown on the web view.
	 */
	public void showErrorMessage(String body)
	{
		String template = "<body>(1)</body>";
		String html = template.replace("(1)", body);
		int uripos = html.indexOf("http");
		if (uripos > -1) {
			String first = html.substring(0, uripos) + "<A HREF=\"";
			String end = html.substring(uripos);
			String end2 = end.replace("</body>", "\"> link <A></body>");
			end = end2.replace("Kutsuplus.fi/sms", "");
			html = first + end;
		}
		sms_message.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
	}

	/* 
	 * start_animation() the animation show the maximum expected 30 second of time to wait the 
	 * receiving ticket. The action is stopped as soon as a ticket is get.
	 */
		public void start_animation()
		{
			if(rootView==null)
			{
				delay_animation=true;
				return;
			}
			if(animation_set)
				stopAnimation();
			timerText = (TextView) rootView.findViewById(R.id.SW_TimeRemainig);
			progressBar = (ProgressBar) rootView.findViewById(R.id.SW_progressBar);
			progressBar.setVisibility(View.VISIBLE);
			sms_message = (WebView) rootView.findViewById(R.id.sms_message);
			// show 30 second time count down
			counter = new CountDownTimer(30000, 1000) {

				public void onTick(long millisUntilFinished) {
					try
					{
					  timerText.setText(getString(R.string.sms_waiting) + " " + millisUntilFinished / 1000);
					}
					catch(Exception e)
					{
						// If activity has been stopped and this is still running
						counter.cancel();
					}
				}

				public void onFinish() {
					try
					{
					 timerText.setText(getString(R.string.sms_timeout));
					}
					catch(Exception e)
					{
						
					}
				}
			}.start();
			animation_set=true;
		}


	/*
	 * When the ticket is got the animation is stopped
	 */
	public void stopAnimation()
	{
		counter.cancel();
		try
		{
		 timerText.setText(getString(R.string.sms_ticket_ok));
		 progressBar.setVisibility(View.GONE);
		}
		catch(IllegalStateException is)
		{
			//
		}
        animation_set=false;
	}
}