package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class EPGFragment extends SherlockListFragment {
    private static final String TAG = EPGFragment.class.toString();
    private static ListView channelList;
    private String currentChan = "";
    private SlidingMenu slidingMenu;
    private AQuery aq;
    private ListView lv;
    private EPGInfoAdapter lvAdapter;
    private ArrayList<EPGInfo> epgInfos;
    private TextView title;
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
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.dvbviewer_controller)
                .showImageForEmptyUri(R.drawable.dvbviewer_controller)
                .showImageOnFail(R.drawable.dvbviewer_controller)
                .cacheInMemory()
                .cacheOnDisc()
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getSherlockActivity())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        load = ImageLoader.getInstance();
        load.init(config);

        channelList = (ListView) slidingMenu.findViewById(R.id.epg_channel_list);


        ArrayList<DVBChannel> epgChannels = new ArrayList<DVBChannel>();
        for (ArrayList<DVBChannel> dvbChans : DVBViewerControllerActivity.DVBChannels) {
            Log.d(TAG, "Adding " + dvbChans.size() + " chans");
            epgChannels.addAll(dvbChans);
        }
        Log.d(TAG, "epgChannels Count " + epgChannels.size());
        Collections.sort(epgChannels, new DVBChannel.DVBChannelNameComparator());

        EPGChannelAdapter epgListAdapter = new EPGChannelAdapter(getSherlockActivity(), epgChannels);

        Log.d(TAG, "Adapter Count: " + epgListAdapter.getCount());
        channelList.setAdapter(epgListAdapter);

        title = ((TextView) getSherlockActivity()
                .findViewById(R.id.epg_channel_name));
        title.setTypeface(((DVBViewerControllerActivity) getSherlockActivity()).robotoCondensed);

        channelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentChan = ((TextView) view.findViewById(R.id.epg_list_item_name)).getText().toString();
                updateTitle();
                updateEPG();

                slidingMenu.showContent();
            }
        });

        updateTitle();

        lv = getListView();
        epgInfos = new ArrayList<EPGInfo>();
        lvAdapter = new EPGInfoAdapter(getSherlockActivity(), epgInfos);

        updateEPG();
    }

    private void updateEPG() {
        if (!currentChan.equals("")) {
            epgInfos.clear();

            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                /**
                 * Get EPG
                 */
            } else {
                EPGInfo epgInfo = new EPGInfo();
                epgInfo.title = "Tagesschau";
                epgInfo.desc = "Heute mit ganz tollen, neuen Nachrichten";

                for (int i = 0; i < 20; i++) {
                    epgInfos.add(epgInfo);
                }
            }

            lv.setAdapter(lvAdapter);
            lvAdapter.notifyDataSetChanged();
        }
    }

    private void updateTitle() {
        if (!currentChan.equals("")) {
            title.setText(currentChan);

            ImageView logo = (ImageView) getSherlockActivity().findViewById(R.id.epg_channel_logo);

            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                String url;
                try {
                    url = "http://" +
                            DVBViewerControllerActivity.dvbIp + ":" +
                            DVBViewerControllerActivity.dvbPort +
                            "/?getChannelLogo=" + URLEncoder.encode(currentChan, "UTF-8");
                    load.displayImage(url, logo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                logo.setImageDrawable(
                        getSherlockActivity().getResources()
                                .getDrawable(R.drawable.dvbviewer_controller)
                );
            }
        }
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

    public class EPGInfoAdapter extends ArrayAdapter<EPGInfo> {
        Context context;
        ArrayList<EPGInfo> epgInfos;

        public EPGInfoAdapter(Context context, ArrayList<EPGInfo> epgInfo) {
            super(context, R.layout.epg_info_item, epgInfo);
            this.epgInfos = epgInfo;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v;

            if (convertView != null)
                v = convertView;
            else
                v = inflater.inflate(R.layout.epg_info_item, parent, false);

            ((TextView) v.findViewById(R.id.epg_item_title)).setText(epgInfos.get(position).title);
            ((TextView) v.findViewById(R.id.epg_item_description)).setText(epgInfos.get(position).desc);


            return v;
        }
    }

    public class EPGChannelAdapter extends ArrayAdapter<DVBChannel> {
        ArrayList<DVBChannel> chans;
        Context context;

        public EPGChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
            super(context, R.layout.epg_channel_list_item, dvbChans);

            chans = dvbChans;

            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v;

            if (convertView != null)
                v = convertView;
            else
                v = inflater.inflate(R.layout.epg_channel_list_item, parent,
                        false);

            ((TextView) v.findViewById(R.id.epg_list_item_name)).setText(chans.get(position).name);
            ((TextView) v.findViewById(R.id.epg_list_item_channelid)).setText(chans.get(position).channelId);

            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                String url;
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
