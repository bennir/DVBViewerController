package de.bennir.DVBViewerController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class EPGFragment extends SherlockFragment {
    private static final String TAG = EPGFragment.class.toString();
    SlidingMenu slidingMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.epg_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        slidingMenu = ((DVBViewerControllerActivity) getSherlockActivity()).menu;

        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setSecondaryMenu(R.layout.epg_channel_list);
        slidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_inverse);

        slidingMenu.showSecondaryMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        slidingMenu.setMode(SlidingMenu.LEFT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(2, 2, 1, R.string.channels);
        item.setIcon(R.drawable.ic_action_channels);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (slidingMenu.isSecondaryMenuShowing()) {
                    slidingMenu.toggle();
                } else {
                    slidingMenu.showSecondaryMenu();
                }

                return true;
            }
        });
    }
}
