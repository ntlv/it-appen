package it.sektionen.android.itappen;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;

import database.ITScheduleTable;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class StickySimpleCursorAdapter extends SimpleCursorAdapter implements
        StickyListHeadersAdapter, SectionIndexer {

    private static final String LOG_TAG = "StickySimpleCursorAdapter";

    private final Context mContext;

    private Cursor mCursor;

    private int[] mSectionIndices;
    private String[] mSectionDateStrings;

    private boolean mSectionsIndicesUpdated = false;

    private boolean mSectionsInvalid = true;

    public StickySimpleCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to) {
        this(context, layout, c, from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    }

    public StickySimpleCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to, int flags) {
        super(context, layout, c, from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mContext = context;
        mCursor = c;

        mSectionIndices = getSectionIndices();
        mSectionDateStrings = getSectionDateStrings();

    }

    protected void onContentChanged() {
        super.onContentChanged();
        Log.d(LOG_TAG, "CONTENT CHANGE DETECTED");
        mSectionIndices = getSectionIndices();
        mSectionDateStrings = getSectionDateStrings();
    }

    private int[] getSectionIndices() {
        if (mCursor == null) {
            return null;
        }

        if (mCursor.getCount() < 1) {
            return null;
        }

        if (!mSectionsInvalid) {
            return mSectionIndices;
        }

        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        int dateColumn = mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_START_DATE);

        mCursor.moveToFirst();

        int currentDateString = Integer.parseInt(mCursor.getString(dateColumn)
                .replace("-", ""));

        int toCompareWith;
        sectionIndices.add(0);
        int i = 0;
        while (mCursor.moveToNext()) {
            ++i;
            toCompareWith = Integer.parseInt(mCursor.getString(dateColumn)
                    .replace("-", ""));
            sectionIndices.add(0);
            if (toCompareWith > currentDateString) {
                currentDateString = toCompareWith;
                sectionIndices.add(i);
            }

        }
        int[] sections = new int[sectionIndices.size()];
        for (int j = 0; j < sectionIndices.size(); j++) {
            sections[j] = sectionIndices.get(j);
        }
        mSectionsIndicesUpdated = true;
        return sections;

    }

    private String[] getSectionDateStrings() {
        if (mCursor == null) {
            return null;
        }

        if (mCursor.getCount() < 1) {
            return null;
        }

        if (!mSectionsIndicesUpdated) {
            throw new IllegalStateException(
                    "Must call getSectionIndices prior to calling this method");
        }

        if (!mSectionsInvalid) {
            return mSectionDateStrings;
        }

        String[] dateStrings = new String[mSectionIndices.length];
        int dateColumn = mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_START_DATE);
        for (int i = 0; i < mSectionIndices.length; i++) {
            mCursor.moveToPosition(mSectionIndices[i]);
            dateStrings[i] = mCursor.getString(dateColumn);

        }
        mSectionsIndicesUpdated = false;

        mSectionsInvalid = false;

        return dateStrings;
    }

    @Override
    public int getCount() {

        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public Object getItem(int position) {

        mCursor.moveToPosition(position);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < mCursor.getColumnCount(); ++i) {
            sb.append(mCursor.getString(i));
        }
        return sb.toString();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EventRowViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new EventRowViewHolder();
            convertView = View.inflate(mContext, R.layout.schedule_event_row, null);

            //viewHolder.eventClassID = (TextView) convertView
            //        .findViewById(R.id.event_class_id);
            viewHolder.eventPrograms = (TextView) convertView
                    .findViewById(R.id.event_programs);
            viewHolder.eventCourseName = (TextView) convertView
                    .findViewById(R.id.event_course_name);
            viewHolder.eventCourseGroups = (TextView) convertView
                    .findViewById(R.id.event_course_groups);
            viewHolder.eventLocation = (TextView) convertView
                    .findViewById(R.id.event_location);
            viewHolder.eventMoment = (TextView) convertView
                    .findViewById(R.id.event_moment);
            viewHolder.eventTeacher = (TextView) convertView
                    .findViewById(R.id.event_teacher);
            viewHolder.eventComment = (TextView) convertView
                    .findViewById(R.id.event_comment);
            viewHolder.eventTime = (TextView) convertView
                    .findViewById(R.id.event_time);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (EventRowViewHolder) convertView.getTag();
        }
        mCursor.moveToPosition(position);


        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_PROGRAMS)), viewHolder.eventPrograms);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_COURSE_NAME)), viewHolder.eventCourseName);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_COURSE_GROUPS)),viewHolder.eventCourseGroups);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_LOCATION)), viewHolder.eventLocation);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_MOMENT)), viewHolder.eventMoment);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_TEACHER)), viewHolder.eventTeacher);

        setTextOrHide(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_COMMENT)), viewHolder.eventComment);


        String dateString = mCursor.getString(mCursor.getColumnIndex(ITScheduleTable.COLUMN_START_TIME))
                + " - "
                + mCursor.getString(mCursor.getColumnIndex(ITScheduleTable.COLUMN_END_TIME));;


        viewHolder.eventTime.setText(dateString);
        return convertView;

    }

    private void setTextOrHide(String text, TextView view) {

        if(text == null){
            view.setVisibility(View.GONE);
            return;
        }

        if(text.length() > 0){
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        DateHeaderViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new DateHeaderViewHolder();
            convertView = View.inflate(mContext, R.layout.date_header, null);
            viewHolder.headerDateText = (TextView) convertView
                    .findViewById(R.id.date_header);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DateHeaderViewHolder) convertView.getTag();
        }

        mCursor.moveToPosition(position);
        viewHolder.headerDateText.setText(mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_START_DATE)));

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {

        // 2013-07-10 10:15 - 12:00
        // 0123456789
        mCursor.moveToPosition(position);
        String dateString = mCursor.getString(mCursor
                .getColumnIndex(ITScheduleTable.COLUMN_START_DATE));
        int pseudoDayNumber = Integer.parseInt(dateString.replace("-", ""));
        return pseudoDayNumber;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        mSectionIndices = getSectionIndices();
        mSectionDateStrings = getSectionDateStrings();

        mSectionsInvalid = true;

        return oldCursor;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        return mSectionDateStrings;
    }

    class EventRowViewHolder {
        TextView eventPrograms;
        TextView eventCourseName;
        TextView eventCourseGroups;
        TextView eventLocation;
        TextView eventMoment;
        TextView eventTeacher;
        TextView eventComment;
        TextView eventTime;
    }

    class DateHeaderViewHolder {
        TextView headerDateText;
    }

}
