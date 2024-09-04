package com.waseem.locationtracking.utils.helper

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.waseem.locationtracking.databinding.DialogAlertBoxBinding
import com.waseem.locationtracking.databinding.DialogLocationPermissionGuideBinding

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object DialogUtils {


    fun Context.customAlertDialog(message: String,callback: () -> Unit){
        val dialog = Dialog(this)
        val binding = DialogAlertBoxBinding.inflate(LayoutInflater.from(this))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        val width = (this.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        binding.apply {
            descriptionTV.text = message
            okButton.setOnClickListener {
                dialog.dismiss()
                callback()
            }
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



/*
    private var internetdialog : Dialog?=null
    fun noInternetCheckDialog(context: Context) {
        if (internetdialog?.isShowing==true){
            return
        }
        internetdialog = Dialog(context)
        val binding = DialogNoInternetBinding.inflate(LayoutInflater.from(context))
        internetdialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        internetdialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        internetdialog?.setCancelable(false)
        internetdialog?.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            internetdialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        try {
            internetdialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.doneBtn.setOnClickListener {
            internetdialog?.dismiss()
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    fun hideInternetDialog(){
        if (internetdialog?.isShowing==true){
            internetdialog?.dismiss()
        }
    }
*/



    fun Context.locationPermissionDialog(callback: () -> Unit){
        val dialog = Dialog(this)
        val binding = DialogLocationPermissionGuideBinding.inflate(LayoutInflater.from(this))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        val width = (this.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        binding.apply {
            doneBtn.setOnClickListener {
                dialog.dismiss()
                callback()
            }
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

/*
    fun imageViewDialog(context: Context,url:String){
        val dialog = Dialog(context)
        val binding = DialogViewImageBinding.inflate(LayoutInflater.from(context))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        binding.apply {
            context.loadImageWithGlide(
                url = url,
                progressBar = progress,
                imageView = binding.imageView,
                placeholder = R.drawable.order_loc_thumb
            )
            saveImageBtn.setSafeOnClickListener {
                progress.visible()
                val bitmap = captureScreenshot(binding.imageView)
                saveScreenshot(context,bitmap, callback = {
                    progress.hide()
                    if (it){
                        context.showToastMsg(context.resources.getString(R.string.saved))
                    }else{
                        context.showToastMsg(context.resources.getString(R.string.failed_to_save))
                    }
                })
            }
        }
        binding.closeBtn.setSafeOnClickListener {
            dialog.dismiss()
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logoutDialog(context: Context,logoutCallback: () -> Unit) {
        val dialog = Dialog(context)
        val binding = DialogBeforeLogoutBinding.inflate(LayoutInflater.from(context))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        binding.apply {
            noBtn.setSafeOnClickListener {
                dialog.dismiss()
            }
            yesBtn.setSafeOnClickListener {
                dialog.dismiss()
                logoutCallback()
            }

        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    private var mDialogDatePicker:Dialog?=null


    fun convertDate(inputDate: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date(inputDate))
    }


}