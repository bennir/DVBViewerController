package de.bennir.DVBViewerController.epg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.bennir.DVBViewerController.R;

public class EPGInfoAdapter extends ArrayAdapter<EPGInfo> {
    private Context mContext;
    private ArrayList<EPGInfo> mEPGInfo;

    static class EPGInfoViewHolder {
        TextView title;
        TextView date;
        TextView time;
    }

    public EPGInfoAdapter(Context context, int resource, ArrayList<EPGInfo> epgInfo) {
        super(context, resource);

        mContext = context;
        mEPGInfo = epgInfo;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        EPGInfoViewHolder viewHolder;
        if(view == null) {
            viewHolder = new EPGInfoViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.epg_list_item, parent, false);

            viewHolder.title = (TextView) view.findViewById(R.id.epg_title);

            view.setTag(viewHolder);
        } else {
            viewHolder = (EPGInfoViewHolder) view.getTag();
        }

        viewHolder.title.setText(mEPGInfo.get(position).title);

        return view;
    }
}