package org.schabi.newpipe.ads;

import android.app.Activity;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.facebook.appevents.AppEventsLogger;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.schabi.newpipe.R;
import org.schabi.newpipe.activities.MainActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
//        Localization.migrateAppLanguageSettingIfNecessary(getApplicationContext());
//        ThemeHelper.setDayNightMode(this);
//        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#010B1A"));
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

        // AppLovin init with current MAX SDK API.
        AppLovinSdk.getInstance(this).getSettings().setVerboseLogging(true);
        final AppLovinSdkInitializationConfiguration initConfig =
                AppLovinSdkInitializationConfiguration.builder("YOUR_SDK_KEY_HERE")
                        .setMediationProvider(AppLovinMediationProvider.MAX)
                        .setTestDeviceAdvertisingIds(Arrays.asList(
                                "ac4caa3d-d7fe-4554-b176-e4482f54234b"
                        ))
                        .build();
        AppLovinSdk.getInstance(this).initialize(initConfig, config -> {
            Log.d("JIVAN", "AppLovin Ads SDK Initialized Successfully!");
            Log.d("JIVAN", "Country Code: " + config.getCountryCode());
        });

        logActivityVariables("onCreate");

        if (AdUtils.isOnline(this)) {
            getAdsData(this);
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

    public void getAdsData(Activity activity) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(activity);
        String url = "53CF096A7892548C91F90E35C99C5044A4C8F7A55C22CC951F2A2AB747C5E91B";
        try {
            url = AESUtils.decrypt(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject ob = new JSONObject(response);

                    if (ob.has("ads_status")) {
                        String status = ob.getString("ads_status");
                        Log.e(TAG, "onResponse: status === " + status);
                        AdUtils.Time_interval = ob.getInt("ads_second");
                        JSONObject obj = ob.getJSONObject("data");

                        if (obj.has("redirect")) {
                            String redirect = obj.getString("redirect");
                            Log.e(TAG, "onResponse: redirect === " + redirect);

                            if (!redirect.isEmpty()) {
                                boolean isAppInstalled = isPackageInstalled(SplashActivity.this, redirect);
                                Toast.makeText(activity, "Please use our updated Application.", Toast.LENGTH_SHORT).show();
                                if (isAppInstalled) {
                                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(redirect);
                                    startActivity(LaunchIntent);
                                } else {
                                    startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://play.google.com/store/apps/details?id=" + redirect)));
                                }

                                AdUtils.Google_App_open_splash = "";
                                AdUtils.Google_App_open_splash_Fail = "";
                                AdUtils.Google_App_open = "";
                                AdUtils.Google_App_open_Fail = "";
                                AdUtils.Google_Intertitial_Splash = "";
                                AdUtils.Google_Intertitial = "";
                                AdUtils.Google_Intertitial_Fail = "";
                                AdUtils.Google_Native = "";
                                AdUtils.Google_Native_Fail = "";
                                AdUtils.Google_Native_Banner = "";
                                AdUtils.Google_Native_Banner_Fail = "";
                                AdUtils.Google_Rewarded = "";
                                AdUtils.Google_Rewarded_Fail = "";
                                AdUtils.Google_Medium_REC = "";
                                AdUtils.Google_Medium_REC_Fail = "";

                                AdUtils.AppLovin_Native = "";
                                AdUtils.AppLovin_Native_Banner = "";
                                AdUtils.AppLovin_Banner = "";
                                AdUtils.AppLovin_Interstitial = "";
                                AdUtils.AppLovin_Interstitial_Splash = "";
                                AdUtils.AppLovin_MREC = "";
                                AdUtils.AppLovin_Back = "";

                                return;
                            }
                        }

                        if (obj.has("ads_click")) {
                            AdUtils.Ad_Click = obj.getInt("ads_click");
                        }

                        if (obj.has("ads_first_click_interstitial")) {
                            String ads_first_click_interstitial = obj.getString("ads_first_click_interstitial");
                            if (!ads_first_click_interstitial.equals("")) {
                                AdUtils.ads_first_click_interstitial = obj.getInt("ads_first_click_interstitial");
                            }
                        }

                        AdUtils.Ad_Count = AdUtils.Ad_Click - AdUtils.ads_first_click_interstitial;

                        if (status.equals("on")) {
                            AdUtils.CheckOnOff = true;

                            if (obj.has("dialog")) {
                                String dialog = obj.getString("dialog");
                                if (!dialog.isEmpty()) {
                                    AdUtils.dialog = dialog;
                                }
                            }

                            if (obj.has("google_interstitial")) {
                                String google_interstitial = obj.getString("google_interstitial");
                                if (!google_interstitial.isEmpty()) {
                                    AdUtils.Google_Intertitial = google_interstitial;
                                }
                            }

                            if (obj.has("google_interstitial_fail")) {
                                String google_interstitial_fail = obj.getString("google_interstitial_fail");
                                if (!google_interstitial_fail.isEmpty()) {
                                    AdUtils.Google_Intertitial_Fail = google_interstitial_fail;
                                }
                            }

                            if (obj.has("google_native")) {
                                String google_native = obj.getString("google_native");
                                if (!google_native.isEmpty()) {
                                    AdUtils.Google_Native = google_native;
                                }
                            }

                            if (obj.has("google_native_fail")) {
                                String google_native_fail = obj.getString("google_native_fail");
                                if (!google_native_fail.isEmpty()) {
                                    AdUtils.Google_Native_Fail = google_native_fail;
                                }
                            }

                            if (obj.has("google_native_banner")) {
                                String google_native_banner = obj.getString("google_native_banner");
                                if (!google_native_banner.isEmpty()) {
                                    AdUtils.Google_Native_Banner = google_native_banner;
                                }
                            }

                            if (obj.has("google_native_banner_fail")) {
                                String google_native_banner_fail = obj.getString("google_native_banner_fail");
                                if (!google_native_banner_fail.isEmpty()) {
                                    AdUtils.Google_Native_Banner_Fail = google_native_banner_fail;
                                }
                            }

                            if (obj.has("google_interstitial_splash")) {
                                String google_interstitial_splash = obj.getString("google_interstitial_splash");
                                if (!google_interstitial_splash.isEmpty()) {
                                    AdUtils.Google_Intertitial_Splash = google_interstitial_splash;
                                }
                            }

                            if (obj.has("google_app_open_splash")) {
                                String google_app_open_splash = obj.getString("google_app_open_splash");
                                if (!google_app_open_splash.isEmpty()) {
                                    AdUtils.Google_App_open_splash = google_app_open_splash;
                                }
                            }

                            if (obj.has("google_app_open_splash_fail")) {
                                String google_app_open_splash_fail = obj.getString("google_app_open_splash_fail");
                                if (!google_app_open_splash_fail.isEmpty()) {
                                    AdUtils.Google_App_open_splash_Fail = google_app_open_splash_fail;
                                }
                            }

                            if (obj.has("google_app_open")) {
                                String google_app_open = obj.getString("google_app_open");
                                if (!google_app_open.isEmpty()) {
                                    AdUtils.Google_App_open = google_app_open;
                                }
                            }

                            if (obj.has("google_app_open_fail")) {
                                String google_app_open_fail = obj.getString("google_app_open_fail");
                                if (!google_app_open_fail.isEmpty()) {
                                    AdUtils.Google_App_open_Fail = google_app_open_fail;
                                }
                            }

                            if (obj.has("google_rewarded")) {
                                String google_rewarded = obj.getString("google_rewarded");
                                if (!google_rewarded.isEmpty()) {
                                    AdUtils.Google_Rewarded = google_rewarded;
                                }
                            }

                            if (obj.has("google_rewarded_fail")) {
                                String google_rewarded_fail = obj.getString("google_rewarded_fail");
                                if (!google_rewarded_fail.isEmpty()) {
                                    AdUtils.Google_Rewarded_Fail = google_rewarded_fail;
                                }
                            }

                            if (obj.has("google_medium_rec")) {
                                String google_medium_rec = obj.getString("google_medium_rec");
                                if (!google_medium_rec.isEmpty()) {
                                    AdUtils.Google_Medium_REC = google_medium_rec;
                                }
                            }

                            if (obj.has("google_medium_rec_fail")) {
                                String google_medium_rec_fail = obj.getString("google_medium_rec_fail");
                                if (!google_medium_rec_fail.isEmpty()) {
                                    AdUtils.Google_Medium_REC_Fail = google_medium_rec_fail;
                                }
                            }

                            if (obj.has("native_button_color")) {
                                String native_button_color = obj.getString("native_button_color");
                                if (!native_button_color.isEmpty()) {
                                    AdUtils.native_button_color = native_button_color;
                                }
                            }

                            if (obj.has("native_button_text_color")) {
                                String native_button_text_color = obj.getString("native_button_text_color");
                                if (!native_button_text_color.isEmpty()) {
                                    AdUtils.native_button_text_color = native_button_text_color;
                                }
                            }

                            if (obj.has("native_bg_color")) {
                                String native_bg_color = obj.getString("native_bg_color");
                                if (!native_bg_color.isEmpty()) {
                                    AdUtils.native_bg_color = native_bg_color;
                                }
                            }

                            if (obj.has("ads_native_second")) {
                                String ads_native_second = obj.getString("ads_native_second");
                                if (!ads_native_second.isEmpty()) {
                                    AdUtils.ads_native_second = Integer.parseInt(ads_native_second);
                                }
                            }


                            if (obj.has("applovin_native")) {
                                String applovin_native = obj.getString("applovin_native");
                                if (!applovin_native.isEmpty()) {
                                    AdUtils.AppLovin_Native = applovin_native;
                                }
                            }

                            if (obj.has("applovin_native_banner")) {
                                String applovin_native_banner = obj.getString("applovin_native_banner");
                                if (!applovin_native_banner.isEmpty()) {
                                    AdUtils.AppLovin_Native_Banner = applovin_native_banner;
                                }
                            }

                            if (obj.has("applovin_banner")) {
                                String applovin_banner = obj.getString("applovin_banner");
                                if (!applovin_banner.isEmpty()) {
                                    AdUtils.AppLovin_Banner = applovin_banner;
                                }
                            }

                            if (obj.has("applovin_interstitial")) {
                                String applovin_interstitial = obj.getString("applovin_interstitial");
                                if (!applovin_interstitial.isEmpty()) {
                                    AdUtils.AppLovin_Interstitial = applovin_interstitial;
                                }
                            }

                            if (obj.has("applovin_interstitial_splash")) {
                                String applovin_interstitial_splash = obj.getString("applovin_interstitial_splash");
                                if (!applovin_interstitial_splash.isEmpty()) {
                                    AdUtils.AppLovin_Interstitial_Splash = applovin_interstitial_splash;
                                }
                            }

                            if (obj.has("applovin_back")) {
                                String applovin_back = obj.getString("applovin_back");
                                if (!applovin_back.isEmpty()) {
                                    AdUtils.AppLovin_Back = applovin_back;
                                }
                            }

                            if (obj.has("applovin_mrec")) {
                                String applovin_mrec = obj.getString("applovin_mrec");
                                if (!applovin_mrec.isEmpty()) {
                                    AdUtils.AppLovin_MREC = applovin_mrec;
                                }
                            }

//                            //ca-app-pub-3940256099942544/5224354917
//                            AdUtils.Google_Rewarded = "ca-app-pub-3940256099942544/5224354917";
//                            AdUtils.Google_Rewarded_Fail = "ca-app-pub-3940256099942544/5224354917";
                            Log.e("JIVAN", "onResponse: 0000");
                            if (AdUtils.dialog) {

                            } else {
                                Log.e("JIVAN", "onResponse: Ad Should LOAD");
                                AdUtils.PreLoad(SplashActivity.this);
                                RewardedAdManager.getInstance().init(SplashActivity.this);
                            }


                        } else {
                            Log.e("JIVAN", "onResponse: 1111");

                            AdUtils.Google_Intertitial_Splash = "";
                            AdUtils.Google_App_open = "";
                            AdUtils.Google_App_open_Fail = "";
                            AdUtils.Google_App_open_splash = "";
                            AdUtils.Google_App_open_splash_Fail = "";

                            AdUtils.Google_Native = "";
                            AdUtils.Google_Native_Fail = "";
                            AdUtils.Google_Native_Banner = "";
                            AdUtils.Google_Native_Banner_Fail = "";
                            AdUtils.Google_Intertitial = "";
                            AdUtils.Google_Intertitial_Fail = "";
                            AdUtils.Google_Rewarded = "";
                            AdUtils.Google_Rewarded_Fail = "";
                            AdUtils.Google_Medium_REC = "";
                            AdUtils.Google_Medium_REC_Fail = "";


                            AdUtils.AppLovin_Native = "";
                            AdUtils.AppLovin_Native_Banner = "";
                            AdUtils.AppLovin_Banner = "";
                            AdUtils.AppLovin_Interstitial = "";
                            AdUtils.AppLovin_Interstitial_Splash = "";
                            AdUtils.AppLovin_MREC = "";
                            AdUtils.AppLovin_Back = "";

                        }


                    } else {

                        Log.e(TAG, "onResponse: ads_status key not found");
                        AdUtils.Google_Intertitial_Splash = "";
                        AdUtils.Google_App_open = "";
                        AdUtils.Google_App_open_Fail = "";
                        AdUtils.Google_App_open_splash = "";
                        AdUtils.Google_App_open_splash_Fail = "";

                        AdUtils.Google_Native = "";
                        AdUtils.Google_Native_Fail = "";
                        AdUtils.Google_Native_Banner = "";
                        AdUtils.Google_Native_Banner_Fail = "";
                        AdUtils.Google_Intertitial = "";
                        AdUtils.Google_Intertitial_Fail = "";
                        AdUtils.Google_Rewarded = "";
                        AdUtils.Google_Rewarded_Fail = "";
                        AdUtils.Google_Medium_REC = "";
                        AdUtils.Google_Medium_REC_Fail = "";

                        AdUtils.AppLovin_Native = "";
                        AdUtils.AppLovin_Native_Banner = "";
                        AdUtils.AppLovin_Banner = "";
                        AdUtils.AppLovin_Interstitial = "";
                        AdUtils.AppLovin_Interstitial_Splash = "";
                        AdUtils.AppLovin_MREC = "";
                        AdUtils.AppLovin_Back = "";

                    }

                    AdUtils.Google_App_open = "";
                    AdUtils.Google_App_open_Fail = "";
                    AdUtils.Google_App_open_splash = "";
                    AdUtils.Google_Intertitial_Splash = "";
                    AdUtils.Google_Intertitial = "";
                    AdUtils.Google_Intertitial_Fail = "";
                    AdUtils.Google_Rewarded = "";
                    AdUtils.Google_Rewarded_Fail = "";
                    AdUtils.Google_Native = "";
                    AdUtils.Google_Native_Fail = "";
                    AdUtils.Google_Native_Banner = "";
                    AdUtils.Google_Native_Banner_Fail = "";
                    AdUtils.Google_Medium_REC = "";
                    AdUtils.Google_Medium_REC_Fail = "";

                    AdUtils.AppLovin_Interstitial_Splash = "";
                    AdUtils.AppLovin_Interstitial = "";
                    AdUtils.AppLovin_Back = "";
                    AdUtils.AppLovin_Native = "";
                    AdUtils.AppLovin_Native_Banner = "";
                    AdUtils.AppLovin_MREC = "";
                    AdUtils.AppLovin_Banner = "";
                    logAdsValue();
                    fetchAd();
                    AdUtils.LoadingAllData = true;
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: JSON parse failed", e);
                    CallIntent(2);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: getAdsData failed", error);
                CallIntent(3);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("appkey", "azjhkndb130ds3452n1nn");
                MyData.put("os", "android");
                MyData.put("ver", "7.6");
                MyData.put("pkg_name", getPackageName());
                MyData.put("device", "htc");
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
        MyRequestQueue.getCache().clear();
    }


    private void fetchAd() {
        LoadAppOpen();
    }


    public void LoadApplovin() {
        if (!AdUtils.AppLovin_Interstitial_Splash.equals("")) {
            Log.e("onAdLoaded: ", "vvvv");
            MaxInterstitialAd interstitialAd = new MaxInterstitialAd(AdUtils.AppLovin_Interstitial_Splash, SplashActivity.this);
            interstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.e("onAdLoaded: ", "aaaaa");
                    if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        AdUtils.AdsOpenIntrestial = true;
                        interstitialAd.showAd();
                    }
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {

                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    Log.e("onAdLoaded: ", "cccc");
                    AdUtils.AdsOpenIntrestial = false;
                    CallIntent(4);
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.e("onAdLoaded: ", "ddddd");
                    AdUtils.AdsOpenIntrestial = false;
                    CallIntent(5);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    Log.e("onAdLoaded: ", "eeee");
                    AdUtils.AdsOpenIntrestial = false;
                    CallIntent(6);
                }
            });

            interstitialAd.loadAd();

        } else {
            AdUtils.AdsOpenIntrestial = false;
            CallIntent(7);

        }
    }

    private void LoadGoogleInterstitial() {
        AdUtils.AdsOpenIntrestial = true;

        AdUtils.OpenAllData = true;

        if (!AdUtils.Google_Intertitial_Splash.equals("")) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(this, AdUtils.Google_Intertitial_Splash, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                interstitialAd.show(SplashActivity.this);
                            }

                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdUtils.AdsOpenIntrestial = false;

                                    CallIntent(8);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {

                                    AdUtils.AdsOpenIntrestial = false;
                                    CallIntent(9);
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {

                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                            AdUtils.AdsOpenIntrestial = false;

                            CallIntent(10);
                        }
                    });
        } else {
            CallIntent(11);
        }

    }

    public void LoadAppOpen() {
        if (!AdUtils.Google_App_open_splash.isEmpty()) {
            loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ap) {
                    super.onAdLoaded(ap);

                    appOpenAd = ap;
                    AdUtils.AdsOpenIntrestial = true;

                    AdUtils.OpenAllData = true;
                    showAdIfAvailable();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    LoadApplovin();
                }
            };

            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(this, AdUtils.Google_App_open_splash, request, loadCallback);
        } else {
            LoadApplovin();
        }

    }


    private AppOpenAd appOpenAd;

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    public void showAdIfAvailable() {
        AdUtils.OpenAllData = true;

        if (isAdAvailable()) {

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AdUtils.AdsOpenIntrestial = false;
                            CallIntent(12);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AdUtils.AdsOpenIntrestial = false;
                            CallIntent(13);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {

                        }
                    };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                appOpenAd.show(SplashActivity.this);
            }
        } else {
            AdUtils.AdsOpenIntrestial = false;
            CallIntent(14);
        }
    }

    private void CallIntent(int from) {
        Log.e("JIVAN", "onResponse: from == " + from);
        fbAppEvent(SplashActivity.this, "Passing intent of main activity");

        AdUtils.REC_AppLovin_Native = "";
        AdUtils.REC_Google_Native = "/6499/example/native";
        AdUtils.REC_Google_Native_Fail = "";
        AdUtils.REC_AppLovin_MREC = "";
        AdUtils.REC_Google_Medium_REC = "";
        AdUtils.REC_Google_Medium_REC_Fail = "";

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public static void fbAppEvent(Context context, String name) {
        String actName = "app: " + name;
        try {
            AppEventsLogger logger = AppEventsLogger.newLogger(context);
            logger.logEvent(actName);
        } catch (Exception e) {
            Log.e("aaaaa", "fbAppEvent: " + e.getMessage());
            e.printStackTrace();
        }
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
        Log.e(TAG, "onResponse: Time_interval === " +AdUtils.Time_interval);
        Log.e(TAG, "onResponse: AdUtils.Ad_Click === " +AdUtils.Ad_Click);
        Log.e(TAG, "onResponse: AdUtils.ads_first_click_interstitial === " +AdUtils.ads_first_click_interstitial);
        Log.e(TAG, "onResponse: AdUtils.Ad_Count === " +AdUtils.Ad_Count);
        Log.e(TAG, "onResponse: AdUtils.CheckOnOff === " +AdUtils.CheckOnOff);
        Log.e(TAG, "onResponse: AdUtils.dialog === " +AdUtils.dialog);
        Log.e(TAG, "onResponse: AdUtils.native_button_color === " +AdUtils.native_button_color);
        Log.e(TAG, "onResponse: AdUtils.native_button_text_color === " +AdUtils.native_button_text_color);
        Log.e(TAG, "onResponse: AdUtils.native_bg_color === " +AdUtils.native_bg_color);
        Log.e(TAG, "onResponse: AdUtils.ads_native_second === " +AdUtils.ads_native_second);

        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Splash === " +AdUtils.Google_Intertitial_Splash);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial === " +AdUtils.Google_Intertitial);
        Log.e(TAG, "onResponse: AdUtils.Google_Intertitial_Fail === " +AdUtils.Google_Intertitial_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open === " +AdUtils.Google_App_open);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_Fail === " +AdUtils.Google_App_open_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_splash === " +AdUtils.Google_App_open_splash);
        Log.e(TAG, "onResponse: AdUtils.Google_App_open_splash_Fail === " +AdUtils.Google_App_open_splash_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Rewarded === " +AdUtils.Google_Rewarded);
        Log.e(TAG, "onResponse: AdUtils.Google_Rewarded_Fail === " +AdUtils.Google_Rewarded_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Native === " +AdUtils.Google_Native);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Fail === " +AdUtils.Google_Native_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Banner === " +AdUtils.Google_Native_Banner);
        Log.e(TAG, "onResponse: AdUtils.Google_Native_Banner_Fail === " +AdUtils.Google_Native_Banner_Fail);
        Log.e(TAG, "onResponse: AdUtils.Google_Medium_REC === " +AdUtils.Google_Medium_REC);
        Log.e(TAG, "onResponse: AdUtils.Google_Medium_REC_Fail === " +AdUtils.Google_Medium_REC_Fail);

        Log.e(TAG, "onResponse: AdUtils.AppLovin_Interstitial_Splash === " +AdUtils.AppLovin_Interstitial_Splash);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_Interstitial === " +AdUtils.AppLovin_Interstitial);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_Back === " +AdUtils.AppLovin_Back);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_Native === " +AdUtils.AppLovin_Native);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_Native_Banner === " +AdUtils.AppLovin_Native_Banner);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_MREC === " +AdUtils.AppLovin_MREC);
        Log.e(TAG, "onResponse: AdUtils.AppLovin_Banner === " +AdUtils.AppLovin_Banner);

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
