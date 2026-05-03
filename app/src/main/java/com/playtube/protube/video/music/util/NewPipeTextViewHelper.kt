package com.playtube.protube.video.music.util

import android.text.Selection
import android.text.Spannable
import android.widget.TextView
import com.playtube.protube.video.music.util.external_communication.ShareUtils

object NewPipeTextViewHelper {
    /**
     * Share the selected text of [NewPipeTextViews][com.playtube.protube.video.music.views.NewPipeTextView] and
     * [NewPipeEditTexts][com.playtube.protube.video.music.views.NewPipeEditText] with
     * [com.playtube.protube.video.music.util.external_communication.ShareUtils.shareText].
     *
     *
     *
     * This allows EMUI users to get the Android share sheet instead of the EMUI share sheet when
     * using the `Share` command of the popup menu which appears when selecting text.
     *
     *
     * @param textView the [TextView] on which sharing the selected text. It should be a
     * [com.playtube.protube.video.music.views.NewPipeTextView] or a [com.playtube.protube.video.music.views.NewPipeEditText]
     * (even if [standard TextViews][TextView] are supported).
     */
    @JvmStatic
    fun shareSelectedTextWithShareUtils(textView: TextView) {
        val textViewText = textView.getText()
        shareSelectedTextIfNotNullAndNotEmpty(textView, getSelectedText(textView, textViewText))
        if (textViewText is Spannable) {
            Selection.setSelection(textViewText, textView.selectionEnd)
        }
    }

    private fun getSelectedText(textView: TextView, text: CharSequence?): CharSequence? {
        if (!textView.hasSelection() || text == null) {
            return null
        }

        val start = textView.selectionStart
        val end = textView.selectionEnd
        return if (start > end) {
            text.subSequence(end, start)
        } else {
            text.subSequence(start, end)
        }
    }

    private fun shareSelectedTextIfNotNullAndNotEmpty(
        textView: TextView,
        selectedText: CharSequence?
    ) {
        if (!selectedText.isNullOrEmpty()) {
            ShareUtils.shareText(textView.context, "", selectedText.toString())
        }
    }
}
