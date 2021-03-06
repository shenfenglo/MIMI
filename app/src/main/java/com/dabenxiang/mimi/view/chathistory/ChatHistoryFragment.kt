package com.dabenxiang.mimi.view.chathistory

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Loaded
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import kotlinx.android.synthetic.main.fragment_chat_history.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ChatHistoryFragment : BaseFragment() {

    private val viewModel: ChatHistoryViewModel by viewModels()

    private val adapter by lazy { ChatHistoryAdapter(listener) }
    private val listener = object : ChatHistoryAdapter.EventListener {
        override fun onClickListener(item: ChatListItem, position: Int) {
            val bundle = ChatContentFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_chatHistoryFragment_to_chatContentFragment,
                    bundle
                )
            )
        }

        override fun onGetAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR_CS)
        }

    }
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val NO_DATA = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun setupFirstTime() {
        viewModel.getChatList()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_history
    }

    override fun setupObservers() {
        viewModel.chatHistory.observe(viewLifecycleOwner, { adapter.submitList(it) })

        viewModel.pagingResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loaded,
                is Error -> {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
            }
        }.also {
            tv_back.setOnClickListener(it)
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getChatList()
        }
    }

    override fun initSettings() {
        super.initSettings()
        tv_title.setText(R.string.title_chat_history)
        rv_content.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rv_content.adapter = adapter
        swipeRefreshLayout.setColorSchemeColors(swipeRefreshLayout.context.getColor(R.color.color_red_1))
    }

    private fun refreshUi(size: Int) {
        rv_content.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }
    }
}