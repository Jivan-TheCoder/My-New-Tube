package com.playtube.protube.video.music.local.subscription

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.evernote.android.state.State
import com.playtube.protube.video.music.ads.AdUtils
import com.playtube.protube.video.music.ads.adapter_ads.NativeAdInjectionConfig
import com.playtube.protube.video.music.ads.adapter_ads.RecyclerNativeAdInjector
import com.playtube.protube.video.music.database.feed.model.FeedGroupEntity
import com.playtube.protube.video.music.error.ErrorInfo
import com.playtube.protube.video.music.error.UserAction
import com.playtube.protube.video.music.fragments.BaseStateFragment
import com.playtube.protube.video.music.ktx.animate
import com.playtube.protube.video.music.local.subscription.dialog.FeedGroupDialog
import com.playtube.protube.video.music.local.subscription.dialog.FeedGroupReorderDialog
import com.playtube.protube.video.music.local.subscription.item.ChannelItem
import com.playtube.protube.video.music.local.subscription.item.FeedGroupAddNewGridItem
import com.playtube.protube.video.music.local.subscription.item.FeedGroupAddNewItem
import com.playtube.protube.video.music.local.subscription.item.FeedGroupCardGridItem
import com.playtube.protube.video.music.local.subscription.item.FeedGroupCardItem
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.GroupieViewHolder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import com.playtube.protube.video.music.R
import com.playtube.protube.video.music.database.feed.model.FeedGroupEntity.Companion.GROUP_ALL_ID
import com.playtube.protube.video.music.databinding.DialogTitleBinding
import com.playtube.protube.video.music.databinding.FeedItemCarouselBinding
import com.playtube.protube.video.music.databinding.FragmentSubscriptionBinding
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import com.playtube.protube.video.music.local.subscription.item.FeedGroupCarouselItem
import com.playtube.protube.video.music.local.subscription.item.GroupsHeader
import com.playtube.protube.video.music.local.subscription.item.Header
import com.playtube.protube.video.music.local.subscription.item.ImportSubscriptionsHintPlaceholderItem
import com.playtube.protube.video.music.util.NavigationHelper
import com.playtube.protube.video.music.util.OnClickGesture
import com.playtube.protube.video.music.util.ServiceHelper
import com.playtube.protube.video.music.util.ThemeHelper
import com.playtube.protube.video.music.util.external_communication.ShareUtils

class SubscriptionFragment : BaseStateFragment<SubscriptionViewModel.SubscriptionState>() {
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var importExportHelper: SubscriptionsImportExportHelper
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val groupAdapter = GroupAdapter<GroupieViewHolder<FeedItemCarouselBinding>>()
    private lateinit var carouselAdapter: GroupAdapter<GroupieViewHolder<FeedItemCarouselBinding>>
    private lateinit var feedGroupsCarousel: FeedGroupCarouselItem
    private lateinit var feedGroupsSortMenuItem: GroupsHeader
    private val subscriptionsSection = Section()

    @State
    @JvmField
    var itemsListState: Parcelable? = null

    @State
    @JvmField
    var feedGroupsCarouselState: Parcelable? = null

    init {
        setHasOptionsMenu(true)
    }

    // /////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle
    // /////////////////////////////////////////////////////////////////////////

