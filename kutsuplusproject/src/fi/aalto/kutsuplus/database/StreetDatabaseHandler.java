package fi.aalto.kutsuplus.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StreetDatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "AaltoKutsuplus";

	// Contacts table name
	private static final String TABLE_STREETS = "streets";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_STREET_ADDRESS = "street_address";

	public StreetDatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_STREETS_TABLE = "CREATE TABLE " + TABLE_STREETS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_STREET_ADDRESS
				+ " TEXT" + ")";
		db.execSQL(CREATE_STREETS_TABLE);
	}

	// Upgrading database
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STREETS);

		// Create tables again
		onCreate(db);
	}

	public void addStreetAddress(StreetAddress sa) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_STREET_ADDRESS, sa.get_StreetAddress());

		// Inserting Row
		db.insert(TABLE_STREETS, null, values);
		db.close(); // Closing database connection
	}
	
	public void clearContent()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STREETS);

		// Create tables again
		onCreate(db);
		db.close(); // Closing database connection
	}
	
	public List<StreetAddress> getAllStreetAddresses() {
	    List<StreetAddress> addressList = new ArrayList<StreetAddress>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_STREETS;
	 
	    SQLiteDatabase db=null;
	    try
	    {
	    db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	        	StreetAddress sa = new StreetAddress();
	            sa.set_ID(Integer.parseInt(cursor.getString(0)));
	            sa.set_StreetAddress(cursor.getString(1));
	            addressList.add(sa);
	        } while (cursor.moveToNext());
	    }
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
		finally{
			if(db!=null)
			  db.close(); // Closing database connection
	    }

	    // return contact list
	    return addressList;
	}
}