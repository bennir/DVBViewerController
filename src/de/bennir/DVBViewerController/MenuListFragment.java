package de.bennir.DVBViewerController;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MenuAdapter adapter = new MenuAdapter(getActivity());

        adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));
        adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels));
        adapter.add(new DVBMenuItem(getString(R.string.timer), R.drawable.ic_action_timers));
        adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg));
        adapter.add(new DVBMenuItem(getString(R.string.settings), R.drawable.ic_action_settings));

		setListAdapter(adapter);
	}

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        Fragment newContent = null;
        int titleRes = 0;
        int icon = 0;
        switch (position) {
            case 0:
                // Remote
                newContent = new RemoteFragment();
                titleRes = R.string.remote;
                icon = R.drawable.ic_action_remote;
                break;
            case 1:
                // Channels
                newContent = new ChannelFragment();
                titleRes = R.string.channels;
                icon = R.drawable.ic_action_channels;
                break;
            case 2:
                // Timers
                newContent = new TimerFragment();
                titleRes = R.string.timer;
                icon = R.drawable.ic_action_timers;
                break;
            case 3:
                // EPG
                newContent = new EPGFragment();
                titleRes = R.string.epg;
                icon = R.drawable.ic_action_epg;
                break;
            case 4:
                // Settings
                newContent = new SettingsFragment();
                titleRes = R.string.settings;
                icon = R.drawable.ic_action_settings;
                break;
        }
        if (newContent != null)
            switchFragment(newContent, titleRes, icon);
    }

    private void switchFragment(Fragment fragment, int titleRes, int icon) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof DVBViewerControllerActivity) {
            DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
            act.switchContent(fragment, titleRes, icon);
        }
    }

	private class DVBMenuItem {
		public String tag;
		public int iconRes;

		public DVBMenuItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class MenuAdapter extends ArrayAdapter<DVBMenuItem> {

		public MenuAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
}
