package de.dennisguse.opentracks.ui.markers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.util.FileUtils;

public class MarkerUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    static final int ICON_ID = R.drawable.ic_marker_orange_pushpin_with_shadow;

    private static final String JPEG_EXTENSION = "jpeg";

    private MarkerUtils() {
    }

    public static Drawable getDefaultPhoto(@NonNull Context context) {
        return ContextCompat.getDrawable(context, ICON_ID);
    }

    /**
     * Sends a take picture request to the camera app.
     * The picture is then stored in the track's folder.
     */
    static Pair<Intent, Uri> createTakePictureIntent(Context context, Track.Id trackId) {
        File dir = getValidatedPhotoDir(context, trackId);

        String fileName = SimpleDateFormat.getDateTimeInstance().format(new Date());
        File file = new File(dir, FileUtils.buildUniqueFileName(dir, fileName, JPEG_EXTENSION));

        Uri photoUri = FileProvider.getUriForFile(context, FileUtils.FILEPROVIDER, file);
        Log.d(TAG, "Taking photo to URI: " + photoUri);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        return new Pair<>(intent, photoUri);
    }

    @VisibleForTesting(otherwise = 3)
    public static String getImageUrl(Context context, Track.Id trackId) {
        File dir = getValidatedPhotoDir(context, trackId);

        String fileName = SimpleDateFormat.getDateTimeInstance().format(new Date());
        File file = new File(dir, FileUtils.buildUniqueFileName(dir, fileName, JPEG_EXTENSION));

        return file.getAbsolutePath();
    }

    public static File getPhotoFileIfExists(Context context, Track.Id trackId, Uri uri) {
        if (uri == null) {
            Log.w(TAG, "URI object is null.");
            return null;
        }

        String filename = uri.getLastPathSegment();
        if (filename == null) {
            Log.w(TAG, "External photo contains no filename.");
            return null;
        }

        File dir = getValidatedPhotoDir(context, trackId);
        File file = new File(dir, filename);
        return file.exists() ? file : null;
    }

    @Nullable
    public static File buildInternalPhotoFile(Context context, Track.Id trackId, @NonNull Uri fileNameUri) {
        if (fileNameUri == null) {
            Log.w(TAG, "URI object is null.");
            return null;
        }

        String filename = fileNameUri.getLastPathSegment();
        if (filename == null) {
            Log.w(TAG, "External photo contains no filename.");
            return null;
        }

        File dir = getValidatedPhotoDir(context, trackId);
        return new File(dir, filename);
    }

    /**
     * Safely constructs and validates the directory for storing marker photos.
     * Ensures path is within the app's external file space.
     */
    private static File getValidatedPhotoDir(Context context, Track.Id trackId) {
        File baseDir = new File(context.getExternalFilesDir(null), "marker_photos");
        File dir = new File(baseDir, trackId.toString());

        try {
            String canonicalBase = baseDir.getCanonicalPath() + File.separator;
            String canonicalDir = dir.getCanonicalPath();

            if (!canonicalDir.startsWith(canonicalBase)) {
                throw new SecurityException("Invalid trackId: path traversal detected");
            }

            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG, "Could not create directory: " + dir.getAbsolutePath());
            }

            return dir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate or create photo directory", e);
        }
    }
}
