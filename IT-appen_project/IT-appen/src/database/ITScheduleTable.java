package database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ITScheduleTable {

	// Database table
	public static final String TABLE_IT3 = "schedule_table";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CLASS_ID = "class_id";
	public static final String COLUMN_START_DATE = "start_date";
	public static final String COLUMN_START_TIME = "start_time";
	public static final String COLUMN_END_DATE = "end_date";
	public static final String COLUMN_END_TIME = "end_time";
	// skippar utrustning
	public static final String COLUMN_PROGRAMS = "programs";
	public static final String COLUMN_COURSE_NAME = "course_name";
	public static final String COLUMN_COURSE_GROUPS = "course_groups";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_MOMENT = "moment";
	public static final String COLUMN_TEACHER = "teacher";
	public static final String COLUMN_COMMENT = "comment";
	
	
	// Startdatum, Starttid, Slutdatum, Sluttid, Utrustning, Program, Kurs,
			// Kursgrupp, Lokal, Moment, Lärare, Kommentar
	
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_IT3
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_CLASS_ID + " integer not null, "
			+ COLUMN_START_DATE + " text not null, "
			+ COLUMN_START_TIME + " text not null, "
			+ COLUMN_END_DATE + " text, "
			+ COLUMN_END_TIME + " text, "
			+ COLUMN_PROGRAMS + " text, "
			+ COLUMN_COURSE_NAME + " text, "
			+ COLUMN_COURSE_GROUPS + " text, "
			+ COLUMN_LOCATION + " text, "
			+ COLUMN_MOMENT + " text, "
			+ COLUMN_TEACHER + " text, "
			+ COLUMN_COMMENT + " text" 
			+ ");";
	private static final String LOG_TAG = "IT3Table";

	public static void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "creating database");
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(ITScheduleTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_IT3);
		onCreate(database);
	}
	
	public static void dropITByClassID(SQLiteDatabase database, int classID){
		database.delete(TABLE_IT3, COLUMN_CLASS_ID + " = " + classID , null);
	}
	

	public static void dropIT(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_IT3);
		database.execSQL(DATABASE_CREATE);
	}

}
