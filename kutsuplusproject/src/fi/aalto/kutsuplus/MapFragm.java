package fi.aalto.kutsuplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import fi.aalto.kutsuplus.events.CurrentLocationChangeEvent;
import fi.aalto.kutsuplus.events.DropOffChangeEvent;
import fi.aalto.kutsuplus.events.EndLocationChangeEvent;
import fi.aalto.kutsuplus.events.OTTOCommunication;
import fi.aalto.kutsuplus.events.PickUpChangeEvent;
import fi.aalto.kutsuplus.events.StartLocationChangeEvent;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;
import fi.aalto.kutsuplus.routes.DownloadTask;

public class MapFragm extends Fragment implements OnMarkerClickListener, OnMapClickListener, OnMarkerDragListener{
	private ISendMapSelection iSendMapSelection;
	private OTTOCommunication communication=OTTOCommunication.getInstance();

	HashMap <Marker, StopObject> markers = new HashMap <Marker, StopObject>();
	HashMap <StopObject, Marker> markers_so = new HashMap <StopObject, Marker>();

	
	private View rootView;//
	private GoogleMap map;
	private HashMap<String, Marker> startEndMarkers = new HashMap<String, Marker>(2);
	private ArrayList <Marker> startEndMarkersWatcher = new ArrayList<Marker>();
	private HashMap<String, Marker> startEndMarkers_onMapClick = new HashMap<String, Marker>(2);
	private ArrayList <Marker> startEndMarkers_onMapClick_Watcher = new ArrayList<Marker>();
	private boolean markerWasDragged = false;
	private boolean draggedStartMarker=false;
	
	//rider scrumb
	//list for scrumb polylines
	private ArrayList<Polyline> riderPolyLines = new ArrayList<Polyline>();
	private Polyline ridingScrumbPolyline = null;
	PolylineOptions ridingScrumbPolyLineOptions = null;
	Location scrumbRouteLastLocation = null;
	float scrumbRouteLength = 0;
	Marker ridingDistanceMarker = null;
	
	//default initial zoom level, when app is opened//
	final static public float initialZoomLevel = 11.5F;
	
	//min zoom level, for showing distance and duration markers
	final public float distanceDurationZoomLevel = 12F;
	public Marker marker_duration_start = null;
	public Marker marker_distance_start = null;
	public Marker marker_duration_end = null;
	public Marker marker_distance_end = null;
	
	private StopTreeHandler stopTreeHandler;
	
	private boolean KPstopsAreVisible = false;
	private boolean KPstopsAreCreated = false;

	private float markerAlpha = 0.7F;


