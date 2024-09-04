package com.waseem.locationtracking.ui.dialogs



import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.waseem.locationtracking.databinding.DialogOverlayPermissionAppBinding
import com.waseem.locationtracking.utils.extension.setSafeOnClickListener



class OverLayPermissionAppDialog(private var mContext: Context, private val callbackDismiss:()->Unit) : Dialog(mContext){
    private lateinit var binding: DialogOverlayPermissionAppBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (mContext.resources.displayMetrics.widthPixels * 0.85).toInt()
        binding = DialogOverlayPermissionAppBinding.inflate(layoutInflater)
        setCancelable(false)
        setContentView(binding.root)
        binding.root.minimumWidth = width

        binding.btnAllow.setSafeOnClickListener {
            dismiss()
            callbackDismiss()
        }

    }

}