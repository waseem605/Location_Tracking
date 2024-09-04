package com.waseem.locationtracking.utils

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.waseem.locationtracking.databinding.MymapInfoWindowBinding
import com.waseem.locationtracking.ui.tracking.LocationSingleTrackingActivity

class CustomMapInfoWindow(private val mContext: Activity, private val id: String, private val email:String)  : GoogleMap.InfoWindowAdapter {
    private val binding: MymapInfoWindowBinding = MymapInfoWindowBinding.inflate(LayoutInflater.from(mContext))

    private fun setInfoWindowText(marker: Marker) {
        val title = marker.title
        binding.infoWindowTitleTv.text = email
//        binding.infoWindowTimeTv.text = time
//        binding.infoWindowLocationTv.text = location
        binding.trackBtn.setOnClickListener {
            val mIntent = Intent(mContext,LocationSingleTrackingActivity::class.java)
            mIntent.putExtra("UserID",id)
            mContext.startActivity(mIntent)
        }
    }

    override fun getInfoWindow(p0: Marker): View {
        setInfoWindowText(p0)
        return binding.root
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindowText(p0)
        return binding.root
    }
}