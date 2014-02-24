package fi.aalto.kutsuplus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class MainActivity extends ActionBarActivity implements
		android.support.v7.app.ActionBar.TabListener {

	/** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR); // TODO: FEATURE_ACTION_BAR requires API level 11
        super.onCreate(savedInstanceState);
		setContentView(R.layout.fragments);


        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		// web from:
		// http://stackoverflow.com/questions/15533343/android-fragment-basics-tutorial
		// Check whether the activity is using the layout version with
		// the phone_fragment_container FrameLayout. If so, we must add the
		// first
		// fragment
		// ONE-PANE LAYOUT
		if (findViewById(R.id.phone_fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			FormFragment formFragment = new FormFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			formFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.phone_fragment_container, formFragment).commit();
		}

		// TWO-PANE LAYOUT
		else {// in two-pane layout set general as initial detail view
				// Capture the detail fragment from the activity layout
			MapFragment mapFrag = (MapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_fragment);
			if (mapFrag != null) {
				// some view-changing method in map-fragmet?
				// mapFrag.updateDetailView(0);
			}
		}


	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	// Open the web browser for the www-users
	public void doOpenBrowser(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.kutsuplus_url)));
		startActivity(browserIntent);
	}

	public void doOrder(View v) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(getString(R.string.sms_hsl_number), null,
				"from to", null, null);
		Intent intent = new Intent(this, SMSNotificationActivity.class);
		startActivity(intent);
	}


}