    override fun onAttach(context: Context) {
        super.onAttach(context)
        subscriptionManager = SubscriptionManager(requireContext())
        importExportHelper = SubscriptionsImportExportHelper(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    override fun onPause() {
        super.onPause()
        itemsListState = binding.itemsList.layoutManager?.onSaveInstanceState()
        feedGroupsCarouselState = feedGroupsCarousel.onSaveInstanceState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    // ////////////////////////////////////////////////////////////////////////
    // Menu
    // ////////////////////////////////////////////////////////////////////////

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        activity.supportActionBar?.setDisplayShowTitleEnabled(true)
        activity.supportActionBar?.setTitle(R.string.tab_subscriptions)

        buildImportExportMenu(menu)
    }

    private fun buildImportExportMenu(menu: Menu) {
        // -- Import --
        val importSubMenu = menu.addSubMenu(R.string.import_from)

        addMenuItemToSubmenu(importSubMenu, R.string.previous_export) { importExportHelper.onImportPreviousSelected() }
            .setIcon(R.drawable.ic_backup)

        for (service in ServiceHelper.getSupportedServices()) {
            val subscriptionExtractor = service.subscriptionExtractor ?: continue

            val supportedSources = subscriptionExtractor.supportedSources
            if (supportedSources.isEmpty()) continue

            addMenuItemToSubmenu(importSubMenu, service.serviceInfo.name) {
                onImportFromServiceSelectedWithAdIfNeeded(service.serviceId)
            }
                .setIcon(ServiceHelper.getIcon(service.serviceId))
        }

        // -- Export --
        val exportSubMenu = menu.addSubMenu(R.string.export_to)

        addMenuItemToSubmenu(exportSubMenu, R.string.file) { importExportHelper.onExportSelected() }
            .setIcon(R.drawable.ic_save)
    }

    private fun addMenuItemToSubmenu(
        subMenu: SubMenu,
        @StringRes title: Int,
        onClick: Runnable
    ): MenuItem {
        return setClickListenerToMenuItem(subMenu.add(title), onClick)
    }

    private fun addMenuItemToSubmenu(
        subMenu: SubMenu,
        title: String,
        onClick: Runnable
    ): MenuItem {
        return setClickListenerToMenuItem(subMenu.add(title), onClick)
    }

    private fun setClickListenerToMenuItem(
        menuItem: MenuItem,
        onClick: Runnable
    ): MenuItem {
        menuItem.setOnMenuItemClickListener {
            onClick.run()
            true
        }
        return menuItem
    }

    private fun onImportFromServiceSelected(serviceId: Int) {
        val supportedServiceIds = ServiceHelper.getSupportedServices().map { it.serviceId }
        val safeServiceId = if (serviceId in supportedServiceIds) {
            serviceId
        } else {
            supportedServiceIds.firstOrNull() ?: serviceId
        }
        val fragmentManager = fm
        NavigationHelper.openSubscriptionsImportFragment(fragmentManager, safeServiceId)
    }

    private fun onImportFromServiceSelectedWithAdIfNeeded(serviceId: Int) {
        if (serviceId == ServiceList.YouTube.serviceId && isAdded) {
            AdUtils.ClickWithAds(
                requireActivity(),
                object : AdUtils.InterClick {
                    override fun ClickAds() {
                        onImportFromServiceSelected(serviceId)
                    }
                }
            )
            return
        }
        onImportFromServiceSelected(serviceId)
    }

    private fun openReorderDialog() {
        FeedGroupReorderDialog().show(parentFragmentManager, null)
    }

    // ////////////////////////////////////////////////////////////////////////
    // Fragment Views
    // ////////////////////////////////////////////////////////////////////////

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        super.initViews(rootView, savedInstanceState)
        _binding = FragmentSubscriptionBinding.bind(rootView)

        groupAdapter.spanCount = if (SubscriptionViewModel.shouldUseGridForSubscription(requireContext())) ThemeHelper.getGridSpanCountChannels(
            context
        ) else 1
        binding.itemsList.layoutManager = GridLayoutManager(requireContext(), groupAdapter.spanCount).apply {
            spanSizeLookup = groupAdapter.spanSizeLookup
        }
        binding.itemsList.adapter = groupAdapter
        RecyclerNativeAdInjector.attach(
            requireActivity(),
            binding.itemsList,
            groupAdapter,
            NativeAdInjectionConfig.interval(AdUtils.sub_after, AdUtils.sub_every)
//                .withMaxAds(3)
                .withMaxUniqueAdsToLoad(AdUtils.sub_max)
                .withAdMixMode(NativeAdInjectionConfig.AdMixMode.ALTERNATE_MREC_FIRST)
                .withPlacementKey("Subscription_AD")
                .withPolicyGuardrails()
        )
        binding.itemsList.itemAnimator = null

        viewModel = ViewModelProvider(this)[SubscriptionViewModel::class.java]
        viewModel.stateLiveData.observe(viewLifecycleOwner) { it?.let(this::handleResult) }
        viewModel.feedGroupsLiveData.observe(viewLifecycleOwner) {
            it?.let { (groups, listViewMode) ->
                handleFeedGroups(groups, listViewMode)
            }
        }

        setupInitialLayout()
    }

    private fun setupInitialLayout() {
        Section().apply {
            carouselAdapter = GroupAdapter<GroupieViewHolder<FeedItemCarouselBinding>>()

            carouselAdapter.setOnItemClickListener { item, _ ->
                when (item) {
                    is FeedGroupCardItem ->
                        NavigationHelper.openFeedFragment(fm, item.groupId, item.name)

                    is FeedGroupCardGridItem ->
                        NavigationHelper.openFeedFragment(fm, item.groupId, item.name)

                    is FeedGroupAddNewItem ->
                        FeedGroupDialog.Companion.newInstance().show(fm, null)

                    is FeedGroupAddNewGridItem ->
                        FeedGroupDialog.Companion.newInstance().show(fm, null)
                }
            }
            carouselAdapter.setOnItemLongClickListener { item, _ ->
                if ((item is FeedGroupCardItem && item.groupId == FeedGroupEntity.Companion.GROUP_ALL_ID) ||
                    (item is FeedGroupCardGridItem && item.groupId == FeedGroupEntity.Companion.GROUP_ALL_ID)
                ) {
                    return@setOnItemLongClickListener false
                }

                when (item) {
                    is FeedGroupCardItem ->
                        FeedGroupDialog.Companion.newInstance(item.groupId).show(fm, null)

                    is FeedGroupCardGridItem ->
                        FeedGroupDialog.Companion.newInstance(item.groupId).show(fm, null)
                }
                return@setOnItemLongClickListener true
            }

            feedGroupsCarousel = FeedGroupCarouselItem(
                carouselAdapter = carouselAdapter,
                listViewMode = viewModel.getListViewMode()
            )

            feedGroupsSortMenuItem = GroupsHeader(
                title = getString(R.string.feed_groups_header_title),
                onSortClicked = ::openReorderDialog,
                onToggleListViewModeClicked = ::toggleListViewMode,
                listViewMode = viewModel.getListViewMode()
            )

            add(Section(feedGroupsSortMenuItem, listOf(feedGroupsCarousel)))
            groupAdapter.clear()
            groupAdapter.add(this)
        }

        subscriptionsSection.setPlaceholder(ImportSubscriptionsHintPlaceholderItem())
        subscriptionsSection.setHideWhenEmpty(true)

        groupAdapter.add(
            Section(
                Header(getString(R.string.tab_subscriptions)),
                listOf(subscriptionsSection)
            )
        )
    }

    private fun toggleListViewMode() {
        viewModel.setListViewMode(!viewModel.getListViewMode())
    }

    private fun showLongTapDialog(selectedItem: ChannelInfoItem) {
        val commands = arrayOf(
            getString(R.string.share),
            getString(R.string.open_in_browser),
            getString(R.string.unsubscribe)
        )

        val actions = DialogInterface.OnClickListener { _, i ->
            when (i) {
                0 -> ShareUtils.shareText(
                    requireContext(),
                    selectedItem.name,
                    selectedItem.url,
                    selectedItem.thumbnails
                )

                1 -> ShareUtils.openUrlInBrowser(requireContext(), selectedItem.url)

                2 -> deleteChannel(selectedItem)
            }
        }

        val dialogTitleBinding = DialogTitleBinding.inflate(LayoutInflater.from(requireContext()))
        dialogTitleBinding.root.isSelected = true
        dialogTitleBinding.itemTitleView.text = selectedItem.name
        dialogTitleBinding.itemAdditionalDetails.visibility = View.GONE

        AlertDialog.Builder(requireContext())
            .setCustomTitle(dialogTitleBinding.root)
            .setItems(commands, actions)
            .show()
    }

    private fun deleteChannel(selectedItem: ChannelInfoItem) {
        disposables.add(
            subscriptionManager.deleteSubscription(selectedItem.serviceId, selectedItem.url).subscribe {
                Toast.makeText(requireContext(), getString(R.string.channel_unsubscribed), Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun doInitialLoadLogic() = Unit
    override fun startLoading(forceLoad: Boolean) = Unit

    private val listenerChannelItem = object : OnClickGesture<ChannelInfoItem> {
        override fun selected(selectedItem: ChannelInfoItem) {
            AdUtils.ClickWithAds(
                requireActivity(),
                object : AdUtils.InterClick {
                    override fun ClickAds() {
                        NavigationHelper.openChannelFragment(
                            fm,
                            selectedItem.serviceId,
                            selectedItem.url,
                            selectedItem.name
                        )
                    }
                }
            )
        }

        override fun held(selectedItem: ChannelInfoItem) = showLongTapDialog(selectedItem)
    }

    override fun handleResult(result: SubscriptionViewModel.SubscriptionState) {
        super.handleResult(result)

        when (result) {
            is SubscriptionViewModel.SubscriptionState.LoadedState -> {
                result.subscriptions.forEach {
                    if (it is ChannelItem) {
                        it.gesturesListener = listenerChannelItem
                        it.itemVersion = if (SubscriptionViewModel.shouldUseGridForSubscription(requireContext())) {
                            ChannelItem.ItemVersion.GRID
                        } else {
                            ChannelItem.ItemVersion.MINI
                        }
                    }
                }

                subscriptionsSection.update(result.subscriptions)
                subscriptionsSection.setHideWhenEmpty(false)

                if (itemsListState != null) {
                    binding.itemsList.layoutManager?.onRestoreInstanceState(itemsListState)
                    itemsListState = null
                }
            }

            is SubscriptionViewModel.SubscriptionState.ErrorState -> {
                result.error?.let {
                    showError(ErrorInfo(result.error, UserAction.SOMETHING_ELSE, "Subscriptions"))
                }
            }
        }
    }

    private fun handleFeedGroups(groups: List<Group>, listViewMode: Boolean) {
        if (feedGroupsCarouselState != null) {
            feedGroupsCarousel.onRestoreInstanceState(feedGroupsCarouselState)
            feedGroupsCarouselState = null
        }

        binding.itemsList.post {
            if (context == null) {
                // since this part was posted to the next UI cycle, the fragment might have been
                // removed in the meantime
                return@post
            }

            feedGroupsCarousel.listViewMode = listViewMode
            feedGroupsSortMenuItem.showSortButton = groups.size > 1
            feedGroupsSortMenuItem.listViewMode = listViewMode
            feedGroupsCarousel.notifyChanged(FeedGroupCarouselItem.PAYLOAD_UPDATE_LIST_VIEW_MODE)
            feedGroupsSortMenuItem.notifyChanged(GroupsHeader.Companion.PAYLOAD_UPDATE_ICONS)

            // update items here to prevent flickering
            carouselAdapter.apply {
                clear()
                if (listViewMode) {
                    add(FeedGroupAddNewItem())
                    add(
                        FeedGroupCardItem(
                            GROUP_ALL_ID,
                            getString(R.string.all),
                            FeedGroupIcon.WHATS_NEW
                        )
                    )
                } else {
                    add(FeedGroupAddNewGridItem())
                    add(
                        FeedGroupCardGridItem(
                            FeedGroupEntity.Companion.GROUP_ALL_ID,
                            getString(R.string.all),
                            FeedGroupIcon.WHATS_NEW
                        )
                    )
                }
                addAll(groups)
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Contract
    // /////////////////////////////////////////////////////////////////////////

    override fun showLoading() {
        super.showLoading()
        binding.itemsList.animate(false, 100)
    }

    override fun hideLoading() {
        super.hideLoading()
        binding.itemsList.animate(true, 200)
    }

    companion object {
        val JSON_MIME_TYPE = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension("json") ?: "application/octet-stream"
    }
}
