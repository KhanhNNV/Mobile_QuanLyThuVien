package com.example.quanlythuvien.ui.setting

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView

class StaffSettingFragment : Fragment(R.layout.fragment_staff_setting) {

    private lateinit var cvCategory: MaterialCardView
    private lateinit var btnLogout: Button

    private lateinit var layoutStudentDiscountContainer: LinearLayout
    private lateinit var tvRegistrationFee: TextView
    private lateinit var tvStudentDiscount: TextView
    private lateinit var tvLateFee: TextView
    private lateinit var tvLostFeeExtra: TextView
    private lateinit var tvDamageFee: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(
            view = view,
            title = "Cài đặt",
            subtitle = "Dành cho nhân viên"
        )

        initViews(view)
        handleCardViewCategoryEvent()
        handleButtonLogOutEvent()

        // Hàm giả lập để set dữ liệu sau này
        loadFeeData()
    }

    private fun initViews(view: View) {
        cvCategory = view.findViewById(R.id.cvCategory)
        btnLogout = view.findViewById(R.id.btnLogout)

        tvRegistrationFee = view.findViewById(R.id.tvRegistrationFee)
        layoutStudentDiscountContainer = view.findViewById(R.id.layoutStudentDiscountContainer)
        tvStudentDiscount = view.findViewById(R.id.tvStudentDiscount)
        tvLateFee = view.findViewById(R.id.tvLateFee)
        tvLostFeeExtra = view.findViewById(R.id.tvLostFeeExtra)
        tvDamageFee = view.findViewById(R.id.tvDamageFee)
    }

    private fun loadFeeData() {
        // TODO: Sau này làm Backend Spring Boot sẽ gọi API lấy thông tin Policy ở đây

        // Ví dụ cách hiển thị dữ liệu:
        // tvRegistrationFee.text = "50.000 VNĐ"
        // tvLateFee.text = "5.000 VNĐ"

        // Logic hiển thị phần giảm giá sinh viên:
        // val hasStudentDiscount = true // Giả sử API trả về true
        // if (hasStudentDiscount) {
        //     layoutStudentDiscountContainer.visibility = View.VISIBLE
        //     tvStudentDiscount.text = "10%"
        // } else {
        //     layoutStudentDiscountContainer.visibility = View.GONE
        // }
    }

    private fun handleCardViewCategoryEvent(){
        cvCategory.setOnClickListener {
            findNavController().navigate(R.id.categoryListFragment)
        }
    }

    private fun handleButtonLogOutEvent(){
        btnLogout.setOnClickListener {
            // Xóa trạng thái đăng nhập
            val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putBoolean("isLoggedIn", false)
                remove("username")
                remove("userRole")
                apply()
            }

            // Xóa toàn bộ lịch sử (Back Stack) để không thể back lại
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()

            // Điều hướng về màn hình Welcome
            findNavController().navigate(R.id.welcomeFragment, null, navOptions)
        }
    }
}