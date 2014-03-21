package fi.aalto.kutsuplus.utils;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;

public class CoordinateConverter {
	
	static public MapPoint toMercator(double lon,double lat) {
		  double x = lon * 20037508.34 / 180;
		  double  y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
		  y = y * 20037508.34 / 180;

		  return new MapPoint ((int)x,(int) y);
		  }

	static public GoogleMapPoint  inverseMercator (double x, double y) {
		  double lon = (x / 20037508.34) * 180;
		  double lat = (y / 20037508.34) * 180;

		  lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

		  return new GoogleMapPoint (lon, lat);
		  }
	
	public static void main(String[] args) {
		
		try
		{  
			System.out.println(toMercator(60.171270,24.956570));		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}


