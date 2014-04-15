package fi.aalto.kutsuplus.routs;

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

        drawDistancesAndLines(distance, duration, lineOptions);
		 
    }        

    
    private void drawDistancesAndLines(String distance, String duration, PolylineOptions lineOptions){
        String dis_str = this.mapF.getString(R.string.walking_distance);
        String dur_str = this.mapF.getString(R.string.walking_duration);
        
        Bitmap dis_bmp = drawTextMarkerOnMap(Color.GREEN, 35, distance);
        Bitmap dur_bmp = drawTextMarkerOnMap(Color.BLUE, 35, duration);
        
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
        	// Drawing polyline in the Google Map for the i-th route
            this.mapF.setWalkingToStartBusStopLine(mapF.getMap().addPolyline(lineOptions));
            
            //draw distance text
            LatLng disPoint = new LatLng(this.mapF.getStartPoint().latitude, this.mapF.getStartPoint().longitude);
            markerOptions_dis.position(disPoint);
			removePreviousNumbers( mapF.getStartDistanceMarkersWatcher());
			Marker marker = mapF.getMap().addMarker(markerOptions_dis);
			mapF.getStartDistanceMarkersWatcher().add(marker);
			
			//draw duration text
            markerOptions_dur.position(this.mapF.getStartPoint());
			removePreviousNumbers( mapF.getStartDurationMarkersWatcher());
			marker = mapF.getMap().addMarker(markerOptions_dur);
			mapF.getStartDurationMarkersWatcher().add(marker);
			
        }
        else if(!this.mapF.isDrawStartWalking()){
        	if(this.mapF.getWalkingToFinishBusStopLine() != null)
        		this.mapF.getWalkingToFinishBusStopLine().remove();
        	// Drawing polyline in the Google Map for the i-th route
            this.mapF.setWalkingToFinishBusStopLine(mapF.getMap().addPolyline(lineOptions));
            
            //draw distance text
            LatLng disPoint = new LatLng(this.mapF.getEndPoint().latitude, this.mapF.getEndPoint().longitude);
            markerOptions_dis.position(disPoint);
			removePreviousNumbers( mapF.getFinishDistanceMarkersWatcher());
			Marker marker = mapF.getMap().addMarker(markerOptions_dis);
			mapF.getFinishDistanceMarkersWatcher().add(marker);
			
			//draw duration text
            markerOptions_dur.position(this.mapF.getEndPoint());
			removePreviousNumbers( mapF.getFinishDurationMarkersWatcher());
			marker = mapF.getMap().addMarker(markerOptions_dur);
			mapF.getFinishDurationMarkersWatcher().add(marker);
        }
    }
    
    private void removePreviousNumbers(ArrayList<Marker> alist){
    	for(Marker m_old : alist){
			m_old.remove();
		}
    }
    
    
    private Bitmap drawTextMarkerOnMap(int textColor, int TextSize, String labelText){
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
 		BitmapDrawable draw = new BitmapDrawable(this.mapF.getResources(), bmpText);
 		Bitmap drawBmp = draw.getBitmap();
 		return drawBmp;
	}

}
