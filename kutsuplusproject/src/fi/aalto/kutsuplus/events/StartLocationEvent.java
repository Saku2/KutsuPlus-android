package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class StartLocationEvent {
	final private Object sender;
	final private LatLng location;

	public StartLocationEvent(Object sender,LatLng location) {
		super();
		this.sender=sender;
		this.location = location;
	}
	
	
	public Object getSender() {
		return sender;
	}

	public LatLng getLocation() {
		return location;
	}
	
}