	Polyline straightLine = null;
	private Polyline walkingToStartBusStopLine = null;
	private Polyline walkingToFinishBusStopLine = null;
	private LatLng startPoint= null;
	private LatLng endPoint = null;
	private boolean drawStartWalking = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.mapfragment, container, false);
		try
		{
        // Fix for black background on devices < 4.1
        if (android.os.Build.VERSION.SDK_INT < 
            android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setMapTransparent((ViewGroup) rootView);
        }
		// get map object
        KutsuplusSupportMapFragment mySupportMapFragment = (KutsuplusSupportMapFragment) this.getFragmentManager().findFragmentByTag("google_map");
		GoogleMap google_map = mySupportMapFragment.getMap();
        google_map.setOnMapClickListener(this);  
        google_map.setOnMarkerDragListener(this);
		google_map.setOnMarkerClickListener((OnMarkerClickListener) this);
		
		//rider scrumb polylineoptions
		ridingScrumbPolyLineOptions = new PolylineOptions();

		setMap(google_map);

        communication.register(this);
        restoretoMemory();
		}
		catch(Exception e)
		{
			// In case the map is not available at the device
			e.printStackTrace();
		}
		return rootView;
	}

    /*
     * restoretoMemory() is called to restore the values of the fragment in 
     * case Android has cleaned it from the memory.  
     */

	private void restoretoMemory()
	{
		OTTOCommunication cb=OTTOCommunication.getInstance();
		if(cb.getStart_location()!=null)
		  onStartLocationChangeEvent(new StartLocationChangeEvent(OTTOCommunication.MAIN_ACTIVITY,cb.getStart_location()));

		if(cb.getEnd_location()!=null)
			  onEndLocationChangeEvent(new EndLocationChangeEvent(OTTOCommunication.MAIN_ACTIVITY,cb.getEnd_location()));
		
		if(cb.getPick_up_stop()!=null)
			  onPickUpChangeEvent(new PickUpChangeEvent(OTTOCommunication.MAIN_ACTIVITY,cb.getPick_up_stop()));
		
		if(cb.getDrop_off_stop()!=null)
			  onDropOffChangeEvent(new DropOffChangeEvent(OTTOCommunication.MAIN_ACTIVITY,cb.getDrop_off_stop()));

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

	private void moveCamera(LatLng ll){
		CameraUpdate center = CameraUpdateFactory.newLatLngZoom(ll, map.getCameraPosition().zoom);
		map.animateCamera(center);//moveCamera
	}
	
	public void clearMap(){
		
		markerWasDragged = false;
		draggedStartMarker=false;
		startPoint= null;
		endPoint = null;
		drawStartWalking = true;
		
		//created polylines
		if(straightLine != null)
			straightLine.remove();
		if(walkingToStartBusStopLine != null)
			walkingToStartBusStopLine.remove();
		if(walkingToFinishBusStopLine != null)
			walkingToFinishBusStopLine.remove();
		//rider scrumb
		for(Polyline pl : riderPolyLines){
			pl.remove();
		}
		
		//created markers
		if(marker_duration_start != null)
			marker_duration_start.remove();
		if(marker_distance_start != null)
			marker_distance_start.remove();
		if(marker_duration_end != null)
			marker_duration_end.remove();
		if(marker_distance_end != null)
			marker_distance_end.remove();
		//rider scrumb
		if(ridingDistanceMarker != null)
			ridingDistanceMarker.remove();
		//KP markers
		this.hideKutsuPlusStopMarkers();
		
		//watchers and hashmaps
		if(startEndMarkers.get("start") != null)
			startEndMarkers.get("start").remove();
		if(startEndMarkers.get("end") != null)
			startEndMarkers.get("end").remove();
		startEndMarkers.clear();
		startEndMarkersWatcher.clear();

		if(startEndMarkers_onMapClick.get("start") != null)
			startEndMarkers_onMapClick.get("start").remove();
		if(startEndMarkers_onMapClick.get("end") != null)
			startEndMarkers_onMapClick.get("end").remove();
		startEndMarkers_onMapClick.clear();
		startEndMarkers_onMapClick_Watcher.clear();
		
		//rider scrumb
		scrumbRouteLastLocation = null;
		scrumbRouteLength = 0;
		ridingScrumbPolyLineOptions = null;
		ridingScrumbPolyLineOptions = new PolylineOptions();
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
		try
		{
		   return BitmapDescriptorFactory.fromResource(R.drawable.kp_marker);
		}
		catch(Exception e)
		{
			// If not initialized, the application will continue
			e.printStackTrace();
			return null;
		}
	}
	
	public void makeKPmarkers(){
		if(!KPstopsAreCreated){
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.icon(setKPicon());
			Collection<StopObject>pysakit = this.stopTreeHandler.getStopTree().values();
			for(StopObject so : pysakit){
				// Constructor uses (lat,long)  remember: latitude=y, longituden=x
				LatLng ll = new LatLng(so.getGmpoint().getY(),so.getGmpoint().getX());
				markerOptions.position(ll)
	 			             .title(so.getFinnishName())
				             .snippet(so.getSwedishName())
				             .flat(false)
				             .draggable(false);
				Marker marker = null;
				if(map != null){
		            marker = map.addMarker(markerOptions);
		            markers.put(marker, so);
		            markers_so.put(so, marker);
		            marker.setVisible(false);
		            marker.setAlpha(markerAlpha);
				}
	            
			}
			
			if(markers.size() > 0)
		        KPstopsAreCreated = true;
		}
	}
	
	public void addAllKutsuPlusStopMarkers(){
		if(!KPstopsAreCreated)
			makeKPmarkers();
		
		showKutsuPlusStopMarkers();
		KPstopsAreVisible = true;
	}


	@Override
	public boolean onMarkerClick(Marker marker) {
		if(marker != null)
		if(marker.getTitle().toString().equals("start"))
		{
			marker.hideInfoWindow();
			iSendMapSelection.setFromActivated();
		}
		else if(marker.getTitle().toString().equals("finish"))
		{
			marker.hideInfoWindow();
			iSendMapSelection.setToActivated();
		}
		else
		{
			if(marker != null){
				marker.showInfoWindow();
				 StopObject so = markers.get(marker);
			     if(so!=null)
			     {
			    	iSendMapSelection.setStopMarkerSelection(so, marker.getPosition());
			     }
			}
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
			 try{
					if(isStartMarker)
						marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.kp_marker_pink));
					else
						marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.kp_marker_green));
		     }
		     catch(IllegalStateException is){
		    	 // Can be happeded, when closing the application
		     }
			 catch (IllegalArgumentException e) {
				 // Can happen if clear map is clicked when updating the markers has not finished
			 }

			marker.showInfoWindow();
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
					if(!KPstopsAreVisible)
						m.setVisible(false);
				}
			}
		}
		
		if(startEndMarkers.get("start") != null && startEndMarkers.get("end") != null){
			drawStraightLineOnMap(startEndMarkers.get("start").getPosition(), startEndMarkers.get("end").getPosition());
		}
		
		if(marker != null){//
			moveCamera(marker.getPosition());
		}
	}
	
	public void updateActualPointMarker(boolean isStartMarker) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.alpha(0.9F)
			         .draggable(true)
			         .anchor(0.5F, 0.5F);
		Marker marker = null;
		if(isStartMarker){
			String satrt_loc = getString(R.string.start_click_on_map);
			Bitmap b = drawLocationIcon(R.drawable.location_start, Color.RED, 30, satrt_loc);
			markerOptions.position(startPoint);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
			markerOptions.title(satrt_loc);
			if(map != null){
				marker = map.addMarker(markerOptions);
				if(marker != null){
					startEndMarkers_onMapClick.put("start", marker);
					handlePreviousLocMarkers(marker);
				}
			}
		}
		else{
			String finish_loc = getString(R.string.finish_click_on_map);
			Bitmap b = drawLocationIcon(R.drawable.location_finish, Color.rgb(1, 88, 30), 30, finish_loc);
			markerOptions.position(endPoint);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
			markerOptions.title(finish_loc);
			if(map != null){
				marker = map.addMarker(markerOptions);
				if(marker != null){
					startEndMarkers_onMapClick.put("end", marker);
					handlePreviousLocMarkers(marker);					
				}
			}
		}
		
		if(marker != null){
			moveCamera(marker.getPosition());
			marker.hideInfoWindow();
		}
	}
	
	private void handlePreviousLocMarkers(Marker marker) {
		if(startEndMarkers_onMapClick_Watcher.size() > 0){
			Iterator<Marker> it = startEndMarkers_onMapClick_Watcher.iterator();
			while (it.hasNext()) {
				Marker wm = it.next();
				if(wm != startEndMarkers_onMapClick.get("end") && wm != startEndMarkers_onMapClick.get("start")){
					wm.remove();//.setVisible(false);
				}
			}
		}
		startEndMarkers_onMapClick_Watcher.add(marker);
	}

	@Override
	public void onMapClick(LatLng ll) {
		try {
			Locale aLocale = new Locale("fi", "FI");
	        Geocoder geo = new Geocoder(rootView.getContext().getApplicationContext(), aLocale);
	        List<Address> addresses = geo.getFromLocation(ll.latitude, ll.longitude, 1);
	        
	        if (addresses.isEmpty()) {
	        	Toast.makeText(rootView.getContext().getApplicationContext(), getString(R.string.toast_address_not_found), Toast.LENGTH_LONG).show();
	        }
	        else {
	            if (addresses.size() > 0) {
	            	Toast.makeText(rootView.getContext().getApplicationContext(), getString(R.string.toast_address_on_map_click) + " "+ addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
	            	iSendMapSelection.setMapLocationSelection( addresses.get(0).getAddressLine(0), ll);	            	
	            }
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace(); // getFromLocation() may sometimes fail
	    }
	}

	private Bitmap drawLocationIcon(int R_resource, int textColor, int TextSize, String labelText){
	    Bitmap bm = BitmapFactory.decodeResource(getResources(), R_resource).copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(bm);
		Paint paint = new Paint();
		paint.setColor(textColor);
		paint.setTextSize(TextSize);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.create("Arial Black", 0));//normal
		canvas.drawText(labelText, bm.getWidth()/2, bm.getHeight()/4, paint); // paint defines the text color, stroke width, size
		BitmapDrawable draw = new BitmapDrawable(getResources(), bm);
		Bitmap drawBmp = draw.getBitmap();
		return drawBmp;
	}

	public void setStopTreeHandler(StopTreeHandler stopTreeHandler) {
		this.stopTreeHandler = stopTreeHandler;
	}//

	public GoogleMap getMap() {
		return map;
	}

	public void setMap(GoogleMap map) {
		map.setOnCameraChangeListener(new OnCameraChangeListener(){
				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					showHideDDmarkers(cameraPosition.zoom);
				}
	      	
	      });
		
	      this.map = map;
	}
	
	public void showHideDDmarkers(float zoom){
		if(zoom  < distanceDurationZoomLevel){
			if(marker_distance_start != null)
    			marker_distance_start.setVisible(false);
    		if(marker_duration_start != null)
    			marker_duration_start.setVisible(false);
    		if(marker_distance_end != null)
    			marker_distance_end.setVisible(false);
    		if(marker_duration_end != null)
    			marker_duration_end.setVisible(false);
			
		}
		else{
			if(marker_distance_start != null)
    			marker_distance_start.setVisible(true);
    		if(marker_duration_start != null)
    			marker_duration_start.setVisible(true);
    		if(marker_distance_end != null)
    			marker_distance_end.setVisible(true);
    		if(marker_duration_end != null)
    			marker_duration_end.setVisible(true);
		}
	}


	public void drawStraightLineOnMap(LatLng startPoint, LatLng endPoint) {
		ArrayList<LatLng> points = new ArrayList<LatLng>();
		PolylineOptions polyLineOptions = new PolylineOptions();
		
		points.add(startPoint);
		points.add(endPoint);
		
		polyLineOptions.addAll(points);
		polyLineOptions.width(2);
		polyLineOptions.color(Color.RED);
		
		if(straightLine != null)
			straightLine.remove();
		
		if(map != null)
		  straightLine = map.addPolyline(polyLineOptions);
		
		
	}
	
	void drawWalkingRoute(boolean isStartMarker){
		DownloadTask downloadTask = new DownloadTask(this);
		String url="";
		if(isStartMarker && startPoint != null){
			drawStartWalking = true;
			LatLng st = this.startEndMarkers.get("start").getPosition();
			url = getDirectionsUrl(st, startPoint);
		}
		else if(!isStartMarker && endPoint != null){
			drawStartWalking = false;
			LatLng ed = this.startEndMarkers.get("end").getPosition();
			url = getDirectionsUrl(endPoint, ed);
		}
		// Start downloading json data from Google Directions API
		if(!url.isEmpty())
			downloadTask.execute(url);
	}
	

	  private String getDirectionsUrl(LatLng origin,LatLng dest){
		  
	        // Origin of route
	        String str_origin = "origin="+origin.latitude+","+origin.longitude;
	 
	        // Destination of route
	        String str_dest = "destination="+dest.latitude+","+dest.longitude;
	 
	        // Sensor enabled
	        String sensor = "sensor=false";
	        
	        //walking mode
	        String mode = "mode=walking";
	 
	        // Building the parameters to the web service
	        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+mode;
	 
	        // Output format
	        String output = "json";
	 
	        // Building the url to the web service
	        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
	 
	        return url;
	    }

	@Override
	public void onMarkerDrag(Marker m) {
		map.animateCamera(CameraUpdateFactory.newLatLng(m.getPosition()));
	}

	@Override
	public void onMarkerDragEnd(Marker m) {
		String satrt_loc = getString(R.string.start_click_on_map);
		if(m.getTitle().equals(satrt_loc))
			draggedStartMarker = true;
		else
			draggedStartMarker = false;
		
		markerWasDragged = true;
		onMapClick(m.getPosition());
	}

	@Override
	public void onMarkerDragStart(Marker m) {

	}
	
	public void updateMarkersAndRoute(LatLng ll, StopObject busstop,boolean isStartMarker) {
		
		/*boolean isStartMarker = true;
		if(markerWasDragged){
			if(draggedStartMarker)
				isStartMarker = true;
			else
				isStartMarker = false;
			
			markerWasDragged = false;
		}
		else *
		if(focusAtFrom){
			isStartMarker = true;
		}		
		else{//
			isStartMarker = false;
		}*/
		
		
		if(isStartMarker)
		{
			communication.setStart_location(OTTOCommunication.MAP_FRAGMENT,ll);
			communication.setPick_up_stop(OTTOCommunication.MAP_FRAGMENT,busstop);
			startPoint = ll;
		}
		else
		{
			communication.setEnd_location(OTTOCommunication.MAP_FRAGMENT,ll);
			communication.setDrop_off_stop(OTTOCommunication.MAP_FRAGMENT,busstop);
			endPoint = ll;
		}
		
		if(!KPstopsAreCreated){
			makeKPmarkers();
		}
	
		Marker busstop_marker = markers_so.get(busstop);
        if(busstop_marker!=null)
        {
		   updatePinkMarker(busstop_marker, isStartMarker);
        }
		updateActualPointMarker(isStartMarker);
		drawWalkingRoute(isStartMarker);

	}
	
	
	
	@SuppressWarnings("static-access")
	public void updateRidingScrumbPolyline(Location location){
		LatLng lat  = new LatLng(location.getLatitude(), location.getLongitude());
		ridingScrumbPolyLineOptions.color(Color.RED);
		ridingScrumbPolyLineOptions.width(6);//
		ridingScrumbPolyLineOptions.add(lat);
		
		//track distance
		float[] results = new float[1];
		if(scrumbRouteLastLocation != null)
			location.distanceBetween(scrumbRouteLastLocation.getLatitude(), scrumbRouteLastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), results);
		scrumbRouteLastLocation = location;
		if(results[0] != 0){
			scrumbRouteLength += (results[0]);
			float kilometers = scrumbRouteLength/1000;
			int integerPart=(int)kilometers; 
			String decimalPart_tmp = ("" + scrumbRouteLength%1000);
			int dotLoc = decimalPart_tmp.indexOf(".");
			String decimalPart = decimalPart_tmp.substring(0, dotLoc+2);
			String distanceStr = "";
			if(kilometers >= 1)
				distanceStr = integerPart + "km " + decimalPart + "m";
			else
				distanceStr = decimalPart + "m";
				
			Log.d("distance so far: ", scrumbRouteLength + "");
			Bitmap dis_bmp = drawTextMarkerOnMap(Color.RED, 35, distanceStr);
			MarkerOptions markerOptions_dis = new MarkerOptions();
	        markerOptions_dis.icon(BitmapDescriptorFactory.fromBitmap(dis_bmp));
	        markerOptions_dis.position(new LatLng(location.getLatitude(), location.getLongitude()));
	        
	        if(ridingDistanceMarker != null){
	        	ridingDistanceMarker.remove();
	        }
	        ridingDistanceMarker = getMap().addMarker(markerOptions_dis);
		}
		
		//add route
		if(map != null){
			this.ridingScrumbPolyline = map.addPolyline(ridingScrumbPolyLineOptions);
			riderPolyLines.add(ridingScrumbPolyline);
			moveCamera(lat);
		}
	}
	
	
	public Bitmap drawTextMarkerOnMap(int textColor, int TextSize, String labelText){
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
 	    Bitmap bmpText = Bitmap.createBitmap(270,100, conf);
 	    
 	    Canvas canvas = new Canvas(bmpText);
 		Paint paint = new Paint();
 		paint.setColor(textColor);
 		paint.setTextSize(TextSize);
 		paint.setTextAlign(Align.CENTER);
 		paint.setShadowLayer(8, 0, 0, Color.GRAY);
 		paint.setTypeface(Typeface.create("Arial Black", 0));//normal
 		canvas.drawText(labelText, bmpText.getWidth()/2, bmpText.getHeight()/4, paint); // paint defines the text color, stroke width, size
 		BitmapDrawable draw = new BitmapDrawable(getResources(), bmpText);
 		Bitmap drawBmp = draw.getBitmap();
 		return drawBmp;
	}

	// Otto-framework related things
	
	@Subscribe
    public void onCurrentLocationChangeEvent(CurrentLocationChangeEvent event){
    	if(event.getSender()!=OTTOCommunication.MAP_FRAGMENT)
    	{
    		// The code to show the user location here
    	}
    }
	
    @Subscribe
    public void onStartLocationChangeEvent(StartLocationChangeEvent event){
    	if(event.getSender()!=OTTOCommunication.MAP_FRAGMENT)
    	{
    		startPoint = event.getLocation();	
    		if(startPoint != null && endPoint != null){
    			try
    			{
    			  drawStraightLineOnMap(startPoint, endPoint);
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
    		}
    	}
    }

    @Subscribe
    public void onEndLocationChangeEvent(EndLocationChangeEvent event){
    	if(event.getSender()!=OTTOCommunication.MAP_FRAGMENT)
    	{
            endPoint= event.getLocation();
            if(startPoint != null && endPoint != null){
    			drawStraightLineOnMap(startPoint, endPoint);
    		}
    	}
    }

    @Subscribe
    public void onPickUpChangeEvent(PickUpChangeEvent event){
    	if(event.getSender()!=OTTOCommunication.MAP_FRAGMENT)
    	{
    		try
    		{
    		if(!KPstopsAreCreated){
    			makeKPmarkers();
    		}
    		Marker busstop_marker = markers_so.get(event.getBus_stop());
            if(busstop_marker!=null)
            {
    		   updatePinkMarker(busstop_marker, true);
            }
    		updateActualPointMarker(true);
    		drawWalkingRoute(true);
    		}
    		catch(IllegalStateException is)
    		{
    			//This can be hapend at the beginning, when the map is not ready and the first messages are sent
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }

    @Subscribe
    public void onDropOffChangeEvent(DropOffChangeEvent event){
    	if(event.getSender()!=OTTOCommunication.MAP_FRAGMENT)
    	{
    		try
    		{
    		 if(!KPstopsAreCreated){
    			makeKPmarkers();
    		 }
    		 Marker busstop_marker = markers_so.get(event.getBus_stop());
             if(busstop_marker!=null)
             {
    		   updatePinkMarker(busstop_marker, false);
             }
    		 updateActualPointMarker(false);
    		 drawWalkingRoute(false);
    		}
    		catch(IllegalStateException is)
    		{
    			// Can be caused then events are send when map is not yet ready
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }

	public boolean isDraggedStartMarker() {
		return draggedStartMarker;
	}

	public LatLng getEndPoint() {
		return endPoint;
	}

	public boolean isDrawStartWalking() {
		return drawStartWalking;
	}

	public boolean isKPstopsAreVisible() {
		return KPstopsAreVisible;
	}

	public boolean isKPstopsAreCreated() {
		return KPstopsAreCreated;
	}

	public Polyline getWalkingToFinishBusStopLine() {
		return walkingToFinishBusStopLine;
	}

	public LatLng getStartPoint() {
		return startPoint;
	}

	public boolean isMarkerWasDragged() {
		return markerWasDragged;
	}

	public Polyline getWalkingToStartBusStopLine() {
		return walkingToStartBusStopLine;
	}

	public void setWalkingToStartBusStopLine(Polyline walkingToStartBusStopLine) {
		this.walkingToStartBusStopLine = walkingToStartBusStopLine;
	}


	public void setWalkingToFinishBusStopLine(Polyline walkingToFinishBusStopLine) {
		this.walkingToFinishBusStopLine = walkingToFinishBusStopLine;
	}

	
    
}
