package com.dabenxiang.mimi.view.post.article

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ArticleItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogFragment
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDialogListener
import com.dabenxiang.mimi.view.dialog.chooseuploadmethod.ChooseUploadMethodDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.EDIT
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post_article.*
import kotlinx.android.synthetic.main.item_setting_bar.*


class PostArticleFragment : BaseFragment() {

    private val viewModel: PostArticleViewModel by viewModels()

    private var haveMainTag = false

    companion object {
        private const val TITLE_LIMIT = 60
        private const val CONTENT_LIMIT = 2000
        private const val HASHTAG_LIMIT = 20
        private const val HASHTAG_TEXT_LIMIT = 10
        private const val INIT_VALUE = 0
        const val UPLOAD_ARTICLE = "upload_article"
        const val TITLE = "title"
        const val REQUEST = "request"
        const val TAG = "tag"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_article
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun setupObservers() {
        viewModel.clubItemResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    txt_clubName.text = it.result.first().title
                    txt_hashtagName.text = it.result.first().tag

                    txt_placeholder.visibility = View.GONE
                    txt_clubName.visibility = View.VISIBLE
                    txt_hashtagName.visibility = View.VISIBLE

                    if (LruCacheUtils.getLruCache(it.result.first().avatarAttachmentId.toString()) == null) {
                        viewModel.getBitmap(it.result.first().avatarAttachmentId.toString())
                    } else {
                        val bitmap = LruCacheUtils.getLruCache(it.result.first().avatarAttachmentId.toString())
                        Glide.with(requireContext()).load(bitmap).circleCrop().into(iv_avatar)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.bitmapResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    val bitmap = LruCacheUtils.getLruCache(it.result)
                    Glide.with(requireContext()).load(bitmap).circleCrop().into(iv_avatar)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        edt_hashtag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > HASHTAG_TEXT_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_hashtag.setText(content)
                        edt_hashtag.setSelection(content.length)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        edt_title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > TITLE_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_title.setText(content)

                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_titleCount.text = String.format(getString(R.string.typing_count, s?.length,
                    TITLE_LIMIT
                ))
            }
        })

