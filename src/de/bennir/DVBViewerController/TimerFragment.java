package de.bennir.DVBViewerController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidquery.AQuery;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TimerFragment extends SherlockFragment {
    private static final String TAG = TimerFragment.class.toString();
    AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.timer_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        aq = ((DVBViewerControllerActivity) getSherlockActivity()).aq;

        if (DVBViewerControllerActivity.recIp.equals("") || DVBViewerControllerActivity.recPort.equals("")) {
            Crouton.makeText(getSherlockActivity(), R.string.recservicefailed, Style.ALERT).show();
        } else {
            TextView tx = (TextView) getSherlockActivity().findViewById(R.id.timer_recserv);
            tx.setText(DVBViewerControllerActivity.recIp + ":" + DVBViewerControllerActivity.recPort);
        }
    }
}
