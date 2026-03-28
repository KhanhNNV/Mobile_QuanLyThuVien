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

        // Khởi tạo NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Lấy Navigation Graph để chuẩn bị cấu hình
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)

        // Đọc trạng thái từ SharedPreferences
        val sharedPreferences = getSharedPreferences("LibraryAppPrefs", MODE_PRIVATE)
        // Mặc định trả về true nếu chưa từng lưu (tức là lần đầu mở app)
        val isFirstLaunch = sharedPreferences.getBoolean("IS_FIRST_LAUNCH", true)

        // Quyết định màn hình bắt đầu
        if (isFirstLaunch) {
            // Lần đầu mở app -> Bắt đầu từ Welcome
            navGraph.setStartDestination(R.id.welcomeFragment)
        } else {
            // Các lần sau -> Bắt đầu thẳng vào Dashboard
            navGraph.setStartDestination(R.id.dashboardFragment)
        }

        // Áp dụng Graph đã được thiết lập điểm bắt đầu cho NavController
        navController.graph = navGraph

        // 1. Kết nối Bottom Navigation với NavController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // 2. Lắng nghe sự thay đổi màn hình để Ẩn/Hiện Bottom Navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcomeFragment,R.id.createBookFragment,R.id.createCategoryFragment -> {
                    // Nếu đang ở Onboarding/Welcome thì giấu thanh điều hướng đi
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // Ở các màn hình khác (Dashboard, Sách) thì hiện lên
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}