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
import de.bennir.DVBViewerController.service.DVBService;

public class DVBChannelAdapter extends ArrayAdapter<DVBChannel> {
    private static final String TAG = DVBChannelAdapter.class.toString();

    static class DVBChannelViewHolder {
        TextView name;
        TextView epg;
        TextView favid;
        ProgressBar progress;
        ImageView logo;
    }

    private ArrayList<DVBChannel> chans;
    private Context mContext;
    private DVBService mDVBService;

    public DVBChannelAdapter(Context context, ArrayList<DVBChannel> dvbChans) {
        super(context, R.layout.channels_channel_list_item, dvbChans);
        this.chans = dvbChans;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DVBChannelViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.channels_channel_list_item, parent, false);

            viewHolder = new DVBChannelViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.channel_item_name);
            viewHolder.epg = (TextView) view.findViewById(R.id.channel_item_current_epg);
            viewHolder.favid = (TextView) view.findViewById(R.id.channel_item_favid);
            viewHolder.progress = (ProgressBar) view.findViewById(R.id.channel_item_progress);
            viewHolder.logo = (ImageView) view.findViewById(R.id.channel_item_logo);

            view.setTag(viewHolder);
        } else {
            viewHolder = (DVBChannelViewHolder) view.getTag();
        }

        mDVBService = DVBService.getInstance(mContext);

        viewHolder.name.setText(chans.get(position).name);
        viewHolder.epg.setText(chans.get(position).epgInfo.time + " - " + chans.get(position).epgInfo.title);
        viewHolder.favid.setText(chans.get(position).favoriteId);

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

            if (!startTime.equals("")) {
                try {
                    curDate = format.parse(curTime);
                    startDate = format.parse(startTime);
                    durDate = format.parse(duration);

                    diff = curDate.getTime() - startDate.getTime();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

            double elapsed = (diff / 1000 / 60);
            long durMinutes = (durDate.getHours() * 60 + durDate.getMinutes());

            viewHolder.progress.setProgress(Double.valueOf((elapsed / durMinutes * 100)).intValue());
        } else {
            viewHolder.progress.setProgress(Double.valueOf(new Random().nextInt(100)).intValue());
        }

        if (!mDVBService.getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
            String url = "";
            try {
                url = mDVBService.getDVBServer().createRequestString("getChannelLogo=" + URLEncoder.encode(chans.get(position).name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            mDVBService.mIon.with(mContext, url)
                    .withBitmap()
                    .animateIn(R.anim.fadein)
                    .intoImageView(viewHolder.logo);
        }

        return view;
    }


}