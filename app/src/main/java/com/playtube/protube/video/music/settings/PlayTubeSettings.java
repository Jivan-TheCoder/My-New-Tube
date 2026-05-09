package com.playtube.protube.video.music.settings;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.playtube.protube.video.music.App;
import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.settings.migration.MigrationManager;
import com.playtube.protube.video.music.util.DeviceUtils;

import java.io.File;
import java.util.Set;

public final class PlayTubeSettings {
    private PlayTubeSettings() { }

    public static void initSettings(final Context context) {
        // first run migrations, then setDefaultValues, since the latter requires the correct types
        MigrationManager.runMigrationsIfNeeded(context);

        // readAgain is true so that if new settings are added their default value is set
        PreferenceManager.setDefaultValues(context, R.xml.main_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.video_audio_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.download_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.appearance_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.history_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.content_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.debug_settings, true);

        saveDefaultVideoDownloadDirectory(context);
        saveDefaultAudioDownloadDirectory(context);

        disableMediaTunnelingIfNecessary(context);
    }

    static void saveDefaultVideoDownloadDirectory(final Context context) {
        saveDefaultDirectory(context, R.string.download_path_video_key,
                Environment.DIRECTORY_MOVIES);
    }

    static void saveDefaultAudioDownloadDirectory(final Context context) {
        saveDefaultDirectory(context, R.string.download_path_audio_key,
                Environment.DIRECTORY_MUSIC);
    }

    private static void saveDefaultDirectory(final Context context, final int keyID,
                                             final String defaultDirectoryName) {
        if (!useStorageAccessFramework(context)) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String key = context.getString(keyID);
            final String downloadPath = prefs.getString(key, null);
            if (!isNullOrEmpty(downloadPath)) {
                return;
            }

            final SharedPreferences.Editor spEditor = prefs.edit();
            spEditor.putString(key, getNewPipeChildFolderPathForDir(getDir(defaultDirectoryName)));
            spEditor.apply();
        }
    }

    @NonNull
    public static File getDir(final String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private static String getNewPipeChildFolderPathForDir(final File dir) {
        return new File(dir, "NewPipe").toURI().toString();
    }

    public static boolean useStorageAccessFramework(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else if (DeviceUtils.isFireTv()) {
            // There's a FireOS bug which prevents SAF open/close dialogs from being confirmed with
            // a remote (see #6455).
            return false;
        }

        final String key = context.getString(R.string.storage_use_saf);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, true);
    }

    private static boolean showSearchSuggestions(final Context context,
                                                 final SharedPreferences sharedPreferences,
                                                 @StringRes final int key) {
        final Set<String> enabledSearchSuggestions = sharedPreferences.getStringSet(
                context.getString(R.string.show_search_suggestions_key), null);

        if (enabledSearchSuggestions == null) {
            return true; // defaults to true
        } else {
            return enabledSearchSuggestions.contains(context.getString(key));
        }
    }

    public static boolean showLocalSearchSuggestions(final Context context,
                                                     final SharedPreferences sharedPreferences) {
        return showSearchSuggestions(context, sharedPreferences,
                R.string.show_local_search_suggestions_key);
    }

    public static boolean showRemoteSearchSuggestions(final Context context,
                                                      final SharedPreferences sharedPreferences) {
        return showSearchSuggestions(context, sharedPreferences,
                R.string.show_remote_search_suggestions_key);
    }

    private static void disableMediaTunnelingIfNecessary(@NonNull final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String disabledTunnelingKey = context.getString(R.string.disable_media_tunneling_key);
        final String disabledTunnelingAutomaticallyKey =
                context.getString(R.string.disabled_media_tunneling_automatically_key);
        final String blacklistVersionKey =
                context.getString(R.string.media_tunneling_device_blacklist_version);

        final int lastMediaTunnelingUpdate = prefs.getInt(blacklistVersionKey, 0);
        final boolean wasDeviceBlacklistUpdated =
                DeviceUtils.MEDIA_TUNNELING_DEVICE_BLACKLIST_VERSION != lastMediaTunnelingUpdate;
        final boolean wasMediaTunnelingEnabledByUser =
                prefs.getInt(disabledTunnelingAutomaticallyKey, -1) == 0
                        && !prefs.getBoolean(disabledTunnelingKey, false);

        if (App.getInstance().isFirstRun()
                || (wasDeviceBlacklistUpdated && !wasMediaTunnelingEnabledByUser)) {
            setMediaTunneling(context);
        }
    }

    /**
     * Check if device does not support media tunneling
     * and disable that exoplayer feature if necessary.
     * @see DeviceUtils#shouldSupportMediaTunneling()
     * @param context
     */
    public static void setMediaTunneling(@NonNull final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!DeviceUtils.shouldSupportMediaTunneling()) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.disable_media_tunneling_key), true)
                    .putInt(context.getString(
                            R.string.disabled_media_tunneling_automatically_key), 1)
                    .putInt(context.getString(R.string.media_tunneling_device_blacklist_version),
                            DeviceUtils.MEDIA_TUNNELING_DEVICE_BLACKLIST_VERSION)
                    .apply();
        } else {
            prefs.edit()
                    .putInt(context.getString(R.string.media_tunneling_device_blacklist_version),
                            DeviceUtils.MEDIA_TUNNELING_DEVICE_BLACKLIST_VERSION).apply();
        }
    }
}
