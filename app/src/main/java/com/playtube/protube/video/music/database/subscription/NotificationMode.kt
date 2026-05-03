package com.playtube.protube.video.music.database.subscription

import androidx.annotation.IntDef

@IntDef(NotificationMode.Companion.DISABLED, NotificationMode.Companion.ENABLED)
@Retention(AnnotationRetention.SOURCE)
annotation class NotificationMode {
    companion object {
        const val DISABLED = 0
        const val ENABLED = 1 // other values reserved for the future
    }
}
