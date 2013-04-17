package de.bennir.DVBViewerController;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import de.bennir.DVBViewerController.channels.DVBChannel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChannelGroupFragment extends SherlockListFragment {
    private static final String TAG = ChannelGroupFragment.class.toString();
    ListView lv;
    AQuery aq;
    private ImageLoader load;

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
        aq = new AQuery(getSherlockActivity());

        addChannelsToListView();
    }

    private void addChannelsToListView() {
        ArrayList<DVBChannel> chans = DVBViewerControllerActivity.DVBChannels.get(DVBViewerControllerActivity.currentGroup);
        Log.d(TAG, "Chan Count: " + chans.size());

        DVBChannelAdapter lvAdapter = new DVBChannelAdapter(
                getSherlockActivity(),
                chans
        );

        lv.setAdapter(lvAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((DVBViewerControllerActivity)getSherlockActivity()).updateChannelList();
                addChannelsToListView();

                return true;
            }
        });
    }

    public void setChannel(String channelId) {
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

    public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
        ArrayList<DVBChannel> chans;
        Context context;

        public DVBChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
            super(context, R.layout.channels_channel_list_item, dvbChans);
            this.chans = dvbChans;
            this.context = context;

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory()
                    .cacheOnDisc()
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();

            load = ImageLoader.getInstance();
            load.init(config);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = null;

            if (convertView != null)
                v = convertView;
            else
                v = inflater.inflate(R.layout.channels_channel_list_item, parent,
                        false);

            ((TextView) v.findViewById(R.id.channel_item_name)).setText(chans.get(position).name);
            ((TextView) v.findViewById(R.id.channel_item_current_epg)).setText(chans.get(position).epgTitle);
            ((TextView) v.findViewById(R.id.channel_item_current_epg_time)).setText(chans.get(position).epgTime);
            ((TextView) v.findViewById(R.id.channel_item_favid)).setText(chans.get(position).favoriteId);

            if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
                String url = null;
                try {
                    url = "http://" +
                            DVBViewerControllerActivity.dvbIp + ":" +
                            DVBViewerControllerActivity.dvbPort +
                            "/?getChannelLogo=" + URLEncoder.encode(chans.get(position).name, "UTF-8");

                    ImageView logo = (ImageView) v.findViewById(R.id.channel_item_logo);
                    load.displayImage(url, logo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            return v;
        }


    }

}
