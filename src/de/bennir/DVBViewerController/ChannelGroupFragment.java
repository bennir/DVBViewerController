package de.bennir.DVBViewerController;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.channels.DVBChannelAdapter;

import java.util.ArrayList;

class ChannelGroupFragment extends SherlockListFragment {
    private static final String TAG = ChannelGroupFragment.class.toString();
    private static DVBChannelAdapter lvAdapter;
    private static ListView lv;
    private AQuery aq;

    public static void addChannelsToListView() {
        lvAdapter.notifyDataSetChanged();
        lv.setAdapter(lvAdapter);
        lv.invalidate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String channelId = ((TextView) view.findViewById(R.id.channel_item_favid)).getText().toString();
                setChannel(channelId);
            }
        });

        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;

        ArrayList<DVBChannel> chans = DVBViewerControllerActivity.DVBChannels.get(DVBViewerControllerActivity.currentGroup);
        ChannelGroupFragment.lvAdapter = new DVBChannelAdapter(
                getSherlockActivity(),
                chans
        );

        addChannelsToListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((DVBViewerControllerActivity) getSherlockActivity()).updateChannelList();
                addChannelsToListView();

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                getSherlockActivity().getFragmentManager().popBackStackImmediate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setChannel(String channelId) {
        ((Vibrator) getSherlockActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
        if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "?setChannel=" + channelId;

            Log.d(TAG, "SetChannel " + url);

            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String html, AjaxStatus status) {

                }

            });
        }
    }
}
