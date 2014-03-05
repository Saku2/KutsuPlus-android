package fi.aalto.kutsuplus.database;

public class Ride {
	int _ID;
	String _StreetAddress_from;
	String _StreetAddress_to;
	
	
	public Ride() {
		super();
	}

	

	public Ride(String _StreetAddress_from, String _StreetAddress_to) {
		super();
		this._StreetAddress_from = _StreetAddress_from;
		this._StreetAddress_to = _StreetAddress_to;
	}



	public Ride(int _ID, String _StreetAddress_from, String _StreetAddress_to) {
		super();
		this._ID = _ID;
		this._StreetAddress_from = _StreetAddress_from;
		this._StreetAddress_to = _StreetAddress_to;
	}



	public int get_ID() {
		return _ID;
	}


	public void set_ID(int _ID) {
		this._ID = _ID;
	}


	public String get_StreetAddress_from() {
		return _StreetAddress_from;
	}


	public void set_StreetAddress_from(String _StreetAddress_from) {
		this._StreetAddress_from = _StreetAddress_from;
	}


	public String get_StreetAddress_to() {
		return _StreetAddress_to;
	}


	public void set_StreetAddress_to(String _StreetAddress_to) {
		this._StreetAddress_to = _StreetAddress_to;
	}
	

}
