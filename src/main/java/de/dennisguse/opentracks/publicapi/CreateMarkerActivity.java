package de.dennisguse.opentracks.publicapi;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.services.TrackRecordingServiceConnection;
import de.dennisguse.opentracks.ui.markers.MarkerEditActivity;
import de.dennisguse.opentracks.util.IntentUtils;

/**
 * INTERNAL: only meant for clients of OSMDashboard API.
 */
public class CreateMarkerActivity extends AppCompatActivity {

    public static final String EXTRA_TRACK_ID = "track_id";
    public static final String EXTRA_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Defensive checks to break the tainted data flow flagged by snyk
        Intent incomingIntent = getIntent();
        if(incomingIntent == null || !incomingIntent.hasExtra(EXTRA_TRACK_ID) || !incomingIntent.hasExtra(EXTRA_LOCATION)){
            Log.w("CreateMarkerActivity", "Missing track ID or location in intent extras.");
            Toast.makeText(this, "Invalid input data. Cannot continue.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long trackIdValue = incomingIntent.getLongExtra(EXTRA_TRACK_ID, -1L);
        Location location = incomingIntent.getParcelableExtra(EXTRA_LOCATION);

        // Validate track ID and location
        if (trackIdValue == -1L || location == null) {
            Log.w("CreateMarkerActivity", "trackId is invalid or location is null.");
            Toast.makeText(this, "Invalid input values. Cannot continue.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Track.Id trackId = new Track.Id(trackIdValue);

        TrackRecordingServiceConnection.execute(this, (service, self) -> {
            Intent intent = IntentUtils
                    .newIntent(this, MarkerEditActivity.class)
                    .putExtra(MarkerEditActivity.EXTRA_TRACK_ID, trackId)
                    .putExtra(MarkerEditActivity.EXTRA_LOCATION, location);
            startActivity(intent);
            finish();
        });
    }

}
