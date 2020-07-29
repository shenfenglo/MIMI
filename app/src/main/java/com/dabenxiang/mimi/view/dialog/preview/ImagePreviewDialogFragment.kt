package com.dabenxiang.mimi.view.dialog.preview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import kotlinx.android.synthetic.main.fragment_dialog_image_preview.*

class ImagePreviewDialogFragment : BaseDialogFragment() {

    private var onCleanDialogListener: OnCleanDialogListener? = null
    private var bitmap: Bitmap? = null

    companion object {

        fun newInstance(
                bitmap: Bitmap?,
                listener: OnCleanDialogListener? = null
        ): ImagePreviewDialogFragment {
            val fragment = ImagePreviewDialogFragment()
            fragment.onCleanDialogListener = listener
            fragment.bitmap = bitmap
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_image_preview
    }

    override fun setupListeners() {
        super.setupListeners()
        img_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bitmap != null) {
            txt_file_invalid.visibility = View.INVISIBLE
            img_logo.visibility = View.INVISIBLE
            Glide.with(App.self)
                    .load(bitmap)
                    .into(ima_bg)
        } else {
            txt_file_invalid.visibility = View.VISIBLE
            img_logo.visibility = View.VISIBLE
        }
    }
}