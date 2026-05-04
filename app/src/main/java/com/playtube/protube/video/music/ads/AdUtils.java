package com.playtube.protube.video.music.ads;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import com.playtube.protube.video.music.R;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdUtils {
    public static String Google_Intertitial_Splash = "";
    public static String Google_Intertitial_Splash_Fail = "";
    public static String Google_Intertitial_Splash_Fail_1 = "";
    public static String Google_App_open = "";
    public static String Google_App_open_Fail = "";
    public static String Google_App_open_Fail_1 = "";
    public static String Google_App_open_splash = "";
    public static String Google_App_open_splash_Fail = "";
    public static String Google_App_open_splash_Fail_1 = "";
    public static String Google_Native = "";
    public static String Google_Native_Fail = "";
    public static String Google_Native_Fail_1 = "";
    public static String Google_Banner = "";
    public static String Google_Banner_Fail = "";
    public static String Google_Banner_Fail_1 = "";
    public static String Google_Native_Banner = "";
    public static String Google_Native_Banner_Fail = "";
    public static String Google_Native_Banner_Fail_1 = "";
    public static String Google_Intertitial = "";
    public static String Google_Intertitial_Fail = "";
    public static String Google_Intertitial_Fail_1 = "";
    public static String Google_Rewarded = "";
    public static String Google_Rewarded_Fail = "";
    public static String Google_Rewarded_Fail_1 = "";
    public static String Google_Medium_REC = "";
    public static String Google_Medium_REC_Fail = "";
    public static String Google_Medium_REC_Fail_1 = "";

    public static String REC_Google_Native = "";
    public static String REC_Google_Native_Fail = "";
    public static String REC_Google_Native_Fail_1 = "";
    public static String REC_Google_Medium_REC = "";
    public static String REC_Google_Medium_REC_Fail = "";
    public static String REC_Google_Medium_REC_Fail_1 = "";

    public static int feed_after = 3;
    public static int feed_max = 1;
    public static int sub_after = 5;
    public static int sub_every = 15;
    public static int sub_max = 2;
    public static int base_after = 2;
    public static int base_every = 11;
    public static int base_max = 3;
    public static int rel_every = 7;

    public static boolean exit_page = false;
    public static String google_exit_inter = "";
    public static String google_exit_inter_fail = "";
    public static String google_exit_inter_fail_1 = "";
    public static String google_exit_native = "";
    public static String google_exit_native_fail = "";
    public static String google_exit_native_fail_1 = "";
    public static String google_exit_mrec = "";
    public static String google_exit_mrec_fail = "";
    public static String google_exit_mrec_fail_1 = "";

    public static int ads_native_second = 10;
    public static long NativeTime_Check = 0;
    public static long NativeBannerTime_Check = 0;

    public static String native_button_color = "#ff8d36";
    public static String native_button_text_color = "#ffffff";
    public static String native_bg_color = "#f7f7f7";
    public static String native_headline_color = "#000000";
    public static String native_body_color = "#616161";

    public static String default_native_button_color = "#8AB4F8";
    public static String default_native_button_text_color = "#202124";
    public static String default_native_bg_color = "#f7f7f7";
    public static String default_native_headline_color = "#000000";
    public static String default_native_body_color = "#616161";

    public static boolean LoadingAllData = false;
    public static boolean OpenAllData = false;
    public static boolean CheckOnOff = false;
    public static boolean ShowRewarded = false;
    public static boolean AdsOpenIntrestial = false;

    public static NativeAd GoogleNativeSmall;
    public static NativeAd GoogleNativeBig;

    public static int Ad_Click = 2;
    public static int ads_first_click_interstitial = 1;
    public static int Ad_Count = 0;
    public static int Time_interval = 31;
    public static long Time_Check = 0;

    public static int width = 0;

    public static Boolean dialog = false;

    private static boolean isActivityAlive(Activity act) {
        return act == null || act.isFinishing() || act.isDestroyed();
    }

    public static boolean isOnline(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public static InterstitialAd GoogleInt = null;

    public static void PreLoad(Context act) {
        if (GoogleInt != null) {
            return;
        }
        if (AdUtils.CheckOnOff) {
            if (AdUtils.isOnline(act)) {
                if (AdUtils.Google_Intertitial.isEmpty()) {
                    preLoadGoogleFailInter(act);
                    return;
                }
                InterstitialAd.load(act, AdUtils.Google_Intertitial, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);

                        GoogleInt = null;

                        preLoadGoogleFailInter(act);

                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        GoogleInt = interstitialAd;
                    }
                });
            }
        }
    }

    public static void preLoadGoogleFailInter(Context act) {
        if (GoogleInt != null) {
            return;
        }
        if (AdUtils.Google_Intertitial_Fail.isEmpty()) {
            preLoadGoogleFailInter_1(act);
            return;
        }

        InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                GoogleInt = null;

                preLoadGoogleFailInter_1(act);

            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                GoogleInt = interstitialAd;
            }
        });
    }

    public static void preLoadGoogleFailInter_1(Context act) {
        if (GoogleInt != null) {
            return;
        }
        if (AdUtils.Google_Intertitial_Fail_1.isEmpty()) {
            return;
        }

        InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail_1, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                GoogleInt = null;

                InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail_1, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        GoogleInt = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        GoogleInt = interstitialAd;
                    }
                });

            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                GoogleInt = interstitialAd;
            }
        });
    }

    public static void PreLoadShow(Activity act, InterClick interClick) {
        if (isActivityAlive(act)) {
            if (interClick != null) {
                interClick.ClickAds();
            }
            return;
        }
        if (AdUtils.CheckOnOff) {
            if (AdUtils.isOnline(act)) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.Time_Check);
                if (AdUtils.CheckOnOff && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000L)) {
                    if (GoogleInt != null) {
                        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            AdUtils.AdsOpenIntrestial = true;
                            GoogleInt.show(act);
                        }

                        GoogleInt.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();

                                AdUtils.Time_Check = System.currentTimeMillis();

                                PreLoad(act);
                                AdUtils.AdsOpenIntrestial = false;
                                AdUtils.Ad_Count = 0;
                                if (interClick != null) {
                                    interClick.ClickAds();
                                }


                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                PreLoad(act);

                                AdUtils.AdsOpenIntrestial = false;
                                if (interClick != null) {
                                    interClick.ClickAds();
                                }
                            }
                        });
                    } else {
                        PreLoad(act);
                        AdUtils.AdsOpenIntrestial = false;
                        if (interClick != null) {
                            interClick.ClickAds();
                        }
                    }
                } else {
                    AdUtils.AdsOpenIntrestial = false;
                    if (interClick != null) {
                        interClick.ClickAds();
                    }
                }
            } else {
                AdUtils.AdsOpenIntrestial = false;
                if (interClick != null) {
                    interClick.ClickAds();
                }
            }
        } else {
            AdUtils.AdsOpenIntrestial = false;
            if (interClick != null) {
                interClick.ClickAds();
            }
        }


    }

    public interface InterClick {
        void ClickAds();
    }

    public static void ClickWithAds(Activity act, InterClick interClick) {
        AdUtils.Ad_Count++;

        if (AdUtils.ShowRewarded) {
            Log.e("JKJKJKJK", "ClickWithAds: rewarded ad path");
            RewardedAdManager.getInstance().show(act, rewarded -> {
                if (interClick != null) {
                    interClick.ClickAds();
                }
            });
        } else {
            Log.e("JKJKJKJK", "ClickWithAds: interstitial ad path");
            GoogleAds(act, interClick);
        }
    }

    private static void GoogleAds(Activity act, InterClick interClick) {
        if (AdUtils.dialog) {
            if (AdUtils.isOnline(act)) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.Time_Check);
                if (AdUtils.CheckOnOff && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000L)) {

                    final Dialog AdDialog = new Dialog(act, R.style.UserDialog1);
                    AdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    AdDialog.setCancelable(false);
                    AdDialog.setContentView(R.layout.ad_dialog_layout);

                    ProgressBar progress = AdDialog.findViewById(R.id.progress);

                    progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.red), PorterDuff.Mode.SRC_IN);


                    AdDialog.show();

                    InterstitialAd.load(act, AdUtils.Google_Intertitial, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            super.onAdLoaded(interstitialAd);

                            AdDialog.dismiss();
                            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                AdUtils.AdsOpenIntrestial = true;
                                interstitialAd.show(act);
                            }

                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                    AdUtils.AdsOpenIntrestial = false;
                                    AdDialog.dismiss();
                                    if (interClick != null) {
                                        interClick.ClickAds();
                                    }
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();

                                    AdUtils.Time_Check = System.currentTimeMillis();

                                    AdUtils.AdsOpenIntrestial = false;
                                    AdUtils.Ad_Count = 0;
                                    if (interClick != null) {
                                        interClick.ClickAds();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            LoadGoogleFail(act, interClick, AdDialog, progress);
                        }
                    });

                } else {
                    AdUtils.AdsOpenIntrestial = false;
                    if (interClick != null) {
                        interClick.ClickAds();
                    }
                }

            } else {
                AdUtils.AdsOpenIntrestial = false;
                if (interClick != null) {
                    interClick.ClickAds();
                }
            }
        } else {
            PreLoadShow(act, interClick);
        }
    }

    private static void LoadGoogleFail(Activity act, InterClick interClick, Dialog AdDialog, ProgressBar progress) {
        if (isActivityAlive(act)) {
            AdUtils.AdsOpenIntrestial = false;
            if (interClick != null) {
                interClick.ClickAds();
            }
            return;
        }

        if (!AdUtils.Google_Intertitial_Fail.isEmpty()) {
            progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

            InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    if (isActivityAlive(act)) {
                        AdUtils.AdsOpenIntrestial = false;
                        if (interClick != null) {
                            interClick.ClickAds();
                        }
                        return;
                    }
                    AdDialog.dismiss();
                    if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        AdUtils.AdsOpenIntrestial = true;
                        interstitialAd.show(act);
                    }

                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            AdUtils.AdsOpenIntrestial = false;
                            AdDialog.dismiss();
                            if (interClick != null) {
                                interClick.ClickAds();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                            AdUtils.Time_Check = System.currentTimeMillis();

                            AdUtils.AdsOpenIntrestial = false;
                            AdUtils.Ad_Count = 0;
                            if (interClick != null) {
                                interClick.ClickAds();
                            }

                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    LoadGoogleFail_1(act, interClick, AdDialog, progress);
                }
            });
        } else {
            LoadGoogleFail_1(act, interClick, AdDialog, progress);
        }
    }

    private static void LoadGoogleFail_1(Activity act, InterClick interClick, Dialog AdDialog, ProgressBar progress) {

        if (AdUtils.Google_Intertitial_Fail_1.isEmpty()) {
            progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

            InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail_1, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    AdDialog.dismiss();
                    if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        AdUtils.AdsOpenIntrestial = true;
                        interstitialAd.show(act);
                    }

                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            AdUtils.AdsOpenIntrestial = false;
                            AdDialog.dismiss();
                            if (interClick != null) {
                                interClick.ClickAds();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                            AdUtils.Time_Check = System.currentTimeMillis();

                            AdUtils.AdsOpenIntrestial = false;
                            AdUtils.Ad_Count = 0;
                            if (interClick != null) {
                                interClick.ClickAds();
                            }

                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    AdDialog.dismiss();
                    AdUtils.AdsOpenIntrestial = false;
                    if (interClick != null) {
                        interClick.ClickAds();
                    }
                }
            });
        } else {
            AdUtils.AdsOpenIntrestial = false;
            AdDialog.dismiss();
            if (interClick != null) {
                interClick.ClickAds();
            }
        }
    }

    public static AdLoader adLoader;

    public static void LOadBigNative(Activity act, FrameLayout frameLayout, String size) {
        NativeAdView adView = null;
        if (size.equalsIgnoreCase("small")) {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_small_banner, null);
        } else {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native, null);
        }

        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("small")) {
            if (!AdUtils.Google_Native_Banner.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeBannerTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Banner)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeBannerTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdViewSmallBanner(GoogleNativeSmall, finalAdView);
                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    GoogleNativeSmall = null;

                                    LOadBigNativeFail(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeSmall == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFail(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdViewSmallBanner(GoogleNativeSmall, adView);

                }
            } else {
                LOadBigNativeFail(act, frameLayout, size);
            }
        }

        if (size.equalsIgnoreCase("big")) {
            if (!AdUtils.Google_Native.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeBig = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeBig, finalAdView);


                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Native failed to load ==> " + adError.getMessage());
                                    GoogleNativeBig = null;

                                    LOadBigNativeFail(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeBig == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFail(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdView(GoogleNativeBig, adView);

                }

            } else {
                Log.e("LOadBigNative: ", "hhhh");
                LOadBigNativeFail(act, frameLayout, size);
            }
        }
    }

    public static void LOadBigNativeFail(Activity act, FrameLayout frameLayout, String size) {
        NativeAdView adView = null;
        if (size.equalsIgnoreCase("small")) {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_small_banner, null);
        } else {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native, null);
        }

        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("small")) {
            if (!AdUtils.Google_Native_Banner_Fail.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeBannerTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Banner_Fail)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeBannerTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdViewSmallBanner(GoogleNativeSmall, finalAdView);
                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    GoogleNativeSmall = null;

                                    LOadBigNativeFail_1(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeSmall == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFail_1(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdViewSmallBanner(GoogleNativeSmall, adView);

                }
            } else {
                LOadBigNativeFail_1(act, frameLayout, size);
            }
        }

        if (size.equalsIgnoreCase("big")) {
            if (!AdUtils.Google_Native_Fail.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Fail)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeBig = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeBig, finalAdView);


                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Native_Fail failed to load ==> " + adError.getMessage());
                                    GoogleNativeBig = null;

                                    LOadBigNativeFail_1(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeBig == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFail_1(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdView(GoogleNativeBig, adView);

                }

            } else {
                Log.e("LOadBigNative: ", "hhhh");
                LOadBigNativeFail_1(act, frameLayout, size);
            }
        }
    }

    public static void LOadBigNativeFail_1(Activity act, FrameLayout frameLayout, String size) {

        NativeAdView adView = null;
        if (size.equalsIgnoreCase("small")) {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_small_banner, null);
        } else {
            adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native, null);
        }


        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("small")) {
            if (GoogleNativeSmall == null) {
                if (!AdUtils.Google_Native_Banner_Fail_1.isEmpty()) {

                    Log.e("LOadBigNative faill: ", "aaaaa");

                    NativeAdView finalAdView = adView;
                    NativeAdView finalAdView1 = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Banner_Fail_1)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative faill: ", "bbbb");

                                AdUtils.NativeBannerTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdViewSmallBanner(GoogleNativeSmall, finalAdView);

                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("LOadBigNative faill: ", "cccc");
                                    frameLayout.removeAllViews();
                                    frameLayout.addView(finalAdView1);

                                    adView1.setVisibility(View.INVISIBLE);

                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                }
            } else {
                Log.e("LOadBigNative faill: ", "ddddd");
                frameLayout.removeAllViews();
                frameLayout.addView(adView);

                populateNativeAdViewSmallBanner(GoogleNativeSmall, adView);

            }
        }

        if (size.equalsIgnoreCase("big")) {
            if (GoogleNativeBig == null) {
                if (!AdUtils.Google_Native_Fail_1.isEmpty()) {

                    Log.e("LOadBigNative faill: ", "aaaaa");

                    NativeAdView finalAdView = adView;
                    NativeAdView finalAdView1 = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Fail_1)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative faill: ", "bbbb");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeBig = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeBig, finalAdView);

                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("LOadBigNative faill: ", "cccc");
                                    frameLayout.removeAllViews();
                                    frameLayout.addView(finalAdView1);

                                    adView1.setVisibility(View.INVISIBLE);

                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                }
            } else {
                Log.e("LOadBigNative faill: ", "ddddd");
                frameLayout.removeAllViews();
                frameLayout.addView(adView);

                populateNativeAdView(GoogleNativeBig, adView);

            }
        }
    }

    private static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        if (adView.getMediaView() == null) {

            mediaView.setMediaContent(nativeAd.getMediaContent());
        }

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
// adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
        ((TextView) Objects.requireNonNull(adView.getBodyView())).setText(nativeAd.getBody());
        ((TextView) Objects.requireNonNull(adView.getCallToActionView())).setText(nativeAd.getCallToAction());

        try {
            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setTextColor(Color.parseColor(AdUtils.native_headline_color));
            ((TextView) Objects.requireNonNull(adView.getBodyView())).setTextColor(Color.parseColor(AdUtils.native_body_color));
        } catch (Exception e) {
            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setTextColor(Color.parseColor(AdUtils.default_native_headline_color));
            ((TextView) Objects.requireNonNull(adView.getBodyView())).setTextColor(Color.parseColor(AdUtils.default_native_body_color));
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);

    }

    public static void populateNativeAdViewSmallBanner(NativeAd nativeAd, NativeAdView adView) {

//        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
// adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
        ((TextView) Objects.requireNonNull(adView.getBodyView())).setText(nativeAd.getBody());
        ((TextView) Objects.requireNonNull(adView.getCallToActionView())).setText(nativeAd.getCallToAction());

        try {
            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setTextColor(Color.parseColor(AdUtils.native_headline_color));
            ((TextView) Objects.requireNonNull(adView.getBodyView())).setTextColor(Color.parseColor(AdUtils.native_body_color));
        } catch (Exception e) {
            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setTextColor(Color.parseColor(AdUtils.default_native_headline_color));
            ((TextView) Objects.requireNonNull(adView.getBodyView())).setTextColor(Color.parseColor(AdUtils.default_native_body_color));
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    public static void loadGoogleBanner(Activity act, FrameLayout adContainer) {
        if (isActivityAlive(act)) {
            hideAdContainer(adContainer);
            return;
        }

        if (adContainer != null) {
            if (AdUtils.CheckOnOff) {
                if (AdUtils.isOnline(act)) {
                    if (AdUtils.Google_Banner.isEmpty()) {
                        loadGoogleBannerFail(act, adContainer);
                        return;
                    }

                    AdView googleAd = new AdView(act);
                    googleAd.setAdUnitId(AdUtils.Google_Banner);
                    googleAd.setAdSize(getAdaptiveBannerSize(act, adContainer));
                    googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    googleAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            showAd(adContainer, googleAd);
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError error) {
                            Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Banner failed to load ==> " + error.getMessage());
                            googleAd.destroy();
                            loadGoogleBannerFail(act, adContainer);
                        }
                    });

                    googleAd.loadAd(new AdRequest.Builder().build());
                } else {
                    hideAdContainer(adContainer);
                }
            } else {
                hideAdContainer(adContainer);
            }
        } else {
            hideAdContainer(adContainer);
        }
    }

    public static void loadGoogleBannerFail(Activity act, FrameLayout adContainer) {
        if (isActivityAlive(act)) {
            hideAdContainer(adContainer);
            return;
        }
        if (AdUtils.Google_Banner_Fail.isEmpty()) {
            loadGoogleBannerFail_1(act, adContainer);
            return;
        }

        AdView googleAd = new AdView(act);
        googleAd.setAdUnitId(AdUtils.Google_Banner_Fail);
        googleAd.setAdSize(getAdaptiveBannerSize(act, adContainer));
        googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        googleAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showAd(adContainer, googleAd);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Banner_Fail failed to load ==> " + error.getMessage());
                googleAd.destroy();
                loadGoogleBannerFail_1(act, adContainer);
            }
        });

        googleAd.loadAd(new AdRequest.Builder().build());
    }

    public static void loadGoogleBannerFail_1(Activity act, FrameLayout adContainer) {
        if (isActivityAlive(act)) {
            hideAdContainer(adContainer);
            return;
        }
        if (AdUtils.Google_Banner_Fail_1.isEmpty()) {
            LOadBigNative(act, adContainer, "small");
            return;
        }

        AdView googleAd = new AdView(act);
        googleAd.setAdUnitId(AdUtils.Google_Banner_Fail_1);
        googleAd.setAdSize(getAdaptiveBannerSize(act, adContainer));
        googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        googleAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showAd(adContainer, googleAd);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Banner_Fail_1 failed to load ==> " + error.getMessage());
                googleAd.destroy();
                LOadBigNative(act, adContainer, "small");
            }
        });

        googleAd.loadAd(new AdRequest.Builder().build());
    }

    private static AdSize getAdaptiveBannerSize(Activity act, ViewGroup adContainer) {
        DisplayMetrics outMetrics = act.getResources().getDisplayMetrics();
        float density = outMetrics.density;

        int adWidthPixels = adContainer.getWidth();
        if (adWidthPixels <= 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        if (adWidth <= 0) {
            adWidth = 320;
        }

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(act, adWidth);
    }

    public static void LoadMrecExit(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        if (AdUtils.CheckOnOff) {
            if (AdUtils.isOnline(act)) {
                loadMediumREC.set(true);
                loadGoogleMREC(act, adContainer, loadMediumREC, size);
            } else {
                loadMediumREC.set(false);
                hideAdContainer(adContainer);
            }
        } else {
            loadMediumREC.set(false);
            hideAdContainer(adContainer);
        }
    }

    public static void loadGoogleMREC(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        if (isActivityAlive(act)) {
            loadMediumREC.set(false);
            hideAdContainer(adContainer);
            return;
        }
        loadMediumREC.set(true);

        if (!AdUtils.google_exit_mrec.isEmpty()) {
            AdView googleAd = new AdView(act);
            googleAd.setAdUnitId(AdUtils.google_exit_mrec);
            googleAd.setAdSize(AdSize.MEDIUM_RECTANGLE);
            googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            googleAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    loadMediumREC.set(true);
                    showAd(adContainer, googleAd);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    loadMediumREC.set(false);
                    googleAd.destroy();

                    loadGoogleMRECFail(act, adContainer, loadMediumREC, size);
                }
            });

            googleAd.loadAd(new AdRequest.Builder().build());
        } else {
            loadGoogleMRECFail(act, adContainer, loadMediumREC, size);
        }
    }

    public static void loadGoogleMRECFail(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        if (isActivityAlive(act)) {
            loadMediumREC.set(false);
            hideAdContainer(adContainer);
            return;
        }
        loadMediumREC.set(true);

        if (!AdUtils.google_exit_mrec_fail.isEmpty()) {
            AdView googleAd = new AdView(act);
            googleAd.setAdUnitId(AdUtils.google_exit_mrec_fail);
            googleAd.setAdSize(AdSize.MEDIUM_RECTANGLE);
            googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            googleAd.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    loadMediumREC.set(true);
                    showAd(adContainer, googleAd);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    loadMediumREC.set(false);
                    googleAd.destroy();

                    loadGoogleMRECFail_1(act, adContainer, loadMediumREC, size);
                }
            });

            googleAd.loadAd(new AdRequest.Builder().build());
        } else {
            loadGoogleMRECFail_1(act, adContainer, loadMediumREC, size);
        }
    }

    public static void loadGoogleMRECFail_1(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        if (isActivityAlive(act)) {
            loadMediumREC.set(false);
            hideAdContainer(adContainer);
            return;
        }
        loadMediumREC.set(true);

        if (!AdUtils.google_exit_mrec_fail_1.isEmpty()) {
            AdView googleAd = new AdView(act);
            googleAd.setAdUnitId(AdUtils.google_exit_mrec_fail_1);
            googleAd.setAdSize(AdSize.MEDIUM_RECTANGLE);
            googleAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            googleAd.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    loadMediumREC.set(true);
                    showAd(adContainer, googleAd);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    loadMediumREC.set(false);
                    googleAd.destroy();

                    LOadBigNativeExit(act, adContainer, size);
                }
            });

            googleAd.loadAd(new AdRequest.Builder().build());
        } else {
            loadMediumREC.set(false);
            LOadBigNativeExit(act, adContainer, size);
        }
    }

    public static void LOadBigNativeExit(Activity act, FrameLayout frameLayout, String size) {
        NativeAdView adView = null;
        adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_exit, null);

        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("big")) {
            if (!AdUtils.google_exit_native.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.google_exit_native)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeSmall, finalAdView);


                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Native failed to load ==> " + adError.getMessage());
                                    GoogleNativeSmall = null;

                                    LOadBigNativeFailExit(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeSmall == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFailExit(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdView(GoogleNativeSmall, adView);

                }

            } else {
                Log.e("LOadBigNative: ", "hhhh");
                LOadBigNativeFailExit(act, frameLayout, size);
            }
        }
    }

    public static void LOadBigNativeFailExit(Activity act, FrameLayout frameLayout, String size) {
        NativeAdView adView = null;
        adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_exit, null);

        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("big")) {
            if (!AdUtils.google_exit_native_fail.isEmpty()) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000L)) {
                    Log.e("LOadBigNative: ", "aaaaaaaaaa");

                    NativeAdView finalAdView = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.google_exit_native_fail)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative: ", "loaddddd");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeSmall, finalAdView);


                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("JJJJJJJJJ", "onAdFailedToLoad: AdUtils.Google_Native_Fail failed to load ==> " + adError.getMessage());
                                    GoogleNativeSmall = null;

                                    LOadBigNativeFailExit_1(act, frameLayout, size);


                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                } else if (GoogleNativeSmall == null) {
                    Log.e("LOadBigNative: ", "bbbbbb");

                    LOadBigNativeFailExit_1(act, frameLayout, size);

                } else {
                    Log.e("LOadBigNative: ", "ddddd");
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    populateNativeAdView(GoogleNativeSmall, adView);

                }

            } else {
                Log.e("LOadBigNative: ", "hhhh");
                LOadBigNativeFailExit_1(act, frameLayout, size);
            }
        }
    }

    public static void LOadBigNativeFailExit_1(Activity act, FrameLayout frameLayout, String size) {

        NativeAdView adView = null;
        adView = (NativeAdView) act.getLayoutInflater().inflate(R.layout.google_native_exit, null);

        NativeAdView adView1 = (NativeAdView) adView.findViewById(R.id.ad_view);
        TextView ad_call_to_action = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);

        try {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
        } catch (Exception e) {
            adView1.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
            card.setCardBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
            ad_call_to_action.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
        }

        if (size.equalsIgnoreCase("big")) {
            if (GoogleNativeSmall == null) {
                if (!AdUtils.google_exit_native_fail_1.isEmpty()) {

                    Log.e("LOadBigNative faill: ", "aaaaa");

                    NativeAdView finalAdView = adView;
                    NativeAdView finalAdView1 = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.google_exit_native_fail_1)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative faill: ", "bbbb");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeSmall, finalAdView);

                            }).withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()).withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    Log.e("LOadBigNative faill: ", "cccc");
                                    frameLayout.removeAllViews();
                                    frameLayout.addView(finalAdView1);

                                    adView1.setVisibility(View.INVISIBLE);

                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdOpened() {
                                    super.onAdOpened();
                                }

                            }).build();
                    adLoader.loadAd(new AdRequest.Builder().build());
                }
            } else {
                Log.e("LOadBigNative faill: ", "ddddd");
                frameLayout.removeAllViews();
                frameLayout.addView(adView);

                populateNativeAdView(GoogleNativeSmall, adView);

            }
        }
    }

    public static void showAd(FrameLayout adContainer, View ad) {
        adContainer.removeAllViews();
        ad.setBackgroundColor(Color.TRANSPARENT);
        adContainer.addView(ad);
        adContainer.setVisibility(View.VISIBLE);
    }

    public static void hideAdContainer(FrameLayout adContainer) {
        adContainer.removeAllViews();
        adContainer.setVisibility(View.GONE);
    }

    public static void ClickWithExitAds(Activity act, InterClick interClick) {
        AdUtils.Ad_Count++;

        Log.e("JKJKJKJK", "ClickWithAds: interstitial ad path");
        GoogleAdsExit(act, interClick);
    }

    private static void GoogleAdsExit(Activity act, InterClick interClick) {
        if (AdUtils.dialog) {
            if (AdUtils.isOnline(act)) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.Time_Check);
                if (AdUtils.CheckOnOff && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000L)) {

                    final Dialog AdDialog = new Dialog(act, R.style.UserDialog1);
                    AdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    AdDialog.setCancelable(false);
                    AdDialog.setContentView(R.layout.ad_dialog_layout);

                    ProgressBar progress = AdDialog.findViewById(R.id.progress);

                    progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.red), PorterDuff.Mode.SRC_IN);

                    AdDialog.show();
                    if (!AdUtils.google_exit_inter.isEmpty()) {
                        InterstitialAd.load(act, AdUtils.google_exit_inter, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                super.onAdLoaded(interstitialAd);

                                AdDialog.dismiss();
                                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                    AdUtils.AdsOpenIntrestial = true;
                                    interstitialAd.show(act);
                                }

                                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                        AdUtils.AdsOpenIntrestial = false;
                                        AdDialog.dismiss();
                                        if (interClick != null) {
                                            interClick.ClickAds();
                                        }
                                    }

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();

                                        AdUtils.Time_Check = System.currentTimeMillis();

                                        AdUtils.AdsOpenIntrestial = false;
                                        AdUtils.Ad_Count = 0;
                                        if (interClick != null) {
                                            interClick.ClickAds();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                LoadGoogleExitFail(act, interClick, AdDialog, progress);
                            }
                        });
                    } else {
                        LoadGoogleExitFail(act, interClick, AdDialog, progress);
                    }
                } else {
                    AdUtils.AdsOpenIntrestial = false;
                    if (interClick != null) {
                        interClick.ClickAds();
                    }
                }

            } else {
                AdUtils.AdsOpenIntrestial = false;
                if (interClick != null) {
                    interClick.ClickAds();
                }
            }
        } else {
            PreLoadShow(act, interClick);
        }
    }

    private static void LoadGoogleExitFail(Activity act, InterClick interClick, Dialog AdDialog, ProgressBar progress) {
        if (isActivityAlive(act)) {
            AdUtils.AdsOpenIntrestial = false;
            if (interClick != null) {
                interClick.ClickAds();
            }
            return;
        }

        if (!AdUtils.google_exit_inter_fail.isEmpty()) {
            progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

            InterstitialAd.load(act, AdUtils.google_exit_inter_fail, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    if (isActivityAlive(act)) {
                        AdUtils.AdsOpenIntrestial = false;
                        if (interClick != null) {
                            interClick.ClickAds();
                        }
                        return;
                    }
                    AdDialog.dismiss();
                    if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        AdUtils.AdsOpenIntrestial = true;
                        interstitialAd.show(act);
                    }

                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            AdUtils.AdsOpenIntrestial = false;
                            AdDialog.dismiss();
                            if (interClick != null) {
                                interClick.ClickAds();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                            AdUtils.Time_Check = System.currentTimeMillis();

                            AdUtils.AdsOpenIntrestial = false;
                            AdUtils.Ad_Count = 0;
                            if (interClick != null) {
                                interClick.ClickAds();
                            }

                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    LoadGoogleExitFail_1(act, interClick, AdDialog, progress);
                }
            });
        } else {
            LoadGoogleExitFail_1(act, interClick, AdDialog, progress);
        }
    }

    private static void LoadGoogleExitFail_1(Activity act, InterClick interClick, Dialog AdDialog, ProgressBar progress) {

        if (AdUtils.google_exit_inter_fail_1.isEmpty()) {
            progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

            InterstitialAd.load(act, AdUtils.google_exit_inter_fail_1, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    AdDialog.dismiss();
                    if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        AdUtils.AdsOpenIntrestial = true;
                        interstitialAd.show(act);
                    }

                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            AdUtils.AdsOpenIntrestial = false;
                            AdDialog.dismiss();
                            if (interClick != null) {
                                interClick.ClickAds();
                            }
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                            AdUtils.Time_Check = System.currentTimeMillis();

                            AdUtils.AdsOpenIntrestial = false;
                            AdUtils.Ad_Count = 0;
                            if (interClick != null) {
                                interClick.ClickAds();
                            }

                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    AdDialog.dismiss();
                    AdUtils.AdsOpenIntrestial = false;
                    if (interClick != null) {
                        interClick.ClickAds();
                    }
                }
            });
        } else {
            AdUtils.AdsOpenIntrestial = false;
            AdDialog.dismiss();
            if (interClick != null) {
                interClick.ClickAds();
            }
        }
    }


}
