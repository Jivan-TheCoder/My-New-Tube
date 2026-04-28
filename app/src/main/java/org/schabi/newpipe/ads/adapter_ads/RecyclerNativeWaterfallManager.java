package org.schabi.newpipe.ads.adapter_ads;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
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
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.ads.AdUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.ref.WeakReference;

public class RecyclerNativeWaterfallManager {
    private static final String TAG = "RecyclerNativeWF";
    private static final String SHIMMER_TAG = "native_ad_shimmer";
    private static final int MAX_PARALLEL_LOADS = 3;
    private static final int MREC_HEIGHT_DP = 250;
    private static final long STATS_NOTIFY_DELAY_MS = 250L;
    private static boolean debugLog = false;

    public enum Provider {
        APPLOVIN_NATIVE,
        GOOGLE_NATIVE,
        GOOGLE_NATIVE_FAIL,
        APPLOVIN_MREC,
        GOOGLE_MREC,
        GOOGLE_MREC_FAIL,
        NONE
    }

    public interface Listener {
        void onStatsChanged(@NonNull String summaryText);
    }

    private static class SlotState {
        volatile boolean loading;
        volatile boolean queued;
        volatile boolean resolved;
        volatile Provider provider = Provider.NONE;
        volatile NativeAdInjectionConfig.AdType adType = NativeAdInjectionConfig.AdType.NATIVE;
        volatile boolean animated;
        NativeAd googleNativeAd;
        final Map<String, NativeAdView> googleNativeViewsBySlot = new ConcurrentHashMap<>();
        MaxAd appLovinNativeAd;
        MaxNativeAdView appLovinNativeView;
        MaxNativeAdLoader appLovinLoader;
        MaxAdView appLovinMrecView;
        AdView googleMrecView;
        final Map<String, WeakReference<FrameLayout>> pendingContainersByRenderKey = new ConcurrentHashMap<>();
    }

    private static volatile RecyclerNativeWaterfallManager instance;

    private final Map<String, SlotState> slots = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    private final AtomicInteger totalSlotsRequested = new AtomicInteger(0);
    private final AtomicInteger appLovinAttempts = new AtomicInteger(0);
    private final AtomicInteger googleAttempts = new AtomicInteger(0);
    private final AtomicInteger googleFailAttempts = new AtomicInteger(0);

    private final AtomicInteger appLovinWins = new AtomicInteger(0);
    private final AtomicInteger googleWins = new AtomicInteger(0);
    private final AtomicInteger googleFailWins = new AtomicInteger(0);
    private final AtomicInteger noFillWins = new AtomicInteger(0);
    private final AtomicInteger actuallyShownWins = new AtomicInteger(0);
    private final AtomicInteger totalBindCalls = new AtomicInteger(0);
    private final AtomicInteger cacheHitBinds = new AtomicInteger(0);
    private final AtomicInteger totalRequestsStarted = new AtomicInteger(0);
    private final AtomicInteger totalRequestsLoaded = new AtomicInteger(0);
    private final AtomicInteger totalRequestsFailed = new AtomicInteger(0);
    private final AtomicInteger currentlyLoading = new AtomicInteger(0);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean statsNotifyScheduled = false;

    public static RecyclerNativeWaterfallManager getInstance() {
        if (instance == null) {
            synchronized (RecyclerNativeWaterfallManager.class) {
                if (instance == null) {
                    instance = new RecyclerNativeWaterfallManager();
                }
            }
        }
        return instance;
    }

    public void addListener(@NonNull Listener listener) {
        listeners.addIfAbsent(listener);
        //only for stats
        listener.onStatsChanged(getProbabilitySummary());
    }

    public void removeListener(@NonNull Listener listener) {
        listeners.remove(listener);
    }

    public void resetStats() {
        totalSlotsRequested.set(0);
        appLovinAttempts.set(0);
        googleAttempts.set(0);
        googleFailAttempts.set(0);
        appLovinWins.set(0);
        googleWins.set(0);
        googleFailWins.set(0);
        noFillWins.set(0);
        actuallyShownWins.set(0);
        totalBindCalls.set(0);
        cacheHitBinds.set(0);
        totalRequestsStarted.set(0);
        totalRequestsLoaded.set(0);
        totalRequestsFailed.set(0);
        currentlyLoading.set(0);
        notifyStatsChangedNow();
    }

