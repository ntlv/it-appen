package it.sektionen.android.itappen.contentprovider;



import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import database.ITScheduleTable;
import database.ScheduleDatabaseHelper;

public class ScheduleContentProvider extends ContentProvider {

	// database
	private ScheduleDatabaseHelper database;

	private static final int EVENTS = 10;
	private static final int EVENT_ID = 20;

	private static final String AUTHORITY = "it.sektionen.ScheduleContentprovider";

	private static final String BASE_PATH = "events";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/events";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/event";

	public static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final String LOG_TAG = "ScheduleContentProvider";
	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, EVENTS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", EVENT_ID);
	}

	@Override
	public boolean onCreate() {
		database = new ScheduleDatabaseHelper(getContext());
		Log.d(LOG_TAG, "onCreate in provider");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		// TODO
		// checkColumns(projection);

		// Set the table
		queryBuilder.setTables(ITScheduleTable.TABLE_IT3);

		int uriType = sUriMatcher.match(uri);
		switch (uriType) {
		case EVENTS:
			break;
		case EVENT_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(ITScheduleTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		// int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case EVENTS:
			id = sqlDB.insert(ITScheduleTable.TABLE_IT3, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case EVENTS:
			rowsDeleted = sqlDB.delete(ITScheduleTable.TABLE_IT3, selection,
					selectionArgs);
			break;
		case EVENT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(ITScheduleTable.TABLE_IT3,
						ITScheduleTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(ITScheduleTable.TABLE_IT3,
						ITScheduleTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case EVENTS:
			rowsUpdated = sqlDB.update(ITScheduleTable.TABLE_IT3, values, selection,
					selectionArgs);
			break;
		case EVENT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ITScheduleTable.TABLE_IT3, values,
						ITScheduleTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(ITScheduleTable.TABLE_IT3, values,
						ITScheduleTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
}
