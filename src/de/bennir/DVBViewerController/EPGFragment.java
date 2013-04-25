package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import de.bennir.DVBViewerController.channels.DVBChannel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class EPGFragment extends SherlockFragment {
    private static final String TAG = EPGFragment.class.toString();
    SlidingMenu slidingMenu;
    public static ListView channelList;
    AQuery aq;
    private ImageLoader load;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.epg_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        slidingMenu = ((DVBViewerControllerActivity) getSherlockActivity()).menu;

        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setSecondaryMenu(R.layout.epg_channel_list);
        slidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_inverse);

        slidingMenu.showSecondaryMenu();

        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;
        channelList = (ListView) slidingMenu.findViewById(R.id.epg_channel_list);


        ArrayList<DVBChannel> epgChannels = new ArrayList<DVBChannel>();
        for(ArrayList<DVBChannel> dvbChans : DVBViewerControllerActivity.DVBChannels) {
            Log.d(TAG, "Adding " + dvbChans.size() + " chans");
            epgChannels.addAll(dvbChans);
        }
        Log.d(TAG, "epgChannels Count " +epgChannels.size());
        Collections.sort(epgChannels, new DVBChannel.DVBChannelComparator());

        EPGChannelAdapter lvAdapter = new EPGChannelAdapter(getSherlockActivity(), epgChannels);


        Log.d(TAG, "Adapter Count: "+lvAdapter.getCount());
        channelList.setAdapter(lvAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        slidingMenu.setMode(SlidingMenu.LEFT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.channels);
        item.setIcon(R.drawable.ic_action_channels);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (slidingMenu.isSecondaryMenuShowing()) {
                    slidingMenu.toggle();
                } else {
                    slidingMenu.showSecondaryMenu();
                }

                return true;
            }
        });
    }

    public class EPGChannelAdapter extends ArrayAdapter<DVBChannel> {
        ArrayList<DVBChannel> chans;
        Context context;

        public EPGChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
            super(context, R.layout.epg_channel_list, dvbChans);

            chans = dvbChans;

            this.context = context;

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.dvbviewer_controller)
                    .showImageForEmptyUri(R.drawable.dvbviewer_controller)
                    .showImageOnFail(R.drawable.dvbviewer_controller)
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
                v = inflater.inflate(R.layout.epg_channel_list_item, parent,
                        false);

            ((TextView) v.findViewById(R.id.epg_list_item_name)).setText(chans.get(position).name);
            ((TextView) v.findViewById(R.id.epg_list_item_channelid)).setText(chans.get(position).channelId);

            if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
                String url = null;
                try {
                    url = "http://" +
                            DVBViewerControllerActivity.dvbIp + ":" +
                            DVBViewerControllerActivity.dvbPort +
                            "/?getChannelLogo=" + URLEncoder.encode(chans.get(position).name, "UTF-8");

                    ImageView logo = (ImageView) v.findViewById(R.id.epg_list_item_logo);
                    load.displayImage(url, logo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                ((ImageView) v.findViewById(R.id.epg_list_item_logo))
                        .setImageDrawable(
                                context.getResources()
                                        .getDrawable(R.drawable.dvbviewer_controller)
                        );
            }

            return v;
        }


    }

}
