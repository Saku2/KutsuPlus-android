package fi.aalto.kutsuplus.kdtree;

public class TreeNotReadyException extends Exception {
	
	private static final long serialVersionUID = 12542623412369L;
	
	public TreeNotReadyException(String exceptionText) {
		super(exceptionText);
	}
	
}