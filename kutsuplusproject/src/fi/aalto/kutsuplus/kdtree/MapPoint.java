package fi.aalto.kutsuplus.kdtree;

import com.savarese.spatial.Point;

public class MapPoint implements Point<Integer> {

	private int x;
	private int y;
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public MapPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public Integer getCoord(int dimension) {
		// TODO Auto-generated method stub
		if (dimension > 1 || dimension < 0) {
			throw new IllegalArgumentException("Dimension should be 0=x or 1=y");
		}
		else if (dimension == 0) {
			return x;
		}
		
		else {
			return y;
		}
	}



	@Override
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 2;
	}

}
