package it.sektionen.android.itappen.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Transformation;

import database.ITBoardMemberTable;
import it.sektionen.android.itappen.MainActivity;
import it.sektionen.android.itappen.R;
import it.sektionen.android.itappen.contentprovider.ITBoardContentProvider;

public class AboutFragment extends SherlockFragment implements
        LoaderCallbacks<Cursor> {

    protected static final String LOG_TAG = "AboutFragment";

    MainActivity mA;

    Context context;

    ListView mListView;

    boolean mContainedInActivity = false;

    private SimpleCursorAdapter mBoardMemberAdapter;

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
                R.layout.board_fragment, container, false);

        mListView = (ListView) retVal.findViewById(R.id.boardMemberList);

        mA = (MainActivity) getActivity();

        String[] from = {
                //ITBoardMemberTable.COLUMN_ID,
                //ITBoardMemberTable.COLUMN_IMAGE_URL,
                ITBoardMemberTable.COLUMN_MAIL, ITBoardMemberTable.COLUMN_NAME,
                ITBoardMemberTable.COLUMN_TITLE};
        int[] to = {
                //R.id.member_id,
                //R.id.member_image_url,
                R.id.member_mail,
                R.id.member_person, R.id.member_title};

        getLoaderManager().initLoader(0, null, this);

        mBoardMemberAdapter = new SimpleWithImageCursorAdapter(context,
                R.layout.board_member_row, null, from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mListView.setAdapter(mBoardMemberAdapter);

        return retVal;
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume() called");
    }

    class SimpleWithImageCursorAdapter extends SimpleCursorAdapter {

        //private CropSquareTransformation mTransformer;

        View.OnClickListener emailListener;



        public SimpleWithImageCursorAdapter(Context context, int layout,
                                            Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);

            emailListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = (String) view.getTag();

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", email, null));

                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            };

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

            String emailAddress = getCursor().getString(
                    getCursor().getColumnIndex(
                            ITBoardMemberTable.COLUMN_MAIL));

            ImageView iV = (ImageView) intermediate
                    .findViewById(R.id.member_face);
            ImageLoader.getInstance().displayImage(uri, iV);

            View email = intermediate.findViewById(R.id.member_mail);
            email.setTag(emailAddress);
            email.setOnClickListener(emailListener);


            //Picasso.with(mContext).load(uri).transform(mTransformer).into(iV);
            return intermediate;
        }
    }

    public class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {

            double scaleFactor = 200.0 / Double.valueOf(source.getWidth());

            int y = (int) (source.getHeight() * scaleFactor);
            Log.d("LOG_TAG", "scaleFactor:" + scaleFactor + " y: " + y);

            Bitmap result = Bitmap.createScaledBitmap(source, 200, y, false);

            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

        CursorLoader cl = new CursorLoader(context,
                ITBoardContentProvider.CONTENT_URI, null, null, null, "_id ASC");

        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mBoardMemberAdapter.swapCursor(arg1);
        mBoardMemberAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mBoardMemberAdapter.swapCursor(null);
        mBoardMemberAdapter.notifyDataSetChanged();

    }

}
