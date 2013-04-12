package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

public class ChannelFragment extends SherlockListFragment {
    private static final String TAG = ChannelFragment.class.toString();
    ListView lv;
    ChanGroupAdapter lvAdapter;
    AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated Size: " + DVBViewerControllerActivity.DVBChannels.size());
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        getSherlockActivity().getSupportActionBar().setTitle(R.string.channels);

        aq = new AQuery(getSherlockActivity());

        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getActivity() instanceof DVBViewerControllerActivity) {
                    DVBViewerControllerActivity.currentGroup = i;
                    DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
                    act.switchContent(new ChannelGroupFragment(), DVBViewerControllerActivity.groupNames.get(i), R.drawable.ic_action_channels, true);
                }
            }
        });

        if (DVBViewerControllerActivity.DVBChannels.isEmpty()) {
            updateChannelList();
        } else {
            lvAdapter = new ChanGroupAdapter(
                    getSherlockActivity(),
                    DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
            );
            lv.setAdapter(lvAdapter);
            lv.invalidate();
        }
    }

    private void updateChannelList() {
        Log.d(TAG, "updating channels");
        DVBViewerControllerActivity.groupNames.clear();
        DVBViewerControllerActivity.DVBChannels.clear();

        if (DVBViewerControllerActivity.dvbHost == "Demo Device") {
            DVBViewerControllerActivity.groupNames.add("ARD");
            ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();

            DVBChannel test = new DVBChannel();
            test.name = "Das Erste HD";
            testChans.add(test);
            test = new DVBChannel();
            test.name = "NDR HD";
            testChans.add(test);
            DVBViewerControllerActivity.DVBChannels.add(testChans);

            DVBViewerControllerActivity.groupNames.add("ZDF");
            testChans = new ArrayList<DVBChannel>();

            test = new DVBChannel();
            test.name = "ZDF HD";
            testChans.add(test);
            test = new DVBChannel();
            test.name = "ZDF Kultur";
            testChans.add(test);
            DVBViewerControllerActivity.DVBChannels.add(testChans);

            lvAdapter = new ChanGroupAdapter(
                    getSherlockActivity(),
                    DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
            );
            lv.setAdapter(lvAdapter);
        } else {
            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "/?getFavList";
            Log.d(TAG, "URL=" + url);

            Style st = new Style.Builder()
                    .setDuration(Style.DURATION_INFINITE)
                    .setBackgroundColorValue(Style.holoBlueLight)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .build();
            Crouton.makeText(getActivity(), R.string.loadingChannels, st).show();

            aq.ajax(url, JSONObject.class, this, "downloadChannelCallback");
        }
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

    public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
        Log.d(TAG, "downloadChannelCallback");
        Crouton.cancelAllCroutons();

        ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();

        try {
            if (json != null) {
                Log.d(TAG, "Received answer");
                JSONArray channelsJSON = new JSONArray(
                        json.getString("channels"));

                String currentGroup = "";

                for (int i = 0; i < channelsJSON.length(); i++) {
                    JSONObject chan = channelsJSON.getJSONObject(i);

                    DVBChannel dvbChannel = new DVBChannel();
                    dvbChannel.name = chan.getString("name");
                    dvbChannel.favoriteId = chan.getString("id");
                    dvbChannel.channelId = chan.getString("channelid");
                    dvbChannel.epgTitle = URLDecoder.decode(chan.getString("epgtitle"));
                    dvbChannel.epgTime = chan.getString("epgtime");
                    dvbChannel.epgDuration = chan.getString("epgduration");

                    String group = chan.getString("group");
                    if (!group.equals(currentGroup)) {
                        if (i > 0) {
                            DVBViewerControllerActivity.DVBChannels.add(dvbChans);
                            dvbChans = new ArrayList<DVBChannel>();
                        }
                        DVBViewerControllerActivity.groupNames.add(group);
                        currentGroup = group;
                    }
                    dvbChans.add(dvbChannel);
                }
                DVBViewerControllerActivity.DVBChannels.add(dvbChans);

                lvAdapter = new ChanGroupAdapter(
                        getSherlockActivity(),
                        DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
                );

                lv.setAdapter(lvAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    public class ChanGroupAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public ChanGroupAdapter(Context context, String[] values) {
            super(context, R.layout.channels_group_list_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = null;

            if (convertView != null)
                v = convertView;
            else
                v = inflater.inflate(R.layout.channels_group_list_item, parent,
                        false);

            TextView chanGroup = (TextView) v
                    .findViewById(R.id.channels_group_list_item);
            chanGroup.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoCondensed);

            chanGroup.setText(values[position]);

            return v;
        }
    }
}
