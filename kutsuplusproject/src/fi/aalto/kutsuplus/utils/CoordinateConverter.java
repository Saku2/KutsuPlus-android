package fi.aalto.kutsuplus.utils;

import fi.aalto.kutsuplus.kdtree.MapPoint;

public class CoordinateConverter {
	
	static public MapPoint toMercator(double lat,double lon) {
		  double x = lat * 20037508.34 / 180;
		  double  y = Math.log(Math.tan((90 + lon) * Math.PI / 360)) / (Math.PI / 180);
		  y = y * 20037508.34 / 180;

		  return new MapPoint ((int)x,(int) y);
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



