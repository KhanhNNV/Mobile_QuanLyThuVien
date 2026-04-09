package com.example.quanlythuvien

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Đọc trạng thái đăng nhập và quyền (Role)
        val sharedPreferences = getSharedPreferences("LibraryAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userRole = sharedPreferences.getString("userRole", "ADMIN") ?: "ADMIN"

        // Phân luồng Start Destination và Menu
        if (isLoggedIn) {
            if (userRole == "STAFF") {
                navGraph.setStartDestination(R.id.staffDashboardFragment)
            } else {
                navGraph.setStartDestination(R.id.dashboardFragment)
            }

            updateBottomNavigationMenu(userRole)

        } else {
            navGraph.setStartDestination(R.id.welcomeFragment)
            // Setup mặc định khi chưa đăng nhập
            bottomNavigationView.setupWithNavController(navController)
        }

        navController.graph = navGraph

        // Quản lý ẩn/hiện Bottom Navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcomeFragment,
                R.id.registerFragment,
                R.id.loginFragment,
                R.id.createCategoryFragment,
                R.id.createBookFragment,
                R.id.staffListFragment,
                R.id.loanPolicyFragment,
                R.id.categoryListFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    fun updateBottomNavigationMenu(role: String) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Xóa menu cũ
        bottomNavigationView.menu.clear()

        // Bơm menu mới dựa vào Role
        if (role == "STAFF") {
            bottomNavigationView.inflateMenu(R.menu.menu_staff)
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu)
        }

        // Kết nối lại với NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        bottomNavigationView.setupWithNavController(navHostFragment.navController)
    }
}