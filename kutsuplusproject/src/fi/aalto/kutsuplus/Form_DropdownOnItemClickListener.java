package fi.aalto.kutsuplus;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import fi.aalto.kutsuplus.events.OTTOCommunication;
import fi.aalto.kutsuplus.events.FromAddressChangeEvent;
import fi.aalto.kutsuplus.events.ToAddressChangeEvent;
import fi.aalto.kutsuplus.utils.AddressHandler;

/*
 * Idea originally:
 * http://www.codeofaninja.com/2013/04/show-listview-as-drop-down-android.html
 */

public class Form_DropdownOnItemClickListener implements OnItemClickListener {
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

		// get the context and main activity to access variables
		Context mContext = v.getContext();
		MainActivity mainActivity = ((MainActivity) mContext);

		// add some animation when a list item was clicked
		Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
		fadeInAnimation.setDuration(10);
		v.startAnimation(fadeInAnimation);

		// dismiss the pop up
		mainActivity.popupWindow_ExtrasList.dismiss();

		// get the text and set it as the button text
		String selectedItemText = ((TextView) v).getText().toString();
		if (mainActivity.getActiveExtraslist() == MainActivity.EXTRAS_FROM) {
			final AutoCompleteTextView fromView = (AutoCompleteTextView) mainActivity.findViewById(R.id.from);
			fromView.setFocusable(false);
			fromView.setFocusableInTouchMode(false);
			if (selectedItemText.equals(MainActivity.CURRENT_LOCATIION))
			{
				if(communication.getCurrent_location()!=null)
				{
					String address=AddressHandler.getAdresss(mainActivity.getApplicationContext(), communication.getCurrent_location());
					if(address!=null)		
					{
			          fromView.setText(address);
		              communication.setFrom_address(OTTOCommunication.FORM_FRAGMENT, address);
					}
	        		communication.setStart_location(OTTOCommunication.FORM_FRAGMENT, communication.getCurrent_location());
				}
				else
				{
					communication.setFrom_address(""); // Set the field to be updated when we get the positioning
					fromView.setFocusable(false); 
					fromView.setFocusableInTouchMode(false); 
					fromView.setText("waiting");
					fromView.setFocusableInTouchMode(true); 
					fromView.setFocusable(true); 
				}
        	}
			else
			{
				communication.setFrom_address(OTTOCommunication.FORM_FRAGMENT, selectedItemText);
				fromView.setFocusable(false); 
				fromView.setFocusableInTouchMode(false); 
				fromView.setText(selectedItemText);
				fromView.setFocusableInTouchMode(true); 
				fromView.setFocusable(true); 
			}
			fromView.setFocusable(true);
			fromView.setFocusableInTouchMode(true);
		} else {
			final AutoCompleteTextView toView = (AutoCompleteTextView) mainActivity.findViewById(R.id.to);
			toView.setFocusable(false);
			toView.setFocusableInTouchMode(false);
			if (selectedItemText.equals(MainActivity.CURRENT_LOCATIION))
			{
				if(communication.getCurrent_location()!=null)
				{
					String address=AddressHandler.getAdresss(mainActivity.getApplicationContext(), communication.getCurrent_location());
					if(address!=null)		
					{
			          toView.setText(address);
		              communication.setTo_address(OTTOCommunication.FORM_FRAGMENT, address);
					}
	        		communication.setEnd_location(OTTOCommunication.FORM_FRAGMENT, communication.getCurrent_location());
				}
				else
				{
					communication.setTo_address("");  // Set the field to be updated when we get the positioning
					toView.setFocusable(false); 
					toView.setFocusableInTouchMode(false); 
					toView.setText("waiting");
					toView.setFocusableInTouchMode(true); 
					toView.setFocusable(true); 
					
				}
			}
			else
			{
				communication.setTo_address(OTTOCommunication.FORM_FRAGMENT, selectedItemText);
				toView.setFocusable(false); 
				toView.setFocusableInTouchMode(false); 
				toView.setText(selectedItemText);
				toView.setFocusableInTouchMode(true); 
				toView.setFocusable(true); 
			}
			toView.setFocusable(true);
			toView.setFocusableInTouchMode(true);
		}

		String selectedItemTag = ((TextView) v).getTag().toString();
		Toast.makeText(mContext, selectedItemTag, Toast.LENGTH_SHORT).show();

	}

}