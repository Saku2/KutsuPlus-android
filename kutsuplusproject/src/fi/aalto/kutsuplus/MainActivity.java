package fi.aalto.kutsuplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;

public class MainActivity extends ActionBarActivity implements
		android.support.v7.app.ActionBar.TabListener {

    /** Called when the activity is first created. */

	List<Fragment> 	mFragments;
	FormFragment 	formFrag;
	MapFragment 	mapFrag;
	TabPagerAdapter mTabPagerAdapter;
	ViewPager		mPager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.fragments);

        
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
	        mFragments = new ArrayList<Fragment>();
	        formFrag = new FormFragment();
	        mapFrag = new MapFragment();

	        mFragments.add(formFrag);
	        mFragments.add(mapFrag);

	        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), mFragments);

	        mPager = (ViewPager) findViewById(R.id.pager);
	        mPager.setAdapter(mTabPagerAdapter);
	        
			mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				 
	            @Override
	            public void onPageSelected(int position) {
	                actionBar.setSelectedNavigationItem(position);
	            }
	 
	            @Override
	            public void onPageScrolled(int arg0, float arg1, int arg2) {
	            	//TODO autocreated stub
	            }
	 
	            @Override
	            public void onPageScrollStateChanged(int arg0) {
	            	//TODO autocreated stub
	            }
	        });
			
			actionBar.addTab(actionBar.newTab()
						.setText("form")
						.setTabListener(this));
			actionBar.addTab(actionBar.newTab()
						.setText("map")
						.setTabListener(this));
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
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

		mPager.setCurrentItem(tab.getPosition());
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
