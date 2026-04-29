package org.schabi.newpipe

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowRgb565
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jakewharton.processphoenix.ProcessPhoenix
import io.reactivex.rxjava3.exceptions.CompositeException
import io.reactivex.rxjava3.exceptions.MissingBackpressureException
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketException
import org.acra.ACRA.init
import org.acra.ACRA.isACRASenderServiceProcess
import org.acra.config.CoreConfigurationBuilder
import org.schabi.newpipe.ads.AdUtils
import org.schabi.newpipe.ads.AppOpenManager
import org.schabi.newpipe.ads.RewardedAdManager
import org.schabi.newpipe.error.ReCaptchaActivity
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.ktx.hasAssignableCause
import org.schabi.newpipe.settings.NewPipeSettings
import org.schabi.newpipe.util.BridgeStateSaverInitializer
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.ServiceHelper
import org.schabi.newpipe.util.StateSaver
import org.schabi.newpipe.util.image.ImageStrategy
import org.schabi.newpipe.util.image.PreferredImageQuality
import org.schabi.newpipe.util.potoken.PoTokenProviderImpl

open class App : Application(), SingletonImageLoader.Factory {

    var isFirstRun = false
        private set
    var notificationsRequested = false
        private set

    fun setNotificationsRequested() {
        notificationsRequested = true
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initACRA()
    }

    var mInstance: App? = null
    var appOpenManager: AppOpenManager? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    @Volatile private var isRemoteFetchInProgress = false

    @Volatile private var lastRemoteFetchAt = 0L

    override fun onCreate() {
        super.onCreate()

        instance = this
        mInstance = this
        appOpenManager = AppOpenManager(this)

        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return
        }

        // check if the last used preference version is set
        // to determine whether this is the first app run
        val lastUsedPrefVersion =
            PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt(getString(R.string.last_used_preferences_version), -1)
        isFirstRun = lastUsedPrefVersion == -1

        // Initialize settings first because other initializations can use its values
        NewPipeSettings.initSettings(this)

        NewPipe.init(
            getDownloader(),
            Localization.getPreferredLocalization(this),
            Localization.getPreferredContentCountry(this)
        )
        Localization.initPrettyTime(Localization.resolvePrettyTime())

        BridgeStateSaverInitializer.init(this)
        StateSaver.init(this)
        initNotificationChannels()

        ServiceHelper.initServices(this)

        // Initialize image loader
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        ImageStrategy.setPreferredImageQuality(
            PreferredImageQuality.fromPreferenceKey(
                this,
                prefs.getString(
                    getString(R.string.image_quality_key),
                    getString(R.string.image_quality_default)
                )
            )
        )

        configureRxJavaErrorHandler()

