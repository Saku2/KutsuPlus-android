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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
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
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.savarese.spatial.NearestNeighbors;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.database.RideDatabaseHandler;
import fi.aalto.kutsuplus.database.StreetAddress;
import fi.aalto.kutsuplus.database.StreetDatabaseHandler;
import fi.aalto.kutsuplus.events.CommunicationBus;
import fi.aalto.kutsuplus.events.DropOffChangeEvent;
import fi.aalto.kutsuplus.events.EndLocationChangeEvent;
import fi.aalto.kutsuplus.events.PickUpChangeEvent;
import fi.aalto.kutsuplus.events.StartLocationChangeEvent;
import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;
import fi.aalto.kutsuplus.kdtree.TreeNotReadyException;
import fi.aalto.kutsuplus.utils.CoordinateConverter;
import fi.aalto.kutsuplus.utils.CustomViewPager;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener, OnSharedPreferenceChangeListener, FormFragment.OnItemClickListener, ISendMapSelection {

	SharedPreferences preferences;
	private Bus communication_bus=CommunicationBus.getInstance().getCommucicationBus();

	final static int MAPFRAG = 1;
	final static int EXTRAS_FROM = 0;
	final static int EXTRAS_TO = 1;
	public int extras_list = EXTRAS_FROM;

	private final String LOG_TAG = "kutsuplus";

	private List<Fragment> mFragments;
	private FormFragment formFrag;
	private MapFragm mapFrag;
	private TabPagerAdapter mTabPagerAdapter;
	private CustomViewPager mPager;

	private StopTreeHandler stopTreeHandler;

	public PopupWindow popupWindow_ExtrasList;

	private GoogleMap google_map = null;
	private boolean isFirstVisitToMap = true;
	private boolean isMapTabSelected = false;
	
	boolean isTwoPaneLayout;
	
	Menu menu_this;
	MenuItem kp_button;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

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
			stop = stopTreeHandler.getClosestStops(new MapPoint(0, 0), 1)[0].getNeighbor().getValue();
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
			isTwoPaneLayout = false;

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

			mPager = (CustomViewPager) findViewById(R.id.pager);
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
			isTwoPaneLayout = true;
			
			MapFragm mapFrag = getMapFragment();
			
			showHelsinkiArea(mapFrag, mapFrag.initialZoomLevel);
			if(kp_button != null)
				kp_button.setVisible(true);
		}
		communication_bus.register(this);
	}
	
	private MapFragm getMapFragment(){
		MapFragm mapFragment = null;;
		if(isTwoPaneLayout){
			mapFragment = (MapFragm) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		}
		else{
			mapFragment = this.mapFrag;
		}
		return mapFragment;
	}

	public FormFragment getFormFragment(){
		FormFragment formFragment = null;
		if(isTwoPaneLayout){
			formFragment = (FormFragment) getSupportFragmentManager().findFragmentById(R.id.large_form_fragment);
		}
		else{
			formFragment = this.formFrag;
		}
		return formFragment;
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
		//MenuItem kp_button = (MenuItem) findViewById(R.id.kutsu_pysakkit);
		// TabPagerAdapter adapter = (TabPagerAdapter) mPager.getAdapter();
		if (isFirstVisitToMap) {
			if (tab.getPosition() == MAPFRAG) {//
				showHelsinkiArea(mapFrag, mapFrag.initialZoomLevel);
				isFirstVisitToMap = false;
			}
		}
		if (tab.getPosition() == MAPFRAG){
			isMapTabSelected = true;
			mPager.setPagingEnabled(false);
			if(kp_button != null)
				kp_button.setVisible(true);
		}
		else{
			isMapTabSelected = false;
			mPager.setPagingEnabled(true);
			if(kp_button != null)
				kp_button.setVisible(false);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu_this = menu;
		if(menu_this != null){
			kp_button = menu_this.findItem(R.id.kutsu_pysakkit);
			if(isTwoPaneLayout)
				kp_button.setVisible(true);//
		}
	    return true;
	}
	
	// creating action-bar menu
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
			startActivity(new Intent(this, SettingsActivity.class));// start the // PrefsActivity.java
			break;
		}
		
		if(item.getItemId() == R.id.kutsu_pysakkit){
			MapFragm mapFragment = getMapFragment();
			//add KP bus stops
			if(mapFragment != null){
				if(!mapFragment.KPstopsAreVisible){
					if(!mapFragment.KPstopsAreCreated){
						mapFragment.addAllKutsuPlusStopMarkers();
					}
					else{
						mapFragment.showKutsuPlusStopMarkers();
					}
				}
				else{
					mapFragment.hideKutsuPlusStopMarkers();
				}
			}
		}
		return true; // to execute the event here
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

	private Locale myLocale;

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

	@Override
	public void setMapLocationSelection(String street_address,LatLng address_gps) {
		Log.d("street adress", street_address);
		FormFragment formFragment = getFormFragment();
        MapFragm mapFragment = getMapFragment();
				
		Log.d(LOG_TAG, "setMapLocationSelected address:"+address_gps.longitude+" "+address_gps.latitude);
		MapPoint mp=CoordinateConverter.wGS84lalo_to_kkj2(address_gps.longitude,address_gps.latitude);
		Log.d(LOG_TAG, "setMapLocationSelected address map:"+mp);
		try {
			NearestNeighbors.Entry<Integer, MapPoint, StopObject>[] stops=stopTreeHandler.getClosestStops(mp, 1);
			if(stops!=null)
			{
				if(stops.length>0)
				{
					StopObject so=stops[0].getNeighbor().getValue();
					formFragment.updatePickupDropOffText(so.getFinnishName() + " " + so.getShortId(), mapFragment.markerWasDragged, mapFragment.draggedStartMarker);
					 
					mapFragment.makeKPmarkers();
					Marker mr = mapFragment.markers_so.get(so);
					mapFragment.updateMarkersAndRoute(address_gps, mr, this);
				}
			}
		} catch (TreeNotReadyException e) {
			e.printStackTrace();
		}
		// PAY ATTENTION TO THE LOCATION OF THE FOLLOWING LINE
		formFragment.updateToFromText(street_address, mapFragment.markerWasDragged, mapFragment.draggedStartMarker);
		
	}


	@Override
	public void setStopMarkerSelection(StopObject so,LatLng address_gps, Marker marker) {
		Log.d("stop name", so.getFinnishName());
		FormFragment formFragment = getFormFragment();
		MapFragm mapFragment = getMapFragment();
		
		formFragment.updatePickupDropOffText(so.getFinnishName() + " " + so.getShortId(), mapFragment.markerWasDragged, mapFragment.draggedStartMarker);
		 // PAY ATTENTION TO THE LOCATION OF THE FOLLOWING LINE
		formFragment.updateToFromText(so.getFinnishName() + " " + so.getShortId(), mapFragment.markerWasDragged, mapFragment.draggedStartMarker);
		
		mapFragment.updateMarkersAndRoute(address_gps, marker, this);
	}

	
    @Override
	public void onSuggestionClicked(LatLng latLng, StopObject so){
		//Toast.makeText(this, "DROPDAWN CLICK!", Toast.LENGTH_LONG).show();
    	MapFragm mapFragment = getMapFragment();
		if(mapFragment.markers.size() == 0){
			mapFragment.makeKPmarkers();
		}
		Marker m = mapFragment.markers_so.get(so);
		mapFragment.updateMarkersAndRoute(m.getPosition(), m, this);
	}
	
    @Subscribe
    public void onStartLocationChangeEvent(StartLocationChangeEvent event){
    	Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onEndLocationChangeEvent(EndLocationChangeEvent event){
    	Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
    }
    
    @Subscribe
    public void onPickUpChangeEvent(PickUpChangeEvent event){
    	Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onDropOffChangeEvent(DropOffChangeEvent event){
    	Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
    }

}
