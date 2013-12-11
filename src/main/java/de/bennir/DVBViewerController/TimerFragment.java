package de.bennir.DVBViewerController;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.haarman.listviewanimations.itemmanipulation.AnimateDismissAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.List;

import de.bennir.DVBViewerController.service.DVBService;
import de.bennir.DVBViewerController.timers.TimerAdapter;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TimerFragment extends ListFragment {
    private static final String TAG = TimerFragment.class.toString();
    public static TimerAdapter lvAdapter;
    private ListView lv;
    private Context mContext;
    private DVBService mDVBService;

    private List<Integer> mSelectedItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mDVBService = DVBService.getInstance(mContext);
        mSelectedItems = new ArrayList<Integer>();


        setHasOptionsMenu(true);
        lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO: edit timer
            }
        });


        if (!mDVBService.getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
            if (mDVBService.getRecordingService().ip.equals("") || mDVBService.getRecordingService().port.equals("")) {
                Crouton.makeText(getActivity(), R.string.recservicefailed, Style.ALERT).show();
            } else if (mDVBService.getRecordingService().ip.equals("0.0.0.0") || mDVBService.getRecordingService().port.equals("0")) {
                Crouton.makeText(getActivity(), R.string.recserviceConfigFail, Style.ALERT).show();
            } else {
                Log.d(TAG, "Recording Service IP:" + mDVBService.getRecordingService().ip);
                Log.d(TAG, "Recording Service Port:" + mDVBService.getRecordingService().port);
            }
        }

        if (mDVBService.getDVBTimers().isEmpty())
            updateTimerList();

        lvAdapter = new TimerAdapter(mDVBService.getDVBTimers(), mContext);
        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(lvAdapter);
        swingBottomInAnimationAdapter.setInitialDelayMillis(0);
        swingBottomInAnimationAdapter.setAbsListView(lv);
        final AnimateDismissAdapter<String> animateDismissAdapter = new AnimateDismissAdapter<String>(swingBottomInAnimationAdapter, new MyOnDismissCallback());
        animateDismissAdapter.setAbsListView(lv);
        lv.setAdapter(animateDismissAdapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                try {
                    if (checked) {
                        mSelectedItems.add(position);
                    } else {
                        mSelectedItems.remove(position);
                    }

                    mode.setTitle(mContext.getString(R.string.item_is_selected, mSelectedItems.size()));
                } catch (IndexOutOfBoundsException ex) {
                    Log.e(TAG, ex.getMessage());
                }

            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.timers_menu_delete:
                        animateDismissAdapter.animateDismiss(mSelectedItems);
                        mSelectedItems.clear();

                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.timers_action_mode, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                mSelectedItems.clear();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });


    }

    private class MyOnDismissCallback implements OnDismissCallback {

        @Override
        public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                lvAdapter.remove(lvAdapter.getItem(position));
            }
        }
    }

    private void updateTimerList() {
        Style st = new Style.Builder()
                .setConfiguration(DVBViewerControllerActivity.croutonInfinite)
                .setBackgroundColorValue(Style.holoBlueLight)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .build();
        Crouton.makeText(getActivity(), mContext.getResources().getString(R.string.loadingTimers), st).show();

        mDVBService.loadTimers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.refresh);
        item.setIcon(R.drawable.ic_action_refresh);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                updateTimerList();
                return true;
            }
        });

        item = menu.add(1, Menu.NONE, Menu.NONE, R.string.timer_add);
        item.setIcon(R.drawable.ic_action_add);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(getActivity(), TimerWizardActivity.class);
                startActivityForResult(i, 1);

                getActivity().overridePendingTransition(R.anim.slide_right, R.anim.nothing);

                return true;
            }
        });
    }


}
