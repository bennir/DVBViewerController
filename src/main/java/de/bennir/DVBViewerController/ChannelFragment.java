package de.bennir.DVBViewerController;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
import de.bennir.DVBViewerController.service.DVBService;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ChannelFragment extends ListFragment {
    private static final String TAG = ChannelFragment.class.toString();
    public static ChanGroupAdapter lvAdapter;
    private ListView lv;
    private Context mContext;
    private DVBService mDVBService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_listview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mContext = getActivity();
        mDVBService = DVBService.getInstance(mContext.getApplicationContext());

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

        lvAdapter = new ChanGroupAdapter(mContext, mDVBService.getGroupNames());
        lv.setAdapter(lvAdapter);
    }

    public void updateChannelList() {
        lv.invalidate();

        Style st = new Style.Builder()
                .setConfiguration(DVBViewerControllerActivity.croutonInfinite)
                .setBackgroundColorValue(Style.holoBlueLight)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .build();
        Crouton.makeText(getActivity(), mContext.getResources().getString(R.string.loadingChannels), st).show();

        mDVBService.loadChannels();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                updateChannelList();

                return true;
            }
        });
    }
}
