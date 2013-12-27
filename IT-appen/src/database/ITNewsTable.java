package database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ITNewsTable {

	// Database table
	public static final String TABLE_NAME = "it_news";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AUTHOR_ID = "author_id";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_POST_DATE = "post_date";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_EXCERPT = "excerpt";
	public static final String COLUMN_MODIFIED = "modified";


	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " integer primary key, "
			+ COLUMN_AUTHOR_ID + " integer not null, " 
			+ COLUMN_URL + " text not null, "
			+ COLUMN_POST_DATE + " text not null, "
			+ COLUMN_TITLE + " text, "
			+ COLUMN_EXCERPT + " text not null, "
			+ COLUMN_MODIFIED + " text not null, "
			+ " FOREIGN KEY ("+COLUMN_AUTHOR_ID+") REFERENCES "
			+ ITAuthorTable.TABLE_NAME+" ("+ITAuthorTable.COLUMN_ID+")"
			+ ");";
	
	private static final String LOG_TAG = "ITNewsTable";

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
