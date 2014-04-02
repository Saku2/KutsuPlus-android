package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class EndLocationEvent {
	final private Object sender;
	final private LatLng location;

	public EndLocationEvent(Object sender,LatLng location) {
		super();
		this.sender=sender;
		this.location = location;
	}

	public LatLng getLocation() {
		return location;
	}

	
	public Object getSender() {
		return sender;
	}

	@Override	
	public String toString() {
		return "StartLocationEvent [location=" + location + "]";
	}
	
}
