package de.bennir.DVBViewerController.epg;

import android.os.Parcel;
import android.os.Parcelable;

public class EPGInfo implements Parcelable {
    public static final Creator<EPGInfo> CREATOR = new Creator<EPGInfo>() {
        @Override
        public EPGInfo createFromParcel(Parcel in) {
            return new EPGInfo(in);
        }

        @Override
        public EPGInfo[] newArray(int size) {
            return new EPGInfo[size];
        }
    };
    private static final String TAG = EPGInfo.class.toString();
    public String time = "";
    public String channel = "";
    public String title = "";
    public String desc = "";
    public String duration = "";

    private EPGInfo(Parcel in) {
        time = in.readString();
        channel = in.readString();
        title = in.readString();
        desc = in.readString();
        duration = in.readString();
    }

    public EPGInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(time);
        out.writeString(channel);
        out.writeString(title);
        out.writeString(desc);
        out.writeString(duration);
    }
}