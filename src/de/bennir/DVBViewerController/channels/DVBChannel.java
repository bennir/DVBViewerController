package de.bennir.DVBViewerController.channels;

import android.os.Parcel;
import android.os.Parcelable;

public class DVBChannel implements Parcelable {
    private static final String TAG = DVBChannel.class.toString();
    public String name;
    public String favoriteId;
    public String channelId;
    public String epgTitle = "";
    public String epgTime = "";
    public String epgDuration = "";

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(favoriteId);
        out.writeString(channelId);
        out.writeString(epgTitle);
        out.writeString(epgTime);
        out.writeString(epgDuration);

    }

    public static final Parcelable.Creator<DVBChannel> CREATOR = new Parcelable.Creator<DVBChannel>() {
        public DVBChannel createFromParcel(Parcel in) {
            return new DVBChannel(in);
        }

        public DVBChannel[] newArray(int size) {
            return new DVBChannel[size];
        }
    };

    private DVBChannel(Parcel in) {
        name = in.readString();
        favoriteId = in.readString();
        channelId = in.readString();
        epgTitle = in.readString();
        epgTime = in.readString();
        epgDuration = in.readString();
    }

    public DVBChannel() {
    }
}
