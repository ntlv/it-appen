package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ScheduleDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "scheduletable.db";
	private static final int DATABASE_VERSION = 1;
	private static final String LOG_TAG = "ScheduleDatabaseHelper";

	public ScheduleDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(LOG_TAG, "finishing helper constructor");
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		ITScheduleTable.onCreate(database);
		Log.d(LOG_TAG, "just returned from IT3Table.onCreate");
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		ITScheduleTable.onUpgrade(database, oldVersion, newVersion);
	}
	
	public void dropITByClassID(SQLiteDatabase db, int classID){
		ITScheduleTable.dropITByClassID(db, classID);
	}
	
	public void dropIT(SQLiteDatabase db) {
		ITScheduleTable.dropIT(db);
	}
	
}
