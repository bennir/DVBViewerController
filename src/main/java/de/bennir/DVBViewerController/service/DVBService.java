package de.bennir.DVBViewerController.service;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.net.URLDecoder;
import java.util.ArrayList;

import de.bennir.DVBViewerController.ChannelFragment;
import de.bennir.DVBViewerController.DVBViewerControllerActivity;
import de.bennir.DVBViewerController.TimerFragment;
import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DVBService {
    private static final String TAG = DVBService.class.toString();
    public static final String DEMO_DEVICE = "demo_device";
    public static final String DVBHOST_KEY = "dvb_host";
    public static final String DVBIP_KEY = "dvb_ip";
    public static final String DVBPORT_KEY = "dvb_port";

    public Ion mIon;

    private Context mContext;

    private ArrayList<ArrayList<DVBChannel>> DVBChannels = new ArrayList<ArrayList<DVBChannel>>();
    private ArrayList<String> groupNames = new ArrayList<String>();
    private ArrayList<String> chanNames = new ArrayList<String>();
    private ArrayList<DVBTimer> DVBTimers = new ArrayList<DVBTimer>();

    private DVBServer mDVBServer;
    private RecordingService mRecordingService;

    private DVBService(Context context, DVBServer server) {
        mContext = context;
        mIon = Ion.getDefault(context);
        mDVBServer = server;

        loadRecordingService();
    }

    /**
     * Load Recording Service Ip and Port
     *
     */
    private void loadRecordingService() {
        mRecordingService = new RecordingService();

        if (!mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            if (mRecordingService.ip.equals("") || mRecordingService.port.equals("")) {
                Log.d(TAG, "Getting Recording Service");

                String url = mDVBServer.createRequestString("getRecordingService");

                mIon.with(mContext, url)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                JsonObject recordingService = result.getAsJsonObject("recordingService");

                                mRecordingService.ip = recordingService.get("ip").getAsString();
                                mRecordingService.port = recordingService.get("port").getAsString();

                                Log.d(TAG, "RecordingService: " + mRecordingService.ip + ":" + mRecordingService.port);
                            }
                        });
            }
        }
    }


    /**
     * Method to preload Timers
     */
    public void loadTimers() {
        Log.d(TAG, "Updating Timers");
        DVBTimers.clear();

        if (mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            createDemoTimers();
        } else {

            String url = mRecordingService.createRequestString("timerlist.html?utf8=");
            mIon.with(mContext, url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String s) {
                            Log.d(TAG, s);
                        }
                    });
        }

    }

    public void loadTimers(final TimerFragment.TimerAdapter lvAdapter) {
        Log.d(TAG, "updating channels");
        DVBTimers.clear();

        if (mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            createDemoTimers();
            lvAdapter.notifyDataSetChanged();
        } else {

            String url = mRecordingService.createRequestString("timerlist.html?utf8=");
            mIon.with(mContext, url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String s) {
                            Log.d(TAG, s);

                            //TODO: fetch XML Timers

                            lvAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void createDemoTimers() {
        DVBTimer timer;
        for (int i = 1; i <= 5; i++) {
            timer = new DVBTimer();
            timer.id = "Demo" + i;
            timer.name = "Timer " + i;
            timer.date = "11.11.2011";
            timer.enabled = i % 2 == 0;
            timer.start = "20:15";
            timer.end = "22:00";
            timer.channelId = "|" + timer.name;
            DVBTimers.add(timer);
        }
    }

    public void loadChannels() {
        Log.d(TAG, "updating channels");
        groupNames.clear();
        DVBChannels.clear();

        if (mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            // Create Demo Content
            createDemoChannels();
        } else {
            String url = mDVBServer.createRequestString("getFavList");
            Log.d(TAG, "URL=" + url);

            Style st = new Style.Builder()
                    .setConfiguration(DVBViewerControllerActivity.croutonInfinite)
                    .setBackgroundColorValue(Style.holoBlueLight)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .build();

//            Crouton.makeText(mContext, R.string.loadingChannels, st).show();

            mIon.with(mContext, url)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject jsonObject) {
                            ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();

                            JsonArray channelsJSON = jsonObject.getAsJsonArray("channels");
                            String currentGroup = "";

                            for (int i = 0; i < channelsJSON.size(); i++) {
                                if(channelsJSON.get(i).isJsonObject()) {
                                    JsonObject chan = (JsonObject) channelsJSON.get(i);

                                    DVBChannel dvbChannel = new DVBChannel();
                                    dvbChannel.name = chan.get("name").getAsString();
                                    dvbChannel.favoriteId = chan.get("id").getAsString();
                                    dvbChannel.channelId = chan.get("channelid").getAsString();
                                    dvbChannel.epgInfo.title = URLDecoder.decode(chan.get("epgtitle").getAsString());
                                    dvbChannel.epgInfo.time = chan.get("epgtime").getAsString();
                                    dvbChannel.epgInfo.duration = chan.get("epgduration").getAsString();

                                    String group = chan.get("group").getAsString();
                                    if (!group.equals(currentGroup)) {
                                        if (i > 0) {
                                            DVBChannels.add(dvbChans);
                                            dvbChans = new ArrayList<DVBChannel>();
                                        }
                                        groupNames.add(group);
                                        currentGroup = group;
                                    }
                                    chanNames.add(dvbChannel.name);
                                    dvbChans.add(dvbChannel);
                                } else {
                                    Log.d(TAG, "No JsonObject: " + channelsJSON.get(i).toString());
                                }
                            }

                            DVBChannels.add(dvbChans);
                        }
                    });
        }
    }

    private void createDemoChannels() {
        groupNames.add("ARD");
        ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();

        DVBChannel test = new DVBChannel();
        test.name = "Das Erste HD";
        EPGInfo epg = new EPGInfo();
        epg.channel = test.name;
        epg.desc = "Nachrichten";
        epg.time = "20:15";
        epg.title = "Nachrichten";
        test.epgInfo = epg;
        testChans.add(test);
        chanNames.add(test.name);
        for (int i = 0; i < 10; i++) {
            test = new DVBChannel();
            test.name = "NDR HD";
            epg = new EPGInfo();
            epg.channel = test.name;
            epg.desc = "Nachrichten";
            epg.time = "20:15";
            epg.title = "Nachrichten";
            test.epgInfo = epg;
            testChans.add(test);
            chanNames.add(test.name);
        }
        DVBChannels.add(testChans);

        groupNames.add("ZDF");
        testChans = new ArrayList<DVBChannel>();

        test = new DVBChannel();
        test.name = "ZDF HD";
        epg = new EPGInfo();
        epg.channel = test.name;
        epg.desc = "Nachrichten";
        epg.time = "20:15";
        epg.title = "Nachrichten";
        test.epgInfo = epg;
        testChans.add(test);
        chanNames.add(test.name);
        for (int i = 0; i < 10; i++) {
            test = new DVBChannel();
            test.name = "ZDF Kultur";
            epg = new EPGInfo();
            epg.channel = test.name;
            epg.desc = "Nachrichten";
            epg.time = "20:15";
            epg.title = "Nachrichten";
            test.epgInfo = epg;
            testChans.add(test);
            chanNames.add(test.name);
        }
        DVBChannels.add(testChans);


        ChannelFragment.lvAdapter = new ChanGroupAdapter(mContext, groupNames);
    }


    /**
     * Returns or creates the current instance
     *
     * @param context Android Context
     * @param server Server Data
     * @return DVBService instance
     */
    public static DVBService getInstance(Context context, DVBServer server) {
        if (_instance == null) {
            _instance = new DVBService(context, server);
        }

        return _instance;
    }

    public static DVBService getInstance(Context context) {
        if (_instance == null) {
            return null;
        }

        return _instance;
    }

    /**
     * getChannelByName
     *
     * @param name Name of the Channel
     * @return DVBChannel Object
     */
    public DVBChannel getChannelByName(String name) {
        DVBChannel ret = null;

        for (ArrayList<DVBChannel> chans : DVBChannels) {
            for (DVBChannel chan : chans) {
                if (chan.name.toLowerCase().equals(name.toLowerCase())) {
                    ret = chan;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Destroys the current instance
     *
     */
    public void destroy() {
        _instance = null;
    }


    /**
     * Getters and Setters
     */
    public ArrayList<ArrayList<DVBChannel>> getDVBChannels() {
        return DVBChannels;
    }

    public ArrayList<String> getGroupNames() {
        return groupNames;
    }

    public ArrayList<String> getChanNames() {
        return chanNames;
    }

    public ArrayList<DVBTimer> getDVBTimers() {
        return DVBTimers;
    }

    public RecordingService getRecordingService() {
        return mRecordingService;
    }

    public DVBServer getDVBServer() {
        return mDVBServer;
    }

    public class RecordingService {
        public String ip = "";
        public String port = "";

        public String createRequestString(String command) {
            String ret = "http://" +
                    ip + ":" +
                    port +
                    "/api/" + command;

            return ret;
        }
    }


    static DVBService _instance;
}
