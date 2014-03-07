package fi.aalto.kutsuplus.stops;

/**
 * A class describing a Kutsuplus stop
 * */

public class Stop {
	private String shortCode = null;
	private final String longCode;
	private final String nameFin;
	private final String nameSwe;
	private final String lon; // x
	private final String lat; // y

	public Stop(String code, String nameFin, String nameSwe, String lon, String lat) {
		this.longCode = code;
		this.nameFin = nameFin;
		this.nameSwe = nameSwe;
		this.lat = lat;
		this.lon = lon;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getLongCode() {
		return longCode;
	}

	public String getFinName() {
		return nameFin;
	}
	
	public String getSweName() {
		return nameSwe;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

}
