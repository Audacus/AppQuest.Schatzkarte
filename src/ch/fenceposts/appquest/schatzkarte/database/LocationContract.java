package ch.fenceposts.appquest.schatzkarte.database;

import android.provider.BaseColumns;

public final class LocationContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public LocationContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class LocationEntry implements BaseColumns {

		public static final String TABLE_NAME = "location";
		public static final String _ID = "locationId";
		public static final String COLUMN_NAME_LATITUDE = "lat";
		public static final String COLUMN_NAME_LONGITUDE = "lon";
		public static final String COLUMN_NAME_NULLABLE = "null";

		private static final String TYPE_INTEGER = " INTEGER";
		private static final String COMMA_SEPERATOR = ",";

		public static final String SQL_CREATE_ENTRIES = "" +
				"CREATE TABLE " + LocationEntry.TABLE_NAME + " (" 
					+ LocationEntry._ID + LocationEntry.TYPE_INTEGER + " PRIMARY KEY" + LocationEntry.COMMA_SEPERATOR 
					+ LocationEntry.COLUMN_NAME_LATITUDE + LocationEntry.TYPE_INTEGER + LocationEntry.COMMA_SEPERATOR
					+ LocationEntry.COLUMN_NAME_LONGITUDE + LocationEntry.TYPE_INTEGER
				+ ")";
		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;
	}
}
