package fi.aalto.kutsuplus.kdtree;

public class GoogleMapPoint {
	private double x;
	private double y;
	
	public GoogleMapPoint(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}

	public String toString() {
		return "GoogleMapPoint [x=" + x + ", y=" + y + "]";
	}

}
