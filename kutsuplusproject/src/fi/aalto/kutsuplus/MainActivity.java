package fi.aalto.kutsuplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.google.android.gms.maps.model.LatLng;
import com.savarese.spatial.NearestNeighbors;

import fi.aalto.kutsuplus.database.RideDatabaseHandler;
import fi.aalto.kutsuplus.database.StreetAddress;
import fi.aalto.kutsuplus.database.StreetDatabaseHandler;
import fi.aalto.kutsuplus.database.TicketInfo;
import fi.aalto.kutsuplus.events.OTTOCommunication;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;
import fi.aalto.kutsuplus.kdtree.TreeNotReadyException;
import fi.aalto.kutsuplus.sms.SMSMessage;
import fi.aalto.kutsuplus.sms.SMSParser;
import fi.aalto.kutsuplus.utils.AddressHandler;
import fi.aalto.kutsuplus.utils.CoordinateConverter;
import fi.aalto.kutsuplus.utils.CustomViewPager;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener, OnSharedPreferenceChangeListener, ISendMapSelection {

	SharedPreferences preferences;
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	final static public String CURRENT_LOCATIION = "Current location";
	final static int FORMFRAG = 0;
	final static int MAPFRAG = 1;
	final static int EXTRAS_FROM = 0;
	final static int EXTRAS_TO = 1;
	public int extras_list = EXTRAS_FROM;

	private final String LOG_TAG = "kutsuplus";

	// Tabs for phone
	private List<Fragment> fragmentList;

	private FormFragment formFragment;
	private TicketFragment ticketFragment;
	private MapFragm mapFragment;

	private TabPagerAdapter tabPagerAdapter;
	private CustomViewPager mPager;

	private StopTreeHandler stopTreeHandler;

	public PopupWindow popupWindow_ExtrasList;

	private boolean isFirstVisitToMap = true;

	boolean isTwoPaneLayout;

	Menu menu;
	MenuItem show_busstops_button;
	private BroadcastReceiver sms_receiver;

	private final LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
			// default location here
			if (communication.getFrom_address()==null || communication.getFrom_address().equals("")) {
				String address=AddressHandler.getAdresss(getApplicationContext(), pos);
				if(address!=null)	
		        communication.setFrom_address(OTTOCommunication.MAIN_ACTIVITY,address);	            	
		    }			
			
			if (communication.getTo_address()==null || communication.getTo_address().equals("")) {
				String address=AddressHandler.getAdresss(getApplicationContext(), pos);
				if(address!=null)	
		        communication.setTo_address(OTTOCommunication.MAIN_ACTIVITY,address);	            	
			}
			communication.setCurrent_location(OTTOCommunication.MAIN_ACTIVITY, pos);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// The preferences menu
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
		String used_language = preferences.getString("prefLanguage", "");
		if (!used_language.equals("")) {
			setLocale(used_language);
		}
		LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 20, mLocationListener);

		supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

		final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.main_fragments);

		// Create stop tree
		// This seems to be fast enough
		Log.d(LOG_TAG, "before creating stopTree");
		try {
			stopTreeHandler = StopTreeHandler.getInstance(getAssets().open(getString(R.string.stop_list_path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "after creating stopTree");
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED); 

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

			formFragment = new FormFragment();
			ticketFragment = new TicketFragment();
			mapFragment = new MapFragm();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			formFragment.setArguments(getIntent().getExtras());

			fragmentList = new ArrayList<Fragment>();
			fragmentList.add(formFragment);
			fragmentList.add(mapFragment);
			fragmentList.add(ticketFragment);

			tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), fragmentList);

			mPager = (CustomViewPager) findViewById(R.id.pager);
			mPager.setAdapter(tabPagerAdapter);

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

		}

		// TWO-PANE LAYOUT
		else {// in two-pane layout set general as initial detail view
				// Capture the detail fragment from the activity layout
			isTwoPaneLayout = true;

			formFragment = new FormFragment();// (FormFragment)
												// getSupportFragmentManager().findFragmentById(R.id.large_form_fragment);
			mapFragment = new MapFragm();// (MapFragm)
											// getSupportFragmentManager().findFragmentById(R.id.map_fragment);

			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().add(R.id.large_form_fragment, formFragment, "Form").commit();
			getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment, "Map").commit();

			mapFragment.setStopTreeHandler(stopTreeHandler);
			if (show_busstops_button != null)
				show_busstops_button.setVisible(true);
		}
	}

	public MapFragm getMapFragment() {

		return this.mapFragment;
	}

	private FormFragment getFormFragment() {
		return this.formFragment;
	}

	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());

		if (isFirstVisitToMap) {
			if (tab.getPosition() == MAPFRAG) {//
				mapFragment.showHelsinkiArea(mapFragment.initialZoomLevel);
				isFirstVisitToMap = false;
			}
		}
		if (tab.getPosition() == MAPFRAG) {
			mPager.setPagingEnabled(false);
			if (show_busstops_button != null)
				show_busstops_button.setVisible(true);
		} else {
			mPager.setPagingEnabled(true);
			if (show_busstops_button != null)
				show_busstops_button.setVisible(false);
		}
	}

	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		if (tab.getPosition() == FORMFRAG) {
			AutoCompleteTextView toView = formFragment.getToView();
			AutoCompleteTextView fromView = formFragment.getFromView();
			if (toView != null) {
				toView.dismissDropDown();
			}
			if (fromView != null) {
				fromView.dismissDropDown();
			}

		}
	}

	// Open the web browser for the www-users
	public void doOpenBrowser(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.kutsuplus_url)));
		startActivity(browserIntent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu = menu;
		if (menu != null) {
			show_busstops_button = menu.findItem(R.id.kp_busstops);
			if (isTwoPaneLayout)
				show_busstops_button.setVisible(true);//
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
		switch (item.getItemId()) {
		case R.id.item_prefs:
			startActivity(new Intent(this, SettingsActivity.class)); // Starts
																		// the
																		// Settings
																		// Activity
			break;
		}

		if (item.getItemId() == R.id.kp_busstops) {
			MapFragm mapFragment = getMapFragment();
			// add KP bus stops
			if (mapFragment != null) {
				if (!mapFragment.isKPstopsAreVisible()) {
					if (!mapFragment.isKPstopsAreCreated()) {
						mapFragment.addAllKutsuPlusStopMarkers();
					} else {
						mapFragment.showKutsuPlusStopMarkers();
					}
				} else {
					mapFragment.hideKutsuPlusStopMarkers();
				}
			}
		}
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		String used_language = sharedPreferences.getString("prefLanguage", "");
		if (!used_language.equals("")) {
			setLocale(used_language);
			Toast.makeText(this, "Restart the application to see the language change in effect.", Toast.LENGTH_LONG).show();
			// TODO changes the language on the fly, but creates new instances
			// Intent refresh = new Intent(this, MainActivity.class);
			// startActivity(refresh);
		}

	}

	private Locale myLocale;

	// This is originally from:
	// http://stackoverflow.com/questions/12908289/how-change-language-of-app-on-user-select-language
	private void setLocale(String lang) {
		myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
	}

	@Override
	public void setMapLocationSelection(String street_address, LatLng address_gps) {
		Log.d("street adress", street_address);
		FormFragment formFragment = getFormFragment();
		MapFragm mapFragment = getMapFragment();

		Log.d(LOG_TAG, "setMapLocationSelected address:" + address_gps.longitude + " " + address_gps.latitude);
		MapPoint mp = CoordinateConverter.wGS84lalo_to_kkj2(address_gps.longitude, address_gps.latitude);
		Log.d(LOG_TAG, "setMapLocationSelected address map:" + mp);
		try {
			NearestNeighbors.Entry<Integer, MapPoint, StopObject>[] stops = stopTreeHandler.getClosestStops(mp, 1);
			if (stops != null) {
				if (stops.length > 0) {
					StopObject so = stops[0].getNeighbor().getValue();
					formFragment.updatePickupDropOffText(so, mapFragment.isMarkerWasDragged(), mapFragment.isDraggedStartMarker());
					boolean focusAtFrom = findViewById(R.id.from).hasFocus();
					mapFragment.updateMarkersAndRoute(address_gps, so, focusAtFrom);
				}
			}
		} catch (TreeNotReadyException e) {
			e.printStackTrace();
		}
		// PAY ATTENTION TO THE LOCATION OF THE FOLLOWING LINE
		formFragment.updateToFromText(street_address, mapFragment.isMarkerWasDragged(), mapFragment.isDraggedStartMarker());

	}

	@Override
	public void setStopMarkerSelection(StopObject bus_stop, LatLng address_gps) {
		Log.d("stop name", bus_stop.getFinnishName());
		FormFragment formFragment = getFormFragment();
		MapFragm mapFragment = getMapFragment();

		formFragment.updatePickupDropOffText(bus_stop, mapFragment.isMarkerWasDragged(), mapFragment.isDraggedStartMarker());
		// PAY ATTENTION TO THE LOCATION OF THE FOLLOWING LINE
		formFragment.updateToFromText(bus_stop.getFinnishName() + " " + bus_stop.getShortId(), mapFragment.isMarkerWasDragged(), mapFragment.isDraggedStartMarker());
		boolean focusAtFrom = findViewById(R.id.from).hasFocus();
		mapFragment.updateMarkersAndRoute(address_gps, bus_stop, focusAtFrom);
	}

	private void checkOldSMSs() {
		final long one_day = 1000 * 60 * 60 * 24;
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Date filterDateStart = new Date();
		String filter = "date>=" + (filterDateStart.getTime() - one_day);
		Cursor c = getContentResolver().query(uriSMSURI, null, filter, null, null);
		while (c.moveToNext()) {

			// Only Inbox messages
			if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {

				SMSMessage read_sms = new SMSMessage(c.getString(c.getColumnIndexOrThrow("_id")), c.getString(c.getColumnIndexOrThrow("address")), c.getString(c.getColumnIndexOrThrow("body")),
						c.getString(c.getColumnIndexOrThrow("date")));

			}

			c.moveToNext();
		}
		return;
	}

	protected void onPause() {

		super.onPause();
		this.unregisterReceiver(sms_receiver);
	}

	public void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
		sms_receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ticketFragment == null)
					return;
				String msg = intent.getStringExtra("get_msg");

				// Process the sms format and extract body &amp; phoneNumber

				String body = msg.substring(msg.indexOf(":") + 1);
				// String pNumber = msg.substring(0, msg.lastIndexOf(":"));
				// sms_message.setText(body);
				SMSParser smsparser = null;
				try {
					smsparser = new SMSParser(getResources().getStringArray(R.array.sms_keyword_array));
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				TicketInfo response = smsparser.parse(body);

				if (smsparser.isTicket()) {
					ticketFragment.showTicket(body);
				} else {
					ticketFragment.showErrorMessage(body);
				}
				ticketFragment.stopAnimation();

			}
		};
		this.registerReceiver(sms_receiver, intentFilter);

	}

	public void doOrder(View v) {

		if (ticketFragment == null)
			ticketFragment = new TicketFragment();
		if (isTwoPaneLayout) {
			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().add(R.id.large_form_fragment, ticketFragment, "Ticket").commit();
		} else {
			final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

			Tab tickettab = actionBar.newTab();
			tickettab.setText(getString(R.string.TAB_ticket));
			tickettab.setContentDescription(getString(R.string.TAB_ticket_description));
			tickettab.setTabListener(this);
			actionBar.addTab(tickettab);

			mPager.setCurrentItem(2);
		}

		SmsManager smsManager = SmsManager.getDefault();
		if (communication == null)
			return;
		if ((communication.getPick_up_stop() == null) || (communication.getDrop_off_stop() == null))
			return;
		// KPE: English message format
		String sms_message = "KPE " + communication.getPick_up_stop().getShortId() + " " + communication.getDrop_off_stop().getShortId();
		smsManager.sendTextMessage(getString(R.string.sms_hsl_number), null, sms_message, null, null);

		// Save the ride to the local database
		String from = communication.getFrom_address();
		String to = communication.getTo_address();
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

}
