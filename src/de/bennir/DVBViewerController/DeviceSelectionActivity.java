package de.bennir.DVBViewerController;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.bennir.DVBViewerController.util.ThreadExecutor;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;

public class DeviceSelectionActivity extends SherlockListActivity implements ServiceListener {
    private final static String TAG = DeviceSelectionActivity.class.toString();

    Typeface robotoThin;
    Typeface robotoLight;
    Typeface robotoCondensed;

    private final static String CTRL_TYPE = "_dvbctrl._tcp.local.";
    private final static String HOSTNAME = "DVBController";
    private final static int DELAY = 500;
    private static JmDNS zeroConf = null;
    private static WifiManager.MulticastLock mLock = null;
    public Handler resultsUpdated = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                boolean result = adapter.notifyFound((String) msg.obj);

                if (result) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };
    private ListView list;
    private ServiceAdapter adapter;
    private ArrayList<String> items = new ArrayList<String>();

    protected void startProbe() throws Exception {
        if (zeroConf != null)
            stopProbe();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.known.clear();
                adapter.notifyDataSetChanged();
            }
        });

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiinfo = wifi.getConnectionInfo();
        int intaddr = wifiinfo.getIpAddress();

        if (intaddr != 0) {
            byte[] byteaddr = new byte[]{(byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff),
                    (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff)};
            InetAddress addr = InetAddress.getByAddress(byteaddr);

            Log.d(TAG, String.format("found intaddr=%d, addr=%s", intaddr, addr.toString()));

            mLock = wifi.createMulticastLock("DVBController Lock");
            mLock.setReferenceCounted(true);
            mLock.acquire();

            zeroConf = JmDNS.create(addr, HOSTNAME);
            zeroConf.addServiceListener(CTRL_TYPE, DeviceSelectionActivity.this);
        } else {
            // Check Wifi State
            Crouton.makeText(this, R.string.wifistate, Style.ALERT).show();
        }
    }

    protected void stopProbe() {
        zeroConf.removeServiceListener(CTRL_TYPE, DeviceSelectionActivity.this);

        ThreadExecutor.runTask(new Runnable() {
            @Override
            public void run() {
                try {
                    zeroConf.close();
                    zeroConf = null;
                } catch (Exception e) {
                    Log.d(TAG, String.format("ZeroConf Error: %s", e.getMessage()));
                }
            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deviceselection);

        robotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        robotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        robotoCondensed = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Bold.ttf");

        ((TextView) findViewById(R.id.select_device)).setTypeface(robotoLight);

        adapter = new ServiceAdapter(this);
        list = getListView();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ServiceInfo serviceInfo = (ServiceInfo) adapter.getItem(position);

                String title = serviceInfo.getPropertyString("CtlN");
                if (title == null)
                    title = serviceInfo.getName();
                final String server = serviceInfo.getHostAddresses()[0];
                final String port = String.valueOf(serviceInfo.getPort());

                Intent mIntent = new Intent(getApplicationContext(), DVBViewerControllerActivity.class);
                mIntent.putExtra("dvbHost", title);
                mIntent.putExtra("dvbIp", server);
                mIntent.putExtra("dvbPort", port);

                startActivity(mIntent);

                try {
                    DeviceSelectionActivity.this.stopProbe();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DeviceSelectionActivity.this.finish();
                overridePendingTransition(R.anim.slide_right, R.anim.nothing);
            }
        });

        ThreadExecutor.runTask(new Runnable() {
            @Override
            public void run() {
                try {
                    startProbe();
                } catch (Exception e) {
                    Log.d(TAG, String.format("onCreate Error: %s", e.getMessage()));
                }
            }
        });

        Button btnSkip = (Button) findViewById(R.id.button_skip);
        btnSkip.setOnClickListener(new

                                           View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   DVBViewerControllerActivity.dvbHost = "Demo Device";
                                                   DVBViewerControllerActivity.dvbIp = "127.0.0.1";
                                                   DVBViewerControllerActivity.dvbPort = "8000";
                                                   DVBViewerControllerActivity.recIp = "127.0.0.1";
                                                   DVBViewerControllerActivity.recPort = "8080";

                                                   Intent i = new Intent(DeviceSelectionActivity.this, DVBViewerControllerActivity.class);
                                                   startActivity(i);

                                                   try {
                                                       DeviceSelectionActivity.this.stopProbe();
                                                   } catch (Exception e) {
                                                       e.printStackTrace();
                                                   }

                                                   DeviceSelectionActivity.this.finish();
                                                   overridePendingTransition(R.anim.slide_right, R.anim.nothing);
                                               }
                                           });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh")
                .setIcon(R.drawable.ic_action_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            adapter.known.clear();
            adapter.notifyDataSetChanged();
            ThreadExecutor.runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        startProbe();
                    } catch (Exception e) {
                        Log.d(TAG, String.format("onCreate Error: %s", e.getMessage()));
                    }
                }
            });
        }
        return false;
    }

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {
        Log.w(TAG, String.format("serviceAdded(event=\n%s\n)", serviceEvent.toString()));
        final String name = serviceEvent.getName();

        resultsUpdated.sendMessageDelayed(Message.obtain(resultsUpdated, -1, name), DELAY);
    }

    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {
        Log.w(TAG, String.format("serviceRemoved(event=\n%s\n)", serviceEvent.toString()));
    }

    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
        Log.w(TAG, String.format("serviceResolved(event=\n%s\n)", serviceEvent.toString()));
    }

    public class ServiceAdapter extends BaseAdapter {
        protected Context context;
        protected LayoutInflater inflater;
        protected LinkedList<ServiceInfo> known = new LinkedList<ServiceInfo>();

        public ServiceAdapter(Context context) {
            this.context = context;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public boolean notifyFound(String dvbctrl) {
            boolean result = false;

            try {
                Log.d(TAG, String.format("DNS Name: %s", dvbctrl));
                ServiceInfo serviceInfo = zeroConf.getServiceInfo(CTRL_TYPE, dvbctrl);

                if (serviceInfo == null)
                    return result;

                String dvbctrlName = serviceInfo.getPropertyString("CtlN");
                if (dvbctrlName == null)
                    dvbctrlName = serviceInfo.getName();

                for (ServiceInfo service : known) {
                    String knownName = service.getPropertyString("CtlN");
                    if (knownName == null)
                        knownName = service.getName();
                    if (dvbctrlName.equalsIgnoreCase(knownName)) {
                        Log.w(TAG, "Already have DatabaseId loaded = " + dvbctrlName);
                        return result;
                    }
                }

                if (!known.contains(serviceInfo)) {
                    known.add(serviceInfo);
                    result = true;
                }
            } catch (Exception e) {
                Log.d(TAG, String.format("Problem getting ZeroConf information %s", e.getMessage()));
            }

            return result;
        }

        @Override
        public int getCount() {
            return known.size();
        }

        @Override
        public Object getItem(int position) {
            return known.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.service_list_item, parent, false);

            ((TextView) convertView.findViewById(R.id.service_name)).setTypeface(robotoCondensed);
            ((TextView) convertView.findViewById(R.id.service_ip)).setTypeface(robotoLight);
            ((TextView) convertView.findViewById(R.id.service_port)).setTypeface(robotoLight);

            try {
                final ServiceInfo serviceInfo = (ServiceInfo) this.getItem(position);

                String title = serviceInfo.getPropertyString("CtlN");
                if (title == null)
                    title = serviceInfo.getName();
                final String server = serviceInfo.getHostAddresses()[0];
                final String port = String.valueOf(serviceInfo.getPort());

                Log.d(TAG, String.format("ZeroConf Server: %s", serviceInfo.getServer()));
                Log.d(TAG, String.format("ZeroConf Port: %s", serviceInfo.getPort()));
                Log.d(TAG, String.format("ZeroConf Title: %s", title));

                ((TextView) convertView.findViewById(R.id.service_name)).setText(title);
                ((TextView) convertView.findViewById(R.id.service_ip)).setText(server);
                ((TextView) convertView.findViewById(R.id.service_port)).setText(port);
            } catch (Exception e) {
                Log.d(TAG, String.format("Problem getting ZeroConf information %s", e.getMessage()));
                ((TextView) convertView.findViewById(R.id.service_name)).setText("Unknown");
                ((TextView) convertView.findViewById(R.id.service_ip)).setText("Unknown");
                ((TextView) convertView.findViewById(R.id.service_port)).setText("Unknown");

            }


            return convertView;
        }
    }
}