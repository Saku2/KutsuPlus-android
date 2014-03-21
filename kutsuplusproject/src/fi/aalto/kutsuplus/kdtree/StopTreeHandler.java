package fi.aalto.kutsuplus.kdtree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import com.savarese.spatial.KDTree;
import com.savarese.spatial.NearestNeighbors;

public class StopTreeHandler {
	
	private static StopTreeHandler INSTANCE;
	
	private KDTree<Integer, MapPoint, StopObject> stopTree;
	private NearestNeighbors<Integer, MapPoint, StopObject> finder;
	
	private static InputStream fileStream;
	
	private boolean treeReady = false;
	
	//lists of latitudes and longitudes
	private ArrayList<Double> longitudes = new ArrayList<Double>();
	private ArrayList<Double> latitudes = new ArrayList<Double>();
	

	private StopTreeHandler() {
		
		stopTree = new KDTree<Integer, MapPoint, StopObject>(2);
		finder = new NearestNeighbors<Integer, MapPoint, StopObject>();
		addStops();
		stopTree.optimize();
		treeReady = true;
	}
	
	public static StopTreeHandler getInstance() throws Exception {

		if (fileStream == null) {
			throw new Exception("Trying to get instance first time without providing file stream");
		}
		
		return INSTANCE;
	}
	
	/**
	 * 
	 * @param stream InputStream that points to the kutsupluspysakit file
	 * in assets directory.
	 * @return Singleton instance
	 */
	public static StopTreeHandler getInstance(InputStream stream) {
		fileStream = stream;
		if (INSTANCE == null) {
			INSTANCE = new StopTreeHandler();
		}
		return INSTANCE;
	}
	
	// Read the stop list and add stops to tree.
	private void addStops() {
		
		//read the file and fill the tree
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, "ISO-8859-1"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.trim().split("#");
				//latitude  is a geographic coordinate that specifies the north-south position of a point on the Earth's surface. 
				// At the file latitude=y is first then longitude=x 
				double latitude= Double.parseDouble(data[12].trim());
				double longitude  = Double.parseDouble(data[13].trim());
				this.latitudes.add(latitude);
				this.longitudes.add(longitude);
				GoogleMapPoint gmpoint = new GoogleMapPoint(longitude, latitude);
				
				StopObject stop = new StopObject(data[0], data[1],
						data[3], data[4], data[5], data[6], gmpoint);
				MapPoint p = new MapPoint(Integer.parseInt(data[10]), Integer.parseInt(data[11]));
				stopTree.put(p, stop);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param p: Point for which neighbors are searched
	 * @param n: N closest neighbors are returned
	 * @return Array of NearestNeighbors.Entry -objects.
	 * Use NearestNeighbors.Entry.getNeighbor() to get
	 * point-value mapping of the stop.
	 * @throws TreeNotReadyException 
	 */
	public NearestNeighbors.Entry<Integer, MapPoint, StopObject>[] getClosestStop(MapPoint p, int n) throws TreeNotReadyException {		
		if (!treeReady) {
			throw new TreeNotReadyException("Tree is not ready yet");
		}
		return finder.get(stopTree, p, n);
	}

    public GoogleMapPoint findInitialCenter(){
    	Collections.sort(latitudes);
    	Collections.sort(longitudes);
		double minLatitude = this.latitudes.get(0);
		double maxLatitude = this.latitudes.get(latitudes.size()-1);
		double minLongitude = this.longitudes.get(0);
		double maxLongitude = this.longitudes.get(longitudes.size()-1);
		double lat_center = (minLatitude + maxLatitude)/2;
		double lon_center = (minLongitude + maxLongitude)/2;
		return new GoogleMapPoint(lon_center, lat_center);
	}
	
	
	///getters-setters
	public static StopTreeHandler getINSTANCE() {
		return INSTANCE;
	}

	public static void setINSTANCE(StopTreeHandler iNSTANCE) {
		INSTANCE = iNSTANCE;
	}

	public KDTree<Integer, MapPoint, StopObject> getStopTree() {
		return stopTree;
	}

	public void setStopTree(KDTree<Integer, MapPoint, StopObject> stopTree) {
		this.stopTree = stopTree;
	}

	public NearestNeighbors<Integer, MapPoint, StopObject> getFinder() {
		return finder;
	}

	public void setFinder(NearestNeighbors<Integer, MapPoint, StopObject> finder) {
		this.finder = finder;
	}

	public static InputStream getFileStream() {
		return fileStream;
	}

	public static void setFileStream(InputStream fileStream) {
		StopTreeHandler.fileStream = fileStream;
	}

	public boolean isTreeReady() {
		return treeReady;
	}

	public void setTreeReady(boolean treeReady) {
		this.treeReady = treeReady;
	}
	
}
