package com.dabenxiang.mimi.view.orderresult

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_result.*

class OrderResultFragment : BaseFragment() {

    private val viewModel: OrderResultViewModel by viewModels()

    private val controller = OrderResultEpoxyController()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { }

        tv_step1.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_blue_1_oval
        )

        tv_step2.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_black_1_oval
        )

        line.setBackgroundColor(requireContext().getColor(R.color.color_black_1))
        tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1))

        recycler_order_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_order_result.adapter = controller.adapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_result
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }
}