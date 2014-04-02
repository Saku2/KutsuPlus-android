package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class StartLocationEvent {
	final private LatLng location;

	public StartLocationEvent(LatLng location) {
		super();
		this.location = location;
	}

	public LatLng getLocation() {
		return location;
	}
	
}
