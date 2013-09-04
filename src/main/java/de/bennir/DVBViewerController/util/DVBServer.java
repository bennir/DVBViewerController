package de.bennir.DVBViewerController.util;

public class DVBServer {
    public String host = "";
    public String ip = "";
    public String port = "";

    public String createRequestString(String command) {
        String ret = "http://" +
                ip + ":" +
                port +
                "/?" + command;

        return ret;
    }
}