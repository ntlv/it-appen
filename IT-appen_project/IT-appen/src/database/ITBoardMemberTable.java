package database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ITBoardMemberTable {

	// Database table
	public static final String TABLE_NAME = "it_board_members";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MAIL = "mail";
	public static final String COLUMN_IMAGE_URL = "image_url";


	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " integer primary key, "
			+ COLUMN_TITLE + " text not null, " 
			+ COLUMN_NAME + " text not null, " 
			+ COLUMN_MAIL + " text, " 
			+ COLUMN_IMAGE_URL + " text"
			+ ");";
	private static final String LOG_TAG = "ITBoardMemberTable";

	public static void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "creating database");
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(ITBoardMemberTable.class.getName(),
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
