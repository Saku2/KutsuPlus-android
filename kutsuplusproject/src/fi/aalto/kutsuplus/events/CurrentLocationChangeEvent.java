package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class CurrentLocationChangeEvent {
	final private int sender;
	final private LatLng location;

	public CurrentLocationChangeEvent(int sender,LatLng location) {
		super();
		this.sender=sender;
		this.location = location;
	}
	
	
	public int getSender() {
		return sender;
	}

	public LatLng getLocation() {
		return location;
	}


	@Override
	public String toString() {
		return "CurrentLocationChangeEvent [sender=" + sender + ", location=" + location + "]";
	}
	
	
}
