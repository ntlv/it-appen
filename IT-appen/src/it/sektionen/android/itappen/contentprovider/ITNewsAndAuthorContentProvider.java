package it.sektionen.android.itappen.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import database.ITAuthorTable;
import database.ITBoardMemberTable;
import database.ITNewsTable;
import database.NewsAndAuthorDatabaseHelper;
import it.sektionen.android.itappen.R;

public class ITNewsAndAuthorContentProvider extends ContentProvider {

	// database
	private NewsAndAuthorDatabaseHelper database;

	private static final int NEWS = 10;
	private static final int NEWS_ID = 20;

	private static final int AUTHORS = 30;
	private static final int AUTHOR_ID = 40;

	private static final String AUTHORITY = "it.sektionen.ITNewsAndAuthorContentProvider";

	private static final String AUTHOR_PATH = ITAuthorTable.TABLE_NAME;

	private static final String NEWS_PATH = ITNewsTable.TABLE_NAME;

	public static final Uri CONTENT_URI_AUTHOR = Uri.parse("content://"
			+ AUTHORITY + "/" + AUTHOR_PATH);

	public static final Uri CONTENT_URI_NEWS = Uri.parse("content://"
			+ AUTHORITY + "/" + NEWS_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/news";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/news";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, AUTHOR_PATH, AUTHORS);
		sURIMatcher.addURI(AUTHORITY, AUTHOR_PATH + "/#", AUTHOR_ID);
		sURIMatcher.addURI(AUTHORITY, NEWS_PATH, NEWS);
		sURIMatcher.addURI(AUTHORITY, NEWS_PATH + "/#", NEWS_ID);
	}

	private static final String LOG_TAG = "ITNewsAndAuthorContentProvider";

	@Override
	public boolean onCreate() {
		Context ctx = getContext();

		database = new NewsAndAuthorDatabaseHelper(ctx);
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
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case AUTHORS:
			queryBuilder.setTables(ITAuthorTable.TABLE_NAME);
			break;
		case NEWS:
			queryBuilder.setTables(ITNewsTable.TABLE_NAME);
			break;
		default:
			throw new IllegalAccessError();

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
		// int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		// int rowsDeleted = 0;
		String path = null;

		long id = 0;
		String tableName = null;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case AUTHORS:
			tableName = ITAuthorTable.TABLE_NAME;
			path = CONTENT_URI_AUTHOR + "/" + AUTHOR_PATH;
			break;
		case NEWS:
			path = CONTENT_URI_NEWS + "/" + NEWS_PATH;
			tableName = ITNewsTable.TABLE_NAME;
			break;
		default:
			throw new IllegalAccessError();

		}

		id = sqlDB.insertWithOnConflict(tableName, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(LOG_TAG, "notifying observers on uri: " + uri);
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(path + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		throw new IllegalArgumentException("Unknown URI: " + uri);

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		throw new IllegalArgumentException("Unknown URI: " + uri);

	}

    class SimpleWithImageCursorAdapter extends SimpleCursorAdapter {

        //private CropSquareTransformation mTransformer;

        public SimpleWithImageCursorAdapter(Context context, int layout,
                                            Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
		/*	Picasso p = Picasso.with(mContext);
			p.setDebugging(true);

			// Obtain the external cache directory
			File cacheDir = context.getExternalCacheDir();
			if (cacheDir == null) {
			    // Fall back to using the internal cache directory
			    cacheDir = context.getCacheDir();
			}
			// Create a response cache using the cache directory and size restriction
			HttpResponseCache responseCache = new Http

					//HttpResponseCache(cacheDir, 10 * 1024 * 1024);
			// Prepare OkHttp
			OkHttpClient httpClient = new OkHttpClient();
			httpClient.setResponseCache(responseCache);
			// Build Picasso with this custom Downloader
			new Picasso.Builder(getContext())
			         .downloader(new OkHttpDownloader(httpClient))
			         .build();


			mTransformer = new CropSquareTransformation();*/

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View intermediate = super.getView(position, convertView, parent);

            String uri = getCursor().getString(
                    getCursor().getColumnIndex(
                            ITBoardMemberTable.COLUMN_IMAGE_URL));

            ImageView iV = (ImageView) intermediate
                    .findViewById(R.id.member_face);
            ImageLoader.getInstance().displayImage(uri, iV);

            //Picasso.with(mContext).load(uri).transform(mTransformer).into(iV);
            return intermediate;
        }
    }


}
