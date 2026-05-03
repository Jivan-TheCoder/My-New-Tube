package com.playtube.protube.video.music.info_list

import android.content.Context
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import com.playtube.protube.video.music.util.OnClickGesture

class InfoItemBuilder(val context: Context) {
    var onStreamSelectedListener: OnClickGesture<StreamInfoItem>? = null
    var onChannelSelectedListener: OnClickGesture<ChannelInfoItem>? = null
    var onPlaylistSelectedListener: OnClickGesture<PlaylistInfoItem>? = null
    var onCommentsSelectedListener: OnClickGesture<CommentsInfoItem>? = null
}
