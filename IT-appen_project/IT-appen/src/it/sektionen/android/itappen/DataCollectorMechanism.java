package it.sektionen.android.itappen;

import it.sektionen.android.itappen.contentprovider.ITBoardContentProvider;
import it.sektionen.android.itappen.contentprovider.ITNewsAndAuthorContentProvider;
import it.sektionen.android.itappen.contentprovider.ScheduleContentProvider;
import it.sektionen.android.itappen.fragments.ScheduleFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import database.ITAuthorTable;
import database.ITBoardMemberTable;
import database.ITNewsTable;
import database.ITScheduleTable;
import database.ScheduleDatabaseHelper;

public class DataCollectorMechanism extends IntentService {

	public static final String INSTRUCTION_SELECTION_FIELD = "it.sektionen.android.itappen.DataCollectorMechanism.instructionSelectionField";
	public static final int GET_SCHEDULE = 10;
	public static final String SCHEDULE_SELECTION_FIELD = "it.sektionen.android.itappen.DataCollectorMechanism.ScheduleSelectionField";
    public static final String FORCE_UPDATE_FIELD = "it.sektionen.android.itappen.DataCollectorMechanism.ForceUpdateField";


	public static final int GET_BOARD_MEMBERS = 20;
	public static final int GET_NEWSANDAUTHORS = 30;
	protected static final String LOG_TAG = "DataCollectorMechanism";

	private RequestQueue mQueue;
	private JsonArrayRequest mBoardJsObjRequest;
	private JsonObjectRequest mNewsjsObjRequest;

	public DataCollectorMechanism() {
		this("DataCollectorService");
	}

	public DataCollectorMechanism(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle instructions = intent.getExtras();
		// android.os.Debug.waitForDebugger();

		switch (instructions.getInt(INSTRUCTION_SELECTION_FIELD)) {
		case GET_SCHEDULE:
			Log.d(LOG_TAG, "grabbing schedule");
            Messenger uiHandler = (Messenger) intent.getParcelableExtra("key");
			new ScheduleGrabber(instructions.getInt(SCHEDULE_SELECTION_FIELD), instructions.getBoolean(FORCE_UPDATE_FIELD))
					.getSchedule();
            if(uiHandler != null){
            Message msg = Message.obtain();
            msg.arg1 = MainActivity.MESSAGE;
            try {

                uiHandler.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }}

            break;
		case GET_BOARD_MEMBERS:
			Log.d(LOG_TAG, "grabbing board members");
			mQueue.add(mBoardJsObjRequest);
			break;
		case GET_NEWSANDAUTHORS:
			Log.d(LOG_TAG, "grabbing news and authors");
			mQueue.add(mNewsjsObjRequest);
			break;
		default:
			throw new IllegalArgumentException(
					"Intent service was invoked with invalid instruction");
		}

	}

	public void onCreate() {

		super.onCreate();

		mQueue = Volley.newRequestQueue(this);

		String url = "http://it.sektionen.se/bokforsaljning/board.php";
		mBoardJsObjRequest = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray response) {
						insertBoardMembers(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(LOG_TAG, "Volley returned an error: " + error);
					}

				});

		url = "http://it.sektionen.se/api/get_recent_posts/";

		mNewsjsObjRequest = new JsonObjectRequest(url, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						insertNewsAndAuthors(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(LOG_TAG, "Volley returned an error: " + error);
					}

				});

	}

	public void onDestroy() {
		mQueue.stop();
        Log.d(LOG_TAG, "data collector mechanism is shutting down");
		super.onDestroy();
	}

	private void insertBoardMembers(JSONArray resultArray) {
		JSONObject jsonMember = null;

		String Id;
		String Title;
		String Person;
		String Mail;
		String ImageUrl;
		ContentValues cv = new ContentValues();

		try {
			for (int i = 0; i < resultArray.length(); i++) {

				jsonMember = resultArray.getJSONObject(i);
				Id = jsonMember.getString("id");
				Title = jsonMember.getString("title");
				Person = jsonMember.getString("person");
				Mail = jsonMember.getString("mail");
				ImageUrl = jsonMember.getString("imageUrl");

				cv.put(ITBoardMemberTable.COLUMN_ID, Id);
				cv.put(ITBoardMemberTable.COLUMN_IMAGE_URL, ImageUrl);
				cv.put(ITBoardMemberTable.COLUMN_MAIL, Mail);
				cv.put(ITBoardMemberTable.COLUMN_NAME, Person);
				cv.put(ITBoardMemberTable.COLUMN_TITLE, Title);

				getContentResolver().insert(ITBoardContentProvider.CONTENT_URI,
						cv);
				cv.clear();

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void insertNewsAndAuthors(JSONObject result) {

		try {
			if (result.getString("status").compareTo("ok") != 0) {
				return;
			}
		} catch (JSONException e) {
			// TODO can we do anything better here?
			e.printStackTrace();
		}
		JSONArray posts = null;
		try {
			posts = result.getJSONArray("posts");
		} catch (JSONException e) {
			// TODO Can we do anything better here?
			e.printStackTrace();
		}

		JSONObject newsItemJson;

		long ItemID;

		String Url;
		String PostDate;
		String Title;
		String Excerpt;
		String Modified;

		JSONObject authorJson;

		long AuthorID;
		String FirstName;
		String LastName;
		String AuthorTitle;

		ContentValues cv = new ContentValues();

		try {

			for (int i = 0; i < posts.length(); i++) {
				newsItemJson = posts.getJSONObject(i);
				ItemID = newsItemJson.getLong("id");
				Url = newsItemJson.getString("url");
				PostDate = newsItemJson.getString("date");
				Title = newsItemJson.getString("title_plain");
				Excerpt = newsItemJson.getString("excerpt");
				Modified = newsItemJson.getString("modified");

				authorJson = newsItemJson.getJSONObject("author");
				AuthorID = authorJson.getLong("id");

				FirstName = authorJson.getString("first_name");
				LastName = authorJson.getString("last_name");
				AuthorTitle = authorJson.getString("slug");

				cv.put(ITAuthorTable.COLUMN_ID, AuthorID);
				cv.put(ITAuthorTable.COLUMN_TITLE, AuthorTitle);
				cv.put(ITAuthorTable.COLUMN_FIRST_NAME, FirstName);
				cv.put(ITAuthorTable.COLUMN_LAST_NAME, LastName);
				getContentResolver().insert(
						ITNewsAndAuthorContentProvider.CONTENT_URI_AUTHOR, cv);
				cv.clear();

				cv.put(ITNewsTable.COLUMN_AUTHOR_ID, AuthorID);
				cv.put(ITNewsTable.COLUMN_EXCERPT, Excerpt);
				cv.put(ITNewsTable.COLUMN_ID, ItemID);
				cv.put(ITNewsTable.COLUMN_MODIFIED, Modified);
				cv.put(ITNewsTable.COLUMN_POST_DATE, PostDate);
				cv.put(ITNewsTable.COLUMN_TITLE, Title);
				cv.put(ITNewsTable.COLUMN_URL, Url);

				getContentResolver().insert(
						ITNewsAndAuthorContentProvider.CONTENT_URI_NEWS, cv);
				cv.clear();

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	class ScheduleGrabber {

		// TODO: hardcoding column indices, might be a bad idea

		final int START_DATE_COLUMN = 0; // startdatum
		final int START_TIME_COLUMN = 1; // starttid
		final int END_DATE_COLUMN = 2; // slutdatum
		final int END_TIME_COLUMN = 3; // sluttid
		// final int EQUIPMENT_COLUMN = 4;
		final int PROGRAM_NAMES = 5; // inblandade program
		final int COURSE_NAME_COLUMN = 6; // namn på kursen
		final int COURSE_GROUP_COLUMN = 7;
		final int LOCATION_COLUMN = 8; // plats
		final int MOMENT_DESCRIPTION_COLUMN = 9;

		final int TEACHER_NAME_COLUMN = 10; // namn på föreläsare
		final int COMMENT_COLUMN = 11; // eventuell kommentar

		// Startdatum, Starttid, Slutdatum, Sluttid, Utrustning, Program, Kurs,
		// Kursgrupp, Lokal, Moment, Lärare, Kommentar

		final String[] IT_SCHEDULE_URL = {
				"https://se.timeedit.net/web/uu/db1/schema/ri.csv?sid=3&p=0.d,4.w&objects=251065.207&ox=0&types=0&fe=0",

				"https://se.timeedit.net/web/uu/db1/schema/ri.csv?sid=3&p=0.m,4.w&objects=251067.207&ox=0&types=0&fe=0",

				"https://se.timeedit.net/web/uu/db1/schema/ri.csv?sid=3&p=0.m,4.w&objects=251068.207&ox=0&types=0&fe=0",

				"https://se.timeedit.net/web/uu/db1/schema/ri.csv?sid=3&p=0.m,4.w&objects=251069.207&ox=0&types=0&fe=0",

				"https://se.timeedit.net/web/uu/db1/schema/ri.csv?sid=3&p=0.m,4.w&objects=251070.207&ox=0&types=0&fe=0" };

		private final int classID;
		private final long lastUpdate;
        private final boolean forceUpdate;

		public ScheduleGrabber(int selectedSchedule, boolean shouldForceUpdate) {
			classID = selectedSchedule;
			lastUpdate = getSharedPreferences(MainActivity.PREFERENCES,
					Context.MODE_PRIVATE).getLong(
					ScheduleFragment.LAST_UPDATE_ + classID, 0);
            forceUpdate = shouldForceUpdate;
		}

		protected void getSchedule() {

			SharedPreferences sPrefs = getSharedPreferences(
					MainActivity.PREFERENCES, Context.MODE_PRIVATE);

			long currentTimeMinusOneHour = Calendar.getInstance()
					.getTimeInMillis() - 3600000;
			Log.d(LOG_TAG, "LAST UPDATE " + classID + " " + lastUpdate
					+ " current time minus one hour: "
					+ currentTimeMinusOneHour);

			String scheduleFetchURL = IT_SCHEDULE_URL[classID];

			if (lastUpdate < currentTimeMinusOneHour || forceUpdate) {
				sPrefs.edit()
						.putLong(ScheduleFragment.LAST_UPDATE_ + classID,
								Calendar.getInstance().getTimeInMillis())
						.commit();
				Log.d(LOG_TAG, "Laddar ner fräsch data...");
				HttpClient connector = new DefaultHttpClient();
				HttpGet request = new HttpGet(scheduleFetchURL);

				HttpResponse answer = null;
				try {
					answer = connector.execute(request);

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				BufferedReader reader = null;

				try {
					reader = new BufferedReader(new InputStreamReader(answer
							.getEntity().getContent()));
				} catch (IllegalStateException e) {
					Log.e(LOG_TAG, Log.getStackTraceString(e));
				} catch (IOException e) {
					Log.e(LOG_TAG, Log.getStackTraceString(e));
				}

				ArrayList<String[]> dataString = null;
				try {
					dataString = buildStringFromReader(reader);
				} catch (IOException e) {
					Log.e(LOG_TAG, Log.getStackTraceString(e));
				}

				ScheduleDatabaseHelper dbHelper = new ScheduleDatabaseHelper(
						DataCollectorMechanism.this);
				SQLiteDatabase dBase = dbHelper.getWritableDatabase();
				dbHelper.dropITByClassID(dBase, classID);
				dBase.close();
				ContentValues cv = new ContentValues();
				for (String[] event : dataString) {
					if (event != null) {

						cv.put(ITScheduleTable.COLUMN_CLASS_ID, classID);
						cv.put(ITScheduleTable.COLUMN_START_DATE,
								event[START_DATE_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_START_TIME,
								event[START_TIME_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_END_DATE,
								event[END_DATE_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_END_TIME,
								event[END_TIME_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_PROGRAMS,
								event[PROGRAM_NAMES]);
						cv.put(ITScheduleTable.COLUMN_COURSE_NAME,
								event[COURSE_NAME_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_COURSE_GROUPS,
								event[COURSE_GROUP_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_LOCATION,
								event[LOCATION_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_MOMENT,
								event[MOMENT_DESCRIPTION_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_TEACHER,
								event[TEACHER_NAME_COLUMN]);
						cv.put(ITScheduleTable.COLUMN_COMMENT,
								event[COMMENT_COLUMN]);
						getContentResolver().insert(
								ScheduleContentProvider.CONTENT_URI, cv);
						cv.clear();

					}
				}

			}

			getContentResolver().notifyChange(
					ScheduleContentProvider.CONTENT_URI, null, false);

		}

		private ArrayList<String[]> buildStringFromReader(
				BufferedReader downloadedContent) throws IOException {
			if (downloadedContent == null) {
				return null;
			}

			// strip first four lines, no useful data there
			for (int i = 0; i < 4; ++i) {
				downloadedContent.readLine();
			}

			ArrayList<String[]> events = new ArrayList<String[]>();
			String line;
			do {
				line = downloadedContent.readLine();
				events.add(splitByComma(line));

			} while (line != null);

			downloadedContent.close();
			return events;
		}

		private String[] splitByComma(String line) {
			if (line == null) {
				return null;
			}

			String[] result = new String[12];
			int currentIndexInResult = 0;
			StringBuilder sb = new StringBuilder();
			boolean isInsideQuote = false;
			char currentChar;
			for (int i = 0; i < line.length(); i++) {
				currentChar = line.charAt(i);
				if (currentChar == '\"') {
					isInsideQuote = !isInsideQuote;
				} else if (currentChar != ',' || isInsideQuote) {
					sb.append(currentChar);
				} else {
					result[currentIndexInResult] = sb.toString().trim();
					sb = new StringBuilder();
					++currentIndexInResult;
				}

			}

			return result;
		}
	}

}
