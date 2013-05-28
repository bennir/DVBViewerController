package de.bennir.DVBViewerController.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import de.bennir.DVBViewerController.wizard.ui.TimerInfoFragment;

import java.util.ArrayList;

public class TimerInfoPage extends Page {
    public static final String NAME_DATA_KEY = "name";
    public static final String PRIORITY_DATA_KEY = "priority";
    public static final String ENABLED_DATA_KEY = "enabled";

    public TimerInfoPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);

        mData.putBoolean(ENABLED_DATA_KEY, true);
    }

    @Override
    public Fragment createFragment() {
        return TimerInfoFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Name", mData.getString(NAME_DATA_KEY), getKey(), -2));
        dest.add(new ReviewItem("Priority", mData.getString(PRIORITY_DATA_KEY), getKey(), -2));
        dest.add(new ReviewItem("Enabled", mData.getBoolean(ENABLED_DATA_KEY), getKey(), -2));
    }

    @Override
    public boolean isCompleted() {
        return true;
    }
}