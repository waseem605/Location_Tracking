package com.waseem.locationtracking.utils.services


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class RestartBackgroundService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Broadcast Listened", "Service tried to stop")
        Log.i("Broadcast Listened", "${intent?.action}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, BGLocationService::class.java))
        } else {
            context?.startService(Intent(context, BGLocationService::class.java))
        }

    }
}