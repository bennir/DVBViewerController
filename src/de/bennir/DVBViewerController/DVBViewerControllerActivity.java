package de.bennir.DVBViewerController;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class DVBViewerControllerActivity extends SlidingFragmentActivity {
	private final String TAG = "DVBViewerControllerActivity";
	private final int mTitleRes;

	protected ListFragment mFrag;

	public DVBViewerControllerActivity() {
		mTitleRes = R.string.app_name;
	}

	public DVBViewerControllerActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);

		setContentView(R.layout.main);
		setBehindContentView(R.layout.main);

		SlidingMenu menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.menu);

	}
}
