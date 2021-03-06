package com.dabenxiang.mimi.view.dialog.comment

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnMeMoreDialogListener
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_my_post_more.*

class MyPostMoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(
            item: BaseMemberPostItem? = null,
            listener: OnMeMoreDialogListener
        ): MyPostMoreDialogFragment {
            val fragment = MyPostMoreDialogFragment()
            fragment.item = item
            fragment.listener = listener
            return fragment
        }
    }

    var item: BaseMemberPostItem? = null
    var listener: OnMeMoreDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_my_post_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_delete.setOnClickListener {
            listener?.onDelete(item!!)
            dismiss()
        }

        tv_cancel.setOnClickListener {
            listener?.onCancel()
        }

        tv_edit.setOnClickListener {
            listener?.onEdit(item!!)
        }

        background.setOnClickListener { dismiss() }
    }
}