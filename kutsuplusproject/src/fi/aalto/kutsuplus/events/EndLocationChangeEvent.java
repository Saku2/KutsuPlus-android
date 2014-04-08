package fi.aalto.kutsuplus.events;

import com.google.android.gms.maps.model.LatLng;

public class EndLocationChangeEvent {
	final private int sender;
	final private LatLng location;

	public EndLocationChangeEvent(int sender,LatLng location) {
		super();
		this.sender=sender;
		this.location = location;
	}

	public LatLng getLocation() {
		return location;
	}

	
	public int getSender() {
		return sender;
	}

	@Override
	public String toString() {
		return "EndLocationChangeEvent [sender=" + sender + ", location=" + location + "]";
	}

	
}
