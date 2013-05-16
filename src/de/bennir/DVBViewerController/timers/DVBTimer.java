package de.bennir.DVBViewerController.timers;

import android.os.Parcel;
import android.os.Parcelable;

public class DVBTimer implements Parcelable {
    public static final Parcelable.Creator<DVBTimer> CREATOR = new Parcelable.Creator<DVBTimer>() {
        public DVBTimer createFromParcel(Parcel in) {
            return new DVBTimer(in);
        }

        public DVBTimer[] newArray(int size) {
            return new DVBTimer[size];
        }
    };
    public String id = "";
    public String name = "";
    public String channelId = "";
    public boolean enabled = true;
    public String date = "";
    public String start = "";
    public String duration = "";
    public String end = "";

    public DVBTimer() {
    }

    public DVBTimer(Parcel in) {
        id = in.readString();
        name = in.readString();
        channelId = in.readString();
        enabled = in.readByte() == 1;
        date = in.readString();
        start = in.readString();
        duration = in.readString();
        end = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(channelId);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString(date);
        dest.writeString(start);
        dest.writeString(duration);
        dest.writeString(end);
    }
}
