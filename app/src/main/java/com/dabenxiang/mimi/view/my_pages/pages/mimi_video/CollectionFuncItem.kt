package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem

data class CollectionFuncItem (
        val getDecryptSetting: ((String) -> DecryptSettingItem?) = { _ -> null },
        val decryptCover: (String, DecryptSettingItem, (ByteArray?) -> Unit) -> Unit = { _, _, _ -> }
)
