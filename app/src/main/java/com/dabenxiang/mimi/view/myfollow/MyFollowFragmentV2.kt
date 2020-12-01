package com.dabenxiang.mimi.view.myfollow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_follow_v2.*
import kotlinx.android.synthetic.main.fragment_my_follow_v2.view.*
import timber.log.Timber

class MyFollowFragmentV2 : BaseFragment() {

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_SMALL_VIDEO = 1
        const val TAB_POST = 2
    }

    private val viewModel: MyFollowViewModel by viewModels()
    private lateinit var tabLayoutMediator: TabLayoutMediator
    override fun getLayoutId() = R.layout.fragment_my_follow_v2

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(getLayoutId(), container, false)

        view.viewpager.adapter = MyFollowViewPagerAdapterV2(childFragmentManager, lifecycle)
        view.viewpager.offscreenPageLimit =7
        tabLayoutMediator = TabLayoutMediator(view.layout_tab,  view.viewpager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tool_bar.title = getString(R.string.personal_follow)

        tool_bar.setNavigationOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Timber.i("onOptionsItemSelected")
        return when (item.itemId) {
            R.id.action_clean -> {
                Timber.i("onOptionsItemSelected action_clean")
               //TODO
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")

    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ClubTabFragment onDestroy")
        if(::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_MiMI_VIDEO -> getString(R.string.follow_tab_mimi_video)
            TAB_SMALL_VIDEO -> getString(R.string.follow_tab_small_video)
            TAB_POST -> getString(R.string.follow_tab_post)
            else -> null
        }
    }
}
