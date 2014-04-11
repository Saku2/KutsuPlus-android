package fi.aalto.kutsuplus.events;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.kdtree.StopObject;

public class OTTOCommunication {
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

	private static OTTOCommunication instance = null;

	protected OTTOCommunication() {
		commucication_bus.register(this);
	}

	public static OTTOCommunication getInstance() {
		if (instance == null) {
			instance = new OTTOCommunication();
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


	public LatLng getEnd_location() {
		return end_location;
	}

	public StopObject getPick_up_stop() {
		return pick_up_stop;
	}

	public StopObject getDrop_off_stop() {
		return drop_off_stop;
	}

	public String getFrom_address() {
		return from_address;
	}

	public String getTo_address() {
		return to_address;
	}

	public void setStart_location(int sender,LatLng start_location) {
		commucication_bus.post(new StartLocationChangeEvent(sender,start_location));
	}
	
	public void setEnd_location(int sender,LatLng end_location) {
		commucication_bus.post(new EndLocationChangeEvent(sender,end_location));
	}

	public void setPick_up_stop(int sender, StopObject pick_up_stop) {
		commucication_bus.post(new PickUpChangeEvent(sender,pick_up_stop));
	}

	public void setDrop_off_stop(int sender, StopObject drop_off_stop) {
		commucication_bus.post(new DropOffChangeEvent(sender,drop_off_stop));
	}

	public void setFrom_address(int sender, String from_address) {
		commucication_bus.post(new FromAddressChangeEvent(sender,from_address));
	}

	public void setTo_address(int sender, String to_address) {
		commucication_bus.post(new ToAddressChangeEvent(sender,to_address));
	}

	public void register(Object subscriber)
	{
		commucication_bus.register(subscriber);
	}

	
}
