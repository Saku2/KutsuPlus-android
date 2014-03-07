package fi.aalto.kutsuplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.aalto.kutsuplus.database.Ride;
import fi.aalto.kutsuplus.database.RideDatabaseHandler;
import fi.aalto.kutsuplus.database.StreetAddress;
import fi.aalto.kutsuplus.database.StreetDatabaseHandler;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;
import fi.aalto.kutsuplus.kdtree.TreeNotReadyException;

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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;

public class MainActivity extends ActionBarActivity implements
		android.support.v7.app.ActionBar.TabListener {

	/** Called when the activity is first created. */
	private final String LOG_TAG = "kutsuplus";
	
	private List<Fragment> mFragments;
	private FormFragment formFrag;
	private MapFragment mapFrag;
	private TabPagerAdapter mTabPagerAdapter;
	private ViewPager mPager;

	private StopTreeHandler stopTreeHandler;
	
	public PopupWindow popupWindow_ExtrasList;
	public static int EXTRAS_FROM = 0;
	public static int EXTRAS_TO = 1;
	public int extras_list = EXTRAS_FROM;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.fragments);
		
		
		//Create stop tree
		//TODO: Run in new thread?
		Log.d(LOG_TAG, "before creating stopTree");
		try {
			stopTreeHandler = StopTreeHandler.getInstance(getAssets().open(getString(R.string.stop_list_path)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "after creating stopTree");
		StopObject stop;
		try {
			stop = stopTreeHandler.getClosestStop(new MapPoint(0,0), 1)[0].getNeighbor().getValue();
			Log.d(LOG_TAG, stop.getFinnishName());
		} catch (TreeNotReadyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "after querying stop");
		
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

			mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),
					mFragments);

			mPager = (ViewPager) findViewById(R.id.pager);
			mPager.setAdapter(mTabPagerAdapter);

			mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// TODO autocreated stub
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO autocreated stub
				}
			});

			Tab formtab=actionBar.newTab();
			formtab.setText(getString(R.string.TAB_form));
			formtab.setContentDescription(getString(R.string.TAB_form_description));
			formtab.setTabListener(this);
			actionBar.addTab(formtab);
			
			Tab maptab=actionBar.newTab();
			maptab.setText(getString(R.string.TAB_map));
			maptab.setContentDescription(getString(R.string.TAB_map_description));
			maptab.setTabListener(this);
			actionBar.addTab(maptab);
			
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

	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());
	}

	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
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
		final AutoCompleteTextView fromView = (AutoCompleteTextView) findViewById(R.id.from);
		final AutoCompleteTextView toView = (AutoCompleteTextView) findViewById(R.id.to);
		String from = fromView.getText().toString();
		String to = toView.getText().toString();
		if (from == null)
			return;
		if (to == null)
			return;
		StreetDatabaseHandler stha = new StreetDatabaseHandler(
				getApplicationContext());
		stha.clearContent();
		stha.addStreetAddress(new StreetAddress(from));
		stha.addStreetAddress(new StreetAddress(to));
		RideDatabaseHandler rides = new RideDatabaseHandler(
				getApplicationContext());
		rides.clearContent();
		rides.addRide(from, to);
	}

}
