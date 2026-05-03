package com.playtube.protube.video.music.local.subscription.item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import com.playtube.protube.video.music.R
import com.playtube.protube.video.music.databinding.SubscriptionHeaderBinding

class Header(private val title: String) : BindableItem<SubscriptionHeaderBinding>() {

    override fun getLayout(): Int = R.layout.subscription_header

    override fun bind(viewBinding: SubscriptionHeaderBinding, position: Int) {
        viewBinding.root.text = title
    }

    override fun initializeViewBinding(view: View) = SubscriptionHeaderBinding.bind(view)
}
