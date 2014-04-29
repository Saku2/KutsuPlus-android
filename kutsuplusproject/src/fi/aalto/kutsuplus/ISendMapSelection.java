package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;

import fi.aalto.kutsuplus.kdtree.StopObject;

public interface ISendMapSelection {
	public void setMapLocationSelection(String streetAddress,LatLng address_gps);
	public void setStopMarkerSelection(StopObject so,LatLng address_gps);
	public void setFromActivated();
	public void setToActivated();
}
