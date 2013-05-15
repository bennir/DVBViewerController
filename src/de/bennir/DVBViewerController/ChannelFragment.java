package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import de.bennir.DVBViewerController.channels.ChanGroupAdapter;

class ChannelFragment extends SherlockListFragment {
    private static final String TAG = ChannelFragment.class.toString();
    private static ListView lv;
    static ChanGroupAdapter lvAdapter;
    private static Context context;

    public static void addChannelsToListView() {
        if (lvAdapter == null) {
            ChannelFragment.lvAdapter = new ChanGroupAdapter(
                    context,
                    DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
            );
        } else {
            lvAdapter.notifyDataSetChanged();
        }
        lv.setAdapter(lvAdapter);
        lv.invalidate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.channel_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated Size: " + DVBViewerControllerActivity.DVBChannels.size());
        super.onActivityCreated(savedInstanceState);
        ((DVBViewerControllerActivity) getSherlockActivity()).mContent = this;

        setHasOptionsMenu(true);
        getSherlockActivity().getSupportActionBar().setTitle(R.string.channels);

        context = getSherlockActivity();
        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getActivity() instanceof DVBViewerControllerActivity) {
                    DVBViewerControllerActivity.currentGroup = i;
                    DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
                    act.switchContent(new ChannelGroupFragment(), DVBViewerControllerActivity.groupNames.get(i), R.drawable.ic_action_channels, true);
                }
            }
        });

        addChannelsToListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((DVBViewerControllerActivity) getSherlockActivity()).updateChannelList();
                addChannelsToListView();

                return true;
            }
        });
    }
}
