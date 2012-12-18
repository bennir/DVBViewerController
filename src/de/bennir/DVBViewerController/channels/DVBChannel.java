package de.bennir.DVBViewerController.channels;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: miriam
 * Date: 18.12.12
 * Time: 14:34
 */
public class DVBChannel implements Parcelable {
    public String name;
    public String group;
    public String favoriteId;
    public String channelId;
    public Bitmap logo;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(group);
        out.writeString(favoriteId);
        out.writeString(channelId);
        out.writeParcelable(logo, flags);

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
        group = in.readString();
        favoriteId = in.readString();
        channelId = in.readString();
        logo = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public DVBChannel() {
        ;
    }
}
