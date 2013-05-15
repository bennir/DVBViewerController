package de.bennir.DVBViewerController.channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.bennir.DVBViewerController.DVBViewerControllerActivity;
import de.bennir.DVBViewerController.R;

public class ChanGroupAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;


    public ChanGroupAdapter(Context context, String[] values) {
        super(context, R.layout.channels_group_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;

        if (convertView != null)
            v = convertView;
        else
            v = inflater.inflate(R.layout.channels_group_list_item, parent,
                    false);

        TextView chanGroup = (TextView) v.findViewById(R.id.channels_group_list_item);
        chanGroup.setTypeface(((DVBViewerControllerActivity) context).robotoLight);

        chanGroup.setText(values[position]);

        return v;
    }
}