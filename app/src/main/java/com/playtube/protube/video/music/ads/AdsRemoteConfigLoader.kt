package com.playtube.protube.video.music.ads

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.playtube.protube.video.music.AppMode

object AdsRemoteConfigLoader {
    @Volatile
    private var isFetchInProgress = false

    fun fetchIfNeeded(context: Context, onComplete: Runnable?) {
        if (!AdUtils.isOnline(context)) {
            onComplete?.run()
            return
        }

        if (AdUtils.LoadingAllData || isFetchInProgress) {
            onComplete?.run()
            return
        }

        isFetchInProgress = true

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val settings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.fetch().addOnCompleteListener { fetchTask ->
            if (!fetchTask.isSuccessful) {
                isFetchInProgress = false
                onComplete?.run()
                return@addOnCompleteListener
            }

            remoteConfig.activate().addOnCompleteListener { activateTask ->
                if (activateTask.isSuccessful) {
                    applyRemoteConfig(remoteConfig)
                    AdUtils.LoadingAllData = true
                }
                isFetchInProgress = false
                onComplete?.run()
            }
        }
    }

    private fun applyRemoteConfig(remoteConfig: FirebaseRemoteConfig) {
        AppMode.applyRemoteConfig(remoteConfig, "Retry")
        AdUtils.CheckOnOff = remoteConfig.getBoolean("ads_status")
        if (!AdUtils.CheckOnOff) return

        AdUtils.Google_App_open = remoteConfig.getString("Google_App_Open")
        AdUtils.Google_App_open_Fail = remoteConfig.getString("Google_App_Open_Fail")
        AdUtils.Google_App_open_Fail_1 = remoteConfig.getString("Google_App_Open_Fail_1")
        AdUtils.Google_App_open_splash = remoteConfig.getString("Google_App_Open_Splash")
        AdUtils.Google_App_open_splash_Fail = remoteConfig.getString("Google_App_Open_Splash_Fail")
        AdUtils.Google_App_open_splash_Fail_1 = remoteConfig.getString("Google_App_Open_Splash_Fail_1")

        AdUtils.Google_Intertitial_Splash = remoteConfig.getString("Google_Intertitial_Splash")
        AdUtils.Google_Intertitial_Splash_Fail = remoteConfig.getString("Google_Intertitial_Splash_Fail")
        AdUtils.Google_Intertitial_Splash_Fail_1 = remoteConfig.getString("Google_Intertitial_Splash_Fail_1")
        AdUtils.Google_Intertitial = remoteConfig.getString("Google_Intertitial")
        AdUtils.Google_Intertitial_Fail = remoteConfig.getString("Google_Intertitial_Fail")
        AdUtils.Google_Intertitial_Fail_1 = remoteConfig.getString("Google_Intertitial_Fail_1")

        AdUtils.Google_Rewarded = remoteConfig.getString("Google_Rewarded")
        AdUtils.Google_Rewarded_Fail = remoteConfig.getString("Google_Rewarded_Fail")
        AdUtils.Google_Rewarded_Fail_1 = remoteConfig.getString("Google_Rewarded_Fail_1")

        AdUtils.Google_Native = remoteConfig.getString("Google_Native")
        AdUtils.Google_Native_Fail = remoteConfig.getString("Google_Native_Fail")
        AdUtils.Google_Native_Fail_1 = remoteConfig.getString("Google_Native_Fail_1")
        AdUtils.Google_Native_Banner = remoteConfig.getString("Google_Native_Banner")
        AdUtils.Google_Native_Banner_Fail = remoteConfig.getString("Google_Native_Banner_Fail")
        AdUtils.Google_Native_Banner_Fail_1 = remoteConfig.getString("Google_Native_Banner_Fail_1")

        AdUtils.Google_Banner = remoteConfig.getString("Google_Banner")
        AdUtils.Google_Banner_Fail = remoteConfig.getString("Google_Banner_Fail")
        AdUtils.Google_Banner_Fail_1 = remoteConfig.getString("Google_Banner_Fail_1")

        AdUtils.Google_Medium_REC = remoteConfig.getString("Google_Medium_REC")
        AdUtils.Google_Medium_REC_Fail = remoteConfig.getString("Google_Medium_REC_Fail")
        AdUtils.Google_Medium_REC_Fail_1 = remoteConfig.getString("Google_Medium_REC_Fail_1")

        AdUtils.REC_Google_Native = remoteConfig.getString("REC_Google_Native")
        AdUtils.REC_Google_Native_Fail = remoteConfig.getString("REC_Google_Native_Fail")
        AdUtils.REC_Google_Native_Fail_1 = remoteConfig.getString("REC_Google_Native_Fail_1")
        AdUtils.REC_Google_Medium_REC = remoteConfig.getString("REC_Google_Medium_REC")
        AdUtils.REC_Google_Medium_REC_Fail = remoteConfig.getString("REC_Google_Medium_REC_Fail")
        AdUtils.REC_Google_Medium_REC_Fail_1 = remoteConfig.getString("REC_Google_Medium_REC_Fail_1")

        AdUtils.native_headline_color = remoteConfig.getString("native_headline_color")
        AdUtils.native_body_color = remoteConfig.getString("native_body_color")
        AdUtils.native_button_color = remoteConfig.getString("native_button_color")
        AdUtils.native_button_text_color = remoteConfig.getString("native_button_text_color")
        AdUtils.native_bg_color = remoteConfig.getString("native_bg_color")

        AdUtils.feed_after = remoteConfig.getLong("feed_after").toInt()
        AdUtils.feed_max = remoteConfig.getLong("feed_max").toInt()
        AdUtils.sub_after = remoteConfig.getLong("sub_after").toInt()
        AdUtils.sub_every = remoteConfig.getLong("sub_every").toInt()
        AdUtils.sub_max = remoteConfig.getLong("sub_max").toInt()
        AdUtils.base_after = remoteConfig.getLong("base_after").toInt()
        AdUtils.base_every = remoteConfig.getLong("base_every").toInt()
        AdUtils.base_max = remoteConfig.getLong("base_max").toInt()
        AdUtils.rel_every = remoteConfig.getLong("rel_every").toInt()

        AdUtils.exit_page = remoteConfig.getBoolean("exit_page")
        AdUtils.google_exit_inter = remoteConfig.getString("google_exit_inter")
        AdUtils.google_exit_inter_fail = remoteConfig.getString("google_exit_inter_fail")
        AdUtils.google_exit_inter_fail_1 = remoteConfig.getString("google_exit_inter_fail_1")
        AdUtils.google_exit_native = remoteConfig.getString("google_exit_native")
        AdUtils.google_exit_native_fail = remoteConfig.getString("google_exit_native_fail")
        AdUtils.google_exit_native_fail_1 = remoteConfig.getString("google_exit_native_fail_1")
        AdUtils.google_exit_mrec = remoteConfig.getString("google_exit_mrec")
        AdUtils.google_exit_mrec_fail = remoteConfig.getString("google_exit_mrec_fail")
        AdUtils.google_exit_mrec_fail_1 = remoteConfig.getString("google_exit_mrec_fail_1")

        AdUtils.ads_native_second = remoteConfig.getLong("ads_native_second").toInt()
        AdUtils.NativeTime_Check = remoteConfig.getLong("NativeTime_Check")
        AdUtils.NativeBannerTime_Check = remoteConfig.getLong("NativeBannerTime_Check")

        AdUtils.ShowRewarded = remoteConfig.getBoolean("ShowRewarded")
        AdUtils.dialog = remoteConfig.getBoolean("dialog")

        AdUtils.Ad_Click = remoteConfig.getLong("Ad_Click").toInt()

        AdUtils.ads_first_click_interstitial = remoteConfig.getLong("ads_first_click_interstitial").toInt()
        AdUtils.Ad_Count = AdUtils.Ad_Click - AdUtils.ads_first_click_interstitial

        AdUtils.Time_interval = remoteConfig.getString("Time_interval").toIntOrNull() ?: 31
    }
}
