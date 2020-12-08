package com.dabenxiang.mimi.callback

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType

class MemberPostFuncItem(
    val onItemClick: (MemberPostItem) -> Unit = { _ -> },
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFollowClick: ((MemberPostItem, List<MemberPostItem>, Boolean, ((Boolean) -> Unit)) -> Unit) = { _, _, _, _ -> },
    val onLikeClick: ((MemberPostItem, Boolean, ((Boolean, Int) -> Unit)) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: (MemberPostItem, Boolean, ((Boolean, Int) -> Unit)) -> Unit = { _, _, _ -> }
)