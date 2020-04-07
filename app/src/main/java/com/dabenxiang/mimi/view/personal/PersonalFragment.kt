package com.dabenxiang.mimi.view.personal

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class PersonalFragment : BaseFragment() {
    private val viewModel by viewModel<PersonalViewModel>()
    private val isLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
        // todo
        /*Handler().postDelayed({
            Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_loginFragment)
        },1000)*/
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun setupObservers() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupListeners")

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btnTopup -> GeneralUtils.showToast(context!!, "btnTopup")
                R.id.btnFavorite -> GeneralUtils.showToast(context!!, "btnFavorite")
                R.id.btnTopupHistory -> GeneralUtils.showToast(context!!, "btnTopupHistory")
                R.id.btnChatHistory -> GeneralUtils.showToast(context!!, "btnChatHistory")
                R.id.btnSetting -> GeneralUtils.showToast(context!!, "btnSetting")
                R.id.btnLogout -> GeneralUtils.showToast(context!!, "btnLogout")
            }
        }.also {
            btnTopup.setOnClickListener(it)
            btnFavorite.setOnClickListener(it)
            btnTopupHistory.setOnClickListener(it)
            btnChatHistory.setOnClickListener(it)
            btnSetting.setOnClickListener(it)
            btnLogout.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        when(isLogin) {
            true -> {
                item_is_Login.visibility = View.VISIBLE
                item_is_not_Login.visibility = View.GONE
            }
            false -> {
                item_is_Login.visibility = View.GONE
                item_is_not_Login.visibility = View.VISIBLE
            }
        }

        // todo: for testing
        tvName.text = "好大一棵洋梨"
        tvCoco.text = "200"
        tvNew.text = "N"
        tvVersion.text = "v1.0.0"
        tvContent1.text = "文字內容文字內容"
        tvContent2.text = "文字內容文字內容文字內容文字內容文字內容文字內容文字內容文字內容"
    }
}