package com.dabenxiang.mimi.view.club.pic

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.post.ClubPostPagerAdapter
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_text.*
import kotlinx.android.synthetic.main.fragment_order.viewPager
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubPicFragment : BaseFragment() {

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
        fun createBundle(item: MemberPostItem, position: Int = 0): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
                it.putSerializable(KEY_POSITION, position)
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_pic

    override fun setupObservers() {

    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberPostItem = arguments?.getSerializable(PictureDetailFragment.KEY_DATA) as MemberPostItem

        tv_title.text = getString(R.string.home_tab_picture)

        viewPager.adapter =
            ClubPostPagerAdapter(
                this,
                memberPostItem!!,
                AdultTabType.PICTURE
            )
        viewPager.isSaveEnabled = false

        val title: ArrayList<String> = arrayListOf(getString(R.string.picture_detail_title), getString(R.string.comment))

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = title[position]
        }.attach()

        val position = arguments?.getInt(KEY_POSITION, 0)
        viewPager.currentItem = position!!
    }
}