package com.dabenxiang.mimi.widget.collapsing

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

abstract class BehaviorRules(
        context: Context?,
        attrs: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var views: List<RuledView> = emptyList()
    private var lastChildHeight = -1
    private var needToUpdateHeight: Boolean = true

    override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
    ): Boolean {
        if (views.isEmpty()) views = child.setUpViews()
        val progress = calcProgress(parent)
        tryToInitHeight(child, dependency, progress)
        views.forEach { performRules(offsetView = it, percent = progress) }
        return true
    }

    override fun onMeasureChild(
            parent: CoordinatorLayout, child: View, parentWidthMeasureSpec: Int,
            widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int
    ): Boolean {

        val canUpdateHeight = canUpdateHeight(calcProgress(parent))
        if (canUpdateHeight) {
            parent.post {
                val newChildHeight = child.height
                if (newChildHeight != lastChildHeight) {
                    lastChildHeight = newChildHeight
                    setUpAppbarHeight(child, parent)
                }
            }
        } else {
            needToUpdateHeight = true
        }
        return super.onMeasureChild(
                parent, child, parentWidthMeasureSpec,
                widthUsed, parentHeightMeasureSpec, heightUsed
        )
    }

    protected abstract fun calcAppbarHeight(child: View): Int
    protected abstract fun View.setUpViews(): List<RuledView>
    protected abstract fun View.provideAppbar(): AppBarLayout
    protected abstract fun View.provideCollapsingToolbar(): CollapsingToolbarLayout
    protected open fun canUpdateHeight(progress: Float): Boolean = true

    private fun calcProgress(parent: CoordinatorLayout): Float {
        val appbar = parent.provideAppbar()
        val scrollRange = appbar.totalScrollRange.toFloat()
        val scrollY = Math.abs(appbar.y)
        val scroll = 1 - scrollY / scrollRange
        return when {
            scroll.isNaN() -> 1f
            else -> scroll
        }
    }

    private fun setUpAppbarHeight(child: View, parent: ViewGroup) {
        parent.provideCollapsingToolbar().setHeight(calcAppbarHeight(child))
    }

    private fun tryToInitHeight(child: View, dependency: View, scrollPercent: Float) {
        if (needToUpdateHeight && canUpdateHeight(scrollPercent)) {
            setUpAppbarHeight(child, dependency as ViewGroup)
            needToUpdateHeight = false
        }
    }

    private fun performRules(offsetView: RuledView, percent: Float) {
        val view = offsetView.view
        val details = offsetView.details
        offsetView.rules.forEach { rule ->
            rule.manage(percent, details, view)
        }
    }
}