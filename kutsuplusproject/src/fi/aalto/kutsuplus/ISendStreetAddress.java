package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;

import fi.aalto.kutsuplus.kdtree.StopObject;

public interface ISendStreetAddress {
	public void fillFromToTextBox(String streetAddress);	
	public void fillSelectedMapLocation(LatLng address_gps);
	public void setPickupDropoff(StopObject so);
}