    public void setDebugLog(boolean enabled) {
        debugLog = enabled;
    }

    public boolean isSlotResolved(@NonNull String slotKey) {
        SlotState slotState = slots.get(slotKey);
        return slotState != null && slotState.resolved;
    }

    public boolean bind(@NonNull Activity activity, int slotId, @NonNull FrameLayout adContainer) {
        return bind(activity, "global#" + slotId, "global#" + slotId, adContainer,
                NativeAdInjectionConfig.AdType.NATIVE);
    }

    public boolean bind(@NonNull Activity activity, @NonNull String slotKey, @NonNull FrameLayout adContainer) {
        return bind(activity, slotKey, slotKey, adContainer, NativeAdInjectionConfig.AdType.NATIVE);
    }

    public boolean bind(@NonNull Activity activity,
                        @NonNull String slotKey,
                        @NonNull FrameLayout adContainer,
                        @NonNull NativeAdInjectionConfig.AdType adType) {
        return bind(activity, slotKey, slotKey, adContainer, adType);
    }

    public boolean bind(@NonNull Activity activity,
                        @NonNull String dataSlotKey,
                        @NonNull String renderSlotKey,
                        @NonNull FrameLayout adContainer,
                        @NonNull NativeAdInjectionConfig.AdType adType) {
        totalBindCalls.incrementAndGet();
        adContainer.setTag(renderSlotKey);
        SlotState slotState = getOrCreateSlotState(dataSlotKey);
        slotState.adType = adType;

        if (!isActivityUsable(activity) || !adContainer.isAttachedToWindow()) {
            showLoading(adContainer, adType);
            registerPendingContainer(slotState, renderSlotKey, adContainer);
            return false;
        }

        if (!AdUtils.isOnline(activity)) {
            debug("bind skip offline slot=" + dataSlotKey);
            renderNone(adContainer);
            return false;
        }

        if (slotState.resolved) {
            cacheHitBinds.incrementAndGet();
            debug("bind cache-hit slot=" + dataSlotKey + " provider=" + slotState.provider);
            return renderResolvedSlot(activity, renderSlotKey, adContainer, slotState);
        }

        if (slotState.loading) {
            showLoading(adContainer, adType);
            registerPendingContainer(slotState, renderSlotKey, adContainer);
            return false;
        }

        if (slotState.queued) {
            showLoading(adContainer, adType);
            registerPendingContainer(slotState, renderSlotKey, adContainer);
            return false;
        }

        if (currentlyLoading.get() >= MAX_PARALLEL_LOADS) {
            slotState.queued = true;
            showLoading(adContainer, adType);
            registerPendingContainer(slotState, renderSlotKey, adContainer);
            debug("queue slot=" + dataSlotKey + " active=" + currentlyLoading.get());
            mainHandler.postDelayed(() -> {
                if (!slotState.resolved && slotState.queued && renderSlotKey.equals(adContainer.getTag())) {
                    slotState.queued = false;
                    bind(activity, dataSlotKey, renderSlotKey, adContainer, adType);
                }
            }, 500L);
            return false;
        }

        slotState.loading = true;
        currentlyLoading.incrementAndGet();
        totalSlotsRequested.incrementAndGet();
        showLoading(adContainer, adType);
        debug("bind start-load slot=" + dataSlotKey);
        if (adType == NativeAdInjectionConfig.AdType.MREC) {
            loadAppLovinMrecThenGoogle(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
        } else {
            loadAppLovinThenGoogle(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
        }
        return false;
    }

    private SlotState getOrCreateSlotState(@NonNull String key) {
        SlotState existing = slots.get(key);
        if (existing != null) {
            return existing;
        }
        SlotState created = new SlotState();
        SlotState race = slots.putIfAbsent(key, created);
        return race != null ? race : created;
    }

    private void loadAppLovinThenGoogle(@NonNull Activity activity, @NonNull String dataSlotKey,
                                        @NonNull String renderSlotKey,
                                        @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_AppLovin_Native)) {
            debug("applovin id empty -> fallback google slot=" + dataSlotKey);
            loadGooglePrimary(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        appLovinAttempts.incrementAndGet();
        debug("request applovin slot=" + dataSlotKey);

        int templateLayout = slotState.adType == NativeAdInjectionConfig.AdType.MREC
                ? R.layout.applovin_native_mrec_fallback
                : R.layout.applovin_native;
        View template = LayoutInflater.from(activity).inflate(templateLayout, adContainer, false);
        Button ctaButton = template.findViewById(R.id.cta_button);
        RelativeLayout mainContainer = template.findViewById(R.id.main_rel);
        styleAppLovinTemplate(mainContainer, ctaButton);

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(template)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_textView)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build();

        MaxNativeAdView nativeAdView = new MaxNativeAdView(binder, activity);
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(AdUtils.REC_AppLovin_Native, activity);
        slotState.appLovinLoader = nativeAdLoader;

        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(MaxNativeAdView loadedAdView, MaxAd ad) {
                if (slotState.resolved) {
                    nativeAdLoader.destroy(ad);
                    return;
                }
                if (slotState.appLovinNativeAd != null && slotState.appLovinLoader != null) {
                    slotState.appLovinLoader.destroy(slotState.appLovinNativeAd);
                }
                slotState.appLovinNativeAd = ad;
                slotState.appLovinNativeView = loadedAdView;
                slotState.provider = Provider.APPLOVIN_NATIVE;
                slotState.resolved = true;
                slotState.loading = false;
                decrementLoading();
                totalRequestsLoaded.incrementAndGet();
                appLovinWins.incrementAndGet();
                notifyStatsChanged();
                debug("loaded applovin slot=" + dataSlotKey);
                renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                renderPendingContainers(activity, slotState);
            }

            @Override
            public void onNativeAdLoadFailed(String adUnitId, MaxError error) {
                if (slotState.resolved) {
                    return;
                }
                totalRequestsFailed.incrementAndGet();
                debug("failed applovin slot=" + dataSlotKey + " code=" + error.getCode());
                loadGooglePrimary(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            }
        });

        nativeAdLoader.loadAd(nativeAdView);
    }

