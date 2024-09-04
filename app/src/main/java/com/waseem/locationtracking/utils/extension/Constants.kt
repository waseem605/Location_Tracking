package com.waseem.locationtracking.utils.extension

import android.os.Looper

const val BASE_URL = "https://dummyjson.com/"
const val LOGIN_EP = "auth/login"
const val All_USERS_EP = "users"
const val All_CARTS_EP = "cart"

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()