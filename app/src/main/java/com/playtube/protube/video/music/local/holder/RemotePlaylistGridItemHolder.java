package com.playtube.protube.video.music.local.holder;

import android.view.ViewGroup;

import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.local.LocalItemBuilder;

public class RemotePlaylistGridItemHolder extends RemotePlaylistItemHolder {
    public RemotePlaylistGridItemHolder(final LocalItemBuilder infoItemBuilder,
                                        final ViewGroup parent) {
        super(infoItemBuilder, R.layout.list_playlist_grid_item, parent);
    }
}
