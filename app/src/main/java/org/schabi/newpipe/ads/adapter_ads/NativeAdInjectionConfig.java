package org.schabi.newpipe.ads.adapter_ads;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class NativeAdInjectionConfig {
    public enum AdType {
        NATIVE,
        MREC
    }

    public enum AdMixMode {
        SINGLE_TYPE,
        ALTERNATE_MREC_FIRST,
        ALTERNATE_NATIVE_FIRST,
        FIRST_MREC_THEN_ALL_NATIVE,
        FIRST_NATIVE_THEN_ALL_MREC
    }

    private enum Mode {
        INTERVAL,
        EXPLICIT_AFTER_CONTENT
    }

    private final Mode mode;
    private final int firstAfterContentItem;
    private final int repeatEveryItems;
    private final List<Integer> explicitAfterContentItems;
    private final int maxAds;
    private final int maxUniqueAdsToLoad;
    private final AdType adType;
    private final AdMixMode adMixMode;
    private final boolean forceShowOnEmptyData;
    private final String placementKey;

    private NativeAdInjectionConfig(Mode mode,
                                    int firstAfterContentItem,
                                    int repeatEveryItems,
                                    @NonNull List<Integer> explicitAfterContentItems,
                                    int maxAds,
                                    int maxUniqueAdsToLoad,
                                    @NonNull AdType adType,
                                    @NonNull AdMixMode adMixMode,
                                    boolean forceShowOnEmptyData,
                                    @NonNull String placementKey) {
        this.mode = mode;
        this.firstAfterContentItem = firstAfterContentItem;
        this.repeatEveryItems = repeatEveryItems;
        this.explicitAfterContentItems = explicitAfterContentItems;
        this.maxAds = maxAds;
        this.maxUniqueAdsToLoad = maxUniqueAdsToLoad;
        this.adType = adType;
        this.adMixMode = adMixMode;
        this.forceShowOnEmptyData = forceShowOnEmptyData;
        this.placementKey = placementKey;
    }

    // Example: first ad after 1 item, then every 6 items -> interval(1, 6)
    public static NativeAdInjectionConfig interval(int firstAfterContentItem, int repeatEveryItems) {
        return new NativeAdInjectionConfig(
                Mode.INTERVAL,
                Math.max(0, firstAfterContentItem),
                Math.max(1, repeatEveryItems),
                Collections.emptyList(),
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                AdType.NATIVE,
                AdMixMode.SINGLE_TYPE,
                false,
                "default_placement"
        );
    }

    // Example: show ads after content items 1st, 2nd, 4th, 7th -> explicitAfterContentItems(1, 2, 4, 7)
    public static NativeAdInjectionConfig explicitAfterContentItems(int... afterContentItems) {
        List<Integer> list = new ArrayList<>();
        if (afterContentItems != null) {
            Arrays.sort(afterContentItems);
            Integer previous = null;
            for (int value : afterContentItems) {
                int safeValue = Math.max(0, value);
                if (previous == null || safeValue != previous) {
                    list.add(safeValue);
                    previous = safeValue;
                }
            }
        }
        return new NativeAdInjectionConfig(
                Mode.EXPLICIT_AFTER_CONTENT,
                1,
                1,
                list,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                AdType.NATIVE,
                AdMixMode.SINGLE_TYPE,
                false,
                "default_placement"
        );
    }

    public NativeAdInjectionConfig withMaxAds(int maxAds) {
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                Math.max(0, maxAds),
                maxUniqueAdsToLoad,
                adType,
                adMixMode,
                forceShowOnEmptyData,
                placementKey
        );
    }

    // Limits unique ads loaded; loaded ads are reused across all ad positions.
    public NativeAdInjectionConfig withMaxUniqueAdsToLoad(int maxUniqueAdsToLoad) {
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                maxAds,
                Math.max(1, maxUniqueAdsToLoad),
                adType,
                adMixMode,
                forceShowOnEmptyData,
                placementKey
        );
    }

    public NativeAdInjectionConfig withAdType(@NonNull AdType adType) {
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                maxAds,
                maxUniqueAdsToLoad,
                adType,
                adMixMode,
                forceShowOnEmptyData,
                placementKey
        );
    }

    public NativeAdInjectionConfig withAdMixMode(@NonNull AdMixMode adMixMode) {
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                maxAds,
                maxUniqueAdsToLoad,
                adType,
                adMixMode,
                forceShowOnEmptyData,
                placementKey
        );
    }

    public NativeAdInjectionConfig withForceShowOnEmptyData(boolean forceShowOnEmptyData) {
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                maxAds,
                maxUniqueAdsToLoad,
                adType,
                adMixMode,
                forceShowOnEmptyData,
                placementKey
        );
    }

    public NativeAdInjectionConfig withPlacementKey(@NonNull String placementKey) {
        String safeKey = placementKey.trim().isEmpty() ? "default_placement" : placementKey.trim();
        return new NativeAdInjectionConfig(
                mode,
                firstAfterContentItem,
                repeatEveryItems,
                explicitAfterContentItems,
                maxAds,
                maxUniqueAdsToLoad,
                adType,
                adMixMode,
                forceShowOnEmptyData,
                safeKey
        );
    }

    // Basic guardrails for policy-friendly ad density.
    public NativeAdInjectionConfig withPolicyGuardrails() {
        if (mode == Mode.INTERVAL) {
            int safeFirst = Math.max(0, firstAfterContentItem);
            int safeEvery = Math.max(4, repeatEveryItems);
            int safeMaxAds = Math.max(0, maxAds);
            return new NativeAdInjectionConfig(
                    mode,
                    safeFirst,
                    safeEvery,
                    explicitAfterContentItems,
                    safeMaxAds,
                    maxUniqueAdsToLoad,
                    adType,
                    adMixMode,
                    forceShowOnEmptyData,
                    placementKey
            );
        }
        return this;
    }

    @NonNull
    String getPlacementKey() {
        return placementKey;
    }

    int getMaxUniqueAdsToLoad() {
        return maxUniqueAdsToLoad;
    }

    @NonNull
    AdType getAdType() {
        return adType;
    }

    @NonNull
    AdType resolveAdTypeForSlot(int adSlotIndex) {
        switch (adMixMode) {
            case ALTERNATE_MREC_FIRST:
                return (adSlotIndex % 2 == 0) ? AdType.MREC : AdType.NATIVE;
            case ALTERNATE_NATIVE_FIRST:
                return (adSlotIndex % 2 == 0) ? AdType.NATIVE : AdType.MREC;
            case FIRST_MREC_THEN_ALL_NATIVE:
                return adSlotIndex == 0 ? AdType.MREC : AdType.NATIVE;
            case FIRST_NATIVE_THEN_ALL_MREC:
                return adSlotIndex == 0 ? AdType.NATIVE : AdType.MREC;
            case SINGLE_TYPE:
            default:
                return adType;
        }
    }

    @NonNull
    List<Integer> resolveAdAdapterPositions(int contentCount) {
        if (maxAds == 0) {
            return Collections.emptyList();
        }
        if (contentCount <= 0) {
            if (!forceShowOnEmptyData) {
                return Collections.emptyList();
            }
            return Collections.singletonList(0);
        }

        List<Integer> result = new ArrayList<>();
        int shown = 0;

        if (mode == Mode.INTERVAL) {
            int contentIndex = firstAfterContentItem;
            while (contentIndex <= contentCount && shown < maxAds) {
                int adapterPosition = contentIndex + shown;
                result.add(adapterPosition);
                shown++;
                contentIndex += repeatEveryItems;
            }
        } else {
            for (int contentIndex : explicitAfterContentItems) {
                if (contentIndex > contentCount || shown >= maxAds) {
                    break;
                }
                int adapterPosition = contentIndex + shown;
                result.add(adapterPosition);
                shown++;
            }
        }

        return result;
    }
}
