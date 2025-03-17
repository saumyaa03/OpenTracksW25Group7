package de.dennisguse.opentracks.markers;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import java.util.ArrayList;
import java.util.List;

import de.dennisguse.opentracks.TrackId;
import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.models.Marker;

public class MarkerListLoader extends CursorLoader {
    private final TrackId trackId;

    public MarkerListLoader(Context context, TrackId trackId) {
        super(context);
        this.trackId = trackId;
    }

    @Override
    public Cursor loadInBackground() {
        ContentProviderUtils contentProviderUtils = new ContentProviderUtils(getContext());
        return contentProviderUtils.getMarkerCursor(trackId);
    }

    @Override
    public void deliverResult(Cursor data) {
        if (isReset()) {
            if (data != null) {
                data.close();
            }
            return;
        }

        List<Marker> markers = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            do {
                markers.add(ContentProviderUtils.createMarker(data));
            } while (data.moveToNext());
        }

        super.deliverResult(data);
    }
} 