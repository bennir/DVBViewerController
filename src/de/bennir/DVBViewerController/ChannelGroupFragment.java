package de.bennir.DVBViewerController;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.channels.DVBChannelAdapter;

import java.util.ArrayList;

public class ChannelGroupFragment extends SherlockListFragment {
    private static final String TAG = ChannelGroupFragment.class.toString();
    public static DVBChannelAdapter lvAdapter;
    private static ListView lv;
    private View activeView;
    private AQuery aq;

    public static void addChannelsToListView() {
        lv.setAdapter(lvAdapter);
        lvAdapter.notifyDataSetChanged();
        lv.invalidate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String channelId = ((TextView) view.findViewById(R.id.channel_item_favid)).getText().toString();
                setChannel(channelId);
            }
        });

        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;

        ArrayList<DVBChannel> chans = DVBViewerControllerActivity.DVBChannels.get(DVBViewerControllerActivity.currentGroup);
        ChannelGroupFragment.lvAdapter = new DVBChannelAdapter(
                getSherlockActivity(),
                chans
        );

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showChannelMenu(view);
                return true;
            }
        });

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    clearChannelMenu();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        addChannelsToListView();
    }

    private void showChannelMenu(View view) {
        clearChannelMenu();
        activeView = view;
        LinearLayout subMenu = (LinearLayout) view.findViewById(R.id.channel_item_submenu);
        subMenu.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.channel_item_progress);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        subMenu.setLayoutParams(params);
    }

    private void clearChannelMenu() {
        if (activeView != null) {
            LinearLayout subMenu = (LinearLayout) activeView.findViewById(R.id.channel_item_submenu);
            subMenu.setVisibility(View.INVISIBLE);
            subMenu.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            activeView = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                lvAdapter.clear();
                lvAdapter.notifyDataSetChanged();
                ((DVBViewerControllerActivity) getSherlockActivity()).updateChannelList();

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                getSherlockActivity().getFragmentManager().popBackStackImmediate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setChannel(String channelId) {
        ((Vibrator) getSherlockActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
        if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "?setChannel=" + channelId;

            Log.d(TAG, "SetChannel " + url);

            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String html, AjaxStatus status) {

                }

            });
        }
    }
}
