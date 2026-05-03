package com.playtube.protube.video.music;

import static com.playtube.protube.video.music.util.SparseItemUtil.fetchStreamInfoAndSaveToDatabase;
import static com.playtube.protube.video.music.util.external_communication.ShareUtils.shareText;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;

import androidx.fragment.app.FragmentManager;

import com.playtube.protube.video.music.R;

import com.playtube.protube.video.music.database.stream.model.StreamEntity;
import com.playtube.protube.video.music.download.DownloadDialog;
import com.playtube.protube.video.music.local.dialog.PlaylistDialog;
import com.playtube.protube.video.music.player.playqueue.PlayQueue;
import com.playtube.protube.video.music.player.playqueue.PlayQueueItem;
import com.playtube.protube.video.music.util.NavigationHelper;
import com.playtube.protube.video.music.util.SparseItemUtil;

import java.util.List;

public final class QueueItemMenuUtil {
    private QueueItemMenuUtil() {
    }

    public static void openPopupMenu(final PlayQueue playQueue,
                                     final PlayQueueItem item,
                                     final View view,
                                     final boolean hideDetails,
                                     final FragmentManager fragmentManager,
                                     final Context context) {
        final ContextThemeWrapper themeWrapper =
                new ContextThemeWrapper(context, R.style.DarkPopupMenu);

        final PopupMenu popupMenu = new PopupMenu(themeWrapper, view);
        popupMenu.inflate(R.menu.menu_play_queue_item);

        if (hideDetails) {
            popupMenu.getMenu().findItem(R.id.menu_item_details).setVisible(false);
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            final int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_item_remove) {
                final int index = playQueue.indexOf(item);
                playQueue.remove(index);
                return true;
            } else if (itemId == R.id.menu_item_details) {
                // playQueue is null since we don't want any queue change
                NavigationHelper.openVideoDetail(context, item.getServiceId(),
                        item.getUrl(), item.getTitle(), null,
                        false);
                return true;
            } else if (itemId == R.id.menu_item_append_playlist) {
                PlaylistDialog.createCorrespondingDialog(
                        context,
                        List.of(new StreamEntity(item)),
                        dialog -> dialog.show(
                                fragmentManager,
                                "QueueItemMenuUtil@append_playlist"
                        )
                );

                return true;
            } else if (itemId == R.id.menu_item_channel_details) {
                SparseItemUtil.fetchUploaderUrlIfSparse(context, item.getServiceId(),
                        item.getUrl(), item.getUploaderUrl(),
                        // An intent must be used here.
                        // Opening with FragmentManager transactions is not working,
                        // as PlayQueueActivity doesn't use fragments.
                        uploaderUrl -> NavigationHelper.openChannelFragmentUsingIntent(
                                context, item.getServiceId(), uploaderUrl, item.getUploader()
                        ));
                return true;
            } else if (itemId == R.id.menu_item_share) {
                shareText(context, item.getTitle(), item.getUrl(),
                        item.getThumbnails());
                return true;
            } else if (itemId == R.id.menu_item_download) {
                fetchStreamInfoAndSaveToDatabase(context, item.getServiceId(), item.getUrl(),
                        info -> {
                            final DownloadDialog downloadDialog = new DownloadDialog(context,
                                    info);
                            downloadDialog.show(fragmentManager, "downloadDialog");
                        });
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}
