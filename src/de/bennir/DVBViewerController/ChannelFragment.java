package de.bennir.DVBViewerController;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpResponse;
import de.bennir.DVBViewerController.channels.ChannelAdapter;
import de.bennir.DVBViewerController.channels.ChannelListParcelable;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChannelFragment extends SherlockListFragment {
    final String TAG = "ChannelFragment";

    ListView lv;
    ChanGroupAdapter lvAdapter;
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

        aq = new AQuery(getSherlockActivity());

        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (DVBChannel chan : DVBChannels.get(i)) {
                    Log.d(TAG, chan.name);
                }
            }
        });

        if (DVBChannels.isEmpty()) {
            updateChannelList();
        } else {
            lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));
        }

        Crouton.makeText(getActivity(), R.string.channelgroup_crouton, Style.INFO).show();
    }

    private void updateChannelList() {
        groupNames.clear();
        DVBChannels.clear();
        chanParcel.clear();

        if (DVBViewerControllerActivity.dvbHost == "Demo Device") {
            groupNames.clear();
            DVBChannels.clear();

            groupNames.add("ARD");
            ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();
            DVBChannel test = new DVBChannel();

            test.name = "Das Erste";
            test.group = "ARD";
            testChans.add(test);
            testChans.add(test);
            DVBChannels.add(testChans);

            groupNames.add("ZDF");
            testChans = new ArrayList<DVBChannel>();
            test = new DVBChannel();

            test.name = "ZDF HD";
            test.group = "ZDF";
            testChans.add(test);
            testChans.add(test);
            DVBChannels.add(testChans);

            lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));
            lv.setAdapter(lvAdapter);
        } else {
            ProgressDialog dialog = new ProgressDialog(getSherlockActivity());

            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setInverseBackgroundForced(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setTitle(R.string.loadingChannels);

            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "/?getFavList";
            Log.d(TAG, "URL=" + url);
            aq.progress(dialog).ajax(url, JSONObject.class, this, "downloadChannelCallback");

            AsyncHttpClient.getDefaultInstance().get(url, new AsyncHttpClient.JSONObjectCallback() {
                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, JSONObject result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    // Do something with JSON
                }
            });
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

    public void setChannel(String channelId) {
        if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
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
    }

    public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
        Log.d(TAG, "downloadChannelCallback");
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
                    dvbChannel.group = chan.getString("group");
                    dvbChannel.epgTitle = URLDecoder.decode(chan.getString("epgtitle"));
                    dvbChannel.epgTime = chan.getString("epgtime");
                    dvbChannel.epgDuration = chan.getString("epgduration");

                    String group = chan.getString("group");
                    if (!group.equals(currentGroup)) {
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

                lvAdapter = new ChanGroupAdapter(getSherlockActivity(), groupNames.toArray(new String[groupNames.size()]));

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

            chanGroup.setText(values[position]);

            return v;
        }
    }
}
