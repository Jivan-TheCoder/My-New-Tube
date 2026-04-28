package org.schabi.newpipe.ads.adapter_ads;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.R;

import java.util.Collections;
import java.util.List;

public final class RecyclerNativeAdInjector {
    private static final long BIND_AFTER_SCROLL_IDLE_DELAY_MS = 120L;
    private static final int MAX_BIND_IDLE_RETRIES = 6;

    private RecyclerNativeAdInjector() {
    }

    // One-line API:
    // RecyclerNativeAdInjector.attach(this, recyclerView, contentAdapter, NativeAdInjectionConfig.interval(1, 6));
    public static RecyclerView.Adapter<?> attach(@NonNull Activity activity,
                                                 @NonNull RecyclerView recyclerView,
                                                 @NonNull RecyclerView.Adapter<?> contentAdapter,
                                                 @NonNull NativeAdInjectionConfig config) {
        String defaultPlacement = activity.getClass().getSimpleName() + "_" + recyclerView.getId();
        String basePlacement = "default_placement".equals(config.getPlacementKey())
                ? defaultPlacement
                : config.getPlacementKey();
        String scopedPlacement = basePlacement + "@rv" + Integer.toHexString(System.identityHashCode(recyclerView));
        NativeAdInjectionConfig safeConfig = config.withPlacementKey(
                scopedPlacement
        );
        AdInjectedAdapter adapter = new AdInjectedAdapter(activity, contentAdapter, safeConfig);
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    private static final class AdInjectedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int AD_VIEW_TYPE = Integer.MIN_VALUE + 5000;

        private final Activity activity;
        private final RecyclerView.Adapter<?> sourceAdapter;
        private final NativeAdInjectionConfig config;
        private final RecyclerNativeWaterfallManager waterfallManager;
        private final String placementKey;
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        private List<Integer> adPositions = Collections.emptyList();
        private int lastSourceCount = -1;
        private boolean observerRegistered;
        private RecyclerView recyclerView;
        private int scrollState = RecyclerView.SCROLL_STATE_IDLE;

        private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                scrollState = newState;
            }
        };

        private final RecyclerView.AdapterDataObserver sourceObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                rebuildPositionsAndRefresh();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                rebuildPositionsAndRefresh();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rebuildPositionsAndRefresh();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                rebuildPositionsAndRefresh();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                rebuildPositionsAndRefresh();
            }
        };

        AdInjectedAdapter(@NonNull Activity activity,
                          @NonNull RecyclerView.Adapter<?> sourceAdapter,
                          @NonNull NativeAdInjectionConfig config) {
            this.activity = activity;
            this.sourceAdapter = sourceAdapter;
            this.config = config;
            this.waterfallManager = RecyclerNativeWaterfallManager.getInstance();
            this.placementKey = config.getPlacementKey();
            this.sourceAdapter.registerAdapterDataObserver(sourceObserver);
            this.observerRegistered = true;
            setHasStableIds(sourceAdapter.hasStableIds());
            rebuildPositionsAndRefresh();
        }

        @Override
        public int getItemCount() {
            ensurePositions();
            return sourceAdapter.getItemCount() + adPositions.size();
        }

        @Override
        public int getItemViewType(int position) {
            ensurePositions();
            if (isAdPosition(position)) {
                return AD_VIEW_TYPE;
            }
            int sourcePosition = toSourcePosition(position);
            return sourceAdapter.getItemViewType(sourcePosition);
        }

        @Override
        public long getItemId(int position) {
            ensurePositions();
            if (isAdPosition(position)) {
                return Long.MIN_VALUE + adIndexForPosition(position);
            }
            return sourceAdapter.getItemId(toSourcePosition(position));
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == AD_VIEW_TYPE) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_native_ad_slot, parent, false);
                return new AdViewHolder(view);
            }
            return sourceAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ensurePositions();
            if (isAdPosition(position)) {
                int adSlotId = adIndexForPosition(position);
                NativeAdInjectionConfig.AdType slotAdType = config.resolveAdTypeForSlot(adSlotId);
                int uniqueSlotId = toUniqueSlotId(adSlotId, slotAdType);
                String dataSlotKey = placementKey + "#" + slotAdType.name() + "_" + uniqueSlotId;
                String renderSlotKey = dataSlotKey + "@p" + adSlotId;
                bindAdWhenScrollIsCalm((AdViewHolder) holder, dataSlotKey, renderSlotKey, slotAdType);
                return;
            }
            int sourcePosition = toSourcePosition(position);
            //noinspection unchecked
            ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onBindViewHolder(holder, sourcePosition);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                     @NonNull List<Object> payloads) {
            ensurePositions();
            if (isAdPosition(position)) {
                onBindViewHolder(holder, position);
                return;
            }
            int sourcePosition = toSourcePosition(position);
            //noinspection unchecked
            ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter)
                    .onBindViewHolder(holder, sourcePosition, payloads);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            this.recyclerView = recyclerView;
            recyclerView.addOnScrollListener(scrollListener);
            //noinspection unchecked
            ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            recyclerView.removeOnScrollListener(scrollListener);
            this.recyclerView = null;
            if (observerRegistered) {
                sourceAdapter.unregisterAdapterDataObserver(sourceObserver);
                observerRegistered = false;
            }
            waterfallManager.releaseByPlacementPrefix(placementKey);
            //noinspection unchecked
            ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onDetachedFromRecyclerView(recyclerView);
            super.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            if (!(holder instanceof AdViewHolder)) {
                //noinspection unchecked
                ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onViewRecycled(holder);
            }
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
            if (holder instanceof AdViewHolder) {
                return super.onFailedToRecycleView(holder);
            }
            //noinspection unchecked
            return ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onFailedToRecycleView(holder);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            if (holder instanceof AdViewHolder) {
                String renderSlotKey = ((AdViewHolder) holder).getSlotKey();
                if (renderSlotKey != null) {
                    NativeAdInjectionConfig.AdType adType = resolveAdTypeFromSlotKey(renderSlotKey);
                    bindAdWhenScrollIsCalm((AdViewHolder) holder,
                            resolveDataSlotKey(renderSlotKey), renderSlotKey, adType);
                }
            } else {
                //noinspection unchecked
                ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onViewAttachedToWindow(holder);
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            if (!(holder instanceof AdViewHolder)) {
                //noinspection unchecked
                ((RecyclerView.Adapter<RecyclerView.ViewHolder>) sourceAdapter).onViewDetachedFromWindow(holder);
            }
        }

        private void rebuildPositionsAndRefresh() {
            lastSourceCount = sourceAdapter.getItemCount();
            adPositions = config.resolveAdAdapterPositions(lastSourceCount);
            notifyDataSetChanged();
        }

        private void ensurePositions() {
            int sourceCount = sourceAdapter.getItemCount();
            if (sourceCount != lastSourceCount) {
                lastSourceCount = sourceCount;
                adPositions = config.resolveAdAdapterPositions(sourceCount);
            }
        }

        private boolean isAdPosition(int adapterPosition) {
            return Collections.binarySearch(adPositions, adapterPosition) >= 0;
        }

        private int adIndexForPosition(int adapterPosition) {
            return Collections.binarySearch(adPositions, adapterPosition);
        }

        private int toUniqueSlotId(int adSlotId, @NonNull NativeAdInjectionConfig.AdType adType) {
            // MREC views are not safely cloneable across multiple visible rows.
            // Keep one physical slot per adapter ad index to avoid view-steal artifacts.
            if (adType == NativeAdInjectionConfig.AdType.MREC) {
                return adSlotId;
            }
            int maxUnique = config.getMaxUniqueAdsToLoad();
            if (maxUnique == Integer.MAX_VALUE) {
                return adSlotId;
            }
            int typeSequenceIndex = 0;
            for (int i = 0; i <= adSlotId; i++) {
                if (config.resolveAdTypeForSlot(i) == adType) {
                    if (i == adSlotId) {
                        break;
                    }
                    typeSequenceIndex++;
                }
            }
            return Math.floorMod(typeSequenceIndex, Math.max(1, maxUnique));
        }

        @NonNull
        private NativeAdInjectionConfig.AdType resolveAdTypeFromSlotKey(@NonNull String slotKey) {
            int hashIndex = slotKey.lastIndexOf('#');
            if (hashIndex < 0 || hashIndex + 1 >= slotKey.length()) {
                return config.getAdType();
            }
            String suffix = slotKey.substring(hashIndex + 1);
            if (suffix.startsWith(NativeAdInjectionConfig.AdType.MREC.name() + "_")) {
                return NativeAdInjectionConfig.AdType.MREC;
            }
            return NativeAdInjectionConfig.AdType.NATIVE;
        }

        @NonNull
        private String resolveDataSlotKey(@NonNull String renderSlotKey) {
            int atIndex = renderSlotKey.lastIndexOf("@p");
            if (atIndex > 0) {
                return renderSlotKey.substring(0, atIndex);
            }
            return renderSlotKey;
        }

        private int toSourcePosition(int adapterPosition) {
            int adsBefore = 0;
            for (int adPosition : adPositions) {
                if (adPosition < adapterPosition) {
                    adsBefore++;
                } else {
                    break;
                }
            }
            return adapterPosition - adsBefore;
        }

        private void bindAdWhenScrollIsCalm(@NonNull AdViewHolder holder,
                                            @NonNull String dataSlotKey,
                                            @NonNull String renderSlotKey,
                                            @NonNull NativeAdInjectionConfig.AdType adType) {
            holder.markSlot(renderSlotKey, adType);
            if (holder.isRenderedForSlot(renderSlotKey)) {
                return;
            }
            if (waterfallManager.isSlotResolved(dataSlotKey)) {
                holder.bind(activity, waterfallManager, dataSlotKey, renderSlotKey, adType);
                return;
            }
            if (!isHolderVisibleEnough(holder)) {
                bindAdAfterDelay(holder, dataSlotKey, renderSlotKey, adType, 0);
                return;
            }
            if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                holder.bind(activity, waterfallManager, dataSlotKey, renderSlotKey, adType);
                return;
            }

            bindAdAfterDelay(holder, dataSlotKey, renderSlotKey, adType, 0);
        }

        private void bindAdAfterDelay(@NonNull AdViewHolder holder,
                                      @NonNull String dataSlotKey,
                                      @NonNull String renderSlotKey,
                                      @NonNull NativeAdInjectionConfig.AdType adType, int attempt) {
            mainHandler.postDelayed(() -> {
                RecyclerView attachedRecycler = recyclerView;
                if (attachedRecycler == null || !renderSlotKey.equals(holder.getSlotKey())) {
                    return;
                }
                if (holder.isRenderedForSlot(renderSlotKey)) {
                    return;
                }
                if (waterfallManager.isSlotResolved(dataSlotKey)) {
                    holder.bind(activity, waterfallManager, dataSlotKey, renderSlotKey, adType);
                    return;
                }
                if (!isHolderVisibleEnough(holder) && attempt < MAX_BIND_IDLE_RETRIES) {
                    bindAdAfterDelay(holder, dataSlotKey, renderSlotKey, adType, attempt + 1);
                    return;
                }
                if (scrollState != RecyclerView.SCROLL_STATE_IDLE && attempt < MAX_BIND_IDLE_RETRIES) {
                    bindAdAfterDelay(holder, dataSlotKey, renderSlotKey, adType, attempt + 1);
                    return;
                }
                holder.bind(activity, waterfallManager, dataSlotKey, renderSlotKey, adType);
            }, BIND_AFTER_SCROLL_IDLE_DELAY_MS);
        }

        private boolean isHolderVisibleEnough(@NonNull AdViewHolder holder) {
            RecyclerView attachedRecycler = recyclerView;
            if (attachedRecycler == null
                    || !holder.itemView.isAttachedToWindow()
                    || !attachedRecycler.isShown()
                    || !holder.itemView.isShown()) {
                return false;
            }
            Rect visibleRect = new Rect();
            return holder.itemView.getGlobalVisibleRect(visibleRect) && visibleRect.height() > 0;
        }
    }

    private static final class AdViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout adContainer;
        private String slotKey;
        private String renderedSlotKey;

        AdViewHolder(@NonNull View itemView) {
            super(itemView);
            adContainer = itemView.findViewById(R.id.ad_container);
        }

        void markSlot(@NonNull String slotKey, @NonNull NativeAdInjectionConfig.AdType adType) {
            if (!slotKey.equals(this.slotKey)) {
                renderedSlotKey = null;
                // Prevent recycled holder from showing previous slot's ad while we wait for bind.
                RecyclerNativeWaterfallManager.renderLoadingPlaceholder(adContainer, adType);
            }
            this.slotKey = slotKey;
            adContainer.setTag(slotKey);
        }

        String getSlotKey() {
            return slotKey;
        }

        void bind(@NonNull Activity activity,
                  @NonNull RecyclerNativeWaterfallManager manager,
                  @NonNull String dataSlotKey,
                  @NonNull String renderSlotKey,
                  @NonNull NativeAdInjectionConfig.AdType adType) {
            if (manager.bind(activity, dataSlotKey, renderSlotKey, adContainer, adType)) {
                renderedSlotKey = renderSlotKey;
            }
        }

        boolean isRenderedForSlot(@NonNull String slotKey) {
            return slotKey.equals(renderedSlotKey)
                    && RecyclerNativeWaterfallManager.isRealAdRendered(adContainer, slotKey);
        }
    }
}
