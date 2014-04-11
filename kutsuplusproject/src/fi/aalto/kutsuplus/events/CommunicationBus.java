package fi.aalto.kutsuplus.events;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.kdtree.StopObject;

public class CommunicationBus {
	private final String LOG_TAG = "communicationBus" + this.getClass().getName();
	public static final int MAIN_ACTIVITY = 0;
	public static final int FORM_FRAGMENT = 1;
	public static final int MAP_FRAGMENT = 2;
	private final Bus commucication_bus = new Bus();
	
	private LatLng start_location=null;
	private LatLng end_location=null;
	private StopObject pick_up_stop=null;
	private StopObject drop_off_stop=null;

	private String from_address=null;
	private String to_address=null;
	
	public Bus getCommucicationBus() {
		return commucication_bus;
	}

	private static CommunicationBus instance = null;

	protected CommunicationBus() {
		commucication_bus.register(this);
	}

	public static CommunicationBus getInstance() {
		if (instance == null) {
			instance = new CommunicationBus();
		}
		return instance;
	}

	@Subscribe
	public void onStartLocationChangeEvent(StartLocationChangeEvent event) {
		Log.d(LOG_TAG, "start location event");
		start_location=event.getLocation();
	}

	@Subscribe
	public void onEndLocationChangeEvent(EndLocationChangeEvent event) {
		Log.d(LOG_TAG, "end location event");
		end_location=event.getLocation();
	}

	@Subscribe
	public void onPickUpChangeEvent(PickUpChangeEvent event) {
		Log.d(LOG_TAG, "pickup event");
		pick_up_stop=event.getBus_stop();
	}

	@Subscribe
	public void onDropOffChangeEvent(DropOffChangeEvent event) {
		Log.d(LOG_TAG, "dropdown event");
		drop_off_stop=event.getBus_stop();
	}

	@Subscribe
	public void onFromAddressChangeEvent(FromAddressChangeEvent event) {
		Log.d(LOG_TAG, "from address event");
		from_address=event.getStreet_address();
	}

	@Subscribe
	public void onToAddressChangeEvent(ToAddressChangeEvent event) {
		Log.d(LOG_TAG, "to address event");
		to_address=event.getStreet_address();
	}
	
	public LatLng getStart_location() {
		return start_location;
	}

	public void setStart_location(LatLng start_location) {
		this.start_location = start_location;
	}

	public LatLng getEnd_location() {
		return end_location;
	}

	public void setEnd_location(LatLng end_location) {
		this.end_location = end_location;
	}

	public StopObject getPick_up_stop() {
		return pick_up_stop;
	}

	public void setPick_up_stop(StopObject pick_up_stop) {
		this.pick_up_stop = pick_up_stop;
	}

	public StopObject getDrop_off_stop() {
		return drop_off_stop;
	}

	public void setDrop_off_stop(StopObject drop_off_stop) {
		this.drop_off_stop = drop_off_stop;
	}

	public String getFrom_address() {
		return from_address;
	}

	public void setFrom_address(String from_address) {
		this.from_address = from_address;
	}

	public String getTo_address() {
		return to_address;
	}

	public void setTo_address(String to_address) {
		this.to_address = to_address;
	}
	
}
