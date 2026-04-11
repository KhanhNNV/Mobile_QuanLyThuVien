package com.example.quanlythuvien

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.quanlythuvien.utils.JwtUtils
import com.example.quanlythuvien.utils.TokenManager
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


        // Đọc AccessToken từ TokenManager
        val tokenManager = TokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            // Đã đăng nhập: Giải mã Token để lấy Role
            val userRole = JwtUtils.getRoleFromToken(accessToken)

            navGraph.setStartDestination(R.id.dashboardFragment)
            updateBottomNavigationMenu(userRole)

        } else {
            // Chưa đăng nhập (hoặc token bị xóa)
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
                R.id.categoryListFragment,
                R.id.readerDetailFragment,
                R.id.notificationFragment-> {
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