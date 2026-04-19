package com.example.quanlythuvien.utils

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

fun Fragment.setupCustomHeader(view: View, title: String, subtitle: String = "") {

    val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
    val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
    val tvAvatar = view.findViewById<TextView>(R.id.tvAvatar) // Ánh xạ Avatar

    tvHeaderTitle?.text = title

    if (subtitle.isNotEmpty()) {
        tvHeaderSubtitle?.text = subtitle
        tvHeaderSubtitle?.visibility = View.VISIBLE
    } else {
        // Nếu không truyền subtitle thì ẩn đi
        tvHeaderSubtitle?.visibility = View.GONE
    }

    tvAvatar?.let { avatar ->
        // 2. Bắt sự kiện Click để mở Menu
        avatar.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), avatar)
            popupMenu.menuInflater.inflate(R.menu.menu_profile, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_view_profile -> {
                        findNavController().navigate(R.id.profileFragment)
                        true
                    }
                    R.id.action_logout -> {
                        val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply {
                            putBoolean("isLoggedIn", false)
                            remove("username")
                            remove("userRole")
                            apply()
                        }

                        val navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                        findNavController().navigate(R.id.welcomeFragment, null, navOptions)
                        true
                    }
                    else -> false
                }
            }

            // Hiển thị Popup
            popupMenu.show()
        }
    }
}