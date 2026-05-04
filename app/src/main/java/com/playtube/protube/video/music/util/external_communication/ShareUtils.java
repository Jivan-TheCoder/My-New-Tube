package com.playtube.protube.video.music.util.external_communication;

import static coil3.Image_androidKt.toBitmap;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.playtube.protube.video.music.BuildConfig;
import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.activities.RouterActivity;
import org.schabi.newpipe.extractor.Image;
import com.playtube.protube.video.music.util.image.ImageStrategy;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import coil3.SingletonImageLoader;
import coil3.disk.DiskCache;
import coil3.memory.MemoryCache;

public final class ShareUtils {
    private static final String TAG = ShareUtils.class.getSimpleName();

    private ShareUtils() {
    }

    public static void installApp(@NonNull final Context context, final String packageId) {
        final Intent marketSchemeIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageId))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!tryOpenIntentInApp(context, marketSchemeIntent)) {
            openUrlInApp(context, "https://play.google.com/store/apps/details?id=" + packageId);
        }
    }

    public static void openUrlInBrowser(@NonNull final Context context, final String url) {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setSelector(browserIntent);
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            intent.setSelector(null);
            openAppChooser(context, intent, true);
        }
    }

    public static void openUrlInApp(@NonNull final Context context, final String url) {
        openIntentInApp(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static boolean tryOpenIntentInApp(@NonNull final Context context,
                                             @NonNull final Intent intent) {
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void openIntentInApp(@NonNull final Context context,
                                       @NonNull final Intent intent) {
        if (!tryOpenIntentInApp(context, intent)) {
            Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private static void openAppChooser(@NonNull final Context context,
                                       @NonNull final Intent intent,
                                       final boolean setTitleChooser) {
        final Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intent);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (setTitleChooser) {
            chooserIntent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.open_with));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            chooserIntent.putExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS,
                    new ComponentName[]{new ComponentName(context, RouterActivity.class)}
            );
        }

        final int permFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (permFlags != 0) {
            ClipData targetClipData = intent.getClipData();
            if (targetClipData == null && intent.getData() != null) {
                final ClipData.Item item = new ClipData.Item(intent.getData());
                final String[] mimeTypes;
                if (intent.getType() != null) {
                    mimeTypes = new String[] {intent.getType()};
                } else {
                    mimeTypes = new String[] {};
                }
                targetClipData = new ClipData(null, mimeTypes, item);
            }
            if (targetClipData != null) {
                chooserIntent.setClipData(targetClipData);
                chooserIntent.addFlags(permFlags);
            }
        }

        try {
            context.startActivity(chooserIntent);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG).show();
        }
    }

    public static void shareText(@NonNull final Context context,
                                 @NonNull final String title,
                                 final String content,
                                 final String imagePreviewUrl) {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        if (!TextUtils.isEmpty(title)) {
            shareIntent.putExtra(Intent.EXTRA_TITLE, title);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !TextUtils.isEmpty(imagePreviewUrl)
                && ImageStrategy.shouldLoadImages()) {

            final ClipData clipData = generateClipDataForImagePreview(context, imagePreviewUrl);
            if (clipData != null) {
                shareIntent.setClipData(clipData);
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        openAppChooser(context, shareIntent, false);
    }

    public static void shareText(@NonNull final Context context,
                                 @NonNull final String title,
                                 final String content,
                                 final List<Image> images) {
        shareText(context, title, content, ImageStrategy.choosePreferredImage(images));
    }

    public static void shareText(@NonNull final Context context,
                                 @NonNull final String title,
                                 final String content) {
        shareText(context, title, content, "");
    }

    public static void copyToClipboard(@NonNull final Context context, final String text) {
        final ClipboardManager clipboardManager =
                ContextCompat.getSystemService(context, ClipboardManager.class);

        if (clipboardManager == null) {
            Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text));
            if (Build.VERSION.SDK_INT < 33) {
                // Android 13 has its own "copied to clipboard" dialog
                Toast.makeText(context, R.string.msg_copied, Toast.LENGTH_SHORT).show();
            }
        } catch (final Exception e) {
            Log.e(TAG, "Error when trying to copy text to clipboard", e);
            Toast.makeText(context, R.string.msg_failed_to_copy, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private static ClipData generateClipDataForImagePreview(
            @NonNull final Context context,
            @NonNull final String thumbnailUrl) {
        try {
            final Context applicationContext = context.getApplicationContext();
            final var loader = SingletonImageLoader.get(context);
            final var value = loader.getMemoryCache()
                    .get(new MemoryCache.Key(thumbnailUrl, Collections.emptyMap()));

            final Bitmap cachedBitmap;
            if (value != null) {
                cachedBitmap = toBitmap(value.getImage());
            } else {
                try (var snapshot = loader.getDiskCache().openSnapshot(thumbnailUrl)) {
                    if (snapshot != null) {
                        cachedBitmap = BitmapFactory.decodeFile(snapshot.getData().toString());
                    } else {
                        cachedBitmap = null;
                    }
                }
            }

            if (cachedBitmap == null) {
                return null;
            }

            final var path = applicationContext.getCacheDir().toPath()
                    .resolve("android_share_sheet_image_preview.jpg");
            try (var outputStream = Files.newOutputStream(path)) {
                cachedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            }

            final ClipData clipData = ClipData.newUri(applicationContext.getContentResolver(), "",
                    FileProvider.getUriForFile(applicationContext,
                            BuildConfig.APPLICATION_ID + ".provider",
                            path.toFile()));

            return clipData;
        } catch (final Exception e) {
            Log.w(TAG, "Error when setting preview image for share sheet", e);
            return null;
        }
    }
}

