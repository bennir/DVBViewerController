package de.bennir.DVBViewerController;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.androidquery.AQuery;
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

        if (DVBChannels.isEmpty()) {
            ProgressDialog dialog = new ProgressDialog(getSherlockActivity());

            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setInverseBackgroundForced(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setTitle(R.string.loadingChannels);

            AQuery aq = new AQuery(getSherlockActivity());
            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "/?getFavList";
            Log.d(TAG, "URL=" + url);
            aq.progress(dialog).ajax(url, JSONObject.class, this, "downloadChannelCallback");
        } else {
            lvAdapter = new ChannelAdapter(getSherlockActivity(), groupNames, DVBChannels);
        }
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
        // TEST CHANNEL END
    }
}
