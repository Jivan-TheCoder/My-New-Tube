package com.playtube.protube.video.music.local.holder;

import android.view.ViewGroup;

import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.local.LocalItemBuilder;

public class LocalStatisticStreamCardItemHolder extends LocalStatisticStreamItemHolder {
    public LocalStatisticStreamCardItemHolder(final LocalItemBuilder infoItemBuilder,
                                              final ViewGroup parent) {
        super(infoItemBuilder, R.layout.list_stream_card_item, parent);
    }
}
