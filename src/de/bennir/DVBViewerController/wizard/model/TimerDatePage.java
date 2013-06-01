package de.bennir.DVBViewerController.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import de.bennir.DVBViewerController.wizard.ui.TimerDateFragment;

import java.util.ArrayList;

public class TimerDatePage extends Page {
    public static final String DATE_DATA_KEY = "date";
    public static final String TIMESTART_DATA_KEY = "starttime";
    public static final String TIMEEND_DATA_KEY = "endtime";

    public TimerDatePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return TimerDateFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Date", mData.getString(DATE_DATA_KEY), getKey(), -2));
        dest.add(new ReviewItem("start_time", mData.getString(TIMESTART_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("end_time", mData.getString(TIMEEND_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !(
                TextUtils.isEmpty(mData.getString(DATE_DATA_KEY)) ||
                        TextUtils.isEmpty(mData.getString(TIMESTART_DATA_KEY)) ||
                        TextUtils.isEmpty(mData.getString(TIMEEND_DATA_KEY))
        );
    }
}