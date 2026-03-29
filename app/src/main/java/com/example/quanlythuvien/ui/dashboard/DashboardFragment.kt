package com.example.quanlythuvien.ui.dashboard

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi"))
            .format(Date())
        setupCustomHeader(
            view = view,
            title = "Trang chủ",
            subtitle = currentDate
        )

    }



}