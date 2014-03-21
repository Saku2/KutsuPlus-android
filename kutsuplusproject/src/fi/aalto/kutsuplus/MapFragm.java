package fi.aalto.kutsuplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;

public class MapFragm extends Fragment implements OnMarkerClickListener, OnMapClickListener{
	private ISendStreetAddress iSendSttreetAddress;
	WeakHashMap <Marker, StopObject> haspMap = new WeakHashMap <Marker, StopObject>();

	
	private View rootView;//
	private GoogleMap map;
	//list for managing markers
	final private ArrayList<Marker> markers = new ArrayList<Marker>();
	//default initial zoom level, when app is opened
	final public float initialZoomLevel = 11.5F;
	//min zoom level, for showing busstop markers
	final public float minZoomLevel = 13.2F;
	private StopTreeHandler stopTreeHandler;
	
	public boolean KPstopsAreVisible = false;
	public boolean KPstopsAreCreated = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.mapfragment, container, false);
		return rootView;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
    }

	public void updateMapView(GoogleMapPoint centerPoint, float zoomLevel) {
		// The constructor takes (lat,long), lat=y, long=x
        LatLng ll = new LatLng(centerPoint.getY(),centerPoint.getX());
		CameraUpdate center = CameraUpdateFactory.newLatLngZoom(ll, zoomLevel);
		map.animateCamera(center);//moveCamera
		//addAllKutsuPlusStopMarkers();
		map.setOnMarkerClickListener((OnMarkerClickListener) this);
        map.setOnMapClickListener(this);  
	}

	public void hideKutsuPlusStopMarkers(){
		for(Marker m : markers){
			m.setVisible(false);
		}
		KPstopsAreVisible = false;
	}
	public void showKutsuPlusStopMarkers(){
		for(Marker m : markers){
			m.setVisible(true);
		}
		KPstopsAreVisible = true;
	}
	
	public void addAllKutsuPlusStopMarkers(){//
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.kp_marker));

		Collection<StopObject>pysakit = this.stopTreeHandler.getStopTree().values();
		for(StopObject so : pysakit){
			// Constructor uses (lat,long)  remember: latitude=y, longituden=x
			LatLng ll = new LatLng(so.getGmpoint().getY(),so.getGmpoint().getX());
			markerOptions.position(ll)
 			             .title(so.getFinnishName())
			             .snippet(so.getSwedishName());
            Marker marker = map.addMarker(markerOptions);
            haspMap.put(marker, so);
            marker.setVisible(true);
            marker.setAlpha(0.5F);
            markers.add(marker);
            
		}
		KPstopsAreVisible = true;
		KPstopsAreCreated = true;
            //show markers only on large zoom level
//            map.setOnCameraChangeListener(new OnCameraChangeListener(){
//				@Override
//				public void onCameraChange(CameraPosition cameraPosition) {
//					for(Marker m : markers){
//						m.setVisible(cameraPosition.zoom >= initialZoomLevel);////minZoomLevel
//						
//					}
//				}
//            	
//            });
          
	}
	


	@Override
	public boolean onMarkerClick(Marker marker) {
		String stopName = marker.getTitle();
		//send data
		StopObject so=haspMap.get(marker);
	    if(so!=null)
	    {
    	  iSendSttreetAddress.setStopMarkerSelection(so,marker.getPosition());
	    }
		return false;
	} 

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	iSendSttreetAddress = (ISendStreetAddress ) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interface");
        }

    }

	@Override
	public void onMapClick(LatLng ll) {
		try {
			Locale aLocale = new Locale("fi", "FI");
	        Geocoder geo = new Geocoder(rootView.getContext().getApplicationContext(), aLocale);
	        //boolean isPresent = Geocoder.isPresent();
	        List<Address> addresses = geo.getFromLocation(ll.latitude, ll.longitude, 1);
	        if (addresses.isEmpty()) {
	        	Toast.makeText(rootView.getContext().getApplicationContext(), "Waiting for Location", Toast.LENGTH_LONG).show();
	        }
	        else {
	            if (addresses.size() > 0) {
	                //yourtextfieldname.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
	                //Toast.makeText(rootView.getContext().getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
	            	Toast.makeText(rootView.getContext().getApplicationContext(), "Address:- " + addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
	            	iSendSttreetAddress.setMapLocationSelection( addresses.get(0).getAddressLine(0),ll);
	            	//Toast.makeText(rootView.getContext().getApplicationContext(), "Address:- " + addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
	            	//Log.d(LOG_TAG, "after querying stop");
	            }
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace(); // getFromLocation() may sometimes fail
	    }
	}

	public void setStopTreeHandler(StopTreeHandler stopTreeHandler) {
		this.stopTreeHandler = stopTreeHandler;
	}

	public GoogleMap getMap() {
		return map;
	}

	public void setMap(GoogleMap map) {
		this.map = map;
	}

	
	
}
