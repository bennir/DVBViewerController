package de.bennir.DVBViewerController.channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import de.bennir.DVBViewerController.DVBViewerControllerActivity;
import de.bennir.DVBViewerController.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
    ArrayList<DVBChannel> chans;
    Context context;
    ImageLoader load;

    public DVBChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
        super(context, R.layout.channels_channel_list_item, dvbChans);
        this.chans = dvbChans;
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
            v = inflater.inflate(R.layout.channels_channel_list_item, parent,
                    false);

        ((TextView) v.findViewById(R.id.channel_item_name)).setText(chans.get(position).name);
        ((TextView) v.findViewById(R.id.channel_item_current_epg)).setText(chans.get(position).epgInfo.title);
        ((TextView) v.findViewById(R.id.channel_item_current_epg_time)).setText(chans.get(position).epgInfo.time);
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
        } else {
            ((ImageView) v.findViewById(R.id.channel_item_logo))
                    .setImageDrawable(
                            context.getResources()
                                    .getDrawable(R.drawable.dvbviewer_controller)
                    );
        }

        return v;
    }


}