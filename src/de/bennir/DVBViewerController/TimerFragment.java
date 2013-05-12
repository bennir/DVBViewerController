package de.bennir.DVBViewerController;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.manuelpeinado.multichoiceadapter.MultiChoiceArrayAdapter;
import com.manuelpeinado.multichoiceadapter.MultiChoiceBaseAdapter;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TimerFragment extends SherlockListFragment {
    private static final String TAG = TimerFragment.class.toString();
    static TimerAdapter lvAdapter;
    static ListView lv;
    private AQuery aq;

    public static void addTimersToListView() {
        lvAdapter.notifyDataSetChanged();
        lvAdapter.setAdapterView(lv);
        lv.invalidate();
    }

    public void deleteTimer(int position) {
        if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
            DVBTimer timer = DVBViewerControllerActivity.DVBTimers.get(position);

            String url = "http://" +
                    DVBViewerControllerActivity.recIp + ":" +
                    DVBViewerControllerActivity.recPort +
                    "/api/timerdelete.html?id="+timer.id;

            Log.d(TAG, "Deleting Timer: " + url);

            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String html, AjaxStatus status) {

                }

            });
        }

        DVBViewerControllerActivity.DVBTimers.remove(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.timer_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        lv = getListView();
        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;

        if (DVBViewerControllerActivity.recIp.equals("") || DVBViewerControllerActivity.recPort.equals("")) {
            Crouton.makeText(getSherlockActivity(), R.string.recservicefailed, Style.ALERT).show();
        } else {
            Log.d(TAG, "Recording Service IP:" + DVBViewerControllerActivity.recIp);
            Log.d(TAG, "Recording Service Port:" + DVBViewerControllerActivity.recPort);
        }

        lv = getListView();
        lvAdapter = new TimerAdapter(savedInstanceState, DVBViewerControllerActivity.DVBTimers, getSherlockActivity());

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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        lvAdapter.save(outState);
    }

    public class TimerAdapter extends MultiChoiceBaseAdapter {
        private final Context context;
        List<DVBTimer> timers;

        public TimerAdapter(Bundle savedInstanceState, List<DVBTimer> timers, Context context) {
            super(savedInstanceState);
            this.context = context;
            this.timers = timers;
        }

        @Override
        public View getViewImpl(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = null;

            if (convertView != null)
                v = convertView;
            else
                v = inflater.inflate(R.layout.timers_list_item, parent,
                        false);

            TextView timerName = (TextView) v
                    .findViewById(R.id.timer_list_item_name);
            timerName.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
            timerName.setText(timers.get(position).name);
            Drawable img;
            if (timers.get(position).enabled)
                img = context.getResources().getDrawable(R.drawable.indicator_enabled);
            else
                img = context.getResources().getDrawable(R.drawable.indicator_disabled);
            img.setBounds(0, 2, 30, 30);
            timerName.setCompoundDrawables(img, null, null, null);

            TextView date = (TextView) v.findViewById(R.id.timer_list_item_date);
            date.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
            date.setText(timers.get(position).date);

            if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
                TextView time = (TextView) v.findViewById(R.id.timer_list_item_time);
                time.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoLight);
                time.setText(timers.get(position).start + " - " + timers.get(position).end);
            }

            String channelId = timers.get(position).channelId;
            ((TextView) v.findViewById(R.id.timer_list_item_channel)).setText(channelId.substring(channelId.indexOf('|') + 1));

            return v;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.timers_action_mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.timers_menu_delete) {
                Set<Long> checked = getCheckedItems();

                for(long pos : checked) {
                    deleteTimer((int)pos);
                }
                addTimersToListView();

                return true;
            }
            return false;
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
