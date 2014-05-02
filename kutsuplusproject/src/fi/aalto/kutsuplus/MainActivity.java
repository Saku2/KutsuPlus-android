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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.location.Criteria;
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
import android.test.suitebuilder.annotation.SmallTest;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.internal.fo;
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

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener, ISendMapSelection, ISendFormSelection, LocationListener, ISendFocusChangeInfo {


	private Locale myLocale;
	//view elements
	Tab formtab;
	Tab maptab;
	
	//riding crumb
	LocationManager locationManager;
    String provider;
	
	
	SharedPreferences preferences;
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	final static int FORMFRAG = 0;
	final static int MAPFRAG = 1;
	final static int FROM = 0;
	final static int TO = 1;
	final static int EXTRAS_FROM = 0;
	final static int EXTRAS_TO = 1;
	private int extras_list = EXTRAS_FROM;
	private int mapturn= FROM;

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


	boolean isTwoPaneLayout;

	Menu thisMenu; 
	MenuItem show_busstops_button;
	MenuItem ride_crumb_button;
	MenuItem clear_map_button;
	private BroadcastReceiver sms_receiver;
    /*
     * The location listener here feeds the location information into OttoCommunication
     * for the generic use.
     */
	private final LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
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
		supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
		
		LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();     
		String provider=mLocationManager.getBestProvider(criteria, false);     
		mLocationManager.requestLocationUpdates(provider, 60000, 20, mLocationListener);
		
		// The preferences menu
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String used_language = preferences.getString("prefLanguage", "");
		if (!used_language.equals("")) {
			setLocale(used_language);
		}


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

			formtab = actionBar.newTab();
			formtab.setText(getString(R.string.TAB_form));
			formtab.setContentDescription(getString(R.string.TAB_form_description));
			formtab.setTabListener(this);
			actionBar.addTab(formtab);

			maptab = actionBar.newTab();
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

			if (show_busstops_button != null)
				show_busstops_button.setVisible(true);
			if (ride_crumb_button != null)
				ride_crumb_button.setVisible(true);
			if (clear_map_button != null)
				clear_map_button.setVisible(true);
			
		}
		mapFragment.setStopTreeHandler(stopTreeHandler);
		checkOldSMSs();
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig){
	    super.onConfigurationChanged(newConfig);
	    Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    if (myLocale != null){
	        newConfig.locale = myLocale;
	        Locale.setDefault(myLocale);
	        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
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

	/*
	 * This is called, when the user selects a tab on a mobile phone screen
	 * 
	 * @see android.support.v7.app.ActionBar.TabListener#onTabSelected(android.support.v7.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());

		if (tab.getPosition() == MAPFRAG) {
			mPager.setPagingEnabled(false);
			if (show_busstops_button != null)
				show_busstops_button.setVisible(true);
			if (ride_crumb_button != null)
				ride_crumb_button.setVisible(true);
			if (clear_map_button != null)
				clear_map_button.setVisible(true);
		} else {
			mPager.setPagingEnabled(true);
			if (show_busstops_button != null)
				show_busstops_button.setVisible(false);
			if (ride_crumb_button != null)
				ride_crumb_button.setVisible(false);
			if (clear_map_button != null)
				clear_map_button.setVisible(false);
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

	/*
	 *  doOpenKutsuplusPage is called when user clicks the Kutsuplus web link on the main screen
	 */
	public void doOpenKutsuplusPage(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.kutsuplus_url)));
		startActivity(browserIntent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {//
		this.thisMenu = menu;
		if (menu != null) {
			show_busstops_button = menu.findItem(R.id.kp_busstops);
			ride_crumb_button = menu.findItem(R.id.ride_crumb);
			clear_map_button = menu.findItem(R.id.clear_map);
			if (isTwoPaneLayout){
				show_busstops_button.setVisible(true);//
				ride_crumb_button.setVisible(true);//
				clear_map_button.setVisible(true);
			}
		}
		return true;
	}

	// Creates the menu at the actionbar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();// create a MenuInflater to
													// help use the menu that we
													// already made in XML
		inflater.inflate(R.menu.menu, menu);// inflate this menu with the XML
											// resource that was created earlier
		return true;// to allow method to be displayed
	}

	/*
	 * When a menu item is selectes at the actionbar menu, the handling is 
	 * made here
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.lang_fi:
			setLocale("fi");
			break;
		case R.id.lang_en:
			setLocale("en");
			break;
		case R.id.lang_sv:
			setLocale("sv");
			break;
		case R.id.ride_crumb:
			startTrackingRide(item);
			break;
		case R.id.clear_map:
			getMapFragment().clearMap();
			break;
		case R.id.focus_txtview:
	    	Log.d("TAG FOCUS", ""+item.getItemId());
			changeTxtViewFocus(item);
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


    private void changeTxtViewFocus(MenuItem item) {
    	Log.d("TAG FOCUS", item.getTitle().toString());
		this.getFormFragment().focusChangedFromActionBar = true;
    	clearAllTextViewsFocusesInFromFragment();
		if(item.getTitle().toString().equals("start_focus")){
			item.setIcon(R.drawable.focus_finish);
			item.setTitle("finish_focus");
			this.getFormFragment().toView.requestFocus();
			mapturn=MainActivity.TO;
		}
		else{
			item.setIcon(R.drawable.focus_start);
			item.setTitle("start_focus");//
			this.getFormFragment().fromView.requestFocus();	
			mapturn=MainActivity.FROM;
		}		
	}

	private void clearAllTextViewsFocusesInFromFragment(){
		FormFragment ff = getFormFragment();
		ff.fromView.clearFocus();
		ff.toView.clearFocus();
		ff.passengers.clearFocus();
		ff.maxPrice.clearFocus();
	}


    private void startTrackingRide(MenuItem item) {
    	// Getting LocationManager object
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
    	if(item.getTitle().toString().equals("off")){
    		item.setTitle("on");
    		item.setIcon(R.drawable.ride_crumb);
            // Creating an empty criteria object
            Criteria criteria = new Criteria();     
            // Getting the name of the provider that meets the criteria
            provider = locationManager.getBestProvider(criteria, false);     
            if(provider!=null && !provider.equals("")){     
                // Get the location from the given provider
                Location location = locationManager.getLastKnownLocation(provider);     
                locationManager.requestLocationUpdates(provider, 20000, 1, this);     
                if(location!=null)
                    onLocationChanged(location);
                else
                    Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();     
            }else{
                Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
            }
    	}
    	else{
    		item.setTitle("off");
    		item.setIcon(R.drawable.ride_crumb_off);
    		// stop rider scrumbs
    		if(locationManager != null)
    			locationManager.removeUpdates(this);
    	}
	}


	// setLocale sets the language that is used at the program.
	// To make the operation smooth, the activity is not restatrted
	// This is originally from:
	// http://stackoverflow.com/questions/12908289/how-change-language-of-app-on-user-select-language
	private void setLocale(String lang) {
		myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		reloadRecources();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("prefLanguage", lang); // value to store
		editor.commit();//This is needed or the edits will not be put into the prefs file
	}
	
	/*
	 *  reloadRecources() updates the language related resources on the fragments
	 *  after a new language is selected.
	 */

	private void reloadRecources(){
		if(formtab != null){
			formtab.setContentDescription(getString(R.string.TAB_form_description));
			formtab.setText(R.string.TAB_form);
		}
		if(maptab != null){
			maptab.setContentDescription(getString(R.string.TAB_map_description));
			maptab.setText(R.string.TAB_map);
		}
		//FORMFRAG
		if(formFragment!=null)
			formFragment.reloadRecources();
	}

	/*
	 * setMapLocationSelection is called from the map fragment to send notification of a 
	 * map point selection. 
	 * 
	 * (non-Javadoc)
	 * @see fi.aalto.kutsuplus.ISendMapSelection#setMapLocationSelection(java.lang.String, com.google.android.gms.maps.model.LatLng)
	 */
	@Override
	public void setMapLocationSelection(String street_address, LatLng address_gps) {

		Log.d("street adress", street_address);
		FormFragment formFragment = getFormFragment();
        MapFragm mapFragment = getMapFragment();
		handleMarkerDragging();
		boolean focusAtFrom = formFragment.fromView.hasFocus();
		if(focusAtFrom){
			formFragment.updateFromText(street_address);
			communication.setFrom_address(OTTOCommunication.MAIN_ACTIVITY, street_address);
			mapturn=MainActivity.FROM;
		}
		else{
			formFragment.updateToText(street_address);
			communication.setTo_address(OTTOCommunication.FORM_FRAGMENT, street_address);
			mapturn=MainActivity.TO;
		}
				
		Log.d(LOG_TAG, "setMapLocationSelected address:"+address_gps.longitude+" "+address_gps.latitude);
		MapPoint mp=CoordinateConverter.wGS84lalo_to_kkj2(address_gps.longitude,address_gps.latitude);
		Log.d(LOG_TAG, "setMapLocationSelected address map:"+mp);
		try {
			NearestNeighbors.Entry<Integer, MapPoint, StopObject>[] stops=stopTreeHandler.getClosestStops(mp, 1);
			if(stops!=null)
			{
				if(stops.length>0)
				{
					StopObject stopObject=stops[0].getNeighbor().getValue();
					mapFragment.makeKPmarkers();
					mapFragment.updateMarkersAndRoute(address_gps, stopObject, focusAtFrom);
				}
			}
		} catch (TreeNotReadyException e) {
			e.printStackTrace();
		}

		
	}
	
	
	//change order-view focus based on which point was dragged on map
	private void handleMarkerDragging(){
        if(this.getMapFragment().isMarkerWasDragged()){  			
			Log.d("TAG FOCUS", "Marker Was Dragged");
        	MenuItem item = this.thisMenu.findItem(R.id.focus_txtview);
        	
			Log.d("TAG FOCUS", "Item title: " + item.getTitle().toString());
        	if(this.getMapFragment().isDraggedStartMarker()){//    			
    			Log.d("TAG FOCUS", "start dragged");
    			clearAllTextViewsFocusesInFromFragment();
        		if(!item.getTitle().toString().equals("start_focus")){
    				item.setIcon(R.drawable.focus_start);
    				item.setTitle("start_focus");
    				this.getFormFragment().fromView.requestFocus();			
    			}
        	}
        	else{   			
    			Log.d("TAG FOCUS", "finish dragged");
    			clearAllTextViewsFocusesInFromFragment();
    			if(item.getTitle().toString().equals("start_focus")){
    				item.setIcon(R.drawable.focus_finish);
    				item.setTitle("finish_focus");
    				this.getFormFragment().toView.requestFocus();
    			}
        	}
        }
	}
	

	//focus has changed from order-view (from-view)
	@Override
	public void onFocusChanged(boolean FromHasFocus) {
		if(!this.getFormFragment().focusChangedFromActionBar){
			Log.d("focus Change View", ""+FromHasFocus);
	    	MenuItem item = this.thisMenu.findItem(R.id.focus_txtview);
	    	if(FromHasFocus){  			
	    		if(!item.getTitle().toString().equals("start_focus")){
					item.setIcon(R.drawable.focus_start);
					item.setTitle("start_focus");		
				}
	    		mapturn=MainActivity.FROM;
	    	}
	    	else{   			
				if(item.getTitle().toString().equals("start_focus")){
					item.setIcon(R.drawable.focus_finish);
					item.setTitle("finish_focus");
				}
				mapturn=MainActivity.TO;
	    	}
		}
		this.getFormFragment().focusChangedFromActionBar = false;
	}




	/*
	 *  setStopMarkerSelection is called from the map fragment to notify that an bus stop marker
	 *  was selected.
	 * (non-Javadoc)
	 * @see fi.aalto.kutsuplus.ISendMapSelection#setStopMarkerSelection(fi.aalto.kutsuplus.kdtree.StopObject, com.google.android.gms.maps.model.LatLng)
	 */
	@Override
	public void setStopMarkerSelection(StopObject bus_stop, LatLng address_gps) {
		Log.d("stop name", bus_stop.getFinnishName());
		FormFragment formFragment = getFormFragment();
		MapFragm mapFragment = getMapFragment();

		if(mapturn==MainActivity.FROM)
			communication.setPick_up_stop(OTTOCommunication.MAIN_ACTIVITY, bus_stop);
		else
			communication.setDrop_off_stop(OTTOCommunication.MAIN_ACTIVITY, bus_stop);
		
		// PAY ATTENTION TO THE LOCATION OF THE FOLLOWING LINE
		String street_address=bus_stop.getFinnishName() + " " + bus_stop.getShortId();
		if(mapturn==MainActivity.FROM)
		{
			formFragment.updateFromText(street_address);
			communication.setFrom_address(OTTOCommunication.MAIN_ACTIVITY, street_address);
			mapturn=MainActivity.TO;
		}
		else
		{
			communication.setTo_address(OTTOCommunication.FORM_FRAGMENT, street_address);
			formFragment.updateToText(street_address);
		}

		boolean focusAtFrom = findViewById(R.id.from).hasFocus();
		mapFragment.updateMarkersAndRoute(address_gps, bus_stop, focusAtFrom);
	}

	/*
	 *   checkOldSMSs() can be used to read old SMS messages from the phone so that, 
	 *   if the phone has been shut down or the program was shut, the ticket can still
	 *   be handled
	 */
	private void checkOldSMSs() {
		final long one_day = 1000 * 60 * 60 * 24;
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Date filterDateStart = new Date();
		String filter = "date>=" + (filterDateStart.getTime() - one_day);
		Cursor c = getContentResolver().query(uriSMSURI, null, filter, null, null);
		while (c.moveToNext()) {

			// Only Inbox messages
			if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {

				SMSMessage received_sms = new SMSMessage(c.getString(c.getColumnIndexOrThrow("_id")), c.getString(c.getColumnIndexOrThrow("address")), c.getString(c.getColumnIndexOrThrow("body")),
						c.getString(c.getColumnIndexOrThrow("date")));
				
				System.out.println("ADDRESS:"+received_sms.getAddress());
				if(true)
				{
					SMSParser smsparser = null;
					try {
						smsparser = new SMSParser(getResources().getStringArray(R.array.sms_keyword_array));
					} catch (NotFoundException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					TicketInfo response = smsparser.parse(received_sms.getMessage());
					if (smsparser.isTicket()) {
						ticketFragment.showTicket(received_sms.getMessage());
					}
				}

			}

			c.moveToNext();
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	protected void onPause() {

		super.onPause();
		this.unregisterReceiver(sms_receiver);
	}

	/*
	 * This creates the SMS notifications receiver.
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
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

	/*
	 * doOrder(View v) reads the current selections from the communicaton singleton and
	 * sends a SMS to get the ticket. Ticket fragment is activated to be ready to show the
	 * receiving ticket. At the end the ride is saved at the local store so that the selections
	 * can be shown when the user opens the program later.
	 */
	public void doOrder(View v) {
		if (communication == null)
			return;
		if ((communication.getPick_up_stop() == null) || (communication.getDrop_off_stop() == null))
			return;
		if (ticketFragment == null)
			ticketFragment = new TicketFragment();
		if (isTwoPaneLayout) {
			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().add(R.id.large_form_fragment, ticketFragment, "Ticket").addToBackStack("Ticket").commit();
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

		Locale current_locale = getResources().getConfiguration().locale;
		
		String sms_message=null;
		if(current_locale.getLanguage().equals("fi"))
			sms_message= "KP " + communication.getPick_up_stop().getShortId() + " " + communication.getDrop_off_stop().getShortId();
		else if(current_locale.getLanguage().equals("sv"))
			sms_message= "KPS " + communication.getPick_up_stop().getShortId() + " " + communication.getDrop_off_stop().getShortId();
		else
			sms_message= "KPE " + communication.getPick_up_stop().getShortId() + " " + communication.getDrop_off_stop().getShortId();
		if(formFragment!=null)
		{
		 int max_price=formFragment.getMaximumPrice();
		 int passengers=formFragment.getPassengerCount();
		 if(passengers>0)
			 sms_message+=" X"+passengers;
		 if(max_price>0)
			 sms_message+=" E"+max_price;
		}
		smsManager.sendTextMessage(getString(R.string.sms_hsl_number), null, sms_message, null, null);
		ticketFragment.start_animation();
		
		

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
	
	public int getActiveExtraslist() {
		return this.extras_list;
	}
	
	public void setActiveExtraslist(int extras_list) {
		this.extras_list = extras_list;
	}

	@Override
	public void setFromActivated() {
			mapturn=MainActivity.FROM;
	}

	@Override
	public void setToActivated() {
			mapturn=MainActivity.TO;
	}

	//LOCATION SCRUMBS
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(getBaseContext(), "Latitude:" + location.getLatitude(), Toast.LENGTH_SHORT).show();
		Toast.makeText(getBaseContext(), "Longitude:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
		this.getMapFragment().updateRidingScrumbPolyline(location);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}


	@Override
	public void onProviderEnabled(String provider) {
	}


	@Override
	public void onProviderDisabled(String provider) {
		
	}
	
	@Override
	public void onBackPressed() {
		if (isTwoPaneLayout) {
			FragmentManager fm = getSupportFragmentManager();
			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStack();
			} else {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

}
