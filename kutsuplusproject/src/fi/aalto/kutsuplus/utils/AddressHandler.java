package fi.aalto.kutsuplus.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

public class AddressHandler {
 static public String getAdresss(Context context,LatLng location)
 {
					Locale aLocale = new Locale("fi", "FI");
			        Geocoder geo = new Geocoder(context, aLocale);
			        List<Address> addresses;
					try {
						addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
			            if (addresses.size() > 0) {
			            	return addresses.get(0).getAddressLine(0);	            	
			            }
					} catch (IOException e) {
						e.printStackTrace();
					}		     
					return null;
 }
}
