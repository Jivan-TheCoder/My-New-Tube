package com.playtube.protube.video.music.local.subscription.item

import android.view.View
import com.playtube.protube.video.music.database.feed.model.FeedGroupEntity
import com.playtube.protube.video.music.local.subscription.FeedGroupIcon
import com.xwray.groupie.viewbinding.BindableItem
import com.playtube.protube.video.music.R
import com.playtube.protube.video.music.databinding.FeedGroupCardItemBinding

data class FeedGroupCardItem(
    val groupId: Long = FeedGroupEntity.Companion.GROUP_ALL_ID,
    val name: String,
    val icon: FeedGroupIcon
) : BindableItem<FeedGroupCardItemBinding>() {
    constructor (feedGroupEntity: FeedGroupEntity) : this(feedGroupEntity.uid, feedGroupEntity.name, feedGroupEntity.icon)

    override fun getId(): Long {
        return when (groupId) {
            FeedGroupEntity.Companion.GROUP_ALL_ID -> super.getId()
            else -> groupId
        }
    }

    override fun getLayout(): Int = R.layout.feed_group_card_item

    override fun bind(viewBinding: FeedGroupCardItemBinding, position: Int) {
        viewBinding.title.text = name
        viewBinding.icon.setImageResource(icon.getDrawableRes())
    }

    override fun initializeViewBinding(view: View) = FeedGroupCardItemBinding.bind(view)
}
