package fi.aalto.kutsuplus;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import fi.aalto.kutsuplus.kdtree.StopObject;


public interface ISendFormSelection {
	public void setFromPosAndStop(LatLng address_gps, StopObject so);
	public void setToPosAndStop(LatLng address_gps, StopObject so);
}
