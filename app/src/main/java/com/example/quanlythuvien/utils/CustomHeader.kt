package com.example.quanlythuvien.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.card.MaterialCardView

fun Fragment.setupCustomHeader(
    view: View,
    title: String,
    subtitle: String = "",
    showBack: Boolean = false,
    showEdit: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {

    val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
    val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
    val btnBell = view.findViewById<MaterialCardView>(R.id.btnBell)
    val ivBack = view.findViewById<ImageView>(R.id.btnBack)
    val ivEdit = view.findViewById<ImageView>(R.id.ivEditProfile)
    tvHeaderTitle?.text = title

    if (subtitle.isNotEmpty()) {
        tvHeaderSubtitle?.text = subtitle
        tvHeaderSubtitle?.visibility = View.VISIBLE
    } else {
        // Nếu không truyền subtitle thì ẩn đi
        tvHeaderSubtitle?.visibility = View.GONE
    }

    //Xữ lý nút back
    if (showBack) {
        ivBack?.visibility = View.VISIBLE
        ivBack?.setOnClickListener {
            findNavController().navigateUp()//Quay lại màn hình cha
        }
    } else {
        ivBack?.visibility = View.GONE
    }

    //Xữ lý nút edit
    if (showEdit) {
        ivEdit?.visibility = View.VISIBLE
        ivEdit?.setOnClickListener {
            onEditClick?.invoke()
        }
    } else {
        ivEdit?.visibility = View.GONE
        }

        btnBell?.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }
    }