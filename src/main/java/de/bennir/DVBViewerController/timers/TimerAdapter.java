package de.bennir.DVBViewerController.timers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.bennir.DVBViewerController.DVBViewerControllerActivity;
import de.bennir.DVBViewerController.R;

public class TimerAdapter extends ArrayAdapter<DVBTimer> {
    private static final String TAG = TimerAdapter.class.toString();

    private final Context mContext;
    private List<DVBTimer> timers;
    private List<DVBTimer> deleteTimers;

    public TimerAdapter(List<DVBTimer> timers, Context context) {
        super(context, R.layout.timers_list_item, timers);
        this.mContext = context;
        this.timers = timers;
        this.deleteTimers = new ArrayList<DVBTimer>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;

        if (convertView != null)
            v = convertView;
        else
            v = inflater.inflate(R.layout.timers_list_item, parent,
                    false);

        DVBTimer timer = timers.get(position);

        final int pos = position;

        ImageButton btn = (ImageButton) v.findViewById(R.id.timer_list_item_delete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteTimer(pos);
            }
        });

        TextView timerName = (TextView) v.findViewById(R.id.timer_list_item_name);
        timerName.setTypeface(DVBViewerControllerActivity.robotoLight);
        timerName.setText(timer.name);
        Drawable img;
        if (timer.enabled)
            img = mContext.getResources().getDrawable(R.drawable.indicator_enabled);
        else
            img = mContext.getResources().getDrawable(R.drawable.indicator_disabled);
        img.setBounds(0, 2, 30, 30);

        ((ImageView) v.findViewById(R.id.timer_list_item_indicator)).setImageDrawable(img);

        TextView date = (TextView) v.findViewById(R.id.timer_list_item_date);
        date.setTypeface(DVBViewerControllerActivity.robotoLight);
        date.setText(timer.date);


        TextView time = (TextView) v.findViewById(R.id.timer_list_item_time);
        time.setTypeface(DVBViewerControllerActivity.robotoLight);
        time.setText(timer.start.split(":")[0] +
                ":" + timer.start.split(":")[1] +
                " - " + timer.end.split(":")[0] +
                ":" + timer.end.split(":")[1]);

        TextView channel = (TextView) v.findViewById(R.id.timer_list_item_channel);
        channel.setTypeface(DVBViewerControllerActivity.robotoLight);
        String channelId = timer.channelId;
        channel.setText(channelId.substring(channelId.indexOf('|') + 1));

        checkIfTimerDelete(v, timer);

        return v;
    }

    public void delete(DVBTimer delete) {
        Log.d(TAG, "Delete Timer " + delete.id);
        deleteTimers.add(delete);
        notifyDataSetChanged();
    }

    private void checkIfTimerDelete(View v, DVBTimer timer) {
        for (DVBTimer delete : deleteTimers) {
            Log.d(TAG, "checkIfTimerDelete");
            deleteIfMarkedDeleteable(v, timer, delete);
        }
    }

    private void deleteIfMarkedDeleteable(View v, final DVBTimer timer, final DVBTimer delete) {
        if (timerIsDeleteable(timer, delete)) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v, "translationX", 0f, 2500f);
            anim.setDuration(400);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d(TAG, "Delete onAnimationEnd");
                    timers.remove(timer);
                    deleteTimers.remove(delete);
//                    lv.invalidate();
//
//                        lvAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private boolean timerIsDeleteable(DVBTimer timer, DVBTimer delete) {
        Log.d(TAG, "timerIsDeleteAble " + timer.id.equals(delete.id));
        return timer.id.equals(delete.id);
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
