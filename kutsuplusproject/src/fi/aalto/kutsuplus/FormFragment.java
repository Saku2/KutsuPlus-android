package fi.aalto.kutsuplus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.database.Ride;
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
import fi.aalto.kutsuplus.utils.HttpHandler;
import fi.aalto.kutsuplus.utils.StreetSearchAdapter;

public class FormFragment extends Fragment {
	private Bus communication_bus = CommunicationBus.getInstance().getCommucicationBus();
	private View rootView;
	String popUpContents[];
	ImageButton buttonShowDropDown_fromExtras;
	ImageButton buttonShowDropDown_toExtras;
	PopupWindow popupWindow_ExtrasList;
	public static final int DIALOG_FRAGMENT = 1;

	private StopObject currentPickupStop = null;
	private StopObject currentDropoffStop = null;

	private final String LOG_TAG = "kutsuplus" + this.getClass().getName();

	AutoCompleteTextView fromView;
	AutoCompleteTextView toView;
	TextView pickupStop;
	TextView dropoffStop;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);

		// Get the streets string array
		String[] streets = readStreets(R.raw.kutsuplus_area_street_names);

		// Get a reference to the AutoCompleteTextView in the layout
		fromView = (AutoCompleteTextView) rootView.findViewById(R.id.from);
		toView = (AutoCompleteTextView) rootView.findViewById(R.id.to);
		// Create the adapter and set it to the AutoCompleteTextView
		final StreetSearchAdapter adapter_from = new StreetSearchAdapter(getActivity(), android.R.layout.simple_list_item_1, streets);

		pickupStop = (TextView) rootView.findViewById(R.id.pickup_stop);
		dropoffStop = (TextView) rootView.findViewById(R.id.dropoff_stop);
		adapter_from.registerDataSetObserver(new DataSetObserver() {

			private Handler handler = new Handler();

			Runnable getCoordinatesTask = new Runnable() {
				public void run() {
					try {
						// To avoid the android.os.NetworkOnMainThreadException
						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						StrictMode.setThreadPolicy(policy);

						String queryText = adapter_from.getItem(0);
						HttpHandler http = new HttpHandler();
						List<NameValuePair> args = new ArrayList<NameValuePair>();
						args.add(new BasicNameValuePair("key", queryText));
						args.add(new BasicNameValuePair("user", getString(R.string.reittiopas_api_user)));
						args.add(new BasicNameValuePair("pass", getString(R.string.reittiopas_api_pass)));
						args.add(new BasicNameValuePair("request", "geocode"));

						JSONArray jsonArray = null;
						JSONObject json = null;
						Log.d(LOG_TAG, "timer called");
						try {
							String tmp = http.makeHttpGet("http://api.reittiopas.fi/hsl/prod/", args);
							Log.d(LOG_TAG, tmp);
							jsonArray = new JSONArray(tmp);
							json = jsonArray.getJSONObject(0);

							String[] coords = json.getString("coords").split(",");
							// latitude is a geographic coordinate that
							// specifies the north-south position of a point
							String longtitude = coords[0];
							String latitude = coords[1];

							try {
								MapPoint mp = new MapPoint(Integer.parseInt(longtitude), Integer.parseInt(latitude));
								currentPickupStop = StopTreeHandler.getInstance().getClosestStops(mp, 1)[0].getNeighbor().getValue();
								Log.d(LOG_TAG, "pickup stop: " + currentPickupStop.getFinnishName() + " " + currentPickupStop.getShortId());
								pickupStop.setText(currentPickupStop.getFinnishName() + " " + currentPickupStop.getShortId());
								LatLng ll = new LatLng(mp.getX(), mp.getY());

								// onItemClick FROM action moved here
								// ------------->
								pickupStop.setText(currentPickupStop.getFinnishName() + " " + currentPickupStop.getShortId());
								GoogleMapPoint gmp = currentPickupStop.getGmpoint();
								mCallback.onSuggestionActivation(new LatLng(gmp.getX(), gmp.getY()), currentPickupStop);
								fromView.requestFocus();
								// <-----------------------------------------------

								communication_bus.post(new StartLocationChangeEvent(CommunicationBus.FORM_FRAGMENT, ll));
							} catch (Exception e) {
								e.printStackTrace();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
						Log.d(LOG_TAG, "Adapter didn't have any items");
					}
				}
			};

			@Override
			public void onChanged() {
				Log.d(LOG_TAG, "onChanged called");
				super.onChanged();
				handler.removeCallbacks(getCoordinatesTask);
				try {
					handler.postDelayed(getCoordinatesTask, 1000l);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		fromView.setAdapter(adapter_from);

		// Get the string array
		final StreetSearchAdapter adapter_to = new StreetSearchAdapter(getActivity(), android.R.layout.simple_list_item_1, streets);
		adapter_to.registerDataSetObserver(new DataSetObserver() {

			private Handler handler = new Handler();

			Runnable getCoordinatesTask = new Runnable() {
				public void run() {
					try {
						// To avoid the android.os.NetworkOnMainThreadException
						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						StrictMode.setThreadPolicy(policy);
						String queryText = adapter_to.getItem(0);
						HttpHandler http = new HttpHandler();
						List<NameValuePair> args = new ArrayList<NameValuePair>();
						args.add(new BasicNameValuePair("key", queryText));
						args.add(new BasicNameValuePair("user", getString(R.string.reittiopas_api_user)));
						args.add(new BasicNameValuePair("pass", getString(R.string.reittiopas_api_pass)));
						args.add(new BasicNameValuePair("request", "geocode"));

						JSONArray jsonArray = null;
						JSONObject json = null;
						Log.d(LOG_TAG, "timer called");
						try {
							String tmp = http.makeHttpGet("http://api.reittiopas.fi/hsl/prod/", args);
							Log.d(LOG_TAG, tmp);
							jsonArray = new JSONArray(tmp);
							json = jsonArray.getJSONObject(0);

							String[] coords = json.getString("coords").split(",");
							// latitude is a geographic coordinate that
							// specifies the north-south position of a point
							String longtitude = coords[0];
							String latitude = coords[1];
							try {
								MapPoint mp = new MapPoint(Integer.parseInt(longtitude), Integer.parseInt(latitude));
								currentDropoffStop = StopTreeHandler.getInstance().getClosestStops(mp, 1)[0].getNeighbor().getValue();
								Log.d(LOG_TAG, "dropoff stop: " + currentDropoffStop.getFinnishName() + " " + currentDropoffStop.getShortId());
								dropoffStop.setText(currentDropoffStop.getFinnishName() + " " + currentDropoffStop.getShortId());
								LatLng ll = new LatLng(mp.getX(), mp.getY());

								// onItemClick TO action moved here
								// ------------->
								dropoffStop.setText(currentDropoffStop.getFinnishName() + " " + currentDropoffStop.getShortId());
								GoogleMapPoint gmp = currentDropoffStop.getGmpoint();
								mCallback.onSuggestionActivation(new LatLng(gmp.getX(), gmp.getY()), currentDropoffStop);
								toView.requestFocus();
								// <-----------------------------------------------
								communication_bus.post(new EndLocationChangeEvent(CommunicationBus.FORM_FRAGMENT, ll));

							} catch (Exception e) {
								e.printStackTrace();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
						Log.d(LOG_TAG, "Adapter didn't have any items");
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			};

			@Override
			public void onChanged() {
				Log.d(LOG_TAG, "onChanged called");
				super.onChanged();
				handler.removeCallbacks(getCoordinatesTask);
				try {
					handler.postDelayed(getCoordinatesTask, 1000l);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		toView.setAdapter(adapter_to);
		createDropDown(rootView);

		// Remember the last ride
		RideDatabaseHandler rides = new RideDatabaseHandler(rootView.getContext());
		List<Ride> ride_list = rides.getAllStreetAddresses();
		if (ride_list != null)
			if (ride_list.size() > 0) {
				String from = ride_list.get(0).get_StreetAddress_from();
				String to = ride_list.get(0).get_StreetAddress_to();
				if ((from != null) && (to != null)) {
					fromView.setText(from);
					toView.setText(to);
				}
			}
		communication_bus.register(this);
		return rootView;
	}

	private String[] readStreets(int resource_id) {
		List<String> streets = new ArrayList<String>();

		InputStream raw = getResources().openRawResource(resource_id);
		try {
			// Special Scandinavian characters handled
			InputStreamReader isr = new InputStreamReader(raw, "ISO-8859-1");
			BufferedReader in = new BufferedReader(isr);
			String line = in.readLine();
			while (line != null) {
				streets.add(line);
				line = in.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return streets.toArray(new String[streets.size()]);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
		// Operating hours 6-23 Helsinki time
		Time now = new Time("UCT");
		now.setToNow();
		now.switchTimezone("Europe/Helsinki");
		if ((now.weekDay == 5) || (now.weekDay == 6) || (now.hour > 23) || (now.hour < 6)) {
			DialogFragment operatingHoursFragment = new OperatingHoursDialogFragment();
			// A click outside of the dialog box will not dismiss the box
			// Back button will not dismiss the box
			operatingHoursFragment.setCancelable(false);
			operatingHoursFragment.show(getFragmentManager(), "operating_hours");
		}		
	}

	private void createDropDown(View rootView) {
		List<String> optionsList = new ArrayList<String>();
		optionsList.add("Current location");
		StreetDatabaseHandler stha = new StreetDatabaseHandler(rootView.getContext());
		try {
			List<StreetAddress> own_addresses = stha.getAllStreetAddresses();

			for (StreetAddress address : own_addresses)
				optionsList.add(address.get_StreetAddress());
		} catch (Exception e) {
			e.printStackTrace(); // Possiblt legal
		}
		popUpContents = new String[optionsList.size()];
		optionsList.toArray(popUpContents);

		/*
		 * initialize pop up window
		 */
		Context mContext = rootView.getContext();
		final MainActivity mainActivity = ((MainActivity) mContext);
		mainActivity.popupWindow_ExtrasList = getPopupWindow();
		popupWindow_ExtrasList = mainActivity.popupWindow_ExtrasList;
		/*
		 * fromExtras button on click listener
		 */
		View.OnClickListener extras_handler = new View.OnClickListener() {
			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.from_extras:
					// show the list view as dropdown
					mainActivity.extras_list = MainActivity.EXTRAS_FROM;
					popupWindow_ExtrasList.showAsDropDown(v, 0, 0);
					break;

				case R.id.to_extras:
					// show the list view as dropdown
					mainActivity.extras_list = MainActivity.EXTRAS_TO;
					popupWindow_ExtrasList.showAsDropDown(v, 0, 0);
					break;
				}
			}
		};

		// toExreas button
		buttonShowDropDown_fromExtras = (ImageButton) rootView.findViewById(R.id.from_extras);
		buttonShowDropDown_fromExtras.setOnClickListener(extras_handler);

		// fromExreas button
		buttonShowDropDown_toExtras = (ImageButton) rootView.findViewById(R.id.to_extras);
		buttonShowDropDown_toExtras.setOnClickListener(extras_handler);

	}

	/*
     * 
     */
	// deprecation caused by new BitmapDrawable()
	// new style is here
	// http://stackoverflow.com/questions/9978884/bitmapdrawable-deprecated-alternative
	@SuppressWarnings("deprecation")
	public PopupWindow getPopupWindow() {
		PopupWindow popupWindow = new PopupWindow(rootView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		// the drop down list is a list view
		ListView listViewExtras = new ListView(getActivity());

		// set our adapter and pass our pop up window contents
		listViewExtras.setAdapter(extrasAdapter(popUpContents));

		// set the item click listener
		listViewExtras.setOnItemClickListener(new ExtrasDropdownOnItemClickListener());
		// Closes the popup window when touch outside of it - when looses focus
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setWidth(450); // TODO check if this can be adapted to fit
									// the screen widths
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

		LinearLayout layout = new LinearLayout(rootView.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(5, 5, 5, 5);
		layout.setBackgroundColor(getResources().getColor(R.color.light_gray));
		// set the list view as pop up window content
		layout.addView(listViewExtras, layoutParams);
		popupWindow.setContentView(layout);

		return popupWindow;
	}

	/*
	 * adapter where the list values will be set
	 */
	private ArrayAdapter<String> extrasAdapter(String dogsArray[]) {

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, dogsArray) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				// setting the ID and text for every items in the list
				String item = getItem(position);

				// visual settings for the list item
				TextView listItem = new TextView(getContext());

				listItem.setText(item);
				listItem.setTag(item);
				listItem.setTextSize(22);
				listItem.setPadding(15, 15, 15, 15);
				listItem.setTextColor(Color.BLACK);
				listItem.setBackgroundColor(Color.WHITE);

				return listItem;
			}
		};

		return adapter;
	}

	// After clicking on a map, update from/to text
	public void updateToFromText(String street_address, boolean markerWasDragged, boolean draggedStartMarker) {
		if (markerWasDragged) {
			if (draggedStartMarker) {
				fromView.setText(street_address);
				fromView.requestFocus();
			} else {
				toView.setText(street_address);
				toView.requestFocus();
			}
		} else {
			if (fromView.hasFocus()) {
				fromView.setFocusable(false); // DO NOT REMOVE THIS
				fromView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
				fromView.setText(street_address);
				fromView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
				fromView.setFocusable(true); // DO NOT REMOVE THIS
			} else {
				toView.setFocusable(false); // DO NOT REMOVE THIS
				toView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
				toView.setText(street_address);
				toView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
				toView.setFocusable(true); // DO NOT REMOVE THIS
			}
		}
	}

	public void updatePickupDropOffText(String stop_description, boolean markerWasDragged, boolean draggedStartMarker) {
		TextView pickupView = (TextView) rootView.findViewById(R.id.pickup_stop);
		TextView dropoffView = (TextView) rootView.findViewById(R.id.dropoff_stop);
		if (markerWasDragged) {
			if (draggedStartMarker) {
				pickupView.setText(stop_description);
				fromView.requestFocus();
			} else {
				dropoffView.setText(stop_description);
				toView.requestFocus();
			}
		} else {
			if (fromView.hasFocus()) {
				pickupView.setText(stop_description);

			} else {
				dropoffView.setText(stop_description);
			}
		}
	}

	public OnItemActivationListener mCallback;

	
	/*
	 * FormFragment uses timing to get the nearest bus stop, but this uses a mouse click activation:
	   It is possible that a quick user clicks the list item too fast and there is a wrong stop ID at  the class variable

	private void initView() {
		this.fromView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				pickupStop.setText(currentPickupStop.getFinnishName() + " " + currentPickupStop.getShortId());
				GoogleMapPoint gmp = currentPickupStop.getGmpoint();
				mCallback.onSuggestionClicked(new LatLng(gmp.getX(), gmp.getY()), currentPickupStop);
				fromView.requestFocus();

			}
		});
		this.toView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				dropoffStop.setText(currentDropoffStop.getFinnishName() + " " + currentDropoffStop.getShortId());
				GoogleMapPoint gmp = currentDropoffStop.getGmpoint();
				mCallback.onSuggestionClicked(new LatLng(gmp.getX(), gmp.getY()), currentDropoffStop);
				toView.requestFocus();
			}
		});
	}*/

	public interface OnItemActivationListener {
		/** Called by FormFragment when a suggestion list item is selected */
		public void onSuggestionActivation(LatLng latLng, StopObject currentPickupStop);
	}

	@Subscribe
	public void onPickUpChangeEvent(PickUpChangeEvent event) {
		Toast.makeText(rootView.getContext().getApplicationContext(), event.toString(), Toast.LENGTH_LONG).show();
	}

	@Subscribe
	public void onDropOffChangeEvent(DropOffChangeEvent event) {
		Toast.makeText(rootView.getContext().getApplicationContext(), event.toString(), Toast.LENGTH_LONG).show();
	}
}