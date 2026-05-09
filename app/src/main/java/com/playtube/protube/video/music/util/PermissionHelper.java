package com.playtube.protube.video.music.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.playtube.protube.video.music.App;
import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.settings.PlayTubeSettings;

public final class PermissionHelper {
    public static final int POST_NOTIFICATIONS_REQUEST_CODE = 779;
    public static final int DOWNLOAD_DIALOG_REQUEST_CODE = 778;
    public static final int DOWNLOADS_REQUEST_CODE = 777;

    private PermissionHelper() {
    }

    public static boolean checkStoragePermissions(final Activity activity, final int requestCode) {
        if (PlayTubeSettings.useStorageAccessFramework(activity)) {
            return true;
        }

        if (!checkReadStoragePermissions(activity, requestCode)) {
            return false;
        }
        return checkWriteStoragePermissions(activity, requestCode);
    }

    public static boolean checkReadStoragePermissions(final Activity activity,
                                                      final int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);

            return false;
        }
        return true;
    }


    public static boolean checkWriteStoragePermissions(final Activity activity,
                                                       final int requestCode) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);

            return false;
        }
        return true;
    }

    public static boolean checkPostNotificationsPermission(final Activity activity,
                                                           final int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(activity,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (!App.getInstance().getNotificationsRequested()) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, requestCode);
                App.getInstance().setNotificationsRequested();
                return false;
            }
        }
        return true;
    }

    public static boolean checkSystemAlertWindowPermission(final Context context) {
        if (!Settings.canDrawOverlays(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                final Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(i);
                } catch (final ActivityNotFoundException ignored) {
                }
                return false;
            } else {
                final String appName = context.getString(R.string.app_name);
                final String title = context.getString(R.string.permission_display_over_apps);
                final String permissionName = context.getString(R.string.permission_display_over_apps_permission_name);
                final String appNameBold = "<b>" + appName + "</b>";
                final String permissionNameBold = "<b>" + permissionName + "</b>";
                final String message = context.getString(R.string.permission_display_over_apps_message, appNameBold, permissionNameBold);

                final Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                if (context instanceof Activity) {
                    final Activity activity = (Activity) context;
                    if (!isActivityAlive(activity)) {
                        return false;
                    }
                    final ContextThemeWrapper themedContext =
                            new ContextThemeWrapper(activity, ThemeHelper.getDialogTheme(activity));
                    final View dialogView = LayoutInflater.from(themedContext)
                            .inflate(R.layout.dialog_overlay_permission, null, false);

                    final TextView titleView = dialogView.findViewById(R.id.overlayDialogTitle);
                    final TextView messageView = dialogView.findViewById(R.id.overlayDialogMessage);
                    titleView.setText(title);
                    messageView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));

                    final AlertDialog dialog = new AlertDialog.Builder(themedContext)
                            .setView(dialogView)
                            .setCancelable(true)
                            .create();

                    final View.OnClickListener dismissListener = v -> dialog.dismiss();

                    dialogView.findViewById(R.id.overlayDialogClose).setOnClickListener(dismissListener);

                    dialogView.findViewById(R.id.overlayDialogCancel).setOnClickListener(dismissListener);

                    dialogView.findViewById(R.id.overlayDialogOk).setOnClickListener(v -> {
                        try {
                            if (isActivityAlive(activity)) {
                                activity.startActivity(overlayIntent);
                            }
                        } catch (final ActivityNotFoundException ignored) {
                            // Ignore because settings screen is not available on this device.
                        }
                        dialog.dismiss();
                    });

                    showDialogIfActivityAlive(activity, dialog);
                } else {
                    try {
                        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(overlayIntent);
                    } catch (final ActivityNotFoundException ignored) {
                        // Ignore: settings screen not available on this device.
                    }
                }
                return false;
            }

        } else {
            return true;
        }
    }

    private static boolean isActivityAlive(final Activity activity) {
        if (activity.isFinishing()) {
            return false;
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1
                || !activity.isDestroyed();
    }

    private static void showDialogIfActivityAlive(final Activity activity,
                                                  final AlertDialog dialog) {
        if (!isActivityAlive(activity)) {
            return;
        }
        try {
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (final RuntimeException ignored) {
            // Defensive: avoid crashing/leaking when lifecycle changes between checks and show().
            dialog.dismiss();
        }
    }

    /**
     * Determines whether the popup is enabled, and if it is not, starts the system activity to
     * request the permission with {@link #checkSystemAlertWindowPermission(Context)} and shows a
     * toast to the user explaining why the permission is needed.
     *
     * @param context the Android context
     * @return whether the popup is enabled
     */
    public static boolean isPopupEnabledElseAsk(final Context context) {
        if (checkSystemAlertWindowPermission(context)) {
            return true;
        } else {
            Toast.makeText(context, R.string.msg_popup_permission, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
