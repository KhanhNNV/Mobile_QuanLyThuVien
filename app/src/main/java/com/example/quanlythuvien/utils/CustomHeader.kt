package com.example.quanlythuvien.utils

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.card.MaterialCardView

fun Fragment.setupCustomHeader(view: View, title: String, subtitle: String = "") {

    val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
    val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
    val btnBell = view.findViewById<MaterialCardView>(R.id.btnBell)

    tvHeaderTitle?.text = title

    if (subtitle.isNotEmpty()) {
        tvHeaderSubtitle?.text = subtitle
        tvHeaderSubtitle?.visibility = View.VISIBLE
    } else {
        // Nếu không truyền subtitle thì ẩn đi
        tvHeaderSubtitle?.visibility = View.GONE
    }

    btnBell?.setOnClickListener {
        findNavController().navigate(R.id.notificationFragment)
    }
}