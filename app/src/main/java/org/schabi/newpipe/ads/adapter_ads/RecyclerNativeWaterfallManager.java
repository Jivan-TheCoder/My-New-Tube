package org.schabi.newpipe.ads.adapter_ads;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecyclerNativeWaterfallManager {
    private static final String SHIMMER_TAG = "native_ad_shimmer";
    private static final int MREC_HEIGHT_DP = 250;
    private static volatile RecyclerNativeWaterfallManager instance;

    private static class SlotState {
        volatile boolean loading;
        volatile boolean resolved;
        volatile NativeAdInjectionConfig.AdType adType = NativeAdInjectionConfig.AdType.NATIVE;
        NativeAd nativeAd;
        AdView mrecAdView;
        String mrecAdUnitId;
        final Map<String, WeakReference<FrameLayout>> pendingContainers = new ConcurrentHashMap<>();
    }

    public interface Listener {
        void onStatsChanged(@NonNull String summaryText);
    }

    private final Map<String, SlotState> slots = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

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
        listener.onStatsChanged(getProbabilitySummary());
    }

    public void removeListener(@NonNull Listener listener) {
        listeners.remove(listener);
    }

    public void resetStats() {
        notifyStatsChangedNow();
    }

    public void setDebugLog(boolean enabled) {
    }

    public boolean isSlotResolved(@NonNull String slotKey) {
        SlotState slot = slots.get(slotKey);
        return slot != null && slot.resolved;
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
        if (!AdUtils.isOnline(activity)) {
            renderNone(adContainer);
            return false;
        }

        SlotState slot = slots.computeIfAbsent(dataSlotKey, k -> new SlotState());
        slot.adType = adType;

        if (slot.resolved) {
            return renderResolved(activity, renderSlotKey, adContainer, slot);
        }

        registerPendingContainer(slot, renderSlotKey, adContainer);
        renderLoadingPlaceholder(adContainer, adType);

        if (slot.loading) {
            return false;
        }

        slot.loading = true;
        if (adType == NativeAdInjectionConfig.AdType.MREC) {
            slot.resolved = true;
            slot.loading = false;
            renderPendingContainers(activity, slot);
            return false;
        }

        String adUnitId = !TextUtils.isEmpty(AdUtils.REC_Google_Native)
                ? AdUtils.REC_Google_Native
                : AdUtils.REC_Google_Native_Fail;
        if (TextUtils.isEmpty(adUnitId)) {
            slot.resolved = true;
            slot.loading = false;
            renderPendingContainers(activity, slot);
            return false;
        }

        AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                .forNativeAd(nativeAd -> {
                    destroyNativeIfAny(slot.nativeAd);
                    slot.nativeAd = nativeAd;
                    slot.resolved = true;
                    slot.loading = false;
                    renderPendingContainers(activity, slot);
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        slot.resolved = true;
                        slot.loading = false;
                        renderPendingContainers(activity, slot);
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
        return false;
    }

    private void registerPendingContainer(@NonNull SlotState slot,
                                          @NonNull String renderSlotKey,
                                          @NonNull FrameLayout container) {
        slot.pendingContainers.put(renderSlotKey, new WeakReference<>(container));
    }

    private boolean renderResolved(@NonNull Activity activity,
                                   @NonNull String renderSlotKey,
                                   @NonNull FrameLayout adContainer,
                                   @NonNull SlotState slot) {
        adContainer.setTag(renderSlotKey);

        if (slot.adType == NativeAdInjectionConfig.AdType.MREC) {
            return renderGoogleMrec(activity, adContainer, renderSlotKey, slot);
        }

        if (slot.nativeAd == null) {
            renderNone(adContainer);
            return false;
        }

        adContainer.removeAllViews();
        NativeAdView adView = (NativeAdView) LayoutInflater.from(activity)
                .inflate(R.layout.google_native, adContainer, false);
        styleGoogleTemplate(adView);
        populateGoogleNativeAdView(slot.nativeAd, adView);
        adView.setTag(R.id.native_ad_rendered_slot_key, renderSlotKey);
        adContainer.addView(adView);
        adContainer.setVisibility(View.VISIBLE);
        return true;
    }

    private void renderPendingContainers(@NonNull Activity activity, @NonNull SlotState slot) {
        for (Map.Entry<String, WeakReference<FrameLayout>> entry : slot.pendingContainers.entrySet()) {
            String renderKey = entry.getKey();
            FrameLayout container = entry.getValue().get();
            if (container != null && container.isAttachedToWindow()) {
                renderResolved(activity, renderKey, container, slot);
            }
        }
        slot.pendingContainers.clear();
    }

    private boolean renderGoogleMrec(@NonNull Activity activity,
                                     @NonNull FrameLayout adContainer,
                                     @NonNull String renderSlotKey,
                                     @NonNull SlotState slot) {
        String adUnitId = !TextUtils.isEmpty(AdUtils.REC_Google_Medium_REC)
                ? AdUtils.REC_Google_Medium_REC
                : AdUtils.REC_Google_Medium_REC_Fail;
        if (TextUtils.isEmpty(adUnitId)) {
            renderNone(adContainer);
            return false;
        }

        // Recreate only if ad unit changed for this slot.
        if (slot.mrecAdView == null || !adUnitId.equals(slot.mrecAdUnitId)) {
            destroyAdViewIfAny(slot.mrecAdView);
            slot.mrecAdView = new AdView(activity);
            slot.mrecAdView.setAdUnitId(adUnitId);
            slot.mrecAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            slot.mrecAdView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(activity, MREC_HEIGHT_DP)
            ));
            slot.mrecAdUnitId = adUnitId;
            slot.mrecAdView.loadAd(new AdRequest.Builder().build());
        }

        AdView adView = slot.mrecAdView;
        if (adView == null) {
            renderNone(adContainer);
            return false;
        }

        // Move the same MREC view across recycled holders for this slot.
        detachFromParent(adView);
        adContainer.removeAllViews();
        ensureMrecContainerHeight(activity, adContainer);
        adView.setTag(R.id.native_ad_rendered_slot_key, renderSlotKey);
        adContainer.addView(adView);
        adContainer.setVisibility(View.VISIBLE);
        return true;
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
            ctaButton.setTextColor(ctaTextColor);
        }

    }

    private void populateGoogleNativeAdView(@NonNull NativeAd nativeAd, @NonNull NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        if (mediaView != null) {
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        TextView headline = (TextView) adView.getHeadlineView();
        TextView body = (TextView) adView.getBodyView();
        TextView callToAction = (TextView) adView.getCallToActionView();

        if (headline != null) {
            headline.setText(nativeAd.getHeadline());
            headline.setTextColor(parseColorOrDefault(AdUtils.native_headline_color, AdUtils.default_native_headline_color));
        }
        if (body != null) {
            body.setText(nativeAd.getBody());
            body.setTextColor(parseColorOrDefault(AdUtils.native_body_color, AdUtils.default_native_body_color));
        }
        if (callToAction != null) callToAction.setText(nativeAd.getCallToAction());

        if (adView.getIconView() instanceof ImageView) {
            ImageView iconView = (ImageView) adView.getIconView();
            if (nativeAd.getIcon() == null) {
                iconView.setVisibility(View.GONE);
            } else {
                iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
                iconView.setVisibility(View.VISIBLE);
            }
        }

        adView.setNativeAd(nativeAd);
    }

    public static void renderLoadingPlaceholder(@NonNull FrameLayout adContainer,
                                                @NonNull NativeAdInjectionConfig.AdType adType) {
        adContainer.setVisibility(View.VISIBLE);
        if (adType == NativeAdInjectionConfig.AdType.MREC) {
            ensureMrecContainerHeight(adContainer.getContext(), adContainer);
        }
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

    public static boolean isRealAdRendered(@NonNull FrameLayout adContainer, @NonNull String slotKey) {
        if (adContainer.getChildCount() != 1) {
            return false;
        }
        Object renderedSlotKey = adContainer.getChildAt(0).getTag(R.id.native_ad_rendered_slot_key);
        return slotKey.equals(renderedSlotKey);
    }

    public void releaseByPlacementPrefix(@NonNull String placementPrefix) {
        for (String key : slots.keySet()) {
            if (key.startsWith(placementPrefix + "#") || key.startsWith(placementPrefix + "@") || key.equals(placementPrefix)) {
                SlotState state = slots.remove(key);
                if (state != null) {
                    destroyNativeIfAny(state.nativeAd);
                    destroyAdViewIfAny(state.mrecAdView);
                    state.nativeAd = null;
                    state.mrecAdView = null;
                    state.mrecAdUnitId = null;
                    state.pendingContainers.clear();
                    state.loading = false;
                    state.resolved = false;
                }
            }
        }
    }

    public String getProbabilitySummary() {
        return "Provider win% | Google Native/MREC active";
    }

    private static int parseColorOrDefault(@NonNull String color, @NonNull String fallback) {
        try {
            return Color.parseColor(color);
        } catch (Exception ignored) {
            return Color.parseColor(fallback);
        }
    }

    private void renderNone(@NonNull FrameLayout adContainer) {
        adContainer.removeAllViews();
        adContainer.setVisibility(View.GONE);
    }

    private void notifyStatsChangedNow() {
        String text = getProbabilitySummary();
        for (Listener listener : listeners) {
            listener.onStatsChanged(text);
        }
    }

    private void destroyNativeIfAny(NativeAd nativeAd) {
        if (nativeAd != null) {
            try {
                nativeAd.destroy();
            } catch (Exception ignored) {
                // Ignore ad SDK destroy edge cases.
            }
        }
    }

    private void destroyAdViewIfAny(AdView adView) {
        if (adView != null) {
            try {
                adView.destroy();
            } catch (Exception ignored) {
                // Ignore ad SDK destroy edge cases.
            }
        }
    }

    private void detachFromParent(@NonNull View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    private static void ensureMrecContainerHeight(@NonNull android.content.Context context,
                                                  @NonNull FrameLayout adContainer) {
        ViewGroup.LayoutParams params = adContainer.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(context, MREC_HEIGHT_DP)
            );
        } else {
            params.height = dpToPx(context, MREC_HEIGHT_DP);
        }
        adContainer.setLayoutParams(params);
    }

    private static int dpToPx(@NonNull android.content.Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
