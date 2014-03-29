package fi.aalto.kutsuplus.routs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import fi.aalto.kutsuplus.MapFragm;

import android.graphics.Color;
import android.os.AsyncTask;

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
        MarkerOptions markerOptions = new MarkerOptions();

        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

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

        if(this.mapF.drawStartWalking){
        	if(this.mapF.walkingToStartBusStopLine != null)
        		this.mapF.walkingToStartBusStopLine.remove();
        	// Drawing polyline in the Google Map for the i-th route
            this.mapF.walkingToStartBusStopLine = mapF.getMap().addPolyline(lineOptions);
        }
        else if(!this.mapF.drawStartWalking){
        	if(this.mapF.walkingToFinishBusStopLine != null)
        		this.mapF.walkingToFinishBusStopLine.remove();
        	// Drawing polyline in the Google Map for the i-th route
            this.mapF.walkingToFinishBusStopLine = mapF.getMap().addPolyline(lineOptions);
        }
		 
        
    }

}
