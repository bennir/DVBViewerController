package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.androidquery.AQuery;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;

public class TimerFragment extends SherlockListFragment {
    private static final String TAG = TimerFragment.class.toString();
    static TimerAdapter lvAdapter;
    static ListView lv;
    AQuery aq;

    public static void addChannelsToListView() {
        lvAdapter.notifyDataSetChanged();
        lv.setAdapter(lvAdapter);
        lv.invalidate();
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

        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;
        lv = getListView();

        if (DVBViewerControllerActivity.recIp.equals("") || DVBViewerControllerActivity.recPort.equals("")) {
            Crouton.makeText(getSherlockActivity(), R.string.recservicefailed, Style.ALERT).show();
        } else {
            Log.d(TAG, "Recording Service IP:" + DVBViewerControllerActivity.recIp);
            Log.d(TAG, "Recording Service Port:" + DVBViewerControllerActivity.recPort);
        }

        lv = getListView();
        lvAdapter = new TimerAdapter(getSherlockActivity(), DVBViewerControllerActivity.DVBTimers);

        ((DVBViewerControllerActivity) getSherlockActivity()).updateTimers();
    }

    public class TimerAdapter extends ArrayAdapter<DVBTimer> {
        private final Context context;
        ArrayList<DVBTimer> timers;

        public TimerAdapter(Context context, ArrayList<DVBTimer> timers) {
            super(context, R.layout.timers_list_item, timers);
            this.context = context;
            this.timers = timers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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

            return v;
        }
    }
}
