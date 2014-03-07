package fi.aalto.kutsuplus.kdtree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.savarese.spatial.KDTree;
import com.savarese.spatial.NearestNeighbors;

public class StopTreeHandler {
	
	private static StopTreeHandler INSTANCE;
	
	private KDTree<Integer, MapPoint, StopObject> stopTree;
	private NearestNeighbors<Integer, MapPoint, StopObject> finder;
	
	private static InputStream fileStream;
	
	private boolean treeReady = false;
	
	

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
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		String line;
		
		//read the file and fill the tree
		try {
			while ((line = br.readLine()) != null) {
				String[] data = line.trim().split("#");
				StopObject stop = new StopObject(data[0], data[1],
						data[3], data[4], data[5], data[6]);
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
	
}
