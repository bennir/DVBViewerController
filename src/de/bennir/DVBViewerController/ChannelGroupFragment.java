package de.bennir.DVBViewerController;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import de.bennir.DVBViewerController.channels.DVBChannel;

import java.util.ArrayList;

public class ChannelGroupFragment extends SherlockListFragment {
    final String TAG = ChannelGroupFragment.class.toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }


    public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
        ArrayList<DVBChannel> chans;

        public DVBChannelAdapter(Context context, int resourceId) {
            super(context, resourceId);
        }

        public DVBChannelAdapter(Context context, int resourceId, ArrayList<DVBChannel> dvbChans) {
            this.chans = dvbChans;

        }


    }
}
