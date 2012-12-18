package de.bennir.DVBViewerController.channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.bennir.DVBViewerController.R;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * User: miriam
 * Date: 18.12.12
 * Time: 14:28
 */
public class ChannelAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> groupNames;
    private ArrayList<ArrayList<DVBChannel>> DVBChannels;

    private LayoutInflater inflater;

    public ChannelAdapter(Context context, ArrayList<String> groups,
                          ArrayList<ArrayList<DVBChannel>> channels) {
        this.groupNames = groups;
        this.DVBChannels = channels;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return DVBChannels.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // Max 1024 childs per group
        return (long) (groupPosition * 1024 + childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View v = null;
        if (convertView != null)
            v = convertView;
        else
            v = inflater.inflate(R.layout.channels_channel_list_item, parent,
                    false);

        DVBChannel chan = (DVBChannel) getChild(groupPosition, childPosition);
        TextView channel = (TextView) v.findViewById(R.id.channel_item_name);
//        ImageView logo = (ImageView) v.findViewById(R.id.channel_item_logo);
//
//        URL logoURL = null;
//        try {
//            logoURL = new URL("http://" + MainActivity.dvbIp + ":"
//                    + MainActivity.dvbPort + "/" + "?getChannelLogo="
//                    + URLEncoder.encode(chan.name, "UTF-8"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        new ImageLoaderTask(logo).execute(logoURL);

        channel.setText(chan.name);

        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return DVBChannels.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (long) (groupPosition * 1024);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View v = null;

        if (convertView != null)
            v = convertView;
        else
            v = inflater.inflate(R.layout.channels_group_list_item, parent,
                    false);

        String group = (String) getGroup(groupPosition);
        TextView chanGroup = (TextView) v
                .findViewById(R.id.channels_group_list_item);

        chanGroup.setText(group);

        return v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public void onGroupExpanded(int groupPosition) {
    }

}