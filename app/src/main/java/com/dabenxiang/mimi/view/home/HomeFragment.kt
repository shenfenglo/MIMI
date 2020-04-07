package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.HomeRecommendAdapter
import com.dabenxiang.mimi.view.adapter.LeaderboardAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeFragment : BaseFragment() {

    private val viewModel by viewModel<HomeViewModel>()

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.also { activity ->
            activity.window.statusBarColor = activity.getColor(R.color.color_bar)

            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                recyclerview_leaderboard.layoutManager = layoutManager
            }


            GridLayoutManager(activity, 2).also { layoutManager ->
                recyclerview_cate1.layoutManager = layoutManager
            }
        }

        //viewModel.loadHomeCategories()

        for (i in 1..10) {
            layout_top_tap.addTab(layout_top_tap.newTab().setText("第${i}層"))
        }

        layout_top_tap.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                Timber.d("onTabReselected: ${tab?.position}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Timber.d("onTabUnselected: ${tab?.position}")
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                Timber.d("onTabSelected: ${tab?.position}")
            }
        })


        recyclerview_leaderboard.adapter = LeaderboardAdapter()
        recyclerview_cate1.adapter = HomeRecommendAdapter()
    }

    override fun setupObservers() {
        Timber.d("${HomeFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${HomeFragment::class.java.simpleName}_setupListeners")
    }
}