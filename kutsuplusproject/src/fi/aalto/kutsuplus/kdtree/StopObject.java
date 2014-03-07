package fi.aalto.kutsuplus.kdtree;

public class StopObject {
	
	private String shortId;
	private String longId;
	private String finnishName;
	private String swedishName;
	private String finnishAddress;
	private String swedishAddress;
	
	public StopObject(String shortId, String longId, String finnishName,
			String swedishName, String finnishAddress, String swedishAddress) {
		
		this.shortId = shortId;
		this.longId = longId;
		this.finnishName = finnishName;
		this.swedishName = swedishName;
		this.finnishAddress = finnishAddress;
		this.swedishAddress = finnishAddress;
		
	}
	
	public String getShortId() {
		return this.shortId;
	}
	
	public String getLongId() {
		return this.longId;
	}
	
	public String getFinnishName() {
		return this.finnishName;
	}
	
	public String getSwedishName() {
		return this.swedishName;
	}
	
	public String getFinnishAddress() {
		return this.finnishAddress;
	}
	
	public String getSwedishAddres() {
		return this.swedishAddress;
	}
}