    private void loadAppLovinMrecThenGoogle(@NonNull Activity activity, @NonNull String dataSlotKey,
                                            @NonNull String renderSlotKey,
                                            @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_AppLovin_MREC)) {
            debug("applovin mrec id empty -> fallback google-mrec slot=" + dataSlotKey);
            loadGoogleMrecPrimary(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        appLovinAttempts.incrementAndGet();
        debug("request applovin-mrec slot=" + dataSlotKey);

        MaxAdView adView = new MaxAdView(AdUtils.REC_AppLovin_MREC, MaxAdFormat.MREC, activity);
        int width = AppLovinSdkUtils.dpToPx(activity, 300);
        int height = AppLovinSdkUtils.dpToPx(activity, 250);
        adView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        adView.setExtraParameter("adaptive_banner", "false");
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (slotState.resolved) {
                    adView.destroy();
                    return;
                }
                if (slotState.appLovinMrecView != null) {
                    try {
                        slotState.appLovinMrecView.destroy();
                    } catch (Exception ignored) {
                    }
                }
                slotState.appLovinMrecView = adView;
                slotState.provider = Provider.APPLOVIN_MREC;
                slotState.resolved = true;
                slotState.loading = false;
                decrementLoading();
                totalRequestsLoaded.incrementAndGet();
                appLovinWins.incrementAndGet();
                notifyStatsChanged();
                debug("loaded applovin-mrec slot=" + dataSlotKey);
                renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                renderPendingContainers(activity, slotState);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (slotState.resolved) {
                    return;
                }
                totalRequestsFailed.incrementAndGet();
                debug("failed applovin-mrec slot=" + dataSlotKey + " code=" + error.getCode());
                loadGoogleMrecPrimary(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
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
            }

            @Override
            public void onAdExpanded(MaxAd ad) {
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
            }
        });
        adView.loadAd();
    }

    private void loadGooglePrimary(@NonNull Activity activity, @NonNull String dataSlotKey,
                                   @NonNull String renderSlotKey,
                                   @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_Google_Native)) {
            debug("google primary id empty -> fallback google-fail slot=" + dataSlotKey);
            loadGoogleFail(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        googleAttempts.incrementAndGet();
        debug("request google-primary slot=" + dataSlotKey);
        AdLoader adLoader = new AdLoader.Builder(activity, AdUtils.REC_Google_Native)
                .forNativeAd(nativeAd -> {
                    if (slotState.resolved) {
                        nativeAd.destroy();
                        return;
                    }
                    slotState.googleNativeAd = nativeAd;
                    slotState.googleNativeViewsBySlot.clear();
                    slotState.provider = Provider.GOOGLE_NATIVE;
                    slotState.resolved = true;
                    slotState.loading = false;
                    decrementLoading();
                    totalRequestsLoaded.incrementAndGet();
                    googleWins.incrementAndGet();
                    notifyStatsChanged();
                    debug("loaded google-primary slot=" + dataSlotKey);
                    renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                    renderPendingContainers(activity, slotState);
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                        .build())
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        if (slotState.resolved) {
                            return;
                        }
                        totalRequestsFailed.incrementAndGet();
                        debug("failed google-primary slot=" + dataSlotKey + " code=" + adError.getCode());
                        loadGoogleFail(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadGoogleFail(@NonNull Activity activity, @NonNull String dataSlotKey,
                                @NonNull String renderSlotKey,
                                @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_Google_Native_Fail)) {
            debug("google-fail id empty -> no-fill slot=" + dataSlotKey);
            resolveAsNoFill(activity, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        googleFailAttempts.incrementAndGet();
        debug("request google-fail slot=" + dataSlotKey);
        AdLoader adLoader = new AdLoader.Builder(activity, AdUtils.REC_Google_Native_Fail)
                .forNativeAd(nativeAd -> {
                    if (slotState.resolved) {
                        nativeAd.destroy();
                        return;
                    }
                    slotState.googleNativeAd = nativeAd;
                    slotState.googleNativeViewsBySlot.clear();
                    slotState.provider = Provider.GOOGLE_NATIVE_FAIL;
                    slotState.resolved = true;
                    slotState.loading = false;
                    decrementLoading();
                    totalRequestsLoaded.incrementAndGet();
                    googleFailWins.incrementAndGet();
                    notifyStatsChanged();
                    debug("loaded google-fail slot=" + dataSlotKey);
                    renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                    renderPendingContainers(activity, slotState);
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                        .build())
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        if (slotState.resolved) {
                            return;
                        }
                        totalRequestsFailed.incrementAndGet();
                        debug("failed google-fail slot=" + dataSlotKey + " code=" + adError.getCode());
                        resolveAsNoFill(activity, renderSlotKey, adContainer, slotState);
                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadGoogleMrecPrimary(@NonNull Activity activity, @NonNull String dataSlotKey,
                                       @NonNull String renderSlotKey,
                                       @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_Google_Medium_REC)) {
            debug("google mrec id empty -> fallback google-mrec-fail slot=" + dataSlotKey);
            loadGoogleMrecFail(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        googleAttempts.incrementAndGet();
        debug("request google-mrec slot=" + dataSlotKey);
        AdView adView = new AdView(activity);
        adView.setAdUnitId(AdUtils.REC_Google_Medium_REC);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (slotState.resolved) {
                    adView.destroy();
                    return;
                }
                if (slotState.googleMrecView != null) {
                    try {
                        slotState.googleMrecView.destroy();
                    } catch (Exception ignored) {
                    }
                }
                slotState.googleMrecView = adView;
                slotState.provider = Provider.GOOGLE_MREC;
                slotState.resolved = true;
                slotState.loading = false;
                decrementLoading();
                totalRequestsLoaded.incrementAndGet();
                googleWins.incrementAndGet();
                notifyStatsChanged();
                debug("loaded google-mrec slot=" + dataSlotKey);
                renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                renderPendingContainers(activity, slotState);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                if (slotState.resolved) {
                    return;
                }
                totalRequestsFailed.incrementAndGet();
                debug("failed google-mrec slot=" + dataSlotKey + " code=" + adError.getCode());
                loadGoogleMrecFail(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadGoogleMrecFail(@NonNull Activity activity, @NonNull String dataSlotKey,
                                    @NonNull String renderSlotKey,
                                    @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (TextUtils.isEmpty(AdUtils.REC_Google_Medium_REC_Fail)) {
            debug("google mrec fail id empty -> native fallback slot=" + dataSlotKey);
            loadNativeFallbackForMrec(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            return;
        }

        totalRequestsStarted.incrementAndGet();
        googleFailAttempts.incrementAndGet();
        debug("request google-mrec-fail slot=" + dataSlotKey);
        AdView adView = new AdView(activity);
        adView.setAdUnitId(AdUtils.REC_Google_Medium_REC_Fail);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (slotState.resolved) {
                    adView.destroy();
                    return;
                }
                if (slotState.googleMrecView != null) {
                    try {
                        slotState.googleMrecView.destroy();
                    } catch (Exception ignored) {
                    }
                }
                slotState.googleMrecView = adView;
                slotState.provider = Provider.GOOGLE_MREC_FAIL;
                slotState.resolved = true;
                slotState.loading = false;
                decrementLoading();
                totalRequestsLoaded.incrementAndGet();
                googleFailWins.incrementAndGet();
                notifyStatsChanged();
                debug("loaded google-mrec-fail slot=" + dataSlotKey);
                renderIfStillBound(activity, renderSlotKey, adContainer, slotState);
                renderPendingContainers(activity, slotState);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                if (slotState.resolved) {
                    return;
                }
                totalRequestsFailed.incrementAndGet();
                debug("failed google-mrec-fail slot=" + dataSlotKey + " code=" + adError.getCode());
                loadNativeFallbackForMrec(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadNativeFallbackForMrec(@NonNull Activity activity, @NonNull String dataSlotKey,
                                           @NonNull String renderSlotKey,
                                           @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        debug("mrec -> native fallback chain slot=" + dataSlotKey);
        loadAppLovinThenGoogle(activity, dataSlotKey, renderSlotKey, adContainer, slotState);
    }

    private void resolveAsNoFill(@NonNull Activity activity, @NonNull String slotKey,
                                 @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        slotState.provider = Provider.NONE;
        slotState.resolved = true;
        slotState.loading = false;
        decrementLoading();
        noFillWins.incrementAndGet();
        notifyStatsChanged();
        debug("resolved no-fill slot=" + slotKey);
        renderIfStillBound(activity, slotKey, adContainer, slotState);
        renderPendingContainers(activity, slotState);
    }

    private void renderIfStillBound(@NonNull Activity activity, @NonNull String slotKey,
                                    @NonNull FrameLayout adContainer, @NonNull SlotState slotState) {
        if (!isActivityUsable(activity) || !adContainer.isAttachedToWindow()) {
            return;
        }
        Object tag = adContainer.getTag();
        if (!(tag instanceof String) || !slotKey.equals(tag)) {
            return;
        }
        renderResolvedSlot(activity, slotKey, adContainer, slotState);
    }

    private boolean renderResolvedSlot(@NonNull Activity activity, @NonNull String slotKey,
                                       @NonNull FrameLayout adContainer,
                                       @NonNull SlotState slotState) {
        if (isRealAdRendered(adContainer, slotKey)) {
            debug("render skip already-real slot=" + slotKey + " provider=" + slotState.provider);
            return true;
        }

        if (slotState.provider == Provider.NONE) {
            renderNone(adContainer);
            return false;
        }

        adContainer.setVisibility(View.VISIBLE);
        NativeAdInjectionConfig.AdType resolvedType =
                slotState.adType == NativeAdInjectionConfig.AdType.MREC
                        ? NativeAdInjectionConfig.AdType.MREC
                        : NativeAdInjectionConfig.AdType.NATIVE;
        applyContainerHeight(adContainer, resolvedType);
        adContainer.removeAllViews();

        View renderedView;
        if (slotState.provider == Provider.APPLOVIN_NATIVE) {
            MaxNativeAdView appLovinView = slotState.appLovinNativeView;
            if (appLovinView == null) {
                renderNone(adContainer);
                return false;
            }
            detachFromParent(appLovinView);
            renderedView = appLovinView;
            adContainer.addView(renderedView);
        } else if (slotState.provider == Provider.GOOGLE_NATIVE || slotState.provider == Provider.GOOGLE_NATIVE_FAIL) {
            if (slotState.googleNativeAd == null) {
                renderNone(adContainer);
                return false;
            }
            NativeAdView slotView = slotState.googleNativeViewsBySlot.get(slotKey);
            if (slotView == null) {
                int googleNativeLayout = slotState.adType == NativeAdInjectionConfig.AdType.MREC
                        ? R.layout.google_native_mrec_fallback
                        : R.layout.google_native;
                slotView = (NativeAdView) activity.getLayoutInflater()
                        .inflate(googleNativeLayout, adContainer, false);
                styleGoogleTemplate(slotView);
                populateGoogleNativeAdView(slotState.googleNativeAd, slotView);
                slotState.googleNativeViewsBySlot.put(slotKey, slotView);
            }
            detachFromParent(slotView);
            renderedView = slotView;
            adContainer.addView(renderedView);
        } else {
            View mrecView = (slotState.provider == Provider.APPLOVIN_MREC)
                    ? slotState.appLovinMrecView
                    : slotState.googleMrecView;
            if (mrecView == null) {
                renderNone(adContainer);
                return false;
            }
            detachFromParent(mrecView);
            renderedView = mrecView;
            adContainer.addView(renderedView);
        }
        renderedView.setTag(R.id.native_ad_rendered_slot_key, slotKey);
        debug("render attach real-ad slot=" + slotKey + " provider=" + slotState.provider);

        if (!slotState.animated) {
            slotState.animated = true;
            actuallyShownWins.incrementAndGet();
            adContainer.setAlpha(0f);
            adContainer.setTranslationY(24f);
            adContainer.animate().alpha(1f).translationY(0f).setDuration(260L).start();
        }
        slotState.pendingContainersByRenderKey.remove(slotKey);
        return true;
    }

    private void registerPendingContainer(@NonNull SlotState slotState,
                                          @NonNull String renderSlotKey,
                                          @NonNull FrameLayout adContainer) {
        slotState.pendingContainersByRenderKey.put(renderSlotKey, new WeakReference<>(adContainer));
    }

    private void renderPendingContainers(@NonNull Activity activity, @NonNull SlotState slotState) {
        if (slotState.provider == Provider.NONE) {
            slotState.pendingContainersByRenderKey.clear();
            return;
        }
        for (Entry<String, WeakReference<FrameLayout>> entry : slotState.pendingContainersByRenderKey.entrySet()) {
            String renderKey = entry.getKey();
            FrameLayout container = entry.getValue().get();
            if (container == null) {
                slotState.pendingContainersByRenderKey.remove(renderKey);
                continue;
            }
            Object tag = container.getTag();
            if (!(tag instanceof String) || !renderKey.equals(tag)) {
                slotState.pendingContainersByRenderKey.remove(renderKey);
                continue;
            }
            if (!container.isAttachedToWindow() || !isActivityUsable(activity)) {
                continue;
            }
            renderResolvedSlot(activity, renderKey, container, slotState);
        }
    }

    private void styleAppLovinTemplate(@NonNull RelativeLayout mainContainer, @NonNull Button ctaButton) {
        int bgColor = parseColorOrDefault(AdUtils.native_bg_color, AdUtils.default_native_bg_color);
        int ctaBgColor = parseColorOrDefault(AdUtils.native_button_color, AdUtils.default_native_button_color);
        int ctaTextColor = parseColorOrDefault(AdUtils.native_button_text_color, AdUtils.default_native_button_text_color);
        mainContainer.setBackgroundColor(bgColor);
        ctaButton.setBackgroundColor(ctaBgColor);
        ctaButton.setBackgroundTintList(ColorStateList.valueOf(ctaBgColor));
        ctaButton.setTextColor(ctaTextColor);
    }

    private void styleGoogleTemplate(@NonNull NativeAdView adView) {
        NativeAdView root = adView.findViewById(R.id.ad_view);
        TextView ctaButton = adView.findViewById(R.id.ad_call_to_action);
        CardView card = adView.findViewById(R.id.card);
        int bgColor = parseColorOrDefault(AdUtils.native_bg_color, AdUtils.default_native_bg_color);
        int ctaBgColor = parseColorOrDefault(AdUtils.native_button_color, AdUtils.default_native_button_color);
        int ctaTextColor = parseColorOrDefault(AdUtils.native_button_text_color, AdUtils.default_native_button_text_color);

        if (root != null) {
            root.setBackgroundColor(bgColor);
        }
        if (card != null) {
            card.setCardBackgroundColor(ctaBgColor);
        }
        if (ctaButton != null) {
            ctaButton.setBackgroundColor(ctaBgColor);
            ctaButton.setBackgroundTintList(ColorStateList.valueOf(ctaBgColor));
            ctaButton.setTextColor(ctaTextColor);
        }
    }

    public static void renderLoadingPlaceholder(@NonNull FrameLayout adContainer,
                                                @NonNull NativeAdInjectionConfig.AdType adType) {
        adContainer.setVisibility(View.VISIBLE);
        applyContainerHeight(adContainer, adType);
        String shimmerTag = SHIMMER_TAG + "_" + adType.name();
        if (adContainer.getChildCount() == 1 && shimmerTag.equals(adContainer.getChildAt(0).getTag())) {
            return;
        }
        adContainer.removeAllViews();
        int shimmerLayout = adType == NativeAdInjectionConfig.AdType.MREC
                ? R.layout.mrec_ad_shimmer
                : R.layout.native_ad_shimmer;
        View shimmerView = LayoutInflater.from(adContainer.getContext())
                .inflate(shimmerLayout, adContainer, false);
        shimmerView.setTag(shimmerTag);
        adContainer.addView(shimmerView);
    }

    private void showLoading(@NonNull FrameLayout adContainer,
                             @NonNull NativeAdInjectionConfig.AdType adType) {
        renderLoadingPlaceholder(adContainer, adType);
    }

    private void renderNone(@NonNull FrameLayout adContainer) {
        adContainer.removeAllViews();
        adContainer.setVisibility(View.GONE);
    }

    public static boolean isRealAdRendered(@NonNull FrameLayout adContainer, @NonNull String slotKey) {
        if (adContainer.getChildCount() != 1) {
            return false;
        }
        Object renderedSlotKey = adContainer.getChildAt(0).getTag(R.id.native_ad_rendered_slot_key);
        return slotKey.equals(renderedSlotKey);
    }

    private void detachFromParent(@NonNull View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    private static void applyContainerHeight(@NonNull FrameLayout adContainer,
                                             @NonNull NativeAdInjectionConfig.AdType adType) {
        ViewGroup.LayoutParams params = adContainer.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        int targetHeight = adType == NativeAdInjectionConfig.AdType.MREC
                ? AppLovinSdkUtils.dpToPx(adContainer.getContext(), MREC_HEIGHT_DP)
                : ViewGroup.LayoutParams.WRAP_CONTENT;
        if (params.height != targetHeight) {
            params.height = targetHeight;
            adContainer.setLayoutParams(params);
        }
    }

    private static int parseColorOrDefault(@NonNull String color, @NonNull String fallback) {
        try {
            return Color.parseColor(color);
        } catch (Exception ignored) {
            return Color.parseColor(fallback);
        }
    }

    private void notifyStatsChanged() {
        if (statsNotifyScheduled) {
            return;
        }
        statsNotifyScheduled = true;
        mainHandler.postDelayed(() -> {
            statsNotifyScheduled = false;
            String text = getProbabilitySummary();
            for (Listener listener : listeners) {
                listener.onStatsChanged(text);
            }
        }, STATS_NOTIFY_DELAY_MS);
    }

    private void notifyStatsChangedNow() {
        statsNotifyScheduled = false;
        String text = getProbabilitySummary();
        for (Listener listener : listeners) {
            listener.onStatsChanged(text);
        }
    }

    public void releaseByPlacementPrefix(@NonNull String placementPrefix) {
        for (Entry<String, SlotState> entry : slots.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(placementPrefix + "#")
                    || key.startsWith(placementPrefix + "@")
                    || key.equals(placementPrefix)) {
                SlotState removed = slots.remove(key);
                if (removed != null) {
                    destroySlotState(removed);
                }
            }
        }
    }

    private void destroySlotState(@NonNull SlotState state) {
        state.loading = false;
        state.queued = false;
        state.pendingContainersByRenderKey.clear();
        try {
            if (state.googleNativeAd != null) {
                state.googleNativeAd.destroy();
                state.googleNativeAd = null;
            }
        } catch (Exception ignored) {
        }
        for (NativeAdView nativeAdView : state.googleNativeViewsBySlot.values()) {
            try {
                detachFromParent(nativeAdView);
            } catch (Exception ignored) {
            }
        }
        state.googleNativeViewsBySlot.clear();
        try {
            if (state.appLovinNativeAd != null && state.appLovinLoader != null) {
                state.appLovinLoader.destroy(state.appLovinNativeAd);
                state.appLovinNativeAd = null;
            }
        } catch (Exception ignored) {
        }
        state.appLovinNativeView = null;
        try {
            if (state.appLovinMrecView != null) {
                state.appLovinMrecView.destroy();
            }
        } catch (Exception ignored) {
        }
        state.appLovinMrecView = null;
        try {
            if (state.googleMrecView != null) {
                state.googleMrecView.destroy();
            }
        } catch (Exception ignored) {
        }
        state.googleMrecView = null;
    }

    private void populateGoogleNativeAdView(@NonNull NativeAd nativeAd, @NonNull NativeAdView adView) {
        final MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        if (mediaView != null) {
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    public String getProbabilitySummary() {
        int total = Math.max(1, totalSlotsRequested.get());
        int appLovin = appLovinWins.get();
        int google = googleWins.get();
        int googleFail = googleFailWins.get();
        int noFill = noFillWins.get();

        int appPercent = Math.round((appLovin * 100f) / total);
        int googlePercent = Math.round((google * 100f) / total);
        int googleFailPercent = Math.round((googleFail * 100f) / total);
        int noFillPercent = Math.round((noFill * 100f) / total);
        int fillRatePercent = Math.round(((appLovin + google + googleFail) * 100f) / total);
        int showRatePercent = Math.round((actuallyShownWins.get() * 100f) / total);

        return "Provider win% | AppLovin: " + appPercent + "%, Google: " + googlePercent
                + "%, Google Fail: " + googleFailPercent + "%, No Fill: " + noFillPercent
                + "% | Fill rate: " + fillRatePercent + "% | Show rate: " + showRatePercent + "%"
                + " | Req: " + totalRequestsStarted.get()
                + " Loaded: " + totalRequestsLoaded.get()
                + " Failed: " + totalRequestsFailed.get()
                + " Binds: " + totalBindCalls.get()
                + " CacheHits: " + cacheHitBinds.get()
                + " InFlight: " + currentlyLoading.get();
    }

    private void debug(@NonNull String message) {
        if (debugLog) {
            Log.d(TAG, message);
        }
    }

    private boolean isActivityUsable(@NonNull Activity activity) {
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    private void decrementLoading() {
        while (true) {
            int value = currentlyLoading.get();
            if (value <= 0) {
                currentlyLoading.set(0);
                return;
            }
            if (currentlyLoading.compareAndSet(value, value - 1)) {
                return;
            }
        }
    }
}
