package org.schabi.newpipe.ads;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.schabi.newpipe.R;

public class RewardedAdManager {

    public interface Callback {
        void onResult(boolean rewarded);
    }

    private static volatile RewardedAdManager instance;

    public static RewardedAdManager getInstance() {
        if (instance == null) {
            synchronized (RewardedAdManager.class) {
                if (instance == null) {
                    instance = new RewardedAdManager();
                }
            }
        }
        return instance;
    }

    private RewardedAd rewardedAd;
    private boolean isLoading = false;
    private boolean isShowing = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RewardedAdManager() {
    }

    private boolean isActivityAlive(Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void dismissDialogSafely(Dialog dialog) {
        if (dialog == null) {
            return;
        }
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception ignored) {
            // Ignore window token/state race while dismissing.
        }
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }

        if (!isDialogEnabled()) {
            preload(context);
        }
    }

    public void show(Activity activity, Callback callback) {
        runOnMain(() -> {
            if (callback == null) {
                return;
            }
            if (!isActivityAlive(activity)) {
                AdUtils.AdsOpenIntrestial = false;
                callback.onResult(false);
                return;
            }

            if (isShowing) {
                callback.onResult(false);
                return;
            }

            AdUtils.Ad_Count++;

            if (!isEligibleForRewarded(activity)) {
                AdUtils.AdsOpenIntrestial = false;
                callback.onResult(false);
                return;
            }

            if (isDialogEnabled()) {
                loadAndShowWithDialog(activity, callback);
            } else {
                showPreloaded(activity, callback);
            }
        });
    }

    private boolean isEligibleForRewarded(Activity activity) {
        if (!AdUtils.isOnline(activity)) {
            return false;
        }

        long current = System.currentTimeMillis();
        long elapsed = current - AdUtils.Time_Check;
        return AdUtils.CheckOnOff
                && AdUtils.Ad_Count > AdUtils.Ad_Click
                && elapsed > (AdUtils.Time_interval * 1000L);
    }

    private boolean isDialogEnabled() {
        return AdUtils.dialog;
    }

    private Dialog createLoadingDialog(Activity activity) {
        Dialog adDialog = new Dialog(activity, R.style.UserDialog1);
        adDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        adDialog.setCancelable(false);
        adDialog.setContentView(R.layout.ad_dialog_layout);

        ProgressBar progress = adDialog.findViewById(R.id.progress);
        if (progress != null && progress.getIndeterminateDrawable() != null) {
            progress.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(activity, R.color.red),
                    PorterDuff.Mode.SRC_IN
            );
        }

        return adDialog;
    }

    private void preload(Context context) {
        if (context == null) {
            return;
        }
        if (isLoading || rewardedAd != null || AdUtils.Google_Rewarded == null || AdUtils.Google_Rewarded.trim().isEmpty()) {
            return;
        }

        isLoading = true;
        RewardedAd.load(context, AdUtils.Google_Rewarded, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                isLoading = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                RewardedAd.load(context, AdUtils.Google_Rewarded_Fail, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        RewardedAd.load(context, AdUtils.Google_Rewarded_Fail_1, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd ad) {
                                rewardedAd = ad;
                                isLoading = false;
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                                rewardedAd = null;
                                isLoading = false;


                                if (!isDialogEnabled()) {
                                    mainHandler.postDelayed(() -> preload(context), 30000);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void showPreloaded(Activity activity, Callback callback) {
        if (!isActivityAlive(activity)) {
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
            return;
        }
        if (rewardedAd != null) {
            showInternal(activity, callback);
            return;
        }

        preload(activity.getApplicationContext());
        AdUtils.AdsOpenIntrestial = false;
        callback.onResult(false);
    }

    private void loadAndShowWithDialog(Activity activity, Callback callback) {
        if (!isActivityAlive(activity)) {
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
            return;
        }
        if (isLoading || AdUtils.Google_Rewarded == null || AdUtils.Google_Rewarded.trim().isEmpty()) {
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
            return;
        }

        Dialog adDialog = createLoadingDialog(activity);
        adDialog.show();

        isLoading = true;
        RewardedAd.load(activity, AdUtils.Google_Rewarded, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                if (!isActivityAlive(activity)) {
                    isLoading = false;
                    dismissDialogSafely(adDialog);
                    AdUtils.AdsOpenIntrestial = false;
                    callback.onResult(false);
                    return;
                }
                isLoading = false;
                rewardedAd = ad;

                dismissDialogSafely(adDialog);

                showInternal(activity, callback);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                RewardedAd.load(activity, AdUtils.Google_Rewarded_Fail, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        if (!isActivityAlive(activity)) {
                            isLoading = false;
                            dismissDialogSafely(adDialog);
                            AdUtils.AdsOpenIntrestial = false;
                            callback.onResult(false);
                            return;
                        }
                        isLoading = false;
                        rewardedAd = ad;

                        dismissDialogSafely(adDialog);

                        showInternal(activity, callback);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        RewardedAd.load(activity, AdUtils.Google_Rewarded_Fail_1, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd ad) {
                                if (!isActivityAlive(activity)) {
                                    isLoading = false;
                                    dismissDialogSafely(adDialog);
                                    AdUtils.AdsOpenIntrestial = false;
                                    callback.onResult(false);
                                    return;
                                }
                                isLoading = false;
                                rewardedAd = ad;

                                dismissDialogSafely(adDialog);

                                showInternal(activity, callback);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                                isLoading = false;
                                rewardedAd = null;

                                dismissDialogSafely(adDialog);

                                AdUtils.AdsOpenIntrestial = false;
                                callback.onResult(false);

                            }
                        });
                    }
                });
            }
        });
    }

    private void showInternal(Activity activity, Callback callback) {
        if (!isActivityAlive(activity)) {
            isShowing = false;
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
            return;
        }
        if (rewardedAd == null) {
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
            return;
        }

        isShowing = true;
        final boolean[] rewardEarned = {false};

        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                AdUtils.AdsOpenIntrestial = true;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                isShowing = false;
                rewardedAd = null;

                AdUtils.Time_Check = System.currentTimeMillis();
                AdUtils.Ad_Count = 0;
                AdUtils.AdsOpenIntrestial = false;

                callback.onResult(rewardEarned[0]);

                if (!isDialogEnabled()) {
                    preload(activity.getApplicationContext());
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                isShowing = false;
                rewardedAd = null;

                AdUtils.AdsOpenIntrestial = false;
                callback.onResult(false);

                if (!isDialogEnabled()) {
                    preload(activity.getApplicationContext());
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            AdUtils.AdsOpenIntrestial = true;
            rewardedAd.show(activity, rewardItem -> rewardEarned[0] = true);
        } else {
            isShowing = false;
//            rewardedAd = null;
            AdUtils.AdsOpenIntrestial = false;
            callback.onResult(false);
        }
    }

    private void runOnMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}
