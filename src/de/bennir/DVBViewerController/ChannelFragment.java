package de.bennir.DVBViewerController;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import de.bennir.DVBViewerController.channels.ChannelAdapter;
import de.bennir.DVBViewerController.channels.ChannelListParcelable;
import de.bennir.DVBViewerController.channels.DVBChannel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * User: benni
 * Date: 16.12.12
 * Time: 16:22
 */
public class ChannelFragment extends SherlockListFragment {
    final String TAG = "ChannelFragment";
    ExpandableListView lv;
    ChannelAdapter lvAdapter;
    ArrayList<String> groupNames = new ArrayList<String>();
    ArrayList<ArrayList<DVBChannel>> DVBChannels = new ArrayList<ArrayList<DVBChannel>>();
    ArrayList<ChannelListParcelable> chanParcel = new ArrayList<ChannelListParcelable>();
    AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        lv = (ExpandableListView) getListView();
        getSherlockActivity().registerForContextMenu(lv);
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                DVBChannel chan = DVBChannels.get((groupPosition / 1024)).get(
                        childPosition - groupPosition);

                setChannel(chan.favoriteId);

                return true;
            }
        });

        if (DVBChannels.isEmpty()) {
            updateChannelList();
        } else {
            lvAdapter = new ChannelAdapter(getSherlockActivity(), groupNames, DVBChannels);
        }
    }

    private void updateChannelList() {
        groupNames.clear();
        DVBChannels.clear();
        chanParcel.clear();

        ProgressDialog dialog = new ProgressDialog(getSherlockActivity());

        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.loadingChannels);

        aq = new AQuery(getSherlockActivity());
        String url = "http://" +
                DVBViewerControllerActivity.dvbIp + ":" +
                DVBViewerControllerActivity.dvbPort +
                "/?getFavList";
        Log.d(TAG, "URL=" + url);
        aq.progress(dialog).ajax(url, JSONObject.class, this, "downloadChannelCallback");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, "Refresh");
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                updateChannelList();

                return true;
            }
        });
    }

    public void setChannel(String channelId) {
        String url = "http://" +
                DVBViewerControllerActivity.dvbIp + ":" +
                DVBViewerControllerActivity.dvbPort +
                "?setChannel=" + channelId;

        aq.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String html, AjaxStatus status) {

            }

        });

    }

    public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
        Log.d(TAG, "downloadChannelCallback");
        ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();

        try {
            if (json != null) {
                JSONArray channelsJSON = new JSONArray(
                        json.getString("channels"));

                String currentGroup = "";

                for (int i = 0; i < channelsJSON.length(); i++) {
                    JSONObject chan = channelsJSON.getJSONObject(i);

                    String key = chan.getString("name");
                    String value = chan.getString("id");
                    String group = chan.getString("group");
                    String channelId = chan.getString("channelid");

                    DVBChannel dvbChannel = new DVBChannel();
                    dvbChannel.name = chan.getString("name");
                    dvbChannel.favoriteId = chan.getString("id");
                    dvbChannel.channelId = chan.getString("channelid");
                    dvbChannel.group = chan.getString("group");

                    if (!group.equals(currentGroup)) {
                        // Log.i("0", "cur: " + currentGroup + " - prev: " +
                        // group);
                        if (i > 0) {
                            DVBChannels.add(dvbChans);
                            dvbChans = new ArrayList<DVBChannel>();
                        }
                        groupNames.add(group);
                        currentGroup = group;

                        dvbChans.add(dvbChannel);
                    } else {
                        dvbChans.add(dvbChannel);
                    }
                }
                DVBChannels.add(dvbChans);

                lvAdapter = new ChannelAdapter(getSherlockActivity(),
                        groupNames, DVBChannels);

                lv.setAdapter(lvAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

        // TEST CHANNEL START
        if (DVBViewerControllerActivity.dvbHost == "Demo Device") {
            groupNames.clear();
            DVBChannels.clear();

            groupNames.add("Testgruppe");
            ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();
            DVBChannel test = new DVBChannel();

            test.name = "Das Erste";
            test.group = "Testgruppe";
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            DVBChannels.add(testChans);

            groupNames.add("Privat");
            testChans = new ArrayList<DVBChannel>();
            test = new DVBChannel();

            test.name = "ZDF HD";
            test.group = "Privat";
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            testChans.add(test);
            DVBChannels.add(testChans);

            lvAdapter = new ChannelAdapter(getSherlockActivity(), groupNames,
                    DVBChannels);
            lv.setAdapter(lvAdapter);
        }
        // TEST CHANNEL END
    }
}
