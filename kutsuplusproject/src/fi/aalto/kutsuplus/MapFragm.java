package fi.aalto.kutsuplus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.savarese.spatial.KDTree;
import com.savarese.spatial.Point;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;
import fi.aalto.kutsuplus.kdtree.StopObject;
import fi.aalto.kutsuplus.kdtree.StopTreeHandler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapFragm extends Fragment {

	private View rootView;//
	public GoogleMap map;
	public StopTreeHandler stopTreeHandler;
	private final String LOG_TAG = "PUNKTID PUNKTID PUNKTID PUNKTID PUNKTID PUNKTID PUNKTID PUNKTID ";

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
        //CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
        map.moveCamera(center);
        Marker marker = map.addMarker(new MarkerOptions()
        .position(ll)
        .title("San Francisco")
        .snippet("Population: 776733"));
	}

	
	
}
