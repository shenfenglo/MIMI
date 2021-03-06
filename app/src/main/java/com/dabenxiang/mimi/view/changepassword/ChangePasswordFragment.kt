package com.dabenxiang.mimi.view.changepassword

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ChangePasswordFragment : BaseFragment() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_change_password
    }

    override fun setupObservers() {
        viewModel.currentError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_current.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_current_error.visibility = View.INVISIBLE
            } else {
                edit_current.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_current_error.text = it
                tv_current_error.visibility = View.VISIBLE
            }
        })

        viewModel.newError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_new.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_new_error.visibility = View.INVISIBLE
            } else {
                edit_new.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_new_error.text = it
                tv_new_error.visibility = View.VISIBLE
            }
        })

        viewModel.confirmError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_confirm.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_confirm_error.visibility = View.INVISIBLE
            } else {
                edit_confirm.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_confirm_error.text = it
                tv_confirm_error.visibility = View.VISIBLE
            }
        })

        viewModel.changeResult.observe(viewLifecycleOwner, {
            when (it) {
                is Empty -> {
                    progressHUD.dismiss()
                    navigateTo(NavigateItem.Up)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.btn_confirm -> viewModel.doLoginValidateAndSubmit()
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
        }

        cb_show_current.setOnCheckedChangeListener { _, isChecked ->
            edit_current.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_current.setSelection(edit_current.length())
        }

        cb_show_new.setOnCheckedChangeListener { _, isChecked ->
            edit_new.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_new.setSelection(edit_new.length())
        }

        cb_show_confirm.setOnCheckedChangeListener { _, isChecked ->
            edit_confirm.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_confirm.setSelection(edit_confirm.length())
        }
    }

    override fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = getString(R.string.setting_current_password_error_2),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    override fun initSettings() {
        tv_title.text = getString(R.string.setting_change_password)
        viewModel.current.bindingEditText = edit_current
        viewModel.new.bindingEditText = edit_new
        viewModel.confirm.bindingEditText = edit_confirm
    }
}