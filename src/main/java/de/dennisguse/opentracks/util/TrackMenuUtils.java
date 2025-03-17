package de.dennisguse.opentracks.util;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.TrackEditActivity;
import de.dennisguse.opentracks.TrackId;
import de.dennisguse.opentracks.markers.MarkerListActivity;

public class TrackMenuUtils {
    
    public static boolean handleTrackMenuAction(Context context, MenuItem item, TrackId trackId) {
        if (item.getItemId() == R.id.track_detail_menu_show_on_map) {
            IntentDashboardUtils.showTrackOnMap(context, false, trackId);
            return true;
        }

        if (item.getItemId() == R.id.track_detail_markers) {
            Intent intent = IntentUtils.newIntent(context, MarkerListActivity.class)
                    .putExtra(MarkerListActivity.EXTRA_TRACK_ID, trackId);
            context.startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.track_detail_edit) {
            Intent intent = IntentUtils.newIntent(context, TrackEditActivity.class)
                    .putExtra(TrackEditActivity.EXTRA_TRACK_ID, trackId);
            context.startActivity(intent);
            return true;
        }

        return false;
    }
} 