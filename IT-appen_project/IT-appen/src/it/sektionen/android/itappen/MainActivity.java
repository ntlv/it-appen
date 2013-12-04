package it.sektionen.android.itappen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.RequestQueue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import it.sektionen.android.itappen.fragments.AboutFragment;
import it.sektionen.android.itappen.fragments.ClassChoiceDialogFragment;
import it.sektionen.android.itappen.fragments.NewsFragment;
import it.sektionen.android.itappen.fragments.ScheduleFragment;

public class MainActivity extends SherlockFragmentActivity implements
        OnPageChangeListener {

    public static final String PREFERENCES = "preferences";
    protected static final String LOG_TAG = "MainActivity";
    private static final String ACTIVE_TAB = "active_tab";
    protected RequestQueue volleyQueue;
    private ActionBar actionBar;
    private SwipingPagerAdapter mSwipeAdaper;
    private android.support.v4.view.ViewPager mViewPager;
    private boolean mSpinnerNotYetTriggered = true;

    private MenuItem mChosenSchedule;


    public Handler handlerOfThings;

    private static class IconResetHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        private final WeakReference<MenuItem> mRefreshButton;


        public IconResetHandler(MainActivity activity, MenuItem menuItem) {
            mActivity = new WeakReference<MainActivity>(activity);
            mRefreshButton = new WeakReference<MenuItem>(menuItem);
        }

        public void handleMessage(Message message) {
            if (mActivity != null && mRefreshButton != null) {
                if (message.arg1 == MESSAGE) {
                    if (mRefreshButton != null) {
                        mRefreshButton.get().setEnabled(true);

                        mRefreshButton.get().setIcon(mActivity.get().getResources().getDrawable(R.drawable.refresh_2));
                    }
                } else if (message.arg1 == MESSAGE2) {

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        updateAll(false);
        initializePager();


    }

    protected void onResume() {
        super.onResume();
        mSpinnerNotYetTriggered = false;
        SharedPreferences sPrefs = getSharedPreferences(PREFERENCES,
                MODE_PRIVATE);
        int page = sPrefs.getInt(ACTIVE_TAB, 0);
        Log.d(LOG_TAG, "Restoring page: " + page);
        mViewPager.setCurrentItem(page);
        getSupportActionBar().setSelectedNavigationItem(page);

    }

    @Override
    protected void onPause() {
        super.onPause();
        int page = mViewPager.getCurrentItem();

        SharedPreferences sPrefs = getSharedPreferences(PREFERENCES,
                MODE_PRIVATE);

        boolean b = sPrefs.edit().putInt(ACTIVE_TAB, page).commit();

        Log.d(LOG_TAG, "Pausing and storing page: " + page
                + " insertion was succesful: " + b);

    }

    protected void onDestroy() {
        super.onDestroy();

    }

    private void initializePager() {
        List<Fragment> fragments = new ArrayList<Fragment>(3);
        fragments.add(Fragment.instantiate(this,
                ScheduleFragment.class.getName()));

        fragments.add(Fragment.instantiate(this, NewsFragment.class.getName()));

        fragments
                .add(Fragment.instantiate(this, AboutFragment.class.getName()));

        mSwipeAdaper = new SwipingPagerAdapter(getSupportFragmentManager(),
                fragments);
        mViewPager = (android.support.v4.view.ViewPager) findViewById(R.id.FragmentsViewPager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSwipeAdaper);

        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mSpinnerNotYetTriggered = true;

        getSupportMenuInflater().inflate(R.menu.fragment_selection_spinner,
                menu);
        SpinnerAdapter mSpinnerAdapter;
        mSpinnerAdapter = new CustomArrayAdapter(this, R.id.spinnerText,
                getResources().getStringArray(R.array.navigation_strings));

        OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                if (mSpinnerNotYetTriggered) {
                    Log.d(LOG_TAG, "First hit trigger in spinner");
                    getSupportActionBar().setSelectedNavigationItem(
                            getSharedPreferences(PREFERENCES, MODE_PRIVATE)
                                    .getInt(ACTIVE_TAB, 0));
                    mSpinnerNotYetTriggered = false;
                } else {
                    Log.d(LOG_TAG, "Selected item " + itemId + " in spinner.");
                    mViewPager.setCurrentItem(position);
                }
                return true;
            }
        };

        actionBar.setListNavigationCallbacks(mSpinnerAdapter,
                mOnNavigationListener);


        int selectedClass = getSharedPreferences(MainActivity.PREFERENCES, MODE_PRIVATE)
                .getInt(ScheduleFragment.SELECTED_SCHEDULE, 0);

        mChosenSchedule = menu.findItem(R.id.class_chooser_button);
        handlerOfThings = new IconResetHandler(this, menu.findItem(R.id.refresh_button));


        updateScheduleChooserButton(selectedClass);

        return true;

    }

    public boolean onClassChoiceButtonClicked(MenuItem item) {
        new ClassChoiceDialogFragment().show(getSupportFragmentManager(),
                "ClassChoiceDialog");
        return true;
    }

    public static final int MESSAGE = 1241561;
    public static final int MESSAGE2 = 1241562;


    public boolean onRefreshButtonClicked(MenuItem item) {

        item.setEnabled(false);
        item.setIcon(R.drawable.cloud_download);
        updateAll(true);
        return true;
    }

    void updateAll(boolean shouldForceUpdate) {

        Log.d(LOG_TAG, "refresh invoked by user");
        Intent msgIntent = new Intent(this, DataCollectorMechanism.class);
        msgIntent.putExtra(DataCollectorMechanism.INSTRUCTION_SELECTION_FIELD,
                DataCollectorMechanism.GET_BOARD_MEMBERS);
        startService(msgIntent);

        Intent aMsgIntent = new Intent(this, DataCollectorMechanism.class);
        aMsgIntent.putExtra(DataCollectorMechanism.INSTRUCTION_SELECTION_FIELD,
                DataCollectorMechanism.GET_NEWSANDAUTHORS);
        startService(aMsgIntent);


        Intent bMsgIntent = new Intent(this, DataCollectorMechanism.class);
        if (handlerOfThings != null) {
            bMsgIntent.putExtra("key", new Messenger(handlerOfThings));
        }
        bMsgIntent.putExtra(DataCollectorMechanism.FORCE_UPDATE_FIELD, shouldForceUpdate);
        bMsgIntent.putExtra(DataCollectorMechanism.INSTRUCTION_SELECTION_FIELD,
                DataCollectorMechanism.GET_SCHEDULE);
        bMsgIntent.putExtra(
                DataCollectorMechanism.SCHEDULE_SELECTION_FIELD,
                getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(
                        ScheduleFragment.SELECTED_SCHEDULE, 0));
        startService(bMsgIntent);

        // new ScheduleGrabber(this, getSharedPreferences(PREFERENCES,
        // MODE_PRIVATE).getInt(ScheduleFragment.SELECTED_SCHEDULE, 0))
        // .execute();
    }


    @Override
    public void onPageScrollStateChanged(int arg) {
        // TODO figure out if we we can do anything fancy here, maybe animate
        // the actionbar

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO figure out if we we can do anything fancy here, maybe animate
        // the actionbar

    }

    @Override
    public void onPageSelected(int arg0) {
        Log.d(LOG_TAG, "selected page: " + arg0);
        getSupportActionBar().setSelectedNavigationItem(arg0);
        if (mChosenSchedule != null) {
            if (arg0 != 0) {
                mChosenSchedule.setVisible(false);
            } else {
                mChosenSchedule.setVisible(true);
            }
        }
    }

    public void updateScheduleChooserButton(int chosenClass) {

        mChosenSchedule.setTitle("IT" + (chosenClass + 1));
        mChosenSchedule.setTitleCondensed("IT" + (chosenClass + 1));


    }
}
