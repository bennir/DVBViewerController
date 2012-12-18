package de.bennir.DVBViewerController.channels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * User: miriam
 * Date: 18.12.12
 * Time: 14:30
 */
public class ChannelListParcelable implements Parcelable {
    public ArrayList<DVBChannel> channels;

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(channels);
    }

    public ChannelListParcelable() {
        ;
    };

    private ChannelListParcelable(Parcel in) {
        in.readList(channels, null);
    }

    public static final Parcelable.Creator<ChannelListParcelable> CREATOR = new Parcelable.Creator<ChannelListParcelable>() {
        public ChannelListParcelable createFromParcel(Parcel in) {
            return new ChannelListParcelable(in);
        }

        @Override
        public ChannelListParcelable[] newArray(int size) {
            return new ChannelListParcelable[size];
        }
    };
}
