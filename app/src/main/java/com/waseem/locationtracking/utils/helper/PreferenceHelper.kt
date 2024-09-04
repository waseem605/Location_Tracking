package com.waseem.locationtracking.utils.helper

import android.content.Context
import android.content.SharedPreferences
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.IS_USER_LOGIN

class PreferenceHelper(context: Context) {
    private val appPrefs: SharedPreferences =
        context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = appPrefs.edit()

    object PreferenceVariable {
//        const val AUTH_TOKEN = "auth_token"
        const val USER_EMAIL="USER_EMAIL"
        const val USER_ID="USER_ID"
        const val IS_USER_LOGIN="IS_USER_LOGIN"
    }

    init {
        editor.apply()
    }
//
//    var authToken: String?
//        get() = appPrefs.getString(AUTH_TOKEN, "")
//        set(token) {
//            editor.putString(AUTH_TOKEN, token)
//            editor.apply()
//        }

    fun saveString(key:String,value:String){
        editor.putString(key, value)
        editor.apply()
    }
    fun getString(key:String):String{
        return appPrefs.getString(key,"")?:""
    }


    fun setLoginStatus() {
        editor.putBoolean(IS_USER_LOGIN, true)
        editor.apply()
    }
    fun getLoginStatus():Boolean{
        return appPrefs.getBoolean(IS_USER_LOGIN,false)
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        editor.putFloat("LATITUDE", latitude.toFloat())
        editor.putFloat("LONGITUDE", longitude.toFloat())
        editor.apply()
    }


    fun getLastLocation(): Pair<Double, Double> {
        val latitude = appPrefs.getFloat("LATITUDE", 0.0f).toDouble()
        val longitude = appPrefs.getFloat("LONGITUDE", 0.0f).toDouble()
        return Pair(latitude, longitude)
    }


    fun clearPreference() {
        editor.clear()
        editor.apply()
    }
}