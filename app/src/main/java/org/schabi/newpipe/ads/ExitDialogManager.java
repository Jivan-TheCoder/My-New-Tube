package org.schabi.newpipe.ads;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.schabi.newpipe.R;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ExitDialogManager {

    private ExitDialogManager() {
    }

    public static void showExitDialog(@NonNull final Activity activity,
                                      @NonNull final Runnable onExitConfirmed) {
        final Dialog exitDialog = new Dialog(activity, R.style.full_dialog);
        exitDialog.setCancelable(true);
        exitDialog.setCanceledOnTouchOutside(true);

        final View view = LayoutInflater.from(activity).inflate(R.layout.exit_dialog, null);
        exitDialog.setContentView(view);

        final TextView tvExitApp = view.findViewById(R.id.tv_exit_app);
        final TextView tvKeepWatching = view.findViewById(R.id.tv_keep_watching);
        final FrameLayout adContainer = view.findViewById(R.id.ad_container);

        AtomicBoolean loadMediumREC = new AtomicBoolean();

        AdUtils.LoadMrecExit(activity, adContainer, loadMediumREC,"big");

        tvExitApp.setOnClickListener(v -> {
            exitDialog.dismiss();
            onExitConfirmed.run();
        });

        tvKeepWatching.setOnClickListener(v -> exitDialog.dismiss());

        exitDialog.show();
    }
}
