package fi.aalto.kutsuplus.database;


public class StreetAddress {
    
    int _ID;
    String _StreetAddress;
    
    
    public StreetAddress() {
		super();
	}
    
	public StreetAddress(int id, String streetAddress) {
		super();
		this._ID = id;
		this._StreetAddress = streetAddress;
	}
	
	public StreetAddress(String streetAddress) {
		super();
		this._StreetAddress = streetAddress;
	}
	
	public int get_ID() {
		return _ID;
	}
	public void set_ID(int _ID) {
		this._ID = _ID;
	}
	public String get_StreetAddress() {
		return _StreetAddress;
	}
	public void set_StreetAddress(String _StreetAddress) {
		this._StreetAddress = _StreetAddress;
	}
    
}
         
   