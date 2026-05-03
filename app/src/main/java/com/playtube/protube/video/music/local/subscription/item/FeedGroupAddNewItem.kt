package com.playtube.protube.video.music.local.subscription.item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import com.playtube.protube.video.music.R
import com.playtube.protube.video.music.databinding.FeedGroupAddNewItemBinding

class FeedGroupAddNewItem : BindableItem<FeedGroupAddNewItemBinding>() {
    override fun getLayout(): Int = R.layout.feed_group_add_new_item
    override fun initializeViewBinding(view: View) = FeedGroupAddNewItemBinding.bind(view)
    override fun bind(viewBinding: FeedGroupAddNewItemBinding, position: Int) {
        // this is a static item, nothing to do here
    }
}
