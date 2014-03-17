package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;

public interface ISendStreetAddress {
	public void fillFromToTextBox(String stopName);	
	public void fillSelectedMapLocation(LatLng address_gps);
}
