package ch.fenceposts.appquest.schatzkarte.database;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import ch.fenceposts.appquest.schatzkarte.database.LocationContract.LocationEntry;

public class LocationDbHelper extends SQLiteOpenHelper {
	
	private static final String DEBUG_TAG = "mydebug";
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Location.db";

	public LocationDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LocationEntry.SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is to simply to discard the data and start over
		db.execSQL(LocationEntry.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public int getDbLocationRowCount() {
		return getReadableDatabase().query(LocationEntry.TABLE_NAME, null, null, null, null, null, null).getCount();
	}

	public List<GeoPoint> getAllLocations() {
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = {
		    LocationEntry._ID,
		    LocationEntry.COLUMN_NAME_LATITUDE,
		    LocationEntry.COLUMN_NAME_LONGITUDE
		};

		// How you want the results sorted in the resulting Cursor
		String sortOrder = LocationEntry._ID + " ASC";

		Cursor cursor = getReadableDatabase().query(
			LocationEntry.TABLE_NAME,	// The table to query
			projection,                             // The columns to return
			null,									// The columns for the WHERE clause
			null,                        			// The values for the WHERE clause
			null,                                   // don't group the rows
			null,                                   // don't filter by row groups
			sortOrder                               // The sort order
		);
		
		List<GeoPoint> locations = new ArrayList<GeoPoint>();
		if (cursor.moveToFirst()) {
			do {
				locations.add(new GeoPoint(cursor.getInt(1), cursor.getInt(2)));
			} while (cursor.moveToNext());
			
		} else {
			Log.d(DEBUG_TAG, "couldn't move cursor to first row");
		}
			
		return locations;
	}
}
