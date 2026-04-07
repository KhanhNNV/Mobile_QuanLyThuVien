package com.example.quanlythuvien.utils

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

fun Fragment.setupHeaderWithBack(view: View, title: String) {
    val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
    val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

    tvHeaderTitle?.text = title

    btnBack?.setOnClickListener {
        findNavController().popBackStack()
    }
}