package com.playtube.protube.video.music.fragments.list;

import com.playtube.protube.video.music.fragments.ViewContract;

public interface ListViewContract<I, N> extends ViewContract<I> {
    void showListFooter(boolean show);

    void handleNextItems(N result);
}
