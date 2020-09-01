package com.dabenxiang.mimi.view.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnMeMoreDialogListener
import com.dabenxiang.mimi.extension.handleException
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.MyPostMoreDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils.showToast
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.net.UnknownHostException

abstract class BaseFragment : Fragment() {

    companion object {
        const val PERMISSION_EXTERNAL_REQUEST_CODE = 637
        const val PERMISSION_CAMERA_REQUEST_CODE = 699
    }

    open var mainViewModel: MainViewModel? = null
    var progressHUD: KProgressHUD? = null

    var mView: View? = null
    var firstCreateView = false

    //    val locationPermissions = arrayOf(
//        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.ACCESS_FINE_LOCATION
//    )

    val externalPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    open var permissions = externalPermissions + cameraPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { mainViewModel = ViewModelProvider(it).get(MainViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        takeIf { mView == null }?.let {
            firstCreateView = true
            mView = inflater.inflate(getLayoutId(), container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusBarVisibility()

        progressHUD = KProgressHUD.create(context)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)

        activity?.bottom_navigation?.visibility = bottomNavigationVisibility

        takeIf { firstCreateView }?.run {
            setupFirstTime()
            firstCreateView = false
        }

        setupListeners()
        setupObservers()

        if (arguments?.getBoolean(PlayerActivity.KEY_IS_FROM_PLAYER) == true) {
            mainViewModel?.isFromPlayer = true
        }
    }

    abstract fun getLayoutId(): Int

    abstract fun setupObservers()

    abstract fun setupListeners()

    open fun setupFirstTime() {}

    open fun statusBarVisibility() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    open fun initSettings() {}

    open val bottomNavigationVisibility: Int = View.VISIBLE

    open fun navigateTo(item: NavigateItem) {
        lifecycleScope.launch {
            navigationTaskJoinOrRun {
                findNavController().also { navController ->
                    when (item) {
                        NavigateItem.Up -> navController.navigateUp()
                        is NavigateItem.PopBackStack -> navController.popBackStack(
                            item.fragmentId,
                            item.inclusive
                        )
                        is NavigateItem.Destination -> {
                            if (item.bundle == null) {
                                navController.navigate(item.action)
                            } else {
                                navController.navigate(item.action, item.bundle)
                            }
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private var navigationTask: Deferred<Any>? = null

    private suspend fun navigationTaskJoinOrRun(block: suspend () -> Any): Any {
        navigationTask?.let {
            return it.await()
        }

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                navigationTask = null
            }

            navigationTask = newTask
            newTask.await()
        }
    }

    fun backToDesktop() {
        activity?.moveTaskToBack(true)
    }

    fun useAdultTheme(value: Boolean) {
        activity?.also {
            (it as MainActivity).setAdult(value)
        }
    }

    open fun onApiError(
        throwable: Throwable,
        onHttpErrorBlock: ((ExceptionResult.HttpError) -> Unit)? = null
    ) {
        when (val errorHandler =
            throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
            is ExceptionResult.RefreshTokenExpired -> logoutLocal()
            is ExceptionResult.HttpError -> {
                if (onHttpErrorBlock == null) handleHttpError(errorHandler)
                else onHttpErrorBlock(errorHandler)
            }
            is ExceptionResult.Crash -> {
                if (errorHandler.throwable is UnknownHostException) {
                    showCrashDialog(HttpErrorMsgType.CHECK_NETWORK)
                } else {
                    showToast(requireContext(), errorHandler.throwable.toString())
                }
            }
        }
    }

    open fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = errorHandler.httpExceptionItem.errorItem.toString(),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun showCrashDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = when (type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    fun getNotGrantedPermissions(permissions: Array<String>): ArrayList<String> {
        val requestList = arrayListOf<String>()
        permissions.indices.forEach {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[it]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestList += permissions[it]
            }
        }
        return requestList
    }

    private fun logoutLocal() {
        view?.let {
            mainViewModel?.logoutLocal()
        }
    }

    fun checkStatus(onConfirmed: () -> Unit) {
        mainViewModel?.checkStatus(onConfirmed)
    }

    fun onMoreClick(item: MemberPostItem, items: ArrayList<MemberPostItem>, onEdit: (BaseMemberPostItem) -> Unit){
        val isMe = mainViewModel?.accountManager?.getProfile()?.userId == item.creatorId
        if (isMe) {
            showMeMoreDialog(item, items, onEdit)
        } else {
            showMoreDialog(item)
        }
    }

    private var meMoreDialog: MyPostMoreDialogFragment? = null
    private fun showMeMoreDialog(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
        onEdit: (BaseMemberPostItem) -> Unit
    ) {
        val onMeMoreDialogListener = object : OnMeMoreDialogListener {
            override fun onCancel() {
                meMoreDialog?.dismiss()
            }

            override fun onDelete(item: BaseMemberPostItem) {
                GeneralDialog.newInstance(
                    GeneralDialogData(
                        titleRes = R.string.is_post_delete,
                        messageIcon = R.drawable.ico_default_photo,
                        secondBtn = getString(R.string.btn_confirm),
                        secondBlock = { mainViewModel?.deletePost(item as MemberPostItem, items) },
                        firstBtn = getString(R.string.cancel),
                        isMessageIcon = false
                    )
                ).show(requireActivity().supportFragmentManager)
            }

            override fun onEdit(item: BaseMemberPostItem) {
                onEdit(item)
                meMoreDialog?.dismiss()
            }
        }
        meMoreDialog = MyPostMoreDialogFragment.newInstance(item, onMeMoreDialogListener)
                .also {
                    it.show(
                        requireActivity().supportFragmentManager,
                        MoreDialogFragment::class.java.simpleName
                    )
                }
    }

    private var moreDialog: MoreDialogFragment? = null
    private fun showMoreDialog(item: MemberPostItem){
        val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
            override fun onProblemReport(item: BaseMemberPostItem) {
                moreDialog?.dismiss()
                checkStatus { (requireActivity() as MainActivity).showReportDialog(item) }
            }

            override fun onCancel() {
                moreDialog?.dismiss()
            }
        }
        moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }

}
