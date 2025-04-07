/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.dennisguse.opentracks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.ComponentName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import de.dennisguse.opentracks.chart.ChartFragment;
import de.dennisguse.opentracks.chart.TrackDataHubInterface;
import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.TrackDataHub;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.databinding.TrackRecordedBinding;
import de.dennisguse.opentracks.fragments.StatisticsRecordedFragment;
import de.dennisguse.opentracks.services.RecordingStatus;
import de.dennisguse.opentracks.services.TrackRecordingService;
import de.dennisguse.opentracks.services.TrackRecordingServiceConnection;
import de.dennisguse.opentracks.settings.SettingsActivity;
import de.dennisguse.opentracks.share.ShareUtils;
import de.dennisguse.opentracks.ui.aggregatedStatistics.ConfirmDeleteDialogFragment;
import de.dennisguse.opentracks.ui.intervals.IntervalsFragment;
import de.dennisguse.opentracks.ui.markers.MarkerListActivity;
import de.dennisguse.opentracks.util.IntentDashboardUtils;
import de.dennisguse.opentracks.util.IntentUtils;

public class TrackRecordedActivity extends AbstractTrackDeleteActivity implements ConfirmDeleteDialogFragment.ConfirmDeleteCaller, TrackDataHubInterface {

    private static final String TAG = TrackRecordedActivity.class.getSimpleName();
    public static final String VIEW_TRACK_ICON = "track_icon";
    public static final String EXTRA_TRACK_ID = "track_id";
    private static final String CURRENT_TAB_TAG_KEY = "current_tab_tag_key";

    private ContentProviderUtils contentProviderUtils;
    private TrackDataHub trackDataHub;
    private TrackRecordedBinding viewBinding;
    private Track.Id trackId;
    private RecordingStatus recordingStatus = TrackRecordingService.STATUS_DEFAULT;
    private TrackRecordingServiceConnection trackRecordingServiceConnection;

    private final TrackRecordingServiceConnection.Callback bindCallback = (service, unused) -> 
        service.getRecordingStatusObservable().observe(TrackRecordedActivity.this, this::onRecordingStatusChanged);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentProviderUtils = new ContentProviderUtils(this);
        
        // Secure intent handling
        Intent intent = getIntent();
        if (!validateIntent(intent)) {
            Log.e(TAG, "Invalid intent received");
            finish();
            return;
        }
        handleIntent(intent);

