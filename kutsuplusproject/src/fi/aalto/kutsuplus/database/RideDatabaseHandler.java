package fi.aalto.kutsuplus.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RideDatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "AaltoKutsuplus";

	// Contacts table name
	private static final String TABLE_RIDES = "rides";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_STREET_FROM = "street_address_from";
	private static final String KEY_STREET_TO = "street_address_to";

	public RideDatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RIDES_TABLE = "CREATE TABLE " + TABLE_RIDES + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_STREET_FROM
				+ " TEXT, " + KEY_STREET_TO + " TEXT)";
		db.execSQL(CREATE_RIDES_TABLE);
	}

	// Upgrading database
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RIDES);

		// Create tables again
		onCreate(db);
	}

	public void addRide(String from, String to) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_STREET_FROM, from);
		values.put(KEY_STREET_TO, to);

		// Inserting Row
		db.insert(TABLE_RIDES, null, values);
		db.close(); // Closing database connection
	}

	public void clearContent() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RIDES);

		// Create tables again
		onCreate(db);
		db.close(); // Closing database connection
	}

	public List<Ride> getAllStreetAddresses() {
		List<Ride> rides = new ArrayList<Ride>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_RIDES;
		SQLiteDatabase db=null;
		try {

			db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					Ride sa = new Ride();
					sa.set_ID(Integer.parseInt(cursor.getString(0)));
					sa.set_StreetAddress_from(cursor.getString(1));
					sa.set_StreetAddress_to(cursor.getString(2));
					rides.add(sa);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			// The database can be empty
		}
		finally{
			if(db!=null)
			  db.close(); // Closing database connection
	    }

		// return contact list
		return rides;
	}
}