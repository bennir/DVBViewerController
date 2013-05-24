package de.bennir.DVBViewerController;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends SherlockListFragment {
    private static final String TAG = TimerFragment.class.toString();
    private static TimerAdapter lvAdapter;
    private static ListView lv;
    private AQuery aq;

    public static void addTimersToListView() {
        lvAdapter.notifyDataSetChanged();
        lv.setAdapter(lvAdapter);
        lv.invalidate();
    }

    void deleteTimer(int position) {
        final DVBTimer timer = DVBViewerControllerActivity.DVBTimers.get(position);
        Log.d(TAG, "deleteTimer: " + timer.id);

        if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {

            String url = "http://" +
                    DVBViewerControllerActivity.recIp + ":" +
                    DVBViewerControllerActivity.recPort +
                    "/api/timerdelete.html?id=" + timer.id;

            Log.d(TAG, "Deleting Timer: " + url);

            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String html, AjaxStatus status) {
                    // Status Ok
                    if(status.getCode() == 200) {
                        lvAdapter.delete(timer);
                    }
                }

            });
        } else {
            lvAdapter.delete(timer);
        }

    }

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
        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Click Click Clack");
                //asd

            }
        });
        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;

        if (DVBViewerControllerActivity.recIp.equals("") || DVBViewerControllerActivity.recPort.equals("")) {
            Crouton.makeText(getSherlockActivity(), R.string.recservicefailed, Style.ALERT).show();
        } else {
            Log.d(TAG, "Recording Service IP:" + DVBViewerControllerActivity.recIp);
            Log.d(TAG, "Recording Service Port:" + DVBViewerControllerActivity.recPort);
        }

        lv = getListView();
        lvAdapter = new TimerAdapter(DVBViewerControllerActivity.DVBTimers, getSherlockActivity());

        ((DVBViewerControllerActivity) getSherlockActivity()).updateTimers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((DVBViewerControllerActivity) getSherlockActivity()).updateTimers();
                addTimersToListView();

                return true;
            }
        });

        item = menu.add(1, Menu.NONE, Menu.NONE, R.string.timer_add);
        item.setIcon(R.drawable.ic_action_add);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(getSherlockActivity(), TimerWizardActivity.class);
                startActivity(i);

                getSherlockActivity().overridePendingTransition(R.anim.slide_right, R.anim.nothing);

                return true;
            }
        });
    }

    public class TimerAdapter extends ArrayAdapter<DVBTimer> {
        private final Context context;
        private List<DVBTimer> timers;
        private List<DVBTimer> deleteTimers;

        public TimerAdapter(List<DVBTimer> timers, Context context) {
            super(context, R.layout.timers_list_item, timers);
            this.context = context;
            this.timers = timers;
            this.deleteTimers = new ArrayList<DVBTimer>();
        }

        public void delete(DVBTimer delete) {
            deleteTimers.add(delete);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
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
                    deleteTimer(pos);
                }
            });

            TextView timerName = (TextView) v.findViewById(R.id.timer_list_item_name);
            timerName.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
            timerName.setText(timer.name);
            Drawable img;
            if (timer.enabled)
                img = context.getResources().getDrawable(R.drawable.indicator_enabled);
            else
                img = context.getResources().getDrawable(R.drawable.indicator_disabled);
            img.setBounds(0, 2, 30, 30);

            ((ImageView) v.findViewById(R.id.timer_list_item_indicator)).setImageDrawable(img);

            TextView date = (TextView) v.findViewById(R.id.timer_list_item_date);
            date.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
            date.setText(timer.date);


            TextView time = (TextView) v.findViewById(R.id.timer_list_item_time);
            time.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                time.setText(timer.start + " - " + timer.end);
            } else {
                time.setText(R.string.timer_time);
            }

            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                String channelId = timer.channelId;
                ((TextView) v.findViewById(R.id.timer_list_item_channel)).setText(channelId.substring(channelId.indexOf('|') + 1));
            }

            checkIfTimerDelete(v, timer);

            return v;
        }

        private void checkIfTimerDelete(View v, DVBTimer timer) {
            for (DVBTimer delete : deleteTimers) {
                deleteIfMarkedDeleteable(v, timer, delete);
            }
        }

        private void deleteIfMarkedDeleteable(View v, final DVBTimer timer, final DVBTimer delete) {
            if (timerIsDeleteable(timer, delete)) {
                Log.d(TAG, "deleteIfMarkedDeleteable: " + timer.id);

                ObjectAnimator anim = ObjectAnimator.ofFloat(v, "translationX", 0f, 2500f);
                anim.setDuration(400);
                anim.start();
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        timers.remove(timer);
                        deleteTimers.remove(delete);

                        addTimersToListView();
                    }
                });
            }
        }

        private boolean timerIsDeleteable(DVBTimer timer, DVBTimer delete) {
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
}