        edt_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > CONTENT_LIMIT) {
                        val content = it.toString().dropLast(1)
                        edt_title.setText(content)

                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txt_contentCount.text = String.format(getString(R.string.typing_count, s?.length,
                    CONTENT_LIMIT
                ))
            }
        })

        clubLayout.setOnClickListener {
            ChooseClubDialogFragment.newInstance(chooseClubDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    ChooseUploadMethodDialogFragment::class.java.simpleName
                )
            }
        }

        edt_hashtag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                if (chipGroup.size == HASHTAG_LIMIT) {
                    Toast.makeText(requireContext(), R.string.post_warning_tag_limit, Toast.LENGTH_SHORT).show()
                } else {
                    val tag = edt_hashtag.text.toString()
                    if (isTagExist(tag)) {
                        Toast.makeText(requireContext(), R.string.post_tag_already_have, Toast.LENGTH_SHORT).show()
                    } else {
                        addTag(tag)
                        edt_hashtag.text.clear()
                    }
                }
            }
            false
        }

        tv_back.setOnClickListener {
            handleBackEvent()
        }

        tv_clean.setOnClickListener {
            val isEdit = arguments?.getBoolean(EDIT)

            val title = edt_title.text.toString()
            val content = edt_content.text.toString()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.isBlank()) {
                Toast.makeText(requireContext(), R.string.post_warning_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (chipGroup.childCount == 0) {
                Toast.makeText(requireContext(), R.string.post_warning_tag, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestContent = ArticleItem(content)

            val tags = arrayListOf<String>()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i)
                chip as Chip
                tags.add(chip.text.toString())
            }

            val jsonString = Gson().toJson(requestContent)

            val bundle = Bundle()
            bundle.putBoolean(UPLOAD_ARTICLE, true)
            bundle.putString(TITLE, title)
            bundle.putString(REQUEST, jsonString)
            bundle.putStringArrayList(TAG, tags)
            if (isEdit != null) {
                val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(R.id.action_postArticleFragment_to_myPostFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_postArticleFragment_to_adultHomeFragment, bundle)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            handleBackEvent()
        }
    }

    private fun handleBackEvent() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    findNavController().navigateUp()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    override fun initSettings() {
        super.initSettings()

        val isEdit = arguments?.getBoolean(EDIT)

        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)
        tv_clean.isEnabled = true

        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)

        txt_titleCount.text = String.format(getString(R.string.typing_count,
            INIT_VALUE,
            TITLE_LIMIT
        ))
        txt_contentCount.text = String.format(getString(R.string.typing_count,
            INIT_VALUE,
            CONTENT_LIMIT
        ))
        txt_hashtagCount.text = String.format(getString(R.string.typing_count,
            INIT_VALUE,
            HASHTAG_LIMIT
        ))

        if (isEdit != null) {
            tv_title.text = getString(R.string.edit_post_title)
            setUI()
        } else {
            tv_title.text = getString(R.string.post_title)
        }

        useAdultTheme(false)
    }

    private fun setUI() {
        val item = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
        val articleItem = Gson().fromJson(item.content, ArticleItem::class.java)

        edt_title.setText(item.title)
        edt_content.setText(articleItem.text)

        for (tag in item.tags!!) {
            addEditTag(tag)
        }

        txt_titleCount.text = String.format(getString(R.string.typing_count,
            item.title.length,
            TITLE_LIMIT
        ))
        txt_contentCount.text = String.format(getString(R.string.typing_count,
            articleItem.text.length,
            CONTENT_LIMIT
        ))
        txt_hashtagCount.text = String.format(getString(R.string.typing_count,
            item.tags?.size,
            HASHTAG_LIMIT
        ))

        haveMainTag = true
    }

    private val chooseClubDialogListener = object : ChooseClubDialogListener {
        override fun onChooseClub(item: MemberClubItem) {
            txt_clubName.text = item.title
            txt_hashtagName.text = item.tag

            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(requireContext())
                .load(bitmap)
                .circleCrop()
                .into(iv_avatar)

            if (chipGroup.size == HASHTAG_LIMIT) {
                Toast.makeText(requireContext(), R.string.post_warning_tag_limit, Toast.LENGTH_SHORT).show()
            } else {
                addTag(item.tag, true)
                txt_placeholder.visibility = View.GONE
                txt_clubName.visibility = View.VISIBLE
                txt_hashtagName.visibility = View.VISIBLE
            }
        }
    }

    private fun addEditTag(tag: String) {
        val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip_item, chipGroup, false) as Chip
        chip.text = tag
        chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
        chip.chipBackgroundColor =
            ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))

        if (chipGroup.size >= 1) {
            chip.closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.btn_close_circle_small_black_n)
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
            }
        } else {
            viewModel.getClub(tag)
        }

        chipGroup.addView(chip)
        setTagCount()
    }

    private fun addTag(tag: String, isMainTag: Boolean = false) {
        val chip = LayoutInflater.from(requireContext()).inflate(R.layout.chip_item, chipGroup, false) as Chip
        chip.text = tag
        chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
        chip.chipBackgroundColor =
            ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))

        if (isMainTag) {
            if (haveMainTag) {
                val mainTag = chipGroup[0] as Chip
                mainTag.text = tag
            } else {
                haveMainTag = true

                if (chipGroup.isEmpty()) {
                    chipGroup.addView(chip)
                    setTagCount()
                } else {
                    val chipList = arrayListOf<Chip>()
                    for (i in 0 until chipGroup.size) {
                        val chipItem = chipGroup[i] as Chip
                        chipList.add(chipItem)
                    }

                    chipGroup.removeAllViews()
                    chipGroup.addView(chip)
                    for (tagItem in chipList) {
                        chipGroup.addView(tagItem)
                    }
                }
            }
        } else {
            chip.closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.btn_close_circle_small_black_n)
            chip.isCloseIconVisible = true
            chip.setCloseIconSizeResource(R.dimen.dp_24)
            chip.setOnCloseIconClickListener {
                chipGroup.removeView(it)
            }
            chipGroup.addView(chip)
            setTagCount()
        }
    }

    private fun setTagCount() {
        txt_hashtagCount.text = String.format(getString(R.string.typing_count, chipGroup.size,
            HASHTAG_LIMIT
        ))
    }

    private fun isTagExist(tag: String): Boolean  {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text == tag) {
                return true
            }
        }
        return false
    }
}