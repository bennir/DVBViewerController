package de.bennir.DVBViewerController.service;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.bennir.DVBViewerController.ChannelFragment;
import de.bennir.DVBViewerController.ChannelGroupFragment;
import de.bennir.DVBViewerController.TimerFragment;
import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
import de.bennir.DVBViewerController.channels.DVBChannel;
import de.bennir.DVBViewerController.epg.EPGInfo;
import de.bennir.DVBViewerController.timers.DVBTimer;
import de.bennir.DVBViewerController.timers.TimerAdapter;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class DVBService {
    private static final String TAG = DVBService.class.toString();
    public static final String DEMO_DEVICE = "demo_device";
    public static final String DVBHOST_KEY = "dvb_host";
    public static final String DVBIP_KEY = "dvb_ip";
    public static final String DVBPORT_KEY = "dvb_port";

    public Ion mIon;
    private Object channelGroup = new Object();

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
     * Sends a simple actions.ini Command
     */
    public void sendCommand(String command) {
        Log.d(TAG, "Remote Command: " + command);

        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
        if (!getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
            String url = getDVBServer().createRequestString(command);

            Ion.with(mContext, url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String s) {

                        }
                    });
        }
    }

    /**
     * Load Recording Service Ip and Port
     */
    private void loadRecordingService() {
        mRecordingService = new RecordingService();

        if (!mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            if (mRecordingService.ip.equals("") || mRecordingService.port.equals("")) {
                String url = mDVBServer.createRequestString("getRecordingService");
                Log.d(TAG, "Load RecSrv: " + url);
                Ion.with(mContext, url)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                JsonObject recordingService = result.getAsJsonObject("recordingService");

                                mRecordingService.ip = recordingService.get("ip").getAsString();
                                mRecordingService.port = recordingService.get("port").getAsString();

                                Log.d(TAG, "RecordingService: " + mRecordingService.ip + ":" + mRecordingService.port);

                                if (!mRecordingService.ip.equals("0.0.0.0") && !mRecordingService.port.equals("0")) {
                                    if (getDVBTimers().isEmpty()) {
                                        Log.d(TAG, "DVBTimers empty");
                                        loadTimers();
                                    }
                                }
                            }
                        });
            }
        }
    }

    /**
     * Load Timers
     */
    public void loadTimers() {
        if (!mRecordingService.ip.equals("0.0.0.0") && !mRecordingService.port.equals("0")) {
            Log.d(TAG, "Updating Timers");
            DVBTimers.clear();

            if (mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
                Log.d(TAG, "Demo Timers!");
                createDemoTimers();
            } else {
                String url = mRecordingService.createRequestString("timerlist.html?utf8=");
                Log.d(TAG, "Loading Timers: " + url);
                mIon.with(mContext, url)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String s) {
                                DVBTimer timer;

                                try {
                                    Document doc = getDomElement(s);
                                    NodeList nl = doc.getElementsByTagName("Timer");

                                    for (int i = 0; i < nl.getLength(); i++) {
                                        Element element = (Element) nl.item(i);

                                        timer = new DVBTimer();

                                        // Attributes
                                        timer.id = element.getAttribute("ID");
                                        timer.enabled = !element.getAttribute("Enabled").equals("0");
                                        timer.date = element.getAttribute("Date");
                                        timer.start = element.getAttribute("Start");
                                        timer.duration = element.getAttribute("Dur");
                                        timer.end = element.getAttribute("End");

                                        // Childs
                                        timer.name = getElementValue(element.getElementsByTagName("Descr").item(0));

                                        Element channel = (Element) element.getElementsByTagName("Channel").item(0);
                                        timer.channelId = channel.getAttribute("ID");

                                        DVBTimers.add(timer);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                if (TimerFragment.lvAdapter == null)
                                    TimerFragment.lvAdapter = new TimerAdapter(getDVBTimers(), mContext);
                                else
                                    TimerFragment.lvAdapter.notifyDataSetChanged();
                            }
                        });
            }
        } else {
            Crouton.cancelAllCroutons();
            Log.d(TAG, "Timer cannot be loaded, RecService config fail");
        }
    }

    /**
     * Load Channels
     */
    public void loadChannels() {
        Log.d(TAG, "Updating Channels");
        groupNames.clear();
        DVBChannels.clear();
        DVBChannels = new ArrayList<ArrayList<DVBChannel>>();

        if (mDVBServer.host.equals(DVBService.DEMO_DEVICE)) {
            // Create Demo Content
            createDemoChannels();
            Crouton.cancelAllCroutons();
        } else {
            String url = mDVBServer.createRequestString("getFavList");
            Log.d(TAG, "URL=" + url);

            mIon.getDefault(mContext)
                    .cancelAll(channelGroup);

            mIon.with(mContext, url)
                    .setLogging("DVBService", Log.DEBUG)
                    .group(channelGroup)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject jsonObject) {
                            ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();

                            try {
                                JsonArray channelsJSON = jsonObject.getAsJsonArray("channels");
                                String currentGroup = "";

                                for (int i = 0; i < channelsJSON.size(); i++) {
                                    if (channelsJSON.get(i).isJsonObject()) {
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
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            /**
                             * ChannelFragment
                             */
                            if (ChannelFragment.lvAdapter == null) {
                                ChannelFragment.lvAdapter = new ChanGroupAdapter(mContext, getGroupNames());
                            } else {
                                ChannelFragment.lvAdapter.notifyDataSetChanged();
                            }

                            /**
                             * ChannelGroupFragment
                             */
                            if (ChannelGroupFragment.lvAdapter != null) {
                                ChannelGroupFragment.lvAdapter.notifyDataSetChanged();
                            }
                            Crouton.cancelAllCroutons();
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

        if (TimerFragment.lvAdapter == null)
            TimerFragment.lvAdapter = new TimerAdapter(getDVBTimers(), mContext);
        else
            TimerFragment.lvAdapter.notifyDataSetChanged();

        Crouton.cancelAllCroutons();
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

        /**
         * ChannelFragment
         */
        if (ChannelFragment.lvAdapter == null) {
            ChannelFragment.lvAdapter = new ChanGroupAdapter(mContext, getGroupNames());
        } else {
            ChannelFragment.lvAdapter.notifyDataSetChanged();
        }

        /**
         * ChannelGroupFragment
         */
        if (ChannelGroupFragment.lvAdapter != null) {
            ChannelGroupFragment.lvAdapter.notifyDataSetChanged();
        }
    }

    public Document getDomElement(String xml) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }

    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }

    public final String getElementValue(Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * Returns or creates the current instance
     *
     * @param context Android Context
     * @param server  Server Data
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
