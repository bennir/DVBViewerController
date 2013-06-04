package de.bennir.DVBViewerController;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;

import java.util.ArrayList;

public class EPGFragment extends ListFragment implements ActionBar.OnNavigationListener {
    private static final String TAG = EPGFragment.class.toString();
    private static final String SELECTED_ITEM = "selected_navigation_item";

    private ActionBar actionBar;
    private ArrayAdapter<String> adapter;
    private ListView lv;
    private EPGInfoAdapter epgAdapter;

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

        actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        adapter = new ArrayAdapter<String>(
                actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item,
                android.R.id.text1,
                DVBViewerControllerActivity.chanNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        actionBar.setListNavigationCallbacks(adapter, EPGFragment.this);

        if(DVBViewerControllerActivity.currentEPGItem != -1) {
            actionBar.setSelectedNavigationItem(DVBViewerControllerActivity.currentEPGItem);
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_ITEM)) {
                actionBar.setSelectedNavigationItem(savedInstanceState.getInt(SELECTED_ITEM));
            }
        }

        updateEPG();
    }

    private void updateEPG() {
        String channel = adapter.getItem(actionBar.getSelectedNavigationIndex());

        ArrayList<EPGInfo> epgInfos = new ArrayList<EPGInfo>();
        DVBChannel chan = DVBViewerControllerActivity.getChannelByName(channel);
        EPGInfo epg = chan.epgInfo;

        epgInfos.add(epg);

        epgAdapter = new EPGInfoAdapter(getActivity(), epgInfos);

        lv.setAdapter(epgAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(SELECTED_ITEM, DVBViewerControllerActivity.currentEPGItem);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        DVBViewerControllerActivity.currentEPGItem = i;
        Log.d(TAG, "Channel Select: " + DVBViewerControllerActivity.chanNames.get(i));

        return true;
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
}
