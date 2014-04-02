package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class EndLocationEvent {
	final private LatLng location;

	public EndLocationEvent(LatLng location) {
		super();
		this.location = location;
	}

	public LatLng getLocation() {
		return location;
	}

	@Override	
	public String toString() {
		return "StartLocationEvent [location=" + location + "]";
	}
	
}
