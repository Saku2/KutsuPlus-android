package fi.aalto.kutsuplus;

import java.util.ArrayList;
import java.util.Collection;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapFragm extends Fragment implements OnMarkerClickListener{
	public ISendStopName iSendStopName;

	private View rootView;//
	public GoogleMap map;
	//list for managing markers
	final private ArrayList<Marker> markers = new ArrayList<Marker>();
	//min zoom level, for showing busstop markers
	public float minZoomLevel = 13.2F;
	public StopTreeHandler stopTreeHandler;
	private final String LOG_TAG = "PUNKTID";

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
        LatLng ll = new LatLng(centerPoint.getX(), centerPoint.getY());
		CameraUpdate center = CameraUpdateFactory.newLatLngZoom(ll, zoomLevel);
		map.moveCamera(center);
		addAllKutsuPlusStopMarkers();
		map.setOnMarkerClickListener((OnMarkerClickListener) this);
	}

	public void addAllKutsuPlusStopMarkers(){
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.kp_marker));

		Collection<StopObject>pysakkit = this.stopTreeHandler.getStopTree().values();
		for(StopObject so : pysakkit){
			LatLng ll = new LatLng(so.getGmpoint().getX(), so.getGmpoint().getY());
			markerOptions.position(ll)
 			             .title(so.getFinnishName())
			             .snippet(so.getSwedishName());
            Marker marker = map.addMarker(markerOptions);
            marker.setVisible(false);
            markers.add(marker);
            
		}
            //show markers only on large zoom level
            map.setOnCameraChangeListener(new OnCameraChangeListener(){
				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					for(Marker m : markers){
						m.setVisible(cameraPosition.zoom > minZoomLevel);
						
					}
				}
            	
            });
            
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		String stopName = marker.getTitle();
		//send data
		iSendStopName.fillFromToTextBox(stopName);
		return false;
	} 

	
	@Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        try {
        	iSendStopName = (ISendStopName ) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interface");
        }

    }
	
}
