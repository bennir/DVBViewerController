package de.bennir.DVBViewerController.channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import de.bennir.DVBViewerController.R;
import de.bennir.DVBViewerController.util.DVBService;

public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
    private static final String TAG = DVBChannelAdapter.class.toString();
    private ArrayList<DVBChannel> chans;
    private Context mContext;
    private DVBService mDVBService;

    public DVBChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
        super(context, R.layout.channels_channel_list_item, dvbChans);
        this.chans = dvbChans;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;

        if (convertView != null)
            v = convertView;
        else
            v = inflater.inflate(R.layout.channels_channel_list_item, parent,
                    false);

        mDVBService = DVBService.getInstance(mContext);

        ((TextView) v.findViewById(R.id.channel_item_name)).setText(chans.get(position).name);
        ((TextView) v.findViewById(R.id.channel_item_current_epg)).setText(chans.get(position).epgInfo.time + " - " + chans.get(position).epgInfo.title);
        ((TextView) v.findViewById(R.id.channel_item_favid)).setText(chans.get(position).favoriteId);

        /**
         * Duration Progress
         */
        if (!mDVBService.getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String curTime = format.format(new Date());
            String startTime = chans.get(position).epgInfo.time;
            String duration = chans.get(position).epgInfo.duration;

            Date curDate;
            Date startDate;
            Date durDate = new Date();
            long diff = 0;

            try {
                curDate = format.parse(curTime);
                startDate = format.parse(startTime);
                durDate = format.parse(duration);

                diff = curDate.getTime() - startDate.getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            double elapsed = (diff / 1000 / 60);
            long durMinutes = (durDate.getHours() * 60 + durDate.getMinutes());

            ProgressBar progress = (ProgressBar) v.findViewById(R.id.channel_item_progress);
            progress.setProgress(Double.valueOf((elapsed / durMinutes * 100)).intValue());
        } else {
            ProgressBar progress = (ProgressBar) v.findViewById(R.id.channel_item_progress);
            progress.setProgress(Double.valueOf(new Random().nextInt(100)).intValue());

        }

        if (!mDVBService.getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
            String url = "";
            try {
                url = mDVBService.getDVBServer().createRequestString("?getChannelLogo=" + URLEncoder.encode(chans.get(position).name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

//            aq.id(R.id.channel_item_logo).image(url, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);

            ImageView logo = (ImageView) v.findViewById(R.id.channel_item_logo);
            mDVBService.mIon.with(mContext, url)
                    .withBitmap()
                    .animateIn(R.anim.fadein)
                    .intoImageView(logo);
        }
        return v;
    }


}