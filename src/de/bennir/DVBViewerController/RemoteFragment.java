package de.bennir.DVBViewerController;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

/**
 * User: benni
 * Date: 16.12.12
 * Time: 16:22
 */
public class RemoteFragment extends SherlockFragment {
    final String TAG = RemoteFragment.class.toString();

    AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.remote_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        aq = new AQuery(getSherlockActivity());

        ImageView remote = (ImageView) getSherlockActivity().findViewById(
                R.id.remote);

        remote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int coords[] = new int[2];
                v.getLocationOnScreen(coords);

                int x = (int) event.getRawX() - coords[0];
                int y = (int) event.getRawY() - coords[1];

                ImageView img = (ImageView) getActivity().findViewById(
                        R.id.remote_touchmap);
                Bitmap bitmap = ((BitmapDrawable) img.getDrawable())
                        .getBitmap();
                int pixel = bitmap.getPixel(x, y);

                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                /**
                 * Button Events
                 */

                // Chan+
                if (red == 119 && blue == 119 && green == 119) {
                    // Log.e("Event: ", "Chan+");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendUp");
                }
                // Chan-
                if (red == 0 && blue == 0 && green == 0) {
                    // Log.e("Event: ", "Chan-");
                    sendCommand("sendDown");
                }
                // Vol+
                if (red == 49 && blue == 49 && green == 49) {
                    // Log.e("Event: ", "Vol+");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendRight");
                }
                // Vol-
                if (red == 204 && blue == 204 && green == 204) {
                    // Log.e("Event: ", "Vol-");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendLeft");
                }
                // Menu
                if (red == 0 && blue == 255 && green == 255) {
                    // Log.e("Event: ", "Menu");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendMenu");
                }
                // Ok
                if (red == 255 && blue == 255 && green == 0) {
                    // Log.e("Event: ", "Ok");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendOk");
                }
                // Back
                if (red == 255 && blue == 0 && green == 168) {
                    // Log.e("Event: ", "Back");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendBack");
                }
                // Red
                if (red == 255 && blue == 0 && green == 0) {
                    // Log.e("Event: ", "Red");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendRed");
                }
                // Yellow
                if (red == 255 && blue == 0 && green == 255) {
                    // Log.e("Event: ", "Yellow");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendYellow");
                }
                // Green
                if (red == 0 && blue == 0 && green == 255) {
                    // Log.e("Event: ", "Green");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendGreen");
                }
                // Blue
                if (red == 0 && blue == 255 && green == 0) {
                    // Log.e("Event: ", "Blue");
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    sendCommand("sendBlue");
                }

                return false;
            }
        });

    }

    public void sendCommand(String command) {
        if (DVBViewerControllerActivity.dvbHost != "Demo Device") {
            String url = "http://" +
                    DVBViewerControllerActivity.dvbIp + ":" +
                    DVBViewerControllerActivity.dvbPort +
                    "/?"+command;

            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String html, AjaxStatus status) {

                }

            });
        }
    }
}