        YoutubeStreamExtractor.setPoTokenProvider(PoTokenProviderImpl)
        initAdsRemoteFallbackFetch()
    }

    private fun initAdsRemoteFallbackFetch() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val manager = connectivityManager ?: return

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                maybeFetchAdsRemoteConfigFallback()
            }
        }

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            manager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }

        maybeFetchAdsRemoteConfigFallback()
    }

    private fun maybeFetchAdsRemoteConfigFallback() {
        if (AdUtils.LoadingAllData) return
        if (isRemoteFetchInProgress) return
        val now = System.currentTimeMillis()
        if (now - lastRemoteFetchAt < 5 * 60 * 1000L) return

        isRemoteFetchInProgress = true
        lastRemoteFetchAt = now

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.fetch().addOnCompleteListener { fetchTask ->
            if (!fetchTask.isSuccessful) {
                isRemoteFetchInProgress = false
                return@addOnCompleteListener
            }
            remoteConfig.activate().addOnCompleteListener { activateTask ->
                if (activateTask.isSuccessful) {
                    applyAdsRemoteConfigFromApp(remoteConfig)
                    AdUtils.LoadingAllData = true
                }
                isRemoteFetchInProgress = false
            }
        }
    }

    private fun applyAdsRemoteConfigFromApp(remoteConfig: FirebaseRemoteConfig) {
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

        AdUtils.native_button_color = remoteConfig.getString("native_button_color")
        AdUtils.native_button_text_color = remoteConfig.getString("native_button_text_color")
        AdUtils.native_bg_color = remoteConfig.getString("native_bg_color")

        AdUtils.ads_native_second = remoteConfig.getLong("ads_native_second").toInt()
        AdUtils.NativeTime_Check = remoteConfig.getLong("NativeTime_Check")
        AdUtils.NativeBannerTime_Check = remoteConfig.getLong("NativeBannerTime_Check")
        AdUtils.ShowRewarded = remoteConfig.getBoolean("ShowRewarded")
        AdUtils.dialog = remoteConfig.getBoolean("dialog")
        AdUtils.Ad_Click = remoteConfig.getLong("Ad_Click").toInt()
        AdUtils.ads_first_click_interstitial = remoteConfig.getLong("ads_first_click_interstitial").toInt()
        AdUtils.Ad_Count = AdUtils.Ad_Click - AdUtils.ads_first_click_interstitial
        AdUtils.Time_interval = remoteConfig.getString("Time_interval").toIntOrNull() ?: 31

        if (!AdUtils.dialog) {
            if (AdUtils.ShowRewarded) {
                RewardedAdManager.getInstance().init(this)
            } else {
                AdUtils.PreLoad(this)
            }
        }
    }

    override fun newImageLoader(context: Context): ImageLoader = ImageLoader
        .Builder(this)
        .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
        .allowRgb565(getSystemService<ActivityManager>()!!.isLowRamDevice)
        .crossfade(true)
        .components {
            add(OkHttpNetworkFetcherFactory(callFactory = DownloaderImpl.getInstance().client))
        }.build()

    protected open fun getDownloader(): Downloader {
        val downloader = DownloaderImpl.init(null)
        setCookiesToDownloader(downloader)
        return downloader
    }

    protected fun setCookiesToDownloader(downloader: DownloaderImpl) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val key = getString(R.string.recaptcha_cookies_key)
        downloader.setCookie(ReCaptchaActivity.RECAPTCHA_COOKIES_KEY, prefs.getString(key, null))
        downloader.updateYoutubeRestrictedModeCookies(this)
    }

    private fun configureRxJavaErrorHandler() {
        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(
            object : Consumer<Throwable> {
                override fun accept(throwable: Throwable) {
                    Log.e(TAG, "RxJavaPlugins.ErrorHandler called with -> : throwable = [${throwable.javaClass.getName()}]")

                    // As UndeliverableException is a wrapper,
                    // get the cause of it to get the "real" exception
                    val actualThrowable = (throwable as? UndeliverableException)?.cause ?: throwable

                    val errors = (actualThrowable as? CompositeException)?.exceptions ?: listOf(actualThrowable)

                    for (error in errors) {
                        if (isThrowableIgnored(error)) {
                            return
                        }
                        if (isThrowableCritical(error)) {
                            reportException(error)
                            return
                        }
                    }

                    // Out-of-lifecycle exceptions should only be reported if a debug user wishes so,
                    // When exception is not reported, log it
                    if (isDisposedRxExceptionsReported()) {
                        reportException(actualThrowable)
                    } else {
                        Log.e(TAG, "RxJavaPlugin: Undeliverable Exception received: ", actualThrowable)
                    }
                }

                fun isThrowableIgnored(throwable: Throwable): Boolean {
                    // Don't crash the application over a simple network problem
                    return throwable // network api cancellation
                        .hasAssignableCause(
                            IOException::class.java,
                            SocketException::class.java, // blocking code disposed
                            InterruptedException::class.java,
                            InterruptedIOException::class.java
                        )
                }

                fun isThrowableCritical(throwable: Throwable): Boolean {
                    // Though these exceptions cannot be ignored
                    return throwable
                        .hasAssignableCause(
                            // bug in app
                            NullPointerException::class.java,
                            IllegalArgumentException::class.java,
                            OnErrorNotImplementedException::class.java,
                            MissingBackpressureException::class.java,
                            // bug in operator
                            IllegalStateException::class.java
                        )
                }

                fun reportException(throwable: Throwable) {
                    // Throw uncaught exception that will trigger the report system
                    Thread
                        .currentThread()
                        .uncaughtExceptionHandler
                        .uncaughtException(Thread.currentThread(), throwable)
                }
            }
        )
    }

    /**
     * Called in [.attachBaseContext] after calling the `super` method.
     * Should be overridden if MultiDex is enabled, since it has to be initialized before ACRA.
     */
    protected fun initACRA() {
        if (isACRASenderServiceProcess()) {
            return
        }

        val acraConfig =
            CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig::class.java)
        init(this, acraConfig)
    }

    private fun initNotificationChannels() {
        // Keep the importance below DEFAULT to avoid making noise on every notification update for
        // the main and update channels
        val mainChannel =
            NotificationChannelCompat
                .Builder(
                    getString(R.string.notification_channel_id),
                    NotificationManagerCompat.IMPORTANCE_LOW
                ).setName(getString(R.string.notification_channel_name))
                .setDescription(getString(R.string.notification_channel_description))
                .build()
        val hashChannel =
            NotificationChannelCompat
                .Builder(
                    getString(R.string.hash_channel_id),
                    NotificationManagerCompat.IMPORTANCE_HIGH
                ).setName(getString(R.string.hash_channel_name))
                .setDescription(getString(R.string.hash_channel_description))
                .build()
        val errorReportChannel =
            NotificationChannelCompat
                .Builder(
                    getString(R.string.error_report_channel_id),
                    NotificationManagerCompat.IMPORTANCE_LOW
                ).setName(getString(R.string.error_report_channel_name))
                .setDescription(getString(R.string.error_report_channel_description))
                .build()
        val newStreamChannel =
            NotificationChannelCompat
                .Builder(
                    getString(R.string.streams_notification_channel_id),
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                ).setName(getString(R.string.streams_notification_channel_name))
                .setDescription(getString(R.string.streams_notification_channel_description))
                .build()

        val channels = listOf(mainChannel, hashChannel, errorReportChannel, newStreamChannel)

        NotificationManagerCompat.from(this).createNotificationChannelsCompat(channels)
    }

    protected open fun isDisposedRxExceptionsReported(): Boolean = false

    companion object {
        const val PACKAGE_NAME: String = BuildConfig.APPLICATION_ID
        private val TAG = App::class.java.toString()

        @JvmStatic
        lateinit var instance: App
            private set
    }
}
