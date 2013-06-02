package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class EPGFragment extends SherlockListFragment {
    private static final String TAG = EPGFragment.class.toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

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
