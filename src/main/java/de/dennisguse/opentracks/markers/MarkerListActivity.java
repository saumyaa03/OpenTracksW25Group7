package de.dennisguse.opentracks.markers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.util.List;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.TrackId;
import de.dennisguse.opentracks.data.models.Marker;
import de.dennisguse.opentracks.databinding.MarkerListBinding;
import de.dennisguse.opentracks.util.IntentUtils;

public class MarkerListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Marker>> {

    public static final String EXTRA_TRACK_ID = "track_id";

    private MarkerListBinding viewBinding;
    private MarkerListAdapter adapter;
    private TrackId trackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = MarkerListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        trackId = getIntent().getParcelableExtra(EXTRA_TRACK_ID);
        if (trackId == null) {
            finish();
            return;
        }

        adapter = new MarkerListAdapter(this, null);
        viewBinding.markerList.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.marker_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<List<Marker>> onCreateLoader(int id, Bundle args) {
        return new MarkerListLoader(this, trackId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Marker>> loader, List<Marker> data) {
        adapter.swapCursor(data);
        viewBinding.markerList.setEmptyView(viewBinding.markerListEmpty);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Marker>> loader) {
        adapter.swapCursor(null);
    }

    public static Intent newIntent(Context context, TrackId trackId) {
        return IntentUtils.newIntent(context, MarkerListActivity.class)
                .putExtra(EXTRA_TRACK_ID, trackId);
    }
} 