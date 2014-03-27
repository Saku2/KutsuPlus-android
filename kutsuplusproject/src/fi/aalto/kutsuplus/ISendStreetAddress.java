package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import fi.aalto.kutsuplus.kdtree.StopObject;

public interface ISendStreetAddress {
	public void setMapLocationSelection(String streetAddress,LatLng address_gps);
	public void setStopMarkerSelection(StopObject so,LatLng address_gps, Marker marker);
	public void setFocusOnFromField();
	public void setFocusOnToField();
}
