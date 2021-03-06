package com.dabenxiang.mimi.view.clip

import android.widget.ImageView
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem
import com.dabenxiang.mimi.model.api.vo.InteractiveHistoryItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import kotlinx.coroutines.CoroutineScope

data class ClipFuncItem(
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((VideoItem, Boolean, (Boolean, Int) -> Unit) -> Unit) = { _, _, _ -> },
    val onLikeClick: ((VideoItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((VideoItem) -> Unit) = { _ -> },
    val onMoreClick: ((VideoItem) -> Unit) = { _ -> },
    val onVideoReport: ((Long, Boolean) -> Unit) = { _, _ -> },
    val onVipClick: (() -> Unit) = {},
    val onPromoteClick: (() -> Unit) = {},
    val getM3U8: (VideoItem, Int,  (Int, String, Int) -> Unit) -> Unit = { _, _, _ -> },
    val getInteractiveHistory: (VideoItem, Int,  (Int, InteractiveHistoryItem) -> Unit) -> Unit = { _, _, _ -> },
    val scrollToNext: ((Int) -> Unit) = { _ -> },
    val getDecryptSetting: ((String) -> DecryptSettingItem?) = { _ -> null },
    val decryptCover: (String, DecryptSettingItem, (ByteArray?) -> Unit) -> Unit = { _, _, _ -> }
)