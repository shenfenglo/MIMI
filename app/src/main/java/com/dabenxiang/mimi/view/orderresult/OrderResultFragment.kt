package com.dabenxiang.mimi.view.orderresult

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.orderresult.itemview.OrderResultFailedItemView
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_order_result.*
import java.util.*
import kotlin.concurrent.timerTask

class OrderResultFragment : BaseFragment() {

    companion object {
        private const val KEY_ERROR = "error"
        private const val DELAY_TOP_UP_TIME = 60000L

        fun createBundle(isError: Boolean): Bundle {
            return Bundle().also { it.putBoolean(KEY_ERROR, isError) }
        }
    }

    private val viewModel: OrderResultViewModel by viewModels()

    private var topUpTimer: Timer? = null
    private var countdownTimer: CountDownTimer? = null

    private val epoxyController by lazy {
        OrderResultEpoxyController(requireContext(), failedListener, successListener)
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_step1.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_blue_1_oval
        )

        tv_step2.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_black_1_oval
        )

        line.setBackgroundColor(requireContext().getColor(R.color.color_black_1_20))
        tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1))
        tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1_30))

        tv_step1.text = "1"
        tv_step2.text = "2"

        recycler_order_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_order_result.adapter = epoxyController.adapter

        if (arguments?.getBoolean(KEY_ERROR) == true) {
            setupStepUi(false)
            epoxyController.setData(OrderPayloadItem())
        } else {
            startTopUpTimer()
            epoxyController.setData(null)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_result
    }

    override fun setupObservers() {
        mainViewModel?.orderItem?.observe(viewLifecycleOwner, {
            if (it != null) {
                setupStepUi(it.orderPayloadItem?.isSuccessful)
                stopTopUpTimer()

                if (viewModel.isOpenPaymentWebView(it.orderPayloadItem)) {
                    startCountdownTimer(it.orderPayloadItem)
                } else {
                    epoxyController.setData(it.orderPayloadItem)
                }
            }
        })
    }

    override fun setupListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(this) { }
    }

    override fun onDestroyView() {
        stopTopUpTimer()
        stopCountdownTimer()
        super.onDestroyView()
    }

    private val failedListener = object : OrderResultFailedItemView.OrderResultFailedListener {
        override fun onConfirm() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }
    }

    private val successListener = object : OrderResultSuccessListener {
        override fun onConfirm() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_orderFragment))
        }

        override fun onClose() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }

        override fun onOpenWebView(url: String) {
            GeneralUtils.openWebView(requireContext(), url)
        }
    }

    private fun setupStepUi(isSuccessful: Boolean?) {
        if (isSuccessful == true) {
            tv_step1.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_blue_1_oval
            )

            tv_step2.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_blue_1_oval
            )

            line.setBackgroundColor(requireContext().getColor(R.color.color_blue_2))
            tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1_30))
            tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1))

            val img = ContextCompat.getDrawable(requireContext(), R.drawable.ico_cheched)
            tv_step1.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
            tv_step1.text = ""

        } else {
            tv_step1.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_black_1_oval
            )

            tv_step2.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_black_1_oval
            )

            line.setBackgroundColor(requireContext().getColor(R.color.color_black_1_20))
            tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1_30))
            tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1_30))
        }
    }

    private fun startTopUpTimer() {
        val task = timerTask {
            epoxyController.setData(OrderPayloadItem())
        }
        topUpTimer = Timer()
        topUpTimer?.schedule(task, DELAY_TOP_UP_TIME)
    }

    private fun stopTopUpTimer() {
        topUpTimer?.cancel()
    }

    private fun startCountdownTimer(item: OrderPayloadItem?) {
        countdownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                item?.also {
                    it.countdown = (millisUntilFinished / 1000).toInt() + 1
                    epoxyController.setData(it)
                }
            }

            override fun onFinish() {
                item?.also {
                    it.isCountdownVisible = false
                    epoxyController.setData(it)
                    successListener.onOpenWebView(it.paymentUrl)
                }
            }
        }
        countdownTimer?.start()
    }

    private fun stopCountdownTimer() {
        countdownTimer?.cancel()
    }

}