package fi.aalto.kutsuplus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;

public class KutsuplusSupportMapFragment extends SupportMapFragment {
	GoogleMap google_map;

    public KutsuplusSupportMapFragment() {
        super();

    }

    public static KutsuplusSupportMapFragment newInstance() {
        KutsuplusSupportMapFragment fragment = new KutsuplusSupportMapFragment();
        return fragment;
    }

    

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
    	super.onActivityCreated(savedInstanceState);
    	google_map = getMap();
    	google_map.setMyLocationEnabled(true);
    	showHelsinkiArea();
    }

	public void updateMapView(GoogleMapPoint centerPoint, float zoomLevel) {
		// The constructor takes (lat,long), lat=y, long=x
        LatLng ll = new LatLng(centerPoint.getY(),centerPoint.getX());
		CameraPosition INIT =
                new CameraPosition.Builder()
                .target(ll)
                .zoom(zoomLevel)
                .bearing(0F) // orientation
                .tilt( 50F) // viewing angle
                .build();
		google_map.moveCamera( CameraUpdateFactory.newCameraPosition(INIT));
	}

	public void showHelsinkiArea() {
			
			// center point on map
			GoogleMapPoint centerPoint = new GoogleMapPoint(24.939029, 60.170187);
			// view-changing method in map-fragmet:
			updateMapView(centerPoint, MapFragm.initialZoomLevel);

	}

}