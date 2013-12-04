package it.sektionen.android.itappen.fragments;

import it.sektionen.android.itappen.DataCollectorMechanism;
import it.sektionen.android.itappen.MainActivity;
import it.sektionen.android.itappen.R;
import it.sektionen.android.itappen.StickySimpleCursorAdapter;
import it.sektionen.android.itappen.contentprovider.ScheduleContentProvider;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

import database.ITScheduleTable;

public class ScheduleFragment extends SherlockFragment implements
		LoaderCallbacks<Cursor>,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String LOG_TAG = "ScheduleFragment";

	private StickyListHeadersListView scheduleList;
	private StickySimpleCursorAdapter scheduleAdapter;
	private Context context;
	public static final String LAST_UPDATE_ = "last_update_";

	public static final String SELECTED_SCHEDULE = "selected_schedule";

	private static final String LOADER_BUNDLE_CLASS_ID = "loader_bundle_class_id";

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		LinearLayout inflatedLayout = (LinearLayout) inflater.inflate(
				R.layout.schedule_fragment, container, false);

		scheduleList = (se.emilsjolander.stickylistheaders.StickyListHeadersListView) inflatedLayout
				.findViewById(R.id.schedule_list);

		String[] from = {
                //ITScheduleTable.COLUMN_CLASS_ID,
				ITScheduleTable.COLUMN_COMMENT,
				ITScheduleTable.COLUMN_COURSE_GROUPS,
				ITScheduleTable.COLUMN_COURSE_NAME,
				ITScheduleTable.COLUMN_START_DATE,
				ITScheduleTable.COLUMN_LOCATION, ITScheduleTable.COLUMN_MOMENT,
				ITScheduleTable.COLUMN_PROGRAMS, ITScheduleTable.COLUMN_TEACHER};
		int[] to = {
                //R.id.event_class_id,
                R.id.event_comment,
				R.id.event_course_groups, R.id.event_course_name,
				R.id.event_time, R.id.event_location, R.id.event_moment,
				R.id.event_programs, R.id.event_teacher };

		Bundle bundle = new Bundle();
		bundle.putInt(
				LOADER_BUNDLE_CLASS_ID,
				context.getSharedPreferences(MainActivity.PREFERENCES,
						Context.MODE_PRIVATE).getInt(SELECTED_SCHEDULE, 0));

		getLoaderManager().initLoader(0, bundle, this);

		scheduleAdapter = new StickySimpleCursorAdapter(context,
				R.layout.schedule_event_row, null, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		scheduleList.setAdapter(scheduleAdapter);

		return inflatedLayout;

	}

	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume() called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "calling onPause");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		context.getSharedPreferences(MainActivity.PREFERENCES,
				Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				this);
	}

	public void onDetach() {
		super.onDetach();
		context.getSharedPreferences(MainActivity.PREFERENCES,
				Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		final int classID = arg1.getInt(LOADER_BUNDLE_CLASS_ID);
		Log.d(LOG_TAG, "loader create");

		CursorLoader cL = new CursorLoader(getActivity(),
				ScheduleContentProvider.CONTENT_URI, null,
				ITScheduleTable.COLUMN_CLASS_ID + " = " + classID, null, null);

		return cL;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		Log.d(LOG_TAG, "loader finished with: " + arg1.toString());
		scheduleAdapter.swapCursor(arg1);
		scheduleAdapter.notifyDataSetChanged();

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(LOG_TAG, "loader reset");
		scheduleAdapter.swapCursor(null);

		scheduleAdapter.notifyDataSetInvalidated();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {
		if (key.compareTo(SELECTED_SCHEDULE) == 0) {
			Log.d(LOG_TAG, "shared prefs change detected");
			int position = sPrefs.getInt(SELECTED_SCHEDULE, 0);

			Intent bMsgIntent = new Intent(getActivity(),
					DataCollectorMechanism.class);
			bMsgIntent.putExtra(
					DataCollectorMechanism.INSTRUCTION_SELECTION_FIELD,
					DataCollectorMechanism.GET_SCHEDULE);
			bMsgIntent.putExtra(
					DataCollectorMechanism.SCHEDULE_SELECTION_FIELD, position);
			context.startService(bMsgIntent);

			Bundle bundle = new Bundle();
			bundle.putInt(LOADER_BUNDLE_CLASS_ID, position);

			getLoaderManager().restartLoader(0, bundle, ScheduleFragment.this);

            ((MainActivity) getSherlockActivity()).updateScheduleChooserButton(position);

		}

	}




}
