package com.playtube.protube.video.music.ads;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.playtube.protube.video.music.AppMode;
import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.activities.MainActivity;
import com.playtube.protube.video.music.util.Localization;
import com.playtube.protube.video.music.util.ServiceHelper;
import com.playtube.protube.video.music.util.ThemeHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private static final String TAG = "SplashActivity";
    private boolean hasNavigatedToMain = false;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        Localization.migrateAppLanguageSettingIfNecessary(getApplicationContext());
        ThemeHelper.setDayNightMode(this);
        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#D3221D"));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        AdUtils.width = displayMetrics.widthPixels;

        AdUtils.LoadingAllData = false;
        AdUtils.AdsOpenIntrestial = true;
        AdUtils.OpenAllData = false;

        AdUtils.GoogleNativeSmall = null;
        AdUtils.GoogleNativeBig = null;

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("JIVAN", "Google Ads SDK Initialized Successfully!");
            }
        });

        logActivityVariables("onCreate");
        if (AdUtils.isOnline(this)) {
            getAdsData();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AdUtils.OpenAllData = false;
                    AdUtils.AdsOpenIntrestial = false;
                    CallIntent(1);
                }
            }, 3000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logActivityVariables("onResume");
    }

    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void getAdsData() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Remote Config fetch failed. Online config unavailable.");
                handleRemoteConfigFailure();
                return;
            }

            remoteConfig.activate().addOnCompleteListener(activateTask -> {
                if (!activateTask.isSuccessful()) {
                    Log.e(TAG, "Remote Config activate failed. Ignoring stale/default values.");
                    handleRemoteConfigFailure();
                    return;
                }

                applyRemoteConfig(remoteConfig);

            });
        });
    }

    private void handleRemoteConfigFailure() {
        AdUtils.CheckOnOff = false;
        AdUtils.dialog = false;
        AdUtils.LoadingAllData = false;
        AdUtils.AdsOpenIntrestial = false;
        CallIntent(200);
    }

    private void applyRemoteConfig(FirebaseRemoteConfig remoteConfig) {
        AppMode.applyRemoteConfig(remoteConfig, "Splash");
        AdUtils.CheckOnOff = remoteConfig.getBoolean("ads_status");
        if (AdUtils.CheckOnOff) {
            String redirect = remoteConfig.getString("redirect");

            if (!redirect.isEmpty()) {
                boolean isAppInstalled = isPackageInstalled(SplashActivity.this, redirect);
                Log.e(TAG, "onResponse: isAppInstalled === " + isAppInstalled);
                Toast.makeText(SplashActivity.this, "Please use our updated Application.", Toast.LENGTH_SHORT).show();
                if (isAppInstalled) {
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(redirect);
                    startActivity(LaunchIntent);
                } else {
                    startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://play.google.com/store/apps/details?id=" + redirect)));
                }

                AdUtils.Google_Intertitial_Splash = "";
                AdUtils.Google_Intertitial_Splash_Fail = "";
                AdUtils.Google_Intertitial_Splash_Fail_1 = "";
                AdUtils.Google_App_open = "";
                AdUtils.Google_App_open_Fail = "";
                AdUtils.Google_App_open_Fail_1 = "";
                AdUtils.Google_App_open_splash = "";
                AdUtils.Google_App_open_splash_Fail = "";
                AdUtils.Google_App_open_splash_Fail_1 = "";

                AdUtils.Google_Native = "";
                AdUtils.Google_Native_Fail = "";
                AdUtils.Google_Native_Fail_1 = "";
                AdUtils.Google_Banner = "";
                AdUtils.Google_Banner_Fail = "";
                AdUtils.Google_Banner_Fail_1 = "";
                AdUtils.Google_Native_Banner = "";
                AdUtils.Google_Native_Banner_Fail = "";
                AdUtils.Google_Native_Banner_Fail_1 = "";
                AdUtils.Google_Intertitial = "";
                AdUtils.Google_Intertitial_Fail = "";
                AdUtils.Google_Intertitial_Fail_1 = "";
                AdUtils.Google_Rewarded = "";
                AdUtils.Google_Rewarded_Fail = "";
                AdUtils.Google_Rewarded_Fail_1 = "";
                AdUtils.Google_Medium_REC = "";
                AdUtils.Google_Medium_REC_Fail = "";
                AdUtils.Google_Medium_REC_Fail_1 = "";
                AdUtils.REC_Google_Native = "";
                AdUtils.REC_Google_Native_Fail = "";
                AdUtils.REC_Google_Native_Fail_1 = "";
                AdUtils.REC_Google_Medium_REC = "";
                AdUtils.REC_Google_Medium_REC_Fail = "";
                AdUtils.REC_Google_Medium_REC_Fail_1 = "";

                AdUtils.google_exit_inter = "";
                AdUtils.google_exit_inter_fail = "";
                AdUtils.google_exit_inter_fail_1 = "";
                AdUtils.google_exit_native = "";
                AdUtils.google_exit_native_fail = "";
                AdUtils.google_exit_native_fail_1 = "";
                AdUtils.google_exit_mrec = "";
                AdUtils.google_exit_mrec_fail = "";
                AdUtils.google_exit_mrec_fail_1 = "";

                finish();
                return;
            }

            AdUtils.Google_App_open = remoteConfig.getString("Google_App_Open");
            AdUtils.Google_App_open_Fail = remoteConfig.getString("Google_App_Open_Fail");
            AdUtils.Google_App_open_Fail_1 = remoteConfig.getString("Google_App_Open_Fail_1");
            AdUtils.Google_App_open_splash = remoteConfig.getString("Google_App_Open_Splash");
            AdUtils.Google_App_open_splash_Fail = remoteConfig.getString("Google_App_Open_Splash_Fail");
            AdUtils.Google_App_open_splash_Fail_1 = remoteConfig.getString("Google_App_Open_Splash_Fail_1");

            AdUtils.Google_Intertitial_Splash = remoteConfig.getString("Google_Intertitial_Splash");
            AdUtils.Google_Intertitial_Splash_Fail = remoteConfig.getString("Google_Intertitial_Splash_Fail");
            AdUtils.Google_Intertitial_Splash_Fail_1 = remoteConfig.getString("Google_Intertitial_Splash_Fail_1");
            AdUtils.Google_Intertitial = remoteConfig.getString("Google_Intertitial");
            AdUtils.Google_Intertitial_Fail = remoteConfig.getString("Google_Intertitial_Fail");
            AdUtils.Google_Intertitial_Fail_1 = remoteConfig.getString("Google_Intertitial_Fail_1");

            AdUtils.Google_Rewarded = remoteConfig.getString("Google_Rewarded");
            AdUtils.Google_Rewarded_Fail = remoteConfig.getString("Google_Rewarded_Fail");
            AdUtils.Google_Rewarded_Fail_1 = remoteConfig.getString("Google_Rewarded_Fail_1");

            AdUtils.Google_Native = remoteConfig.getString("Google_Native");
            AdUtils.Google_Native_Fail = remoteConfig.getString("Google_Native_Fail");
            AdUtils.Google_Native_Fail_1 = remoteConfig.getString("Google_Native_Fail_1");
            AdUtils.Google_Native_Banner = remoteConfig.getString("Google_Native_Banner");
            AdUtils.Google_Native_Banner_Fail = remoteConfig.getString("Google_Native_Banner_Fail");
            AdUtils.Google_Native_Banner_Fail_1 = remoteConfig.getString("Google_Native_Banner_Fail_1");

            AdUtils.Google_Banner = remoteConfig.getString("Google_Banner");
            AdUtils.Google_Banner_Fail = remoteConfig.getString("Google_Banner_Fail");
            AdUtils.Google_Banner_Fail_1 = remoteConfig.getString("Google_Banner_Fail_1");

            AdUtils.Google_Medium_REC = remoteConfig.getString("Google_Medium_REC");
            AdUtils.Google_Medium_REC_Fail = remoteConfig.getString("Google_Medium_REC_Fail");
            AdUtils.Google_Medium_REC_Fail_1 = remoteConfig.getString("Google_Medium_REC_Fail_1");

            AdUtils.REC_Google_Native = remoteConfig.getString("REC_Google_Native");
            AdUtils.REC_Google_Native_Fail = remoteConfig.getString("REC_Google_Native_Fail");
            AdUtils.REC_Google_Native_Fail_1 = remoteConfig.getString("REC_Google_Native_Fail_1");
            AdUtils.REC_Google_Medium_REC = remoteConfig.getString("REC_Google_Medium_REC");
            AdUtils.REC_Google_Medium_REC_Fail = remoteConfig.getString("REC_Google_Medium_REC_Fail");
            AdUtils.REC_Google_Medium_REC_Fail_1 = remoteConfig.getString("REC_Google_Medium_REC_Fail_1");

            AdUtils.native_headline_color = remoteConfig.getString("native_headline_color");
            AdUtils.native_body_color = remoteConfig.getString("native_body_color");
            AdUtils.native_button_color = remoteConfig.getString("native_button_color");
            AdUtils.native_button_text_color = remoteConfig.getString("native_button_text_color");
            AdUtils.native_bg_color = remoteConfig.getString("native_bg_color");

            AdUtils.feed_after = (int) remoteConfig.getLong("feed_after");
            AdUtils.feed_max = (int) remoteConfig.getLong("feed_max");
            AdUtils.sub_after = (int) remoteConfig.getLong("sub_after");
            AdUtils.sub_every = (int) remoteConfig.getLong("sub_every");
            AdUtils.sub_max = (int) remoteConfig.getLong("sub_max");
            AdUtils.base_after = (int) remoteConfig.getLong("base_after");
            AdUtils.base_every = (int) remoteConfig.getLong("base_every");
            AdUtils.base_max = (int) remoteConfig.getLong("base_max");
            AdUtils.rel_every = (int) remoteConfig.getLong("rel_every");
            AdUtils.exit_page = remoteConfig.getBoolean("exit_page");
            AdUtils.google_exit_inter = remoteConfig.getString("google_exit_inter");
            AdUtils.google_exit_inter_fail = remoteConfig.getString("google_exit_inter_fail");
            AdUtils.google_exit_inter_fail_1 = remoteConfig.getString("google_exit_inter_fail_1");
            AdUtils.google_exit_native = remoteConfig.getString("google_exit_native");
            AdUtils.google_exit_native_fail = remoteConfig.getString("google_exit_native_fail");
            AdUtils.google_exit_native_fail_1 = remoteConfig.getString("google_exit_native_fail_1");
            AdUtils.google_exit_mrec = remoteConfig.getString("google_exit_mrec");
            AdUtils.google_exit_mrec_fail = remoteConfig.getString("google_exit_mrec_fail");
            AdUtils.google_exit_mrec_fail_1 = remoteConfig.getString("google_exit_mrec_fail_1");

            AdUtils.ads_native_second = (int) remoteConfig.getLong("ads_native_second");
            AdUtils.NativeTime_Check = remoteConfig.getLong("NativeTime_Check");
            AdUtils.NativeBannerTime_Check = remoteConfig.getLong("NativeBannerTime_Check");


            AdUtils.ShowRewarded = remoteConfig.getBoolean("ShowRewarded");
            AdUtils.dialog = remoteConfig.getBoolean("dialog");

            AdUtils.Ad_Click = (int) remoteConfig.getLong("Ad_Click");
            AdUtils.ads_first_click_interstitial = (int) remoteConfig.getLong("ads_first_click_interstitial");
            AdUtils.Ad_Count = AdUtils.Ad_Click - AdUtils.ads_first_click_interstitial;
            try {
                AdUtils.Time_interval = Integer.parseInt(remoteConfig.getString("Time_interval"));
            } catch (NumberFormatException ignored) {
                AdUtils.Time_interval = 31;
            }

            logAdsValue();

            requestConsentBeforeLoadingAds();
        } else {
            AdUtils.AdsOpenIntrestial = false;

            AdUtils.Google_Intertitial_Splash = "";
            AdUtils.Google_Intertitial_Splash_Fail = "";
            AdUtils.Google_Intertitial_Splash_Fail_1 = "";
            AdUtils.Google_App_open = "";
            AdUtils.Google_App_open_Fail = "";
            AdUtils.Google_App_open_Fail_1 = "";
            AdUtils.Google_App_open_splash = "";
            AdUtils.Google_App_open_splash_Fail = "";
            AdUtils.Google_App_open_splash_Fail_1 = "";

            AdUtils.Google_Native = "";
            AdUtils.Google_Native_Fail = "";
            AdUtils.Google_Native_Fail_1 = "";
            AdUtils.Google_Banner = "";
            AdUtils.Google_Banner_Fail = "";
            AdUtils.Google_Banner_Fail_1 = "";
            AdUtils.Google_Native_Banner = "";
            AdUtils.Google_Native_Banner_Fail = "";
            AdUtils.Google_Native_Banner_Fail_1 = "";
            AdUtils.Google_Intertitial = "";
            AdUtils.Google_Intertitial_Fail = "";
            AdUtils.Google_Intertitial_Fail_1 = "";
            AdUtils.Google_Rewarded = "";
            AdUtils.Google_Rewarded_Fail = "";
            AdUtils.Google_Rewarded_Fail_1 = "";
            AdUtils.Google_Medium_REC = "";
            AdUtils.Google_Medium_REC_Fail = "";
            AdUtils.Google_Medium_REC_Fail_1 = "";
            AdUtils.REC_Google_Native = "";
            AdUtils.REC_Google_Native_Fail = "";
            AdUtils.REC_Google_Native_Fail_1 = "";
            AdUtils.REC_Google_Medium_REC = "";
            AdUtils.REC_Google_Medium_REC_Fail = "";
            AdUtils.REC_Google_Medium_REC_Fail_1 = "";


            AdUtils.google_exit_inter = "";
            AdUtils.google_exit_inter_fail = "";
            AdUtils.google_exit_inter_fail_1 = "";
            AdUtils.google_exit_native = "";
            AdUtils.google_exit_native_fail = "";
            AdUtils.google_exit_native_fail_1 = "";
            AdUtils.google_exit_mrec = "";
            AdUtils.google_exit_mrec_fail = "";
            AdUtils.google_exit_mrec_fail_1 = "";

            CallIntent(2);
        }
    }

    private void requestConsentBeforeLoadingAds() {
        final ConsentInformation consentInformation =
                UserMessagingPlatform.getConsentInformation(this);
        final ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();

        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        this,
                        (FormError formError) -> {
                            if (formError != null) {
                                Log.w(TAG, "Consent form error: " + formError.getMessage());
                            }
                            continueLoadingAdsAfterConsent(consentInformation);
                        }),
                requestConsentError -> {
                    Log.w(TAG, "Consent info update failed: " + requestConsentError.getMessage());
                    continueLoadingAdsAfterConsent(consentInformation);
                });
    }

    private void continueLoadingAdsAfterConsent(@NonNull final ConsentInformation consentInformation) {
        if (!consentInformation.canRequestAds()) {
            AdUtils.AdsOpenIntrestial = false;
            AdUtils.LoadingAllData = false;
            CallIntent(12);
            return;
        }

        if (!AdUtils.dialog) {
            if (AdUtils.ShowRewarded) {
                RewardedAdManager.getInstance().init(SplashActivity.this);
            } else {
                AdUtils.PreLoad(SplashActivity.this);
            }
        }
        fetchAd();
        AdUtils.LoadingAllData = true;
    }

    private void fetchAd() {
        String[] appOpenIds = new String[]{
                AdUtils.Google_App_open_splash,
                AdUtils.Google_App_open_splash_Fail,
                AdUtils.Google_App_open_splash_Fail_1
        };
        loadAppOpen(appOpenIds, 0);
    }


    private void loadAppOpen(String[] appOpenIds, int index) {
        if (index >= appOpenIds.length) {
            String[] interstitialIds = new String[]{
                    AdUtils.Google_Intertitial_Splash,
                    AdUtils.Google_Intertitial_Splash_Fail,
                    AdUtils.Google_Intertitial_Splash_Fail_1
            };
            loadInterstitialByIndex(interstitialIds, 0);
            return;
        }

        String adUnitId = appOpenIds[index];
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            loadAppOpen(appOpenIds, index + 1);
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ap) {
                super.onAdLoaded(ap);
                appOpenAd = ap;
                AdUtils.AdsOpenIntrestial = true;
                AdUtils.OpenAllData = true;
                showAppOpenAd();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                loadAppOpen(appOpenIds, index + 1);
            }
        };

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(this, adUnitId, request, loadCallback);
    }

    private void loadInterstitialByIndex(String[] interstitialIds, int index) {
        if (index >= interstitialIds.length) {
            AdUtils.AdsOpenIntrestial = false;
            CallIntent(3);
            return;
        }

        String adUnitId = interstitialIds[index];
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            loadInterstitialByIndex(interstitialIds, index + 1);
            return;
        }

        AdUtils.AdsOpenIntrestial = true;
        AdUtils.OpenAllData = true;

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        AdUtils.AdsOpenIntrestial = false;
                        CallIntent(4);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        AdUtils.AdsOpenIntrestial = false;
                        CallIntent(5);
                    }
                });

                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    interstitialAd.show(SplashActivity.this);
                } else {
                    AdUtils.AdsOpenIntrestial = false;
                    CallIntent(6);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                loadInterstitialByIndex(interstitialIds, index + 1);
            }
        });
    }

    private void showAppOpenAd() {
        AdUtils.OpenAllData = true;

        if (isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AdUtils.AdsOpenIntrestial = false;
                            CallIntent(7);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            AdUtils.AdsOpenIntrestial = false;
                            CallIntent(8);
                        }
                    };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                appOpenAd.show(SplashActivity.this);
            } else {
                AdUtils.AdsOpenIntrestial = false;
                CallIntent(9);
            }
        } else {
            AdUtils.AdsOpenIntrestial = false;
            String[] interstitialIds = new String[]{
                    AdUtils.Google_Intertitial_Splash,
                    AdUtils.Google_Intertitial_Splash_Fail,
                    AdUtils.Google_Intertitial_Splash_Fail_1
            };
            loadInterstitialByIndex(interstitialIds, 0);
        }
    }

    private AppOpenAd appOpenAd;

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    private void CallIntent(int from) {
        if (hasNavigatedToMain || isFinishing() || isDestroyed()) {
            return;
        }
        hasNavigatedToMain = true;
        Log.e("JIVAN", "onResponse: from == " + from);

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }

    private void logAdsValue() {
        Log.e(TAG, "onResponse: Start");
        Log.e(TAG, "onResponse: Time_interval === " + AdUtils.Time_interval);
        Log.e(TAG, "onResponse: AdUtils.Ad_Click === " + AdUtils.Ad_Click);
        Log.e(TAG, "onResponse: AdUtils.ads_first_click_interstitial === " + AdUtils.ads_first_click_interstitial);
        Log.e(TAG, "onResponse: AdUtils.Ad_Count === " + AdUtils.Ad_Count);
        Log.e(TAG, "onResponse: AdUtils.CheckOnOff === " + AdUtils.CheckOnOff);
        Log.e(TAG, "onResponse: AdUtils.dialog === " + AdUtils.dialog);
        Log.e(TAG, "onResponse: AdUtils.native_button_color === " + AdUtils.native_button_color);
        Log.e(TAG, "onResponse: AdUtils.native_button_text_color === " + AdUtils.native_button_text_color);
        Log.e(TAG, "onResponse: AdUtils.native_bg_color === " + AdUtils.native_bg_color);
        Log.e(TAG, "onResponse: AdUtils.ads_native_second === " + AdUtils.ads_native_second);
        Log.e(TAG, "onResponse: AdUtils.ShowRewarded === " + AdUtils.ShowRewarded);

        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Splash === " + AdUtils.Google_Intertitial_Splash);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Splash_Fail === " + AdUtils.Google_Intertitial_Splash_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Splash_Fail_1 === " + AdUtils.Google_Intertitial_Splash_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial === " + AdUtils.Google_Intertitial);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Fail === " + AdUtils.Google_Intertitial_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Fail_1 === " + AdUtils.Google_Intertitial_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open === " + AdUtils.Google_App_open);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_Fail === " + AdUtils.Google_App_open_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_Fail_1 === " + AdUtils.Google_App_open_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_splash === " + AdUtils.Google_App_open_splash);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_splash_Fail === " + AdUtils.Google_App_open_splash_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_splash_Fail_1 === " + AdUtils.Google_App_open_splash_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Rewarded === " + AdUtils.Google_Rewarded);
        Log.e(TAG, "onResponse: AdUtils.Google_Rewarded_Fail === " + AdUtils.Google_Rewarded_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Rewarded_Fail_1 === " + AdUtils.Google_Rewarded_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Native === " + AdUtils.Google_Native);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Fail === " + AdUtils.Google_Native_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Fail_1 === " + AdUtils.Google_Native_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Banner === " + AdUtils.Google_Native_Banner);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Banner_Fail === " + AdUtils.Google_Native_Banner_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Banner_Fail_1 === " + AdUtils.Google_Native_Banner_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Banner === " + AdUtils.Google_Banner);
        Log.e(TAG, "onResponse: AdUtils.Google_Banner_Fail === " + AdUtils.Google_Banner_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Banner_Fail_1 === " + AdUtils.Google_Banner_Fail_1);
        Log.e(TAG, "onResponse: AdUtils.Google_Medium_REC === " + AdUtils.REC_Google_Medium_REC);
        Log.e(TAG, "onResponse: AdUtils.Google_Medium_REC_Fail === " + AdUtils.REC_Google_Medium_REC_Fail);
        Log.e(TAG, "onResponse: AdUtils.REC_Google_Medium_REC_Fail_1 === " + AdUtils.REC_Google_Medium_REC_Fail_1);

        Log.e(TAG, "onResponse: End");
    }

    private void logActivityVariables(String stage) {
        Log.d(TAG, "----- Activity variables " + stage + " -----");
        Field[] fields = SplashActivity.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Log.d(TAG, field.getName() + " = " + String.valueOf(field.get(this)));
            } catch (Exception e) {
                Log.e(TAG, "Failed to read " + field.getName(), e);
            }
        }
        Log.d(TAG, "----- End activity variables -----");
    }
}


