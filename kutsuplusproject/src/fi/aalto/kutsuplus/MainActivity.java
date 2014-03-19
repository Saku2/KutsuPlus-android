package fi.aalto.kutsuplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import fi.aalto.kutsuplus.database.RideDatabaseHandler;
import fi.aalto.kutsuplus.database.StreetAddress;
import fi.aalto.kutsuplus.database.StreetDatabaseHandler;
import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;
import fi.aalto.kutsuplus.kdtree.TreeNotReadyException;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener, OnSharedPreferenceChangeListener, ISendStreetAddress {

	SharedPreferences preferences;

	final static int MAPFRAG = 1;
	final static int EXTRAS_FROM = 0;
	final static int EXTRAS_TO = 1;
	public int extras_list = EXTRAS_FROM;

	private final String LOG_TAG = "kutsuplus";

	private List<Fragment> mFragments;
	private FormFragment formFrag;
	private MapFragm mapFrag;
	private TabPagerAdapter mTabPagerAdapter;
	private ViewPager mPager;

	private StopTreeHandler stopTreeHandler;

	public PopupWindow popupWindow_ExtrasList;

	private GoogleMap google_map = null;
	private boolean isFirstVisitToMap = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// field requires API level 11, messes the textviews...
		if (android.os.Build.VERSION.RELEASE.startsWith("1.") ||
		        android.os.Build.VERSION.RELEASE.startsWith("2.") )
		        ;
		else
           getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.fragments);

		// Create stop tree
		// TODO: Run in new thread?
		Log.d(LOG_TAG, "before creating stopTree");
		try {
			stopTreeHandler = StopTreeHandler.getInstance(getAssets().open(getString(R.string.stop_list_path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "after creating stopTree");
		StopObject stop;
		try {
			stop = stopTreeHandler.getClosestStop(new MapPoint(0, 0), 1)[0].getNeighbor().getValue();
			Log.d(LOG_TAG, stop.getFinnishName());
		} catch (TreeNotReadyException e) {
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "after querying stop");

		// web from:
		// http://stackoverflow.com/questions/15533343/android-fragment-basics-tutorial
		// Check whether the activity is using the layout version with
		// the phone_fragment_container FrameLayout. If so, we must add the
		// first fragment
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
			mapFrag = new MapFragm();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			formFrag.setArguments(getIntent().getExtras());

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
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});

			Tab formtab = actionBar.newTab();
			formtab.setText(getString(R.string.TAB_form));
			formtab.setContentDescription(getString(R.string.TAB_form_description));
			formtab.setTabListener(this);
			actionBar.addTab(formtab);

			Tab maptab = actionBar.newTab();
			maptab.setText(getString(R.string.TAB_map));
			maptab.setContentDescription(getString(R.string.TAB_map_description));
			maptab.setTabListener(this);
			actionBar.addTab(maptab);

			// The preferences menu//s
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
			preferences.registerOnSharedPreferenceChangeListener(this);

		}

		// TWO-PANE LAYOUT
		else {// in two-pane layout set general as initial detail view
				// Capture the detail fragment from the activity layout
			MapFragm mapFrag = (MapFragm) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
			showHelsinkiArea(mapFrag, mapFrag.initialZoomLevel);
		}

	}

	private void showHelsinkiArea(MapFragm mapFrag, float zoomLevel) {
		if (mapFrag != null) {//
			// get map object
			SupportMapFragment mySupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			google_map = mySupportMapFragment.getMap();
			mapFrag.setMap(google_map);
			mapFrag.setStopTreeHandler(stopTreeHandler);
			// center point on map
			GoogleMapPoint centerPoint = this.stopTreeHandler.findInitialCenter();
			// view-changing method in map-fragmet:
			try {
				mapFrag.updateMapView(centerPoint, zoomLevel);
			} catch (Exception e) {
				System.out.println("Probably no map initialized: " + e.getMessage());
			}

		}
	}

	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());
		// TabPagerAdapter adapter = (TabPagerAdapter) mPager.getAdapter();
		if (isFirstVisitToMap) {
			if (tab.getPosition() == MAPFRAG) {//
				showHelsinkiArea(mapFrag, mapFrag.initialZoomLevel);
				isFirstVisitToMap = false;
			}
		}
	}

	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}

	// Open the web browser for the www-users
	public void doOpenBrowser(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.kutsuplus_url)));
		startActivity(browserIntent);
	}

	public void doOrder(View v) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(getString(R.string.sms_hsl_number), null, "from to", null, null);
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
		StreetDatabaseHandler stha = new StreetDatabaseHandler(getApplicationContext());
		stha.clearContent();
		stha.addStreetAddress(new StreetAddress(from));
		stha.addStreetAddress(new StreetAddress(to));
		RideDatabaseHandler rides = new RideDatabaseHandler(getApplicationContext());
		rides.clearContent();
		rides.addRide(from, to);
	}

	// called when user first clicks menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();// create a MenuInflater to
													// help use the menu that we
													// already made in XML
		inflater.inflate(R.menu.menu, menu);// inflate this menu with the XML
											// resource that was created earlier
		return true;// to allow method to be displayed
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {// decide which MenuItem was pressed based on
									// its id
		case R.id.item_prefs:
			startActivity(new Intent(this, SettingsActivity.class));// start the
																	// PrefsActivity.java
			break;
		}
		return true; // to execute the event here
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

	Locale myLocale;

	private void setLocale(String lang) {
		myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		Intent refresh = new Intent(this, MainActivity.class);
		startActivity(refresh);
	}

	// ISendStopName implementation
	@Override
	public void fillFromToTextBox(String street_address) {
		Log.d("street adress", street_address);
		FormFragment formFragment = (FormFragment) getSupportFragmentManager().findFragmentById(R.id.large_form_fragment);
		if (formFragment != null) {// large view
			formFragment.updateToFromText(street_address);
		} else {// small view
			formFrag.updateToFromText(street_address);
		}
	}

	@Override	
	public void setPickupDropoff(StopObject so) {
		Log.d("stop name", so.getFinnishName());
		FormFragment formFragment = (FormFragment) getSupportFragmentManager().findFragmentById(R.id.large_form_fragment);
		if (formFragment != null) {// large view
			formFragment.updatePickupDropOffText(so.getFinnishName()+" "+so.getShortId());
		} else {// small view
			formFrag. updatePickupDropOffText(so.getFinnishName()+so.getShortId());
		}
		
		
	}
	
	public void fillPickupDropoffTextBox(String stopName) {
	}

	public void fillSelectedMapLocation(LatLng address_gps) {
		if(findViewById(R.id.from).hasFocus())
		{
			address_from_gps=address_gps;
		}
		else
		{
			address_to_gps=address_gps;
		}
		showRouteOnMap();
	}

	LatLng address_from_gps=null;
	LatLng address_to_gps=null;
	
	public void showRouteOnMap() {
		if(address_from_gps==null)
			return;
		if(address_to_gps==null)
			return;
		
		ArrayList<LatLng> points = null;
		PolylineOptions polyLineOptions = null;

		points = new ArrayList<LatLng>();
		polyLineOptions = new PolylineOptions();

		points.add(address_from_gps);
		points.add(address_to_gps);

		polyLineOptions.addAll(points);
		polyLineOptions.width(2);
		polyLineOptions.color(Color.RED);
		google_map.clear();
		
		google_map.addPolyline(polyLineOptions);
		MapFragm mapFragment = (MapFragm) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.addAllKutsuPlusStopMarkers();
		
	}

	


}
