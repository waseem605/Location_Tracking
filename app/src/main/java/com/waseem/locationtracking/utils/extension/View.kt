package com.waseem.locationtracking.utils.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.waseem.locationtracking.R

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.setSafeOnClickListener(onClickAction: (View) -> Unit) {
    setOnClickListener {
        it?.let {
            if (it.context.isInternetAvailable()) {
                onClickAction(it)
            } else {
                snackBar(resources.getString(R.string.no_internet_available))
            }
        }
    }
}

fun View.snackBar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).show()
}
