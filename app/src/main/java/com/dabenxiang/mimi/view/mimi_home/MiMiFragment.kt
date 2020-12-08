package com.dabenxiang.mimi.view.mimi_home

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getScreenSize
import com.dabenxiang.mimi.widget.utility.GeneralUtils.pxToDp
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class MiMiFragment : BaseFragment() {

    companion object {
        private const val ANIMATE_INTERVAL = 6500L
    }

    private val viewModel: MiMiViewModel by viewModels()

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = pxToDp(requireContext(), getScreenSize(requireActivity()).first)
        viewModel.adHeight = (viewModel.adWidth / 7)

        btn_retry.setOnClickListener { viewModel.getMenu() }

        viewModel.inviteVipShake.observe(this, {
            if (layout_invitevip.visibility != View.GONE) {
                if (it == true) {
                    iv_invitevip.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.anim_shake
                        )
                    )
                } else {
                    viewModel.startAnim(ANIMATE_INTERVAL)
                }
            }
        })

        viewModel.menusItems.observe(this, {
            when (it) {
                is Success -> setupUi(it.result)
                is Error -> {
                    onApiError(it.throwable)
                    layout_server_error.visibility = View.VISIBLE
                }
            }
        })

        viewModel.getMenu()
        viewModel.startAnim(ANIMATE_INTERVAL)
    }

    override fun getLayoutId() = R.layout.fragment_mimi_home

    private fun setupUi(menusItems: List<SecondMenuItem>) {
        layout_server_error.visibility = View.INVISIBLE
        viewpager.offscreenPageLimit = menusItems.size
        viewpager.isSaveEnabled = false
        viewpager.adapter = MiMiViewPagerAdapter(this, menusItems)

        TabLayoutMediator(layout_tab, viewpager) { tab, position ->
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab, null)
            val textView = view?.findViewById<TextView>(R.id.tv_title)
            textView?.text = menusItems[position].name
            textView?.takeIf { position == 0 }?.run { setupTextViewSelected(true, this) }
            tab.customView = view
        }.attach()

        layout_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView = tab?.customView?.findViewById(R.id.tv_title) as TextView
                setupTextViewSelected(true, textView)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val textView = tab?.customView?.findViewById(R.id.tv_title) as TextView
                setupTextViewSelected(false, textView)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_invitevip.setOnClickListener {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_inviteVipFragment,
                    null
                )
            )
        }

        iv_invitevip_close.setOnClickListener {
            layout_invitevip.visibility = View.GONE
        }
    }

    private fun setupTextViewSelected(isSelected: Boolean, textView: TextView) {
        if (isSelected) {
            textView.setTypeface(null, Typeface.BOLD)
            textView.setTextColor(requireContext().getColor(R.color.color_black_1))
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
            textView.setTextColor(requireContext().getColor(R.color.color_black_1_50))
        }
    }
}
