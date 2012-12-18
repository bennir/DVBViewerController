package de.bennir.DVBViewerController;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class DVBViewerControllerActivity extends SherlockFragmentActivity {
	private final String TAG = "DVBViewerControllerActivity";

    private Fragment mContent;

    private SlidingMenu menu;

    public static String dvbIp = "1.2.3.4";
    public static String dvbPort = "8000";
    public static String recIp = "";
    public static String recPort = "";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.remote);
        getSupportActionBar().setIcon(R.drawable.ic_action_remote);

        /**
         * Above View
         */
        // set the Above View
        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        if (mContent == null)
            mContent = new RemoteFragment();

        // set the Above View
        setContentView(R.layout.main);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mContent)
                .commit();


        /**
         * Behind View
         */
		menu = new SlidingMenu(this);
        menu.setMenu(R.layout.menu);

        /**
         * Menu Customize
         */
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
        menu.setBehindScrollScale(0.5f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menu.toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "mContent", mContent);
    }

    public void switchContent(Fragment fragment, int titleRes, int icon) {
        getSupportActionBar().setTitle(titleRes);
        getSupportActionBar().setIcon(icon);
        mContent = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        menu.showContent();
    }
}
