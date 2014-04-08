package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class StartLocationChangeEvent {
	final private int sender;
	final private LatLng location;

	public StartLocationChangeEvent(int sender,LatLng location) {
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
		return "StartLocationChangeEvent [sender=" + sender + ", location=" + location + "]";
	}
	
	
}
