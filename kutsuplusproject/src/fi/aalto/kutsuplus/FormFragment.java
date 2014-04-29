package fi.aalto.kutsuplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.database.Ride;
import fi.aalto.kutsuplus.database.RideDatabaseHandler;
import fi.aalto.kutsuplus.database.StreetAddress;
import fi.aalto.kutsuplus.database.StreetDatabaseHandler;
import fi.aalto.kutsuplus.events.DropOffChangeEvent;
import fi.aalto.kutsuplus.events.OTTOCommunication;
import fi.aalto.kutsuplus.events.PickUpChangeEvent;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.utils.ReittiopasHttpHandler;
import fi.aalto.kutsuplus.utils.StreetSearchAdapter;

public class FormFragment extends Fragment {
	private OTTOCommunication communication = OTTOCommunication.getInstance();
	private View rootView;
	String popUpContents[];
	ImageButton buttonShowDropDown_fromExtras;
	ImageButton buttonShowDropDown_toExtras;
	PopupWindow popupWindow_ExtrasList;
	public static final int DIALOG_FRAGMENT = 1;

	private final String LOG_TAG = "kutsuplus" + this.getClass().getName();

	AutoCompleteTextView fromView;
	AutoCompleteTextView toView;
	EditText passengers;
	TextView pickupStop;
	TextView dropoffStop;
	TextView estimatedPrice;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.formfragment, container, false);

		// Get the streets string array
		String[] streets = readStreets(R.raw.kutsuplus_area_street_names);

		// Get a reference to the AutoCompleteTextView in the layout
		fromView = (AutoCompleteTextView) rootView.findViewById(R.id.from);
		toView = (AutoCompleteTextView) rootView.findViewById(R.id.to);
		restoretoMemory();

		// Create the adapter and set it to the AutoCompleteTextView
		final StreetSearchAdapter adapter_from = new StreetSearchAdapter(getActivity(), android.R.layout.simple_list_item_1, streets);

		pickupStop = (TextView) rootView.findViewById(R.id.pickup_stop);
		dropoffStop = (TextView) rootView.findViewById(R.id.dropoff_stop);
		estimatedPrice = (TextView) rootView.findViewById(R.id.estimated_price);
		passengers = (EditText) rootView.findViewById(R.id.number_of_passengers);
		adapter_from.registerDataSetObserver(new DataSetObserver() {

			private Handler handler = new Handler();

			Runnable getCoordinatesTask = new Runnable() {
				public void run() {
					try {
						String queryText = adapter_from.getItem(0);
						if (queryText != null)
							handleFromFieldActivation(queryText);
					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
						Log.d(LOG_TAG, "Adapter didn't have any items");
					}
				}

			};

			@Override
			public void onChanged() {
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
						String queryText = adapter_to.getItem(0);
						if (queryText != null)
							handleToFieldActivation(queryText);

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
		communication.register(this);
		initView();
		return rootView;
	}

	private void restoretoMemory() {
		OTTOCommunication cb = OTTOCommunication.getInstance();
		if (cb.getFrom_address() != null) {
			fromView.setFocusable(false); // DO NOT REMOVE THIS
			fromView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
			fromView.setText(cb.getFrom_address());
			fromView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
			fromView.setFocusable(true); // DO NOT REMOVE THIS
		}

		if (cb.getTo_address() != null) {
			toView.setFocusable(false); // DO NOT REMOVE THIS
			toView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
			toView.setText(cb.getTo_address());
			toView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
			toView.setFocusable(true); // DO NOT REMOVE THIS
		}
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
		if ((now.weekDay == 0) || (now.weekDay == 6) || (now.hour > 23) || (now.hour < 6)) {
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
					mainActivity.setActiveExtraslist(MainActivity.EXTRAS_FROM);
					popupWindow_ExtrasList.showAsDropDown(v, 0, 0);
					break;

				case R.id.to_extras:
					// show the list view as dropdown
					mainActivity.setActiveExtraslist(MainActivity.EXTRAS_TO);
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
		listViewExtras.setOnItemClickListener(new Form_DropdownOnItemClickListener());
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

	// After clicking on a map, update From text
	public void updateFromText(String street_address) {
		fromView.setFocusable(false); // DO NOT REMOVE THIS
		fromView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
		fromView.setText(street_address);
		fromView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
		fromView.setFocusable(true); // DO NOT REMOVE THIS
	}

	// After clicking on a map, update To text
	public void updateToText(String street_address) {
		toView.setFocusable(false); // DO NOT REMOVE THIS
		toView.setFocusableInTouchMode(false); // DO NOT REMOVE THIS
		toView.setText(street_address);
		toView.setFocusableInTouchMode(true); // DO NOT REMOVE THIS
		toView.setFocusable(true); // DO NOT REMOVE THIS
	}


	private void initView() {
		this.fromView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				String queryText = parent.getItemAtPosition(position).toString();
				handleFromFieldActivation(queryText);
			}
		});
		this.fromView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String queryText = parent.getItemAtPosition(position).toString();
				handleFromFieldActivation(queryText);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		this.toView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				String queryText = parent.getItemAtPosition(position).toString();
				handleToFieldActivation(queryText);
			}
		});

		this.toView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String queryText = parent.getItemAtPosition(position).toString();
				handleToFieldActivation(queryText);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private String last_From_query = "";

	private void handleFromFieldActivation(String queryText) {
		// Do not make duplicate queries
		if (last_From_query.equals(queryText))
			return;
		last_From_query = queryText;

		ReittiopasHttpHandler http = new ReittiopasHttpHandler();
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("key", queryText));
		args.add(new BasicNameValuePair("user", getString(R.string.reittiopas_api_user)));
		args.add(new BasicNameValuePair("pass", getString(R.string.reittiopas_api_pass)));
		args.add(new BasicNameValuePair("request", "geocode"));

		communication.setFrom_address(OTTOCommunication.FORM_FRAGMENT, queryText);
		Log.d(LOG_TAG, "timer called");
		http.makeGetStartAddress("http://api.reittiopas.fi/hsl/prod/", args);
	}

	private String last_To_query = "";

	private void handleToFieldActivation(String queryText) {
		// Do not make duplicate queries
		if (last_To_query.equals(queryText))
			return;
		last_To_query = queryText;

		ReittiopasHttpHandler http = new ReittiopasHttpHandler();
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("key", queryText));
		args.add(new BasicNameValuePair("user", getString(R.string.reittiopas_api_user)));
		args.add(new BasicNameValuePair("pass", getString(R.string.reittiopas_api_pass)));
		args.add(new BasicNameValuePair("request", "geocode"));

		communication.setTo_address(OTTOCommunication.FORM_FRAGMENT, queryText);
		Log.d(LOG_TAG, "timer called");
		http.makeGetEndAddress("http://api.reittiopas.fi/hsl/prod/", args);
	}

	@Subscribe
	public void onPickUpChangeEvent(PickUpChangeEvent event) {
		StopObject bus_stop = event.getBus_stop();
		Log.d(LOG_TAG, "Pickup stop: " + bus_stop.getFinnishName() + " " + bus_stop.getShortId());
		pickupStop.setText(bus_stop.getFinnishName() + " " + bus_stop.getShortId());
		AsyncEstimate estimate = new AsyncEstimate();
		estimate.execute();
	}

	@Subscribe
	public void onDropOffChangeEvent(DropOffChangeEvent event) {
		StopObject bus_stop = event.getBus_stop();
		Log.d(LOG_TAG, "Dropoff stop: " + bus_stop.getFinnishName() + " " + bus_stop.getShortId());
		dropoffStop.setText(bus_stop.getFinnishName() + " " + bus_stop.getShortId());
		AsyncEstimate estimate = new AsyncEstimate();
		estimate.execute();
	}

	private class AsyncEstimate extends AsyncTask<Object, Integer, String> {
		protected String doInBackground(Object... args) {
			return estimatePrice();
		}

		public String estimatePrice() {
			Log.d("estimate", "going to estimate next");
			// the price is 0,45 euros / km + base fee of 3,5 euros.
			if (communication.getPick_up_stop() == null || communication.getDrop_off_stop() == null) {
				Log.e("estimate", "getPick_up_stop was null when estimating price");
				return "";
			}
			int distance = calculateDistance(communication.getPick_up_stop().getFinnishAddress(), "simonkatu 1, Helsinki");
			Log.e("estimate", "Estimate busstop adresses: From: " + communication.getPick_up_stop().getFinnishAddress() + " to: " + communication.getDrop_off_stop().getFinnishAddress());
			Log.e("estimate", "Street adresses: From: " + communication.getFrom_address() + " to: " + communication.getTo_address());
			float distance_price = 0.45f * distance / 1000;
			float base_price = 3.5f;
			float estimated_price = distance_price + base_price;
			Log.e("estimate", "estimated value: " + String.valueOf(estimated_price));
			DecimalFormat moneyFormatter = new DecimalFormat("##.##");
			return moneyFormatter.format(estimated_price) + " €";
		}

		private int calculateDistance(String location1, String location2) {
			int distance_int = 0;
			try {

				// Origin of the route
				String str_origin = communication.getPick_up_stop().getGmpoint().getY() + "," + communication.getPick_up_stop().getGmpoint().getX();

				// Destination of the route
				String str_destination = communication.getDrop_off_stop().getGmpoint().getY() + "," + communication.getDrop_off_stop().getGmpoint().getX();

				String search_string = "http://maps.googleapis.com/maps/api/directions/xml?origin=" + str_origin + "&destination=" + str_destination + "&language=FI&sensor=false";

				Log.e("distance", "here's the search string: " + search_string);
				String distance = parse_distance(httpGET(search_string));
				Log.e("distance", "here's the distance string: " + distance);
				distance_int = Integer.parseInt(distance);
				Log.e("distance", "here's the distance int in m: " + String.valueOf(distance_int));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("distance", "exception: distance int failed");
			}

			return distance_int;

		}

		private String httpGET(String urlString) {
			StringBuffer result = new StringBuffer();
			BufferedReader reader = null;
			URL url;
			try {
				url = new URL(urlString);
				URLConnection connection = url.openConnection();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
			}

			catch (Exception e) {
				e.printStackTrace();
				Log.e("exception", "Exception: was unable to get HTTP response");
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return result.toString();
		}

		private String parse_distance(String google_result) {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			InputSource source = new InputSource(new StringReader(google_result));
			int distance = 0;
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(source);
				XPathExpression expr = xpath.compile("/DirectionsResponse/route/leg/step/distance/value");
				NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					distance = distance + Integer.parseInt(node.getTextContent());
				}
			} catch (Exception e) {
				Log.e("exception", "parsing failed");
				e.printStackTrace();
			}
			return String.valueOf(distance);
		}

		protected void onPostExecute(String result) {
			estimatedPrice.setText(result);
		}
	}

	public AutoCompleteTextView getFromView() {
		return fromView;
	}

	public AutoCompleteTextView getToView() {
		return toView;
	}
}
