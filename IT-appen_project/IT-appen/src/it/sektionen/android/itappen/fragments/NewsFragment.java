package it.sektionen.android.itappen.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import database.ITNewsTable;
import it.sektionen.android.itappen.MainActivity;
import it.sektionen.android.itappen.R;
import it.sektionen.android.itappen.contentprovider.ITNewsAndAuthorContentProvider;

public class NewsFragment extends SherlockFragment implements
        LoaderCallbacks<Cursor> {

    protected static final String LOG_TAG = "NewsFragment";

    MainActivity mA;

    ListView mListView;

    Context context;



    private SimpleCursorAdapter mCursorAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }


        LinearLayout retVal = (LinearLayout) inflater.inflate(
                R.layout.news_fragment, container, false);

        mListView = (ListView) retVal.findViewById(R.id.news_feed);

        mA = (MainActivity) getActivity();

        String[] from = {
                //ITNewsTable.COLUMN_ID,
                // ITNewsTable.COLUMN_AUTHOR_ID,
                //ITNewsTable.COLUMN_EXCERPT,
                ITNewsTable.COLUMN_POST_DATE,
                ITNewsTable.COLUMN_TITLE
        //        ITNewsTable.COLUMN_URL
        };
        int[] to = {
                //R.id.news_id,
                // R.id.news_author_all,
                //R.id.news_excerpt,
                R.id.news_post_date, R.id.news_title
                //R.id.news_url,
                 };

        getLoaderManager().initLoader(0, null, this);
        mCursorAdapter = new SimpleHTMLInjectingCursorAdapter(context,
                R.layout.news_item_row, null, from, to, 0);

        mListView.setAdapter(mCursorAdapter);

        return retVal;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl = new CursorLoader(context,
                ITNewsAndAuthorContentProvider.CONTENT_URI_NEWS, null, null, null,
                ITNewsTable.COLUMN_POST_DATE + " DESC");

        return cl;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mCursorAdapter.swapCursor(arg1);
        mCursorAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mCursorAdapter.swapCursor(null);
        mCursorAdapter.notifyDataSetInvalidated();

    }


    class SimpleHTMLInjectingCursorAdapter extends SimpleCursorAdapter {

        View.OnClickListener urlListener;


        public SimpleHTMLInjectingCursorAdapter(Context context, int layout,
                                                Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);

            urlListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = (String) view.getTag();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                }
            };

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View intermediate = super.getView(position, convertView, parent);

            Cursor cursor = getCursor();

            String HTMLString = cursor.getString(
                    cursor.getColumnIndex(
                            ITNewsTable.COLUMN_EXCERPT));


            TextView excerpt = (TextView) intermediate.findViewById(R.id.news_excerpt);
            View url = intermediate.findViewById(R.id.news_url);
            url.setTag(cursor.getString(cursor.getColumnIndex(ITNewsTable.COLUMN_URL)));
            url.setOnClickListener(urlListener);

            excerpt.setText(Html.fromHtml(HTMLString));

            excerpt.setMovementMethod(LinkMovementMethod.getInstance());


            return intermediate;
        }
    }

}
