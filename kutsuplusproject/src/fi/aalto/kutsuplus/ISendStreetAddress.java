package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;

public interface ISendStreetAddress {
	public void fillPickupDropoffTextBox(String stopName);	
	public void fillFromToTextBox(String streetAddress);	
	public void fillSelectedMapLocation(LatLng address_gps);
}
