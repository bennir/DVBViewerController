package de.bennir.DVBViewerController;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockListFragment;

public class ChannelGroupFragment extends SherlockListFragment {
    final String TAG = ChannelGroupFragment.class.toString();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }
}
