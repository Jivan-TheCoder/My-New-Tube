package com.playtube.protube.video.music.info_list.holder;

import android.view.ViewGroup;

import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.info_list.InfoItemBuilder;

public class PlaylistGridInfoItemHolder extends PlaylistMiniInfoItemHolder {
    public PlaylistGridInfoItemHolder(final InfoItemBuilder infoItemBuilder,
                                      final ViewGroup parent) {
        super(infoItemBuilder, R.layout.list_playlist_grid_item, parent);
    }
}
