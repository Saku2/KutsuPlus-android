package fi.aalto.kutsuplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import android.app.Activity;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
 
 

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;

public class MapFragm extends Fragment implements OnMarkerClickListener, OnMapClickListener{
	private ISendMapSelection iSendMapSelection;
	HashMap <Marker, StopObject> markers = new HashMap <Marker, StopObject>();

	
	private View rootView;//
	private GoogleMap map;
	public HashMap<String, Marker> startEndMarkers = new HashMap<String, Marker>(2);
	ArrayList <Marker> startEndMarkersWatcher = new ArrayList<Marker>();
	//default initial zoom level, when app is opened
	final public float initialZoomLevel = 11.5F;
	//min zoom level, for showing busstop markers
	final public float minZoomLevel = 13.2F;
	private StopTreeHandler stopTreeHandler;
	
	public boolean KPstopsAreVisible = false;
	public boolean KPstopsAreCreated = false;

	private float markerAlpha = 0.5F;


	List<Polyline> routeLines = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.mapfragment, container, false);
        // Fix for black background on devices < 4.1
        if (android.os.Build.VERSION.SDK_INT < 
            android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setMapTransparent((ViewGroup) rootView);
        }
		return rootView;
	}

	private void setMapTransparent(ViewGroup group) {
	    int childCount = group.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	        View child = group.getChildAt(i);
	        if (child instanceof ViewGroup) {
	            setMapTransparent((ViewGroup) child);
	        } else if (child instanceof SurfaceView) {
	            child.setBackgroundColor(0x00000000);
	        }  
	    }
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
		map.setOnMarkerClickListener((OnMarkerClickListener) this);
        map.setOnMapClickListener(this);  
	}

	public void hideKutsuPlusStopMarkers(){
		for (Marker m : markers.keySet()) {
		    m.setVisible(false);
		}
		Marker sm = startEndMarkers.get("start");
		if(sm != null){sm.setVisible(true);}
		Marker em = startEndMarkers.get("end");
		if(em != null){em.setVisible(true);}
		
		KPstopsAreVisible = false;
	}
	public void showKutsuPlusStopMarkers(){
		for (Marker m : markers.keySet()) {
		    if(m != startEndMarkers.get("start") || m != startEndMarkers.get("end"))
	        	m.setVisible(true);
		}
		KPstopsAreVisible = true;
	}
	
	private BitmapDescriptor setKPicon(){
		BitmapDescriptor bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		return bd;
	}
	
	public void addAllKutsuPlusStopMarkers(){//
		MarkerOptions markerOptions = new MarkerOptions();
		//Hue h = new Hue();
		markerOptions.icon(setKPicon());

		Collection<StopObject>pysakit = this.stopTreeHandler.getStopTree().values();
		for(StopObject so : pysakit){
			// Constructor uses (lat,long)  remember: latitude=y, longituden=x
			LatLng ll = new LatLng(so.getGmpoint().getY(),so.getGmpoint().getX());
			markerOptions.position(ll)
 			             .title(so.getFinnishName())
			             .snippet(so.getSwedishName());
            Marker marker = map.addMarker(markerOptions);
            markers.put(marker, so);
            marker.setVisible(true);
            marker.setAlpha(markerAlpha);
            //markers.put(ll, marker);
            
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
		
		//send data
		StopObject so = markers.get(marker);
	    if(so!=null)
	    {
	    	String stopName = marker.getTitle();
		    LatLng pos = marker.getPosition();
    	    iSendMapSelection.setStopMarkerSelection(so, marker.getPosition(), marker);
	    }
	    else
	    {
	    	// A route marker was clicked
	    	if(marker.getTitle().equals("start") || marker.getTitle().equals("pickup stop"))
	    		iSendMapSelection.setFocusOnFromField();
	    	if(marker.getTitle().equals("end") || marker.getTitle().equals("dropoff stop"))
	    		iSendMapSelection.setFocusOnToField();
	    	
	    }
		return false;
	} 

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	iSendMapSelection = (ISendMapSelection ) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interface");
        }

    }
	
	public void updatePinkMarker(Marker marker, boolean isStartMarker) {
		
		if(marker == null)
			return;
		
		if(isStartMarker){
			startEndMarkers.put("start", marker);
		}
		else{
			startEndMarkers.put("end", marker);
		}
		//new pink marker
		if(marker != null){
			marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
			marker.setAlpha(1);
			marker.setVisible(true);
			startEndMarkersWatcher.add(marker);
		}
		
		//handle previously pinked markers
		if(startEndMarkersWatcher.size() > 0){
			Iterator<Marker> it = startEndMarkersWatcher.iterator();
			while (it.hasNext()) {
				Marker m = it.next();
				if(m != startEndMarkers.get("end") && m != startEndMarkers.get("start")){
					m.setIcon(setKPicon());
					m.setAlpha(this.markerAlpha);
					it.remove();
				}
			}
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
	            	iSendMapSelection.setMapLocationSelection( addresses.get(0).getAddressLine(0),ll);
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
	}//


	
	Marker routeStart_marker=null;
	Marker routePickup_marker=null;
	Marker routeDropoff_marker=null;
	Marker routeEnd_marker=null;

	public void addRouteLineOnMap(LatLng startPoint,LatLng pickupPoint,LatLng dropoffPoint, LatLng endPoint) {
		hideKutsuPlusStopMarkers();
		
		if(routeLines != null)
		{
			for(Polyline p:routeLines)
				p.remove();
			routeLines.clear();
		}
		else
		{
			routeLines=new ArrayList<Polyline>();
		}
		
		// From pickup point to dropoff
		
		ArrayList<LatLng> points_toPickup = new ArrayList<LatLng>();
		PolylineOptions polyLineOptions_toPickup = new PolylineOptions();
		
		points_toPickup.add(pickupPoint);
		points_toPickup.add(dropoffPoint);
		
		polyLineOptions_toPickup.addAll(points_toPickup);
		polyLineOptions_toPickup.width(2);
		polyLineOptions_toPickup.color(Color.RED);
		
		routeLines.add(map.addPolyline(polyLineOptions_toPickup));

		addRouteLineStartOnMap_markings(startPoint,pickupPoint);
		addRouteLineEndOnMap_markings(dropoffPoint,endPoint);
		this.routePickup_marker.setVisible(true);	
		this.routeDropoff_marker.setVisible(true);	

	}
	public void addRouteLineStartOnMap(LatLng startPoint,LatLng pickupPoint) {
		hideKutsuPlusStopMarkers();
		
		if(routeLines != null)
		{
			for(Polyline p:routeLines)
				p.remove();
			routeLines.clear();
		}
		else
		{
			routeLines=new ArrayList<Polyline>();
		}

		addRouteLineStartOnMap_markings(startPoint,pickupPoint);
	}
	
	private void addRouteLineStartOnMap_markings(LatLng startPoint,LatLng pickupPoint) {

		// From start to pickup point
		
		ArrayList<LatLng> points_toPickup = new ArrayList<LatLng>();
		PolylineOptions polyLineOptions_toPickup = new PolylineOptions();
		
		points_toPickup.add(startPoint);
		points_toPickup.add(pickupPoint);
		
		polyLineOptions_toPickup.addAll(points_toPickup);
		polyLineOptions_toPickup.width(2);
		polyLineOptions_toPickup.color(Color.BLACK);
		
		routeLines.add(map.addPolyline(polyLineOptions_toPickup));

		
		// http://mapicons.nicolasmollet.com/category/markers/transportation/
		if(routeStart_marker==null)
		{
		  MarkerOptions markerOptions_start = new MarkerOptions();
		  markerOptions_start.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.direction_down));
	      markerOptions_start.position(startPoint);
          this.routeStart_marker = map.addMarker(markerOptions_start);
          this.routeStart_marker.setTitle("start");
          this.routeStart_marker.setVisible(true);
          startEndMarkers.put("start", routeStart_marker);
		}
		else
			this.routeStart_marker.setPosition(startPoint);	
                

		if(routePickup_marker==null)
		{
		  MarkerOptions markerOptions_start = new MarkerOptions();
		  markerOptions_start.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.busstop));
	      markerOptions_start.position(pickupPoint);
          this.routePickup_marker = map.addMarker(markerOptions_start);
          this.routePickup_marker.setTitle("pickup stop");
          this.routePickup_marker.setVisible(true);
          startEndMarkers.put("start", this.routePickup_marker);
		}
		else
			this.routePickup_marker.setPosition(pickupPoint);	

	}
	public void addRouteLineEndOnMap(LatLng dropoffPoint, LatLng endPoint) {
		hideKutsuPlusStopMarkers();
		
		if(routeLines != null)
		{
			for(Polyline p:routeLines)
				p.remove();
			routeLines.clear();
		}
		else
		{
			routeLines=new ArrayList<Polyline>();
		}
		addRouteLineEndOnMap_markings(dropoffPoint, endPoint);
	}
	
	private void addRouteLineEndOnMap_markings(LatLng dropoffPoint, LatLng endPoint) {

		// From dropoff point to the destination
		ArrayList<LatLng> points_fromDropoff = new ArrayList<LatLng>();
		PolylineOptions polyLineOptions_fromDropoff = new PolylineOptions();
				
		points_fromDropoff.add(dropoffPoint);
		points_fromDropoff.add(endPoint);
				
		polyLineOptions_fromDropoff.addAll(points_fromDropoff);
		polyLineOptions_fromDropoff.width(2);
		polyLineOptions_fromDropoff.color(Color.BLACK);
				
		routeLines.add(map.addPolyline(polyLineOptions_fromDropoff));
		
		// http://mapicons.nicolasmollet.com/category/markers/transportation/

		if(routeEnd_marker==null)
		{
			MarkerOptions markerOptions_stop = new MarkerOptions();
		
		    markerOptions_stop.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.stop));
	        markerOptions_stop.position(endPoint);
            this.routeEnd_marker = map.addMarker(markerOptions_stop);
            this.routeEnd_marker.setTitle("end");
            this.routeEnd_marker.setVisible(true);
            startEndMarkers.put("start", routeEnd_marker);
		}
		else
			this.routeEnd_marker.setPosition(endPoint);

		if(routeDropoff_marker==null)
		{
		  MarkerOptions markerOptions_start = new MarkerOptions();
		  markerOptions_start.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.busstop));
	      markerOptions_start.position(dropoffPoint);
          this.routeDropoff_marker = map.addMarker(markerOptions_start);
          this.routeDropoff_marker.setTitle("dropoff stop");
          this.routeDropoff_marker.setVisible(true);
          startEndMarkers.put("start", this.routeDropoff_marker);
		}
		else
			this.routeDropoff_marker.setPosition(dropoffPoint);	

	}

	
	// When Pickup=start point and dropoff=end
	public void addRouteLineOnMap(LatLng startPoint, LatLng endPoint) {
		hideKutsuPlusStopMarkers();

		ArrayList<LatLng> points = new ArrayList<LatLng>();
		PolylineOptions polyLineOptions = new PolylineOptions();
		
		points.add(startPoint);
		points.add(endPoint);
		
		polyLineOptions.addAll(points);
		polyLineOptions.width(2);
		polyLineOptions.color(Color.BLACK);
		
		if(routeLines != null)
		{
			for(Polyline p:routeLines)
				p.remove();
			routeLines.clear();
		}
		else
		{
			routeLines=new ArrayList<Polyline>();
		}
		routeLines.add(map.addPolyline(polyLineOptions));
		
		
		// http://mapicons.nicolasmollet.com/category/markers/transportation/
		if(routeStart_marker==null)
		{
		  MarkerOptions markerOptions_start = new MarkerOptions();
		  markerOptions_start.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.direction_down));
	      markerOptions_start.position(startPoint);
          this.routeStart_marker = map.addMarker(markerOptions_start);
          this.routeStart_marker.setTitle("start");
          this.routeStart_marker.setVisible(true);
          startEndMarkers.put("start", routeStart_marker);
		}
		else
			this.routeStart_marker.setPosition(startPoint);	
                

		if(routeEnd_marker==null)
		{
			MarkerOptions markerOptions_stop = new MarkerOptions();
		
		    markerOptions_stop.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.stop));
	        markerOptions_stop.position(endPoint);
            this.routeEnd_marker = map.addMarker(markerOptions_stop);
            this.routeEnd_marker.setTitle("end");
            this.routeEnd_marker.setVisible(true);
            startEndMarkers.put("start", routeEnd_marker);
		}
		else
			this.routeEnd_marker.setPosition(endPoint);

		
		if(routePickup_marker!=null)
			this.routePickup_marker.setVisible(false);	

		if(routeDropoff_marker!=null)
			this.routeDropoff_marker.setVisible(false);	
		
	}

	
}
