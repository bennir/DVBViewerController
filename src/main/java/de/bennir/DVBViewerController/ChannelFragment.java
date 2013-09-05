package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
import de.bennir.DVBViewerController.util.DVBService;

public class ChannelFragment extends ListFragment {
    private static final String TAG = ChannelFragment.class.toString();
    public static ChanGroupAdapter lvAdapter;
    private static ListView lv;
    private static Context mContext;
    private DVBService mDVBService;

//    public static void addChannelsToListView() {
//        Log.d(TAG, "addChannelsToListView");
//        if (lvAdapter == null) {
//            ChannelFragment.lvAdapter = new ChanGroupAdapter(mContext, DVBViewerControllerActivity.groupNames);
//        } else {
//            lvAdapter.notifyDataSetChanged();
//        }
//        lv.setAdapter(lvAdapter);
//        lv.invalidate();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_listview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mDVBService = DVBService.getInstance(mContext);

        Log.d(TAG, "onActivityCreated Size: " + mDVBService.getDVBChannels().size());

        super.onActivityCreated(savedInstanceState);
        ((DVBViewerControllerActivity) getActivity()).mContent = this;

        setHasOptionsMenu(true);
        getActivity().getActionBar().setTitle(R.string.channels);

        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getActivity() instanceof DVBViewerControllerActivity) {
                    DVBViewerControllerActivity.currentGroup = i;
                    DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
                    act.switchContent(new ChannelGroupFragment(), mDVBService.getGroupNames().get(i), R.drawable.ic_action_channels, true);
                }
            }
        });

        ChannelFragment.lvAdapter = new ChanGroupAdapter(mContext, mDVBService.getGroupNames());
//        addChannelsToListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mDVBService.updateChannelList();

                return true;
            }
        });
    }
}
