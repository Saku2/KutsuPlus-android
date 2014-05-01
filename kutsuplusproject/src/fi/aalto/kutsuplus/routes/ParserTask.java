package fi.aalto.kutsuplus.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import fi.aalto.kutsuplus.MapFragm;
import fi.aalto.kutsuplus.R;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{



MapFragm mapF = null;

public ParserTask( MapFragm mapF_){
	this.mapF = mapF_;
	}
	
	@Override
	protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
		 JSONObject jObject;
         List<List<HashMap<String, String>>> routes = null;

         try{
             jObject = new JSONObject(jsonData[0]);
             DirectionsJSONParser parser = new DirectionsJSONParser();

             // Starts parsing data
             routes = parser.parse(jObject);
         }catch(Exception e){
             e.printStackTrace();
         }
         return routes;
	}
	
	 // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        String distance = "";
        String duration = "";

        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                if(j==0){    // Get distance from the list
                    distance = (String)point.get("distance");
                    continue;
                }else if(j==1){ // Get duration from the list
                    duration = (String)point.get("duration");
                    continue;
                }
                
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.GREEN);
        }

        try
        {
         drawDistancesAndLines(distance, duration, lineOptions);
        }
        catch(IllegalStateException is)
        {
        	// Can be happeded, when closing the application
        }
		 
    }        

    
    private void drawDistancesAndLines(String distance, String duration, PolylineOptions lineOptions){
        String dis_str = this.mapF.getString(R.string.walking_distance);
        String dur_str = this.mapF.getString(R.string.walking_duration);
        
        Bitmap dis_bmp = mapF.drawTextMarkerOnMap(Color.GREEN, 35, distance);
        Bitmap dur_bmp = mapF.drawTextMarkerOnMap(Color.BLUE, 35, duration);
        
        //distance
        MarkerOptions markerOptions_dis = new MarkerOptions();
        markerOptions_dis.icon(BitmapDescriptorFactory.fromBitmap(dis_bmp));
        markerOptions_dis.title(dis_str)
        			     .anchor(1, 0.5f);
        
        //duration
        MarkerOptions markerOptions_dur = new MarkerOptions();
		markerOptions_dur.icon(BitmapDescriptorFactory.fromBitmap(dur_bmp));
        markerOptions_dur.title(dur_str)
        			     .anchor(1, 0);
        
        //draw distance and duration text + lines
    	if(this.mapF.isDrawStartWalking()){
        	if(this.mapF.getWalkingToStartBusStopLine() != null)
        		this.mapF.getWalkingToStartBusStopLine().remove();
        	if(mapF.getMap()==null)
        		return;
        	// Drawing polyline in the Google Map for the i-th route
        	try
        	{
              this.mapF.setWalkingToStartBusStopLine(mapF.getMap().addPolyline(lineOptions));
        	}
        	catch(NullPointerException ne)
        	{
        		// In case this is not initialized yet
        		return;
        	}
			addDurationDistanceMarkers(markerOptions_dis, this.mapF.getStartPoint(), true, true);// mapF.getStartDistanceMarkersWatcher(), mapF.marker_distance_start);
			addDurationDistanceMarkers(markerOptions_dur, this.mapF.getStartPoint(), false, true);// mapF.getStartDurationMarkersWatcher(), mapF.marker_duration_start);
			
        }
        else if(!this.mapF.isDrawStartWalking()){
        	if(this.mapF.getWalkingToFinishBusStopLine() != null)
        		this.mapF.getWalkingToFinishBusStopLine().remove();
        	if(mapF.getMap()==null)
        		return;
        	// Drawing polyline in the Google Map for the i-th route
        	try
        	{
              this.mapF.setWalkingToFinishBusStopLine(mapF.getMap().addPolyline(lineOptions));
        	}
        	catch(NullPointerException ne)
        	{
        		//When refreshing the MainActivity and there is no map
        		return;
        	}
			addDurationDistanceMarkers(markerOptions_dis, this.mapF.getEndPoint(), true, false);//mapF.getFinishDistanceMarkersWatcher(), mapF.marker_distance_end);
			addDurationDistanceMarkers(markerOptions_dur, this.mapF.getEndPoint(), false, false);// mapF.getFinishDurationMarkersWatcher(), mapF.marker_duration_end);
        }
    }
    
    private void addDurationDistanceMarkers(MarkerOptions mOptions, LatLng latlng, boolean isDistance, boolean isStart){
        mOptions.position(latlng);
        if(isStart){
        	if(isDistance){
				removePreviousNumbers(mapF.getStartDistanceMarkersWatcher());
				mapF.marker_distance_start = mapF.getMap().addMarker(mOptions);
				mapF.getStartDistanceMarkersWatcher().add(mapF.marker_distance_start);
        	}
        	else{
				removePreviousNumbers(mapF.getStartDurationMarkersWatcher());
				mapF.marker_duration_start = mapF.getMap().addMarker(mOptions);
				mapF.getStartDurationMarkersWatcher().add(mapF.marker_duration_start);
        	}
	    }
        else{
        	if(isDistance){
				removePreviousNumbers(mapF.getFinishDistanceMarkersWatcher());
				mapF.marker_distance_end = mapF.getMap().addMarker(mOptions);
				mapF.getFinishDistanceMarkersWatcher().add(mapF.marker_distance_end);
        	}
        	else{
				removePreviousNumbers(mapF.getFinishDurationMarkersWatcher());
				mapF.marker_duration_end = mapF.getMap().addMarker(mOptions);
				mapF.getFinishDurationMarkersWatcher().add(mapF.marker_duration_end);
        	}
        }
		mapF.showHideDDmarkers(mapF.getMap().getCameraPosition().zoom);
    }
    
   
    private void removePreviousNumbers(ArrayList<Marker> alist){
    	for(Marker m_old : alist){
			m_old.remove();
		}
    }
    
    

}
