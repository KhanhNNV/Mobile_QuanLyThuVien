package com.example.quanlythuvien.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var btnBell: MaterialCardView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        btnBell = view.findViewById(R.id.btnBell)

    }

    private fun setupListeners() {
        btnBell.setOnClickListener {
            navigateToNotificationScreen()
        }

    }

    private fun navigateToNotificationScreen() {
        findNavController().navigate(R.id.notificationFragment)
    }

}