package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsAndAuthorDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "newsAndAuthor.db";
	private static final int DATABASE_VERSION = 1;
	private static final String LOG_TAG = "NewsAndBoardDatabaseHelper";

	public NewsAndAuthorDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(LOG_TAG, "finishing helper constructor");
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		ITAuthorTable.onCreate(database);
		ITNewsTable.onCreate(database);		
		Log.d(LOG_TAG, "just returned from table helper onCreate");
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		ITAuthorTable.onUpgrade(database, oldVersion, newVersion);
		ITNewsTable.onUpgrade(database, oldVersion, newVersion);
	}
	
	
		
	public void dropIT(SQLiteDatabase db) {
		ITNewsTable.dropIT(db);
		ITAuthorTable.dropIT(db);
	}
	
}
