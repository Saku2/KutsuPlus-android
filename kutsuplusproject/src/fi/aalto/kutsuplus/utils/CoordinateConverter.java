package fi.aalto.kutsuplus.utils;

import fi.aalto.kutsuplus.kdtree.MapPoint;

public class CoordinateConverter {
	
	// Converted from http://aapo.rista.net/tmp/coordinates.py
	static public MapPoint wGS84lalo_to_KKJ2(double lo,double la)
	{
		  double dLa = Math.toRadians( -1.24766  + 0.269941 * la  - 0.191342 * lo  - 0.00356086 * la * la + 0.00122353 * la * lo +
				  0.000335456 * lo * lo ) / 3600.0;
		  double dLo = Math.toRadians(  28.6008  - 1.14139 * la +  0.581329 * lo +  0.0152376 * la * la - 0.0118166 * la * lo - 0.000826201 * lo * lo ) / 3600.0;
		  double kkj_la = Math.toDegrees(Math.toRadians(la) + dLa);
		  double kkj_lo = Math.toDegrees(Math.toRadians(lo) + dLo);
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

	
	
	public static void main(String[] args) {
		
		try
		{  
			//6673679#2553277#
			System.out.println(wGS84lalo_to_KKJ2(24.956570,60.171270));
					
	}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}



