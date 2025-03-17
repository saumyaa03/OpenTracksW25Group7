package de.dennisguse.opentracks;

import android.os.Parcel;
import android.os.Parcelable;

public record TrackId(long id) implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
    }

    public static final Creator<TrackId> CREATOR = new Creator<>() {
        public TrackId createFromParcel(Parcel in) {
            return new TrackId(in.readLong());
        }

        public TrackId[] newArray(int size) {
            return new TrackId[size];
        }
    };
} 