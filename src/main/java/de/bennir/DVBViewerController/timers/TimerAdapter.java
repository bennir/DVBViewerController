package de.bennir.DVBViewerController.timers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.bennir.DVBViewerController.DVBViewerControllerActivity;
import de.bennir.DVBViewerController.R;

public class TimerAdapter extends ArrayAdapter<DVBTimer> {
    private static final String TAG = TimerAdapter.class.toString();

    static class TimerViewHolder {
        ImageButton btn;
        TextView name;
        ImageView indicator;
        TextView date;
        TextView time;
        TextView channel;
    }

    private Context mContext;
    private List<DVBTimer> timers;

    public TimerAdapter(List<DVBTimer> timers, Context context) {
        super(context, R.layout.timers_list_item, timers);
        this.mContext = context;
        this.timers = timers;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TimerViewHolder viewHolder;

        if (view == null) {
            view = inflater.inflate(R.layout.timers_list_item, parent, false);

            viewHolder = new TimerViewHolder();
            viewHolder.btn = (ImageButton) view.findViewById(R.id.timer_list_item_delete);
            viewHolder.name = (TextView) view.findViewById(R.id.timer_list_item_name);
            viewHolder.indicator = (ImageView) view.findViewById(R.id.timer_list_item_indicator);
            viewHolder.date = (TextView) view.findViewById(R.id.timer_list_item_date);
            viewHolder.time = (TextView) view.findViewById(R.id.timer_list_item_time);
            viewHolder.channel = (TextView) view.findViewById(R.id.timer_list_item_channel);

            view.setTag(viewHolder);
        } else {
            viewHolder = (TimerViewHolder) view.getTag();
        }


        final DVBTimer timer = timers.get(position);
        final View v = view;
        viewHolder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                v
                        .animate()
                        .setDuration(150)
                        .translationX(1000)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                TimerAdapter.this.remove(timer);
                                TimerAdapter.this.notifyDataSetChanged();
                                v.setAlpha(1);
                            }
                        });
            }
        });

        viewHolder.name.setTypeface(DVBViewerControllerActivity.robotoLight);
        viewHolder.name.setText(timer.name);

        Drawable img;
        if (timer.enabled)
            img = mContext.getResources().getDrawable(R.drawable.indicator_enabled);
        else
            img = mContext.getResources().getDrawable(R.drawable.indicator_disabled);
        img.setBounds(0, 2, 30, 30);
        viewHolder.indicator.setImageDrawable(img);

        viewHolder.date.setTypeface(DVBViewerControllerActivity.robotoLight);
        viewHolder.date.setText(timer.date);

        viewHolder.time.setTypeface(DVBViewerControllerActivity.robotoLight);
        viewHolder.time.setText(timer.start.split(":")[0] +
                ":" + timer.start.split(":")[1] +
                " - " + timer.end.split(":")[0] +
                ":" + timer.end.split(":")[1]);

        String channelId = timer.channelId;
        viewHolder.channel.setTypeface(DVBViewerControllerActivity.robotoLight);
        viewHolder.channel.setText(channelId.substring(channelId.indexOf('|') + 1));

        return v;
    }

    @Override
    public int getCount() {
        return timers.size();
    }

    @Override
    public DVBTimer getItem(int position) {
        return timers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
