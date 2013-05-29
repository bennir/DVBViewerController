package de.bennir.DVBViewerController;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.bennir.DVBViewerController.util.DateUtils;
import de.bennir.DVBViewerController.wizard.model.*;
import de.bennir.DVBViewerController.wizard.ui.PageFragmentCallbacks;
import de.bennir.DVBViewerController.wizard.ui.ReviewFragment;
import de.bennir.DVBViewerController.wizard.ui.StepPagerStrip;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TimerWizardActivity extends SherlockFragmentActivity implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private boolean mEditingAfterReview;
    private AbstractWizardModel mWizardModel = new TimerWizardModel(this);
    private boolean mConsumePageSelectedEvent;
    private Button mNextButton;
    private Button mPrevButton;
    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timerwizard_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.timer_add);
        getSupportActionBar().setIcon(R.drawable.ic_action_timers);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                    DialogFragment dg = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return new AlertDialog.Builder(getActivity())
                                    .setMessage("Please confirm")
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("TimerWizard", "Review Confirm");
                                            String channel = mWizardModel.findByKey("Channel").getData().getString(Page.SIMPLE_DATA_KEY);
                                            String name = mWizardModel.getCurrentPageSequence().get(1).getData().getString(TimerInfoPage.NAME_DATA_KEY);
                                            String priority = mWizardModel.getCurrentPageSequence().get(1).getData().getString(TimerInfoPage.PRIORITY_DATA_KEY);
                                            Boolean enabled = mWizardModel.getCurrentPageSequence().get(1).getData().getBoolean(TimerInfoPage.ENABLED_DATA_KEY);
                                            String date = mWizardModel.getCurrentPageSequence().get(2).getData().getString(TimerDatePage.DATE_DATA_KEY);
                                            String starttime = mWizardModel.getCurrentPageSequence().get(2).getData().getString(TimerDatePage.TIMESTART_DATA_KEY);
                                            String endtime = mWizardModel.getCurrentPageSequence().get(2).getData().getString(TimerDatePage.TIMEEND_DATA_KEY);
                                            String action = mWizardModel.findByKey("Timer Action").getData().getString(Page.SIMPLE_DATA_KEY);
                                            String after = mWizardModel.findByKey("After Timer").getData().getString(Page.SIMPLE_DATA_KEY);

                                            if (name == null)
                                                name = channel;

                                            if (priority == null)
                                                priority = "50";

                                            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                                                try {
                                                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                                    Date d1 = df.parse(date + " " + starttime);
                                                    Date d2 = df.parse(date + " " + endtime);

                                                    String start = DateUtils.getFloatDate(d1);
                                                    String stop = DateUtils.getFloatDate(d2);

                                                    Double dor = Double.valueOf(start.split("\\.")[0]);

                                                    long startVal = Math.round((Double.valueOf(start) - dor) * 60 * 24);
                                                    long stopVal = Math.round((Double.valueOf(stop) - dor) * 60 * 24);

                                                    String url = "http://";
                                                    url += DVBViewerControllerActivity.recIp + ":";
                                                    url += DVBViewerControllerActivity.recPort;
                                                    url += "/api/timeradd.html?";
                                                    url += (enabled ? "enable=1" : "enable=0");
                                                    url += "&ch=" + URLEncoder.encode(DVBViewerControllerActivity.getChannelByName(channel).channelId, "UTF-8");
                                                    url += "&title=" + URLEncoder.encode(name, "UTF-8");
                                                    url += "&dor=" + start.split("\\.")[0];
                                                    url += "&start=" + startVal;
                                                    url += "&stop=" + stopVal;
                                                    url += "&prio=" + priority;
                                                    if (action != null)
                                                        url += (action.equals("Record")) ? "&action=0" : "&action=1";
                                                    if (after != null) {
                                                        if (after.equals("Power Off"))
                                                            url += "&endact=1";
                                                        else if (after.equals("Standby"))
                                                            url += "&endact=2";
                                                        else
                                                            url += "endact=3";
                                                    }

                                                    Log.d("TimerWizard", url);

                                                    AQuery aq = new AQuery(getActivity());
                                                    aq.ajax(url, String.class, new AjaxCallback<String>() {

                                                        @Override
                                                        public void callback(String url, String html, AjaxStatus status) {
                                                            // Status Ok
                                                            if (status.getCode() == 200) {
                                                                setResult(1);
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                DVBTimer timer = new DVBTimer();
                                                timer.id += new Random().nextInt(100);
                                                timer.channelId = "|" + channel;
                                                timer.name = name;
                                                timer.date = date;
                                                timer.enabled = enabled;
                                                timer.start = starttime;
                                                timer.end = endtime;

                                                DVBViewerControllerActivity.DVBTimers.add(timer);
                                                TimerFragment.addTimersToListView();

                                                setResult(0);
                                                finish();
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .create();
                        }
                    };
                    dg.show(getSupportFragmentManager(), "place_order_dialog");
                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        mPager.setAdapter(mPagerAdapter);
        updateBottomBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText("Finish");
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview
                    ? "Review"
                    : "Next");
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }
    }
}
