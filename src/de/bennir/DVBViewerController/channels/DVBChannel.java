package de.bennir.DVBViewerController.channels;

import android.os.Parcel;
import android.os.Parcelable;
import de.bennir.DVBViewerController.epg.EPGInfo;

import java.util.Comparator;

public class DVBChannel implements Parcelable {
    public static final Parcelable.Creator<DVBChannel> CREATOR = new Parcelable.Creator<DVBChannel>() {
        public DVBChannel createFromParcel(Parcel in) {
            return new DVBChannel(in);
        }

        public DVBChannel[] newArray(int size) {
            return new DVBChannel[size];
        }
    };
    private static final String TAG = DVBChannel.class.toString();
    public String name;
    public String favoriteId;
    public String channelId;
    public EPGInfo epgInfo = new EPGInfo();

    private DVBChannel(Parcel in) {
        name = in.readString();
        favoriteId = in.readString();
        channelId = in.readString();
        epgInfo = in.readParcelable(EPGInfo.class.getClassLoader());
    }

    public DVBChannel() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(favoriteId);
        out.writeString(channelId);
        out.writeParcelable(epgInfo, 0);

    }

    public static class DVBChannelComparator implements Comparator<DVBChannel> {

        @Override
        public int compare(DVBChannel dvbChannel, DVBChannel dvbChannel2) {
            return dvbChannel.name.compareTo(dvbChannel2.name);
        }
    }
}
