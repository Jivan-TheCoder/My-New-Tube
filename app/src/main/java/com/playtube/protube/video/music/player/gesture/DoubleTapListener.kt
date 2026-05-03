package com.playtube.protube.video.music.player.gesture

interface DoubleTapListener {
    fun onDoubleTapStarted(portion: DisplayPortion)
    fun onDoubleTapProgressDown(portion: DisplayPortion)
    fun onDoubleTapFinished()
}
