package fi.aalto.kutsuplus.utils;

import fi.aalto.kutsuplus.kdtree.GoogleMapPoint;
import fi.aalto.kutsuplus.kdtree.MapPoint;

public class CoordinateConverter {
	
	// Adapted from http://aapo.rista.net/tmp/coordinates.py
	static public MapPoint wGS84lalo_to_kkj2(double lo,double la)
	{
		  double dLa = Math.toRadians( -1.24766  + 0.269941 * la  - 0.191342 * lo  - 0.00356086 * la * la + 0.00122353 * la * lo +
				  0.000335456 * lo * lo ) / 3600.0;
		  
		  double dLo = Math.toRadians(  28.6008  - 1.14139 * la +  0.581329 * lo +  0.0152376 * la * la - 0.0118166 * la * lo - 0.000826201 * lo * lo ) / 3600.0;
		  

		  
		  double kkj_la = Math.toDegrees(Math.toRadians(la) + dLa);
		  double kkj_lo = Math.toDegrees(Math.toRadians(lo) + dLo);
		  
		  return kkj2alo_to_kkj2xy(kkj_lo,kkj_la);
		  
	}

	static private MapPoint kkj2alo_to_kkj2xy(double kkj_lo, double kkj_la)
	{
		  double Lo =  Math.toRadians(kkj_lo) - Math.toRadians(24);

		  double a  = 6378388.0;//            # Hayford ellipsoid
		  double f  = 1/297.0;

		  double b  = (1.0 - f) * a;
		  double bb = b * b;              
		  double c  = (a / b) * a;        
		  double ee = (a * a - bb) / bb;  
		  double n = (a - b)/(a + b);     
		  double nn = n * n;              

		  double cosLa = Math.cos(Math.toRadians(kkj_la));

		  double NN = ee * cosLa * cosLa; 

		  double LaF = Math.atan(Math.tan(Math.toRadians(kkj_la)) / Math.cos(Lo * Math.sqrt(1 + NN)));

		  double cosLaF = Math.cos(LaF);

		  double t   = (Math.tan(Lo) * cosLaF) / Math.sqrt(1 + ee * cosLaF * cosLaF);

		  double A   = a / ( 1 + n );

		  double A1  = A * (1 + nn / 4 + nn * nn / 64);

		  double A2  = A * 1.5 * n * (1 - nn / 8);

		  double A3  = A * 0.9375 * nn * (1 - nn / 4);

		  double A4  = A * 35/48.0 * nn * n;

		  double p= A1 * LaF - A2 * Math.sin(2 * LaF) +  A3 * Math.sin(4 * LaF) - A4 * Math.sin(6 * LaF);
		  double i=	 c * Math.log(t + Math.sqrt(1+t*t)) + 500000.0 + 2 * 1000000.0;
		  return new MapPoint ((int)i,(int) p);

	}
	

	// Adapted from http://aapo.rista.net/tmp/coordinates.py
	/*
	 *  Input:     dictionary with kkj_p is KKJ Northing
	                               kkj_i in KKJ Eeasting
        Output:    GoogleMapPoint dictionary with ['La'] is latitude in degrees (WGS84)
                                    			  ['Lo'] is longitude in degrees (WGS84)
       Scan iteratively the target area, until find matching
       KKJ coordinate value.  Area is defined with Hayford Ellipsoid.
	 */
	static public GoogleMapPoint kkj2xy_to_wGS84lalo(double kkj_i,double kkj_p)
	{
		
		double MinLa = Math.toRadians(59.0f);
		double MaxLa = Math.toRadians(70.5f);
		double MinLo = Math.toRadians(18.5f);
	    double MaxLo = Math.toRadians(32.0f);

	    double la=0, lo=0;
	    
		int i = 1;
		while (i < 35)
		{
				    double DeltaLa = MaxLa - MinLa;
				    double DeltaLo = MaxLo - MinLo;

				    la = Math.toDegrees(MinLa + 0.5 * DeltaLa);
				    lo = Math.toDegrees(MinLo + 0.5 * DeltaLo);

				    MapPoint KKJt = kkj2alo_to_kkj2xy(lo, la);

				    if (KKJt.getY() < kkj_p)
				      MinLa = MinLa + 0.45 * DeltaLa;
				    else
				      MaxLa = MinLa + 0.55 * DeltaLa;

				    if (KKJt.getX() < kkj_i)
				      MinLo = MinLo + 0.45 * DeltaLo;
				    else
				      MaxLo = MinLo + 0.55 * DeltaLo;

				    i = i + 1;
		}
	   double dLa = Math.toRadians( 1.24867  + -0.269982 *la +	0.19133 * lo + 	0.00356119 * la * la - 0.00122312 * la * lo - 0.000335514 * lo * lo ) / 3600.0;
       double dLo = Math.toRadians( -28.6111 +  1.14183 * la  - 0.581428 * lo - 0.0152421 * la * la + 0.0118177 * la * lo +  0.000826646 * lo * lo ) / 3600.0;
       double rLa=Math.toDegrees(Math.toRadians(la) + dLa);
       double rLo=Math.toDegrees(Math.toRadians(lo) + dLo);

	   return new GoogleMapPoint (rLa,rLo);
	}

}


