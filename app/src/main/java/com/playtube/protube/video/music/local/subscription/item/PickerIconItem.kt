package com.playtube.protube.video.music.local.subscription.item

import android.view.View
import androidx.annotation.DrawableRes
import com.playtube.protube.video.music.local.subscription.FeedGroupIcon
import com.xwray.groupie.viewbinding.BindableItem
import com.playtube.protube.video.music.R
import com.playtube.protube.video.music.databinding.PickerIconItemBinding

class PickerIconItem(
    val icon: FeedGroupIcon
) : BindableItem<PickerIconItemBinding>() {
    @DrawableRes
    val iconRes: Int = icon.getDrawableRes()

    override fun getLayout(): Int = R.layout.picker_icon_item

    override fun bind(viewBinding: PickerIconItemBinding, position: Int) {
        viewBinding.iconView.setImageResource(iconRes)
    }

    override fun initializeViewBinding(view: View) = PickerIconItemBinding.bind(view)
}
