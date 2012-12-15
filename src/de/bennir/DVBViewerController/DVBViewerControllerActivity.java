package de.bennir.DVBViewerController;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class DVBViewerControllerActivity extends SherlockFragmentActivity {
	private final String TAG = "DVBViewerControllerActivity";
    private SlidingMenu menu;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Remote");
        getSupportActionBar().setIcon(R.drawable.ic_action_menu);

        setContentView(R.layout.main);


		menu = new SlidingMenu(this);
        menu.setMenu(R.layout.menu);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
        menu.setBehindScrollScale(0.5f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

	}

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menu.toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
