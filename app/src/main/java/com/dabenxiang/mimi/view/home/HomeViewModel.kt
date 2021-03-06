package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.club.ClubDataSource
import com.dabenxiang.mimi.view.home.club.ClubFactory
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostFactory
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import com.dabenxiang.mimi.view.home.postfollow.PostFollowFactory
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import com.dabenxiang.mimi.view.home.video.VideoFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

class HomeViewModel : BaseViewModel() {

    companion object {
        const val PAGING_LIMIT = 20
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
    }

    var lastListIndex = 0 // 垂直recycler view 跳出後的最後一筆資料
    var lastPosition = 0

    private var _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private var _carouselResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBaseItem<List<CategoryBanner>>>>>()
    val carouselResult: LiveData<Pair<Int, ApiResult<ApiBaseItem<List<CategoryBanner>>>>> =
        _carouselResult

    private var _videosResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<StatisticsItem>>>>>()
    val videosResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<StatisticsItem>>>>> =
        _videosResult

    private var _clipsResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>>()
    val clipsResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>> =
        _clipsResult

    private var _pictureResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>>()
    val pictureResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>> =
        _pictureResult

    private var _clubResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberClubItem>>>>>()
    val clubResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberClubItem>>>>> =
        _clubResult

    private val _postFollowItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val postFollowItemListResult: LiveData<PagedList<MemberPostItem>> = _postFollowItemListResult

    private val _clipPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val clipPostItemListResult: LiveData<PagedList<MemberPostItem>> = _clipPostItemListResult

    private val _picturePostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val picturePostItemListResult: LiveData<PagedList<MemberPostItem>> = _picturePostItemListResult

    private val _textPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val textPostItemListResult: LiveData<PagedList<MemberPostItem>> = _textPostItemListResult

    private val _clubItemListResult = MutableLiveData<PagedList<MemberClubItem>>()
    val clubItemListResult: LiveData<PagedList<MemberClubItem>> = _clubItemListResult

    private val _totalCountResult = MutableLiveData<Pair<CategoryType,Int>>()
    val totalCountResult: LiveData<Pair<CategoryType,Int>> = _totalCountResult

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private val _inviteVipShake = MutableLiveData<Boolean>()
    val inviteVipShake: LiveData<Boolean> = _inviteVipShake

    fun loadNestedStatisticsListForCarousel(
        position: Int,
        src: HomeTemplate.Carousel,
        isAdult: Boolean = false
    ) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().fetchHomeBanner(if (isAdult) 2 else 1)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }.flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _carouselResult.value = Pair(position, it) }
        }
    }

    fun loadNestedStatisticsList(position: Int, src: HomeTemplate.Statistics) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().statisticsHomeVideos(
                    category = src.categories,
                    isAdult = src.isAdult,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videosResult.value = Pair(position, it) }
        }
    }

    fun loadNestedClipList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersPost(
                    type = PostType.VIDEO,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clipsResult.value = Pair(position, it) }
        }
    }

    fun loadNestedPictureList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersPost(
                    type = PostType.IMAGE,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _pictureResult.value = Pair(position, it) }
        }
    }

    fun loadNestedClubList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClubPost(
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubResult.value = Pair(position, it) }
        }
    }

    fun clubFollow(item: MemberClubItem, isFollow: Boolean, update: ((Boolean) -> Unit)) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(item.id)
                    else -> apiRepository.cancelFollowClub(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(isFollow))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    var totalCount: Int = 0
    fun followMember(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach {
                    if (it.creatorId == item.creatorId) {
                        it.isFollow = isFollow
                    }
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, isLike: Boolean, update: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(item.likeCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isLike, it.result)
//                            getAllOtherPosts(lastPosition)
                        }
                    }
                }
        }
    }

    fun favoritePost(item: MemberPostItem, isFavorite: Boolean, update: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = isFavorite
                if (isFavorite) item.favoriteCount++ else item.favoriteCount--
                emit(ApiResult.success(item.favoriteCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isFavorite, it.result)
                        }
                    }
                }
        }
    }

    fun getAllOtherPosts(lastPosition: Int) {
        if (lastPosition != 1) getVideos(null, true)
        if (lastPosition != 2) getPostFollows()
        if (lastPosition != 3) getClipPosts()
        if (lastPosition != 4) getPicturePosts()
        if (lastPosition != 5) getTextPosts()
        if (lastPosition != 6) getClubs()
    }

    fun getVideos(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            getVideoPagingItems(category, isAdult).asFlow()
                .collect { _videoList.value = it }
        }
    }

    fun getPostFollows() {
        viewModelScope.launch {
            getPostFollowPagingItems().asFlow()
                .collect { _postFollowItemListResult.value = it }
        }
    }

    fun getClipPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.VIDEO).asFlow()
                .collect { _clipPostItemListResult.value = it }
        }
    }

    fun getPicturePosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.IMAGE).asFlow()
                .collect { _picturePostItemListResult.value = it }
        }
    }

    fun getTextPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.TEXT).asFlow()
                .collect { _textPostItemListResult.value = it }
        }
    }

    private fun getPostFollowPagingItems(): LiveData<PagedList<MemberPostItem>> {
        val postFollowDataSource =
            PostFollowDataSource(HomePagingCallBack(CategoryType.FOLLOW), viewModelScope, domainManager, adWidth, adHeight)
        val pictureFactory = PostFollowFactory(postFollowDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    private fun getMemberPostPagingItems(postType: PostType): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource =
            MemberPostDataSource(
                HomePagingCallBack(CategoryType.valueOf(postType.name)),
                viewModelScope,
                domainManager,
                postType,
                adWidth,
                adHeight
            )
        val pictureFactory = MemberPostFactory(pictureDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    private fun getVideoPagingItems(
        category: String?,
        isAdult: Boolean
    ): LiveData<PagedList<BaseVideoItem>> {
        val videoDataSource = VideoDataSource(
            isAdult,
            category,
            viewModelScope,
            domainManager,
            HomePagingCallBack(CategoryType.VIDEO_ON_DEMAND),
            adWidth,
            adHeight,
            true
        )
        val videoFactory = VideoFactory(videoDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(videoFactory, config).build()
    }

    fun getClubs() {
        viewModelScope.launch {
            getClubPagingItems().asFlow()
                .collect { _clubItemListResult.value = it }
        }
    }

    private fun getClubPagingItems(): LiveData<PagedList<MemberClubItem>> {
        val clubDataSource = ClubDataSource(
            HomePagingCallBack(CategoryType.CLUB), viewModelScope, domainManager, adWidth, adHeight
        )
        val clubFactory = ClubFactory(clubDataSource)
        val config = PagedList.Config.Builder().setPrefetchDistance(4).build()
        return LivePagedListBuilder(clubFactory, config).build()
    }

    fun clearLiveDataValue() {
        _totalCountResult.value = null
    }

    inner class HomePagingCallBack(private val type: CategoryType) : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {

        }

        override fun onTotalCount(count: Long) {
            _totalCountResult.postValue(Pair(type, count.toInt()))
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            totalCount = if (isInitial) count.toInt()
            else totalCount.plus(count.toInt())
            if (isInitial) cleanRemovedPosList()
        }
    }

    fun startAnim(inverval: Long){
        viewModelScope.launch {
                _inviteVipShake.postValue(true)
                delay(inverval)
                _inviteVipShake.postValue(false)
        }
    }
}