        trackDataHub = new TrackDataHub(this);
        CustomFragmentPagerAdapter pagerAdapter = new CustomFragmentPagerAdapter(this);
        viewBinding.trackDetailActivityViewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(viewBinding.trackDetailActivityTablayout, viewBinding.trackDetailActivityViewPager,
                (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))).attach();
        
        if (savedInstanceState != null) {
            viewBinding.trackDetailActivityViewPager.setCurrentItem(savedInstanceState.getInt(CURRENT_TAB_TAG_KEY));
        }

        trackRecordingServiceConnection = new TrackRecordingServiceConnection(bindCallback);
        viewBinding.bottomAppBarLayout.bottomAppBar.replaceMenu(R.menu.track_detail);
        setSupportActionBar(viewBinding.bottomAppBarLayout.bottomAppBar);
        postponeEnterTransition();
    }

    // Secure intent validation method
    private boolean validateIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        // Verify the intent came from our app
        ComponentName component = intent.getComponent();
        if (component != null && !getPackageName().equals(component.getPackageName())) {
            return false;
        }

        // Verify no embedded intents
        if (intent.hasExtra("client_intent") || intent.hasExtra("original_intent")) {
            return false;
        }

        // Verify the track ID exists and is valid
        Track.Id trackId = intent.getParcelableExtra(EXTRA_TRACK_ID);
        if (trackId == null) {
            return false;
        }

        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!validateIntent(intent)) {
            Log.e(TAG, "Invalid intent in onNewIntent");
            finish();
            return;
        }
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        trackId = intent.getParcelableExtra(EXTRA_TRACK_ID);
        if (trackId == null) {
            Log.e(TAG, "Missing track ID in handleIntent");
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Secure all intent creations with explicit components and validation
        switch (item.getItemId()) {
            case R.id.track_detail_share:
                Intent shareIntent = Intent.createChooser(ShareUtils.newShareFileIntent(this, trackId), null);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shareIntent);
                return true;

            case R.id.track_detail_menu_show_on_map:
                IntentDashboardUtils.showTrackOnMap(this, false, trackId);
                return true;

            case R.id.track_detail_markers:
                Intent markersIntent = new Intent(this, MarkerListActivity.class)
                        .putExtra(MarkerListActivity.EXTRA_TRACK_ID, trackId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(markersIntent);
                return true;

            case R.id.track_detail_edit:
                Intent editIntent = new Intent(this, TrackEditActivity.class)
                        .putExtra(TrackEditActivity.EXTRA_TRACK_ID, trackId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(editIntent);
                return true;

            case R.id.track_detail_delete:
                deleteTracks(trackId);
                return true;

            case R.id.track_detail_resume_track:
                TrackRecordingServiceConnection.executeForeground(this, (service, connection) -> {
                    service.resumeTrack(trackId);
                    Intent recordingIntent = new Intent(TrackRecordedActivity.this, TrackRecordingActivity.class)
                            .putExtra(TrackRecordingActivity.EXTRA_TRACK_ID, trackId)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(recordingIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });
                return true;

            case R.id.track_detail_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    protected Track.Id getRecordingTrackId() {
        return recordingStatus.trackId();
    }

    @Override
    protected void onDeleteConfirmed() {
        runOnUiThread(this::finish);
    }

    public void onDeleteFinished() {
        runOnUiThread(this::finish);
    }

    /**
     * Gets the {@link TrackDataHub}.
     */
    @Override
    public TrackDataHub getTrackDataHub() {
        return trackDataHub;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Received null intent.");
            finish();
            return;
        }

        // Verify the intent came from our app
        ComponentName callingActivity = intent.getComponent();
        if (callingActivity != null && !getPackageName().equals(callingActivity.getPackageName())) {
            Log.e(TAG, "Intent received from untrusted source: " + callingActivity);
            finish();
            return;
        }

        Track.Id maybeTrackId = intent.getParcelableExtra(EXTRA_TRACK_ID);
        if (maybeTrackId == null) {
            Log.e(TAG, "Missing EXTRA_TRACK_ID in Intent.");
            finish();
            return;
        }

        // Additional validation if needed
        trackId = maybeTrackId;
    }

    private class CustomFragmentPagerAdapter extends FragmentStateAdapter {

        public CustomFragmentPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        private static class InvalidFragmentPositionException extends IllegalArgumentException {
            public InvalidFragmentPositionException(int position) {
                super("There isn't a Fragment associated with the position: " + position + ". Expected between 0 and 3.");
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 0 -> StatisticsRecordedFragment.newInstance(trackId);
                case 1 -> IntervalsFragment.newInstance(trackId, true);
                case 2 -> ChartFragment.newInstance(false);
                case 3 -> ChartFragment.newInstance(true);
                default ->
                        throw new InvalidFragmentPositionException(position);
            };
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        public CharSequence getPageTitle(int position) {
            return switch (position) {
                case 0 -> getString(R.string.track_detail_stats_tab);
                case 1 -> getString(R.string.track_detail_intervals_tab);
                case 2 -> getString(R.string.settings_chart_by_time);
                case 3 -> getString(R.string.settings_chart_by_distance);
                default ->
                        throw new InvalidFragmentPositionException(position);
            };
        }
    }

    public void startPostponedEnterTransitionWith(View viewIcon) {
        ViewCompat.setTransitionName(viewIcon, TrackRecordedActivity.VIEW_TRACK_ICON);
        startPostponedEnterTransition();
    }

    private void onRecordingStatusChanged(RecordingStatus status) {
        recordingStatus = status;
    }
}