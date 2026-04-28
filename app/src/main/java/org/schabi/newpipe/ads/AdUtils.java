package org.schabi.newpipe.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinSdkUtils;
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

import org.schabi.newpipe.R;

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

    public static String AppLovin_Native = "";
    public static String AppLovin_Native_Banner = "";
    public static String AppLovin_Banner = "";
    public static String AppLovin_MREC = "";
    public static String AppLovin_Back = "";
    public static String AppLovin_Interstitial = "";
    public static String AppLovin_Interstitial_Splash = "";

    // Dedicated RecyclerView ad-unit IDs (kept separate from other ad placements).
    public static String REC_Google_Native = "";
    public static String REC_Google_Native_Fail = "";
    public static String REC_Google_Native_Fail_1 = "";
    public static String REC_Google_Medium_REC = "";
    public static String REC_Google_Medium_REC_Fail = "";
    public static String REC_Google_Medium_REC_Fail_1 = "";
    public static String REC_AppLovin_Native = "";
    public static String REC_AppLovin_MREC = "";

    public static int ads_native_second = 10;
    public static long NativeTime_Check = 0;
    public static long NativeBannerTime_Check = 0;

    public static String native_button_color = "#ff8d36";
    public static String native_button_text_color = "#ffffff";
    public static String native_bg_color = "#f7f7f7";

    public static String default_native_button_color = "#ff8d36";
    public static String default_native_button_text_color = "#ffffff";
    public static String default_native_bg_color = "#f7f7f7";

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

    public static boolean dialog = true;

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager;
        if (!(context == null || (connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)) == null)) {
            if (Build.VERSION.SDK_INT >= 29) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (networkCapabilities == null ||
                        (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                && !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                && !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                    return false;
                }
                return true;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static InterstitialAd GoogleInt = null;

    public static void PreLoad(Context act) {
        if (AdUtils.isOnline(act)) {
            InterstitialAd.load(act, AdUtils.Google_Intertitial, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    GoogleInt = null;

                    InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
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
    }

    public static void PreLoadShow(Activity act, InterClick interClick) {
        if (AdUtils.isOnline(act)) {
            long ctime = System.currentTimeMillis();
            long aa = ctime - (AdUtils.Time_Check);
            if (AdUtils.CheckOnOff && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000)) {
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


    }

    public interface InterClick {
        void ClickAds();
    }

    public static void ClickWithAds(Activity act, InterClick interClick) {
        AdUtils.Ad_Count++;

        // TEST ONLY (remove later)
        AdUtils.CheckOnOff = true;
        AdUtils.Ad_Click = 0;        // so Ad_Count (1,2,3...) is always > 0 after increment
        AdUtils.Time_interval = 0;   // no wait time
        AdUtils.Time_Check = 0L;     // makes aa huge

        Log.e("JKJKJKJK", "ClickWithAds: new inter ad showing");

        if (AdUtils.AppLovin_Interstitial.isEmpty()) {
            GoogleAds(act, interClick);
        } else {
            long ctime = System.currentTimeMillis();
            long aa = ctime - (AdUtils.Time_Check);
            if (AdUtils.CheckOnOff && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000L)) {

                final Dialog AdDialog = new Dialog(act, R.style.UserDialog1);
                AdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                AdDialog.setCancelable(false);
                AdDialog.setContentView(R.layout.ad_dialog_layout);

                ProgressBar progress = AdDialog.findViewById(R.id.progress);

                AdDialog.show();

                progress.getIndeterminateDrawable()
                        .setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

                MaxInterstitialAd interstitialAd = new MaxInterstitialAd(AdUtils.AppLovin_Interstitial, act);
                interstitialAd.setListener(new MaxAdListener() {
                    @Override
                    public void onAdLoaded(MaxAd ad) {

                        Log.e("onAdLoadedApplovin: ", "aaaaaa");
                        AdDialog.dismiss();
                        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            AdUtils.AdsOpenIntrestial = true;
                            interstitialAd.showAd();
                        }
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {
                        Log.e("onAdLoadedApplovin: ", "bbbb");
                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        Log.e("onAdLoadedApplovin: ", "cccc");
                        AdDialog.dismiss();
                        AdUtils.Ad_Count = 0;
                        AdUtils.Time_Check = System.currentTimeMillis();
                        AdUtils.AdsOpenIntrestial = false;
                        if (interClick != null) {
                            interClick.ClickAds();
                        }
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                        Log.e("onAdLoadedApplovin: ", "fff");
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        Log.e("onAdLoadedApplovin: ", "ddddd");
                        GoogleAdsFailAppLovin(act, interClick, progress, AdDialog);
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        Log.e("onAdLoadedApplovin: ", "mmmm");
                        AdDialog.dismiss();
                        AdUtils.AdsOpenIntrestial = false;
                        if (interClick != null) {
                            interClick.ClickAds();
                        }
                    }
                });

                interstitialAd.loadAd();


            } else {
                AdUtils.AdsOpenIntrestial = false;
                if (interClick != null) {
                    interClick.ClickAds();
                }
            }
        }


    }

    private static void GoogleAdsFailAppLovin(Activity act, InterClick interClick, ProgressBar progress, Dialog AdDialog) {

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

    }

    private static void GoogleAds(Activity act, InterClick interClick) {
        if (AdUtils.dialog) {
            if (AdUtils.isOnline(act)) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.Time_Check);
                if (AdUtils.CheckOnOff == true && AdUtils.Ad_Count > AdUtils.Ad_Click && aa > (AdUtils.Time_interval * 1000)) {

                    final Dialog AdDialog = new Dialog(act, R.style.UserDialog1);
                    AdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    AdDialog.setCancelable(false);
                    AdDialog.setContentView(R.layout.ad_dialog_layout);

                    ProgressBar progress = AdDialog.findViewById(R.id.progress);

                    progress.getIndeterminateDrawable()
                            .setColorFilter(ContextCompat.getColor(act, R.color.red), PorterDuff.Mode.SRC_IN);


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

        progress.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(act, R.color.blue), PorterDuff.Mode.SRC_IN);

        InterstitialAd.load(act, AdUtils.Google_Intertitial_Fail, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
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
    }

    public static AdLoader adLoader;
    public static MaxAd nativeAd;

    public static void LOadBigNative(Activity act, FrameLayout frameLayout, String size) {

        if (size.equals("big")) {
            if (AdUtils.AppLovin_Native.isEmpty()) {
                LOadBigNativeFailAppLovin(act, frameLayout, size);
            } else {
                View adView = LayoutInflater.from(act).inflate(R.layout.applovin_native, null);

                Button cta_button = (Button) adView.findViewById(R.id.cta_button);
                RelativeLayout main_rel = adView.findViewById(R.id.main_rel);

                try {
                    main_rel.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
                    cta_button.setBackgroundColor(Color.parseColor(AdUtils.native_button_color));
                    cta_button.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
                } catch (Exception e) {
                    main_rel.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
                    cta_button.setBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
                    cta_button.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
                }

                MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(adView)
                        .setTitleTextViewId(R.id.title_text_view)
                        .setBodyTextViewId(R.id.body_text_view)
                        .setAdvertiserTextViewId(R.id.advertiser_textView)
                        .setIconImageViewId(R.id.icon_image_view)
                        .setMediaContentViewGroupId(R.id.media_view_container)
                        .setOptionsContentViewGroupId(R.id.ad_options_view)
                        .setCallToActionButtonId(R.id.cta_button)
                        .build();

                MaxNativeAdView nativeAdView = new MaxNativeAdView(binder, act);

                MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(AdUtils.AppLovin_Native, act);
                nativeAdLoader.loadAd(nativeAdView);
                nativeAdLoader.setRevenueListener(ad -> {

                });

                nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                    @Override
                    public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {

                        if (nativeAd != null) {
                            nativeAdLoader.destroy(nativeAd);
                        }

                        nativeAd = ad;

                        frameLayout.removeAllViews();
                        frameLayout.addView(nativeAdView);

                    }

                    @Override
                    public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                        Log.e("onNativeAdLoadFailed: ", "addd");
                        LOadBigNativeFailAppLovin(act, frameLayout, size);

                    }

                    @Override
                    public void onNativeAdClicked(final MaxAd ad) {

                    }
                });
            }
        }

        if (size.equals("small")) {
            if (AdUtils.AppLovin_Native_Banner.equals("")) {
                LOadBigNativeFailAppLovin(act, frameLayout, size);
            } else {
                View adView = LayoutInflater.from(act).inflate(R.layout.applovin_native_small, null);

                Button cta_button = adView.findViewById(R.id.cta_button);
                RelativeLayout main_rel = adView.findViewById(R.id.main_rel);
                CardView card = adView.findViewById(R.id.card);

                try {
                    main_rel.setBackgroundColor(Color.parseColor(AdUtils.native_bg_color));
                    card.setBackgroundColor(Color.parseColor(AdUtils.native_button_color));
                    cta_button.setTextColor(Color.parseColor(AdUtils.native_button_text_color));
                } catch (Exception e) {
                    main_rel.setBackgroundColor(Color.parseColor(AdUtils.default_native_bg_color));
                    card.setBackgroundColor(Color.parseColor(AdUtils.default_native_button_color));
                    cta_button.setTextColor(Color.parseColor(AdUtils.default_native_button_text_color));
                }

                MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(adView)
                        .setTitleTextViewId(R.id.title_text_view)
                        .setBodyTextViewId(R.id.body_text_view)
                        .setAdvertiserTextViewId(R.id.advertiser_textView)
                        .setIconImageViewId(R.id.icon_image_view)
                        .setOptionsContentViewGroupId(R.id.ad_options_view)
                        .setCallToActionButtonId(R.id.cta_button)
                        .build();

                MaxNativeAdView nativeAdView = new MaxNativeAdView(binder, act);

                MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(AdUtils.AppLovin_Native_Banner, act);
                nativeAdLoader.loadAd(nativeAdView);
                nativeAdLoader.setRevenueListener(ad -> {

                });
                nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                    private MaxAd nativeAd;

                    @Override
                    public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                        if (nativeAd != null) {
                            nativeAdLoader.destroy(nativeAd);
                        }

                        nativeAd = ad;

                        Log.e("onResumedd: ", "bbbb");

                        frameLayout.removeAllViews();
                        frameLayout.addView(nativeAdView);

                    }

                    @Override
                    public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                        Log.e("onResumedd: ", "ccc");
                        LOadBigNativeFailAppLovin(act, frameLayout, size);
                    }

                    @Override
                    public void onNativeAdClicked(final MaxAd ad) {
                        Log.e("onResumedd: ", "ddd");
                    }
                });
            }
        }
    }

    private static void LOadBigNativeFailAppLovin(Activity act, FrameLayout frameLayout, String size) {
        NativeAdView adView = null;
        if (size.equals("small")) {
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

        if (size.equals("small")) {
            if (!AdUtils.Google_Native_Banner.equals("")) {
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
                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                                    .build())
                            .withAdListener(new AdListener() {
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
                    if (GoogleNativeSmall != null) {
                        Log.e("LOadBigNative: ", "ddddd");
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);

                        populateNativeAdViewSmallBanner(GoogleNativeSmall, adView);

                    } else {
                        Log.e("LOadBigNative: ", "ffff");
                        LOadBigNativeFail(act, frameLayout, size);
                    }
                }
            } else {
                LOadBigNativeFail(act, frameLayout, size);
            }
        }

        if (size.equals("big")) {
            if (!AdUtils.Google_Native.equals("")) {
                long ctime = System.currentTimeMillis();
                long aa = ctime - (AdUtils.NativeTime_Check);
                if (aa > (AdUtils.ads_native_second * 1000)) {
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


                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                                    .build())
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
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
                    if (GoogleNativeBig != null) {
                        Log.e("LOadBigNative: ", "ddddd");
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);

                        populateNativeAdView(GoogleNativeBig, adView);

                    } else {
                        Log.e("LOadBigNative: ", "ffff");
                        LOadBigNativeFail(act, frameLayout, size);
                    }
                }

            } else {
                Log.e("LOadBigNative: ", "hhhh");
                LOadBigNativeFail(act, frameLayout, size);
            }
        }
    }

    public static void LOadBigNativeFail(Activity act, FrameLayout frameLayout, String size) {

        NativeAdView adView = null;
        if (size.equals("small")) {
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

        if (size.equals("small")) {
            if (GoogleNativeSmall == null) {
                if (!AdUtils.Google_Native_Banner_Fail.equals("")) {

                    Log.e("LOadBigNative faill: ", "aaaaa");

                    NativeAdView finalAdView = adView;
                    NativeAdView finalAdView1 = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Banner_Fail)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative faill: ", "bbbb");

                                AdUtils.NativeBannerTime_Check = System.currentTimeMillis();

                                GoogleNativeSmall = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdViewSmallBanner(GoogleNativeSmall, finalAdView);

                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                                    .build())
                            .withAdListener(new AdListener() {
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

        if (size.equals("big")) {
            if (GoogleNativeBig == null) {
                if (!AdUtils.Google_Native_Fail.equals("")) {

                    Log.e("LOadBigNative faill: ", "aaaaa");

                    NativeAdView finalAdView = adView;
                    NativeAdView finalAdView1 = adView;
                    adLoader = new AdLoader.Builder(act, AdUtils.Google_Native_Fail)

                            .forNativeAd(nativeAds -> {
                                Log.e("LOadBigNative faill: ", "bbbb");

                                AdUtils.NativeTime_Check = System.currentTimeMillis();

                                GoogleNativeBig = nativeAds;

                                frameLayout.removeAllViews();
                                frameLayout.addView(finalAdView);

                                populateNativeAdView(GoogleNativeBig, finalAdView);

                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                                    .build())
                            .withAdListener(new AdListener() {
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

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
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

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    public static void LoadAppLovinBanner(Activity act, FrameLayout linear) {
        FrameLayout adContainer;
        if (linear != null) {
            adContainer = linear;
        } else {
            adContainer = new FrameLayout(act);
            adContainer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            linear.removeAllViews();
            linear.addView(adContainer);
        }

        if (!AdUtils.isOnline(act)) {
            hideAdContainer(adContainer);
            return;
        }

        if (AdUtils.AppLovin_Banner.isEmpty()) {
            loadGoogleBanner(act, adContainer);
            return;
        }

        adContainer.removeAllViews();

        MaxAdView adView = new MaxAdView(AdUtils.AppLovin_Banner, act);
//        final boolean isTablet = AppLovinSdkUtils.isTablet(act);
        final int heightPx = AppLovinSdkUtils.dpToPx(act, 50);
        adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adContainer.addView(adView);

        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                showAd(adContainer, adView);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                adContainer.removeAllViews();
                adView.destroy();
                loadGoogleBanner(act, adContainer);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
            }

            @Override
            public void onAdHidden(MaxAd ad) {
            }

            @Override
            public void onAdClicked(MaxAd ad) {
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                adContainer.removeAllViews();
                adView.destroy();
                loadGoogleBanner(act, adContainer);
            }
        });

        adView.loadAd();
    }

    public static void loadGoogleBanner(Activity act, FrameLayout adContainer) {
        AdUtils.Google_Banner = "";
        AdUtils.Google_Banner_Fail = "";
        AdUtils.AppLovin_Native_Banner = "";
        AdUtils.Google_Native_Banner = "/6499/example/native";
        if (AdUtils.Google_Banner.isEmpty()) {
            loadGoogleBannerFail(act, adContainer);
            return;
        }

        adContainer.removeAllViews();

        AdView googleAd = new AdView(act);
        googleAd.setAdUnitId(AdUtils.Google_Banner);
        googleAd.setAdSize(getAdaptiveBannerSize(act, adContainer));
        googleAd.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        googleAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showAd(adContainer, googleAd);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                adContainer.removeAllViews();
                googleAd.destroy();
                loadGoogleBannerFail(act, adContainer);
            }
        });

        googleAd.loadAd(new AdRequest.Builder().build());
    }

    public static void loadGoogleBannerFail(Activity act, FrameLayout adContainer) {
        if (AdUtils.Google_Banner_Fail.isEmpty()) {
            LOadBigNative(act, adContainer, "small");
            return;
        }

        adContainer.removeAllViews();

        AdView googleAd = new AdView(act);
        googleAd.setAdUnitId(AdUtils.Google_Banner_Fail);
        googleAd.setAdSize(getAdaptiveBannerSize(act, adContainer));
        googleAd.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        googleAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showAd(adContainer, googleAd);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                adContainer.removeAllViews();
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

    public static void LoadAppLovinMREC(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        loadMediumREC.set(true);

        if (AdUtils.isOnline(act)) {
            if (!AppLovin_MREC.isEmpty()) {
                MaxAdView adView = new MaxAdView(AdUtils.AppLovin_MREC, MaxAdFormat.MREC, act);
//                final int widthPx = AppLovinSdkUtils.dpToPx( act, 300 );
                final int heightPx = AppLovinSdkUtils.dpToPx(act, 250);
//                adView.setLayoutParams(new FrameLayout.LayoutParams(widthPx, heightPx, Gravity.CENTER));
                adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
                adView.setBackgroundColor(Color.TRANSPARENT);
                adContainer.addView(adView);

                adView.setListener(new MaxAdViewAdListener() {
                    @Override
                    public void onAdExpanded(MaxAd ad) {

                    }

                    @Override
                    public void onAdCollapsed(MaxAd ad) {

                    }

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        loadMediumREC.set(true);

                        showAd(adContainer, adView);
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        loadMediumREC.set(false);

                        adContainer.removeAllViews();
                        adView.destroy();

                        loadGoogleMREC(act, adContainer, loadMediumREC, size);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {
                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        adContainer.removeAllViews();
                        adView.destroy();

                        loadGoogleMREC(act, adContainer, loadMediumREC, size);
                    }
                });

                adView.loadAd();
            } else {
                loadGoogleMREC(act, adContainer, loadMediumREC, size);
            }
        } else {
            loadMediumREC.set(false);
            hideAdContainer(adContainer);
        }
    }

    public static void loadGoogleMREC(Activity act, FrameLayout adContainer, AtomicBoolean loadMediumREC, String size) {
        loadMediumREC.set(true);

        if (!AdUtils.Google_Medium_REC.isEmpty()) {
            adContainer.removeAllViews();

            AdView googleAd = new AdView(act);
            googleAd.setAdUnitId(AdUtils.Google_Medium_REC);
            googleAd.setAdSize(AdSize.MEDIUM_RECTANGLE);
            googleAd.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            googleAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    loadMediumREC.set(true);
                    showAd(adContainer, googleAd);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    loadMediumREC.set(false);

                    adContainer.removeAllViews();
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
        loadMediumREC.set(true);

        if (!AdUtils.Google_Medium_REC_Fail.isEmpty()) {
            adContainer.removeAllViews();

            AdView googleAd = new AdView(act);
            googleAd.setAdUnitId(AdUtils.Google_Medium_REC_Fail);
            googleAd.setAdSize(AdSize.MEDIUM_RECTANGLE);
            googleAd.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            googleAd.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    loadMediumREC.set(true);
                    showAd(adContainer, googleAd);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    loadMediumREC.set(false);

                    adContainer.removeAllViews();
                    googleAd.destroy();

//                    hideAdContainer(adContainer);
                    LOadBigNative(act, adContainer, size);
                }
            });

            googleAd.loadAd(new AdRequest.Builder().build());
        } else {
            loadMediumREC.set(false);
//            hideAdContainer(adContainer);
            LOadBigNative(act, adContainer, size);
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

}
