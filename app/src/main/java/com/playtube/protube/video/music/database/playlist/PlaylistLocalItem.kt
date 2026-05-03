package com.playtube.protube.video.music.database.playlist

import com.playtube.protube.video.music.database.LocalItem

interface PlaylistLocalItem : LocalItem {
    val orderingName: String?
    val displayIndex: Long?
    val uid: Long
    val thumbnailUrl: String?
}
