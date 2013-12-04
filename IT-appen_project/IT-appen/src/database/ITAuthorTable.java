package database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ITAuthorTable {
	public static final String TABLE_NAME = "it_authors";
	public static final String COLUMN_ID = "_id";
	
	public static final String COLUMN_FIRST_NAME = "first_name";
	public static final String COLUMN_LAST_NAME = "last_name";
	public static final String COLUMN_TITLE = "title";
	
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " integer primary key, "
			+ COLUMN_FIRST_NAME + " string not null, "
			+ COLUMN_LAST_NAME + " string not null, "
			+ COLUMN_TITLE + " string "	
			+ ")";
	
	private static final String LOG_TAG = "ITAuthorTable";

	public static void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "creating database");
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(ITNewsTable.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

	public static void dropIT(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		database.execSQL(DATABASE_CREATE);
	}
	

}
