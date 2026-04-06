package com.example.quanlythuvien.ui.dashboard

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.example.quanlythuvien.viewmodel.SharedFilterLoanViewModel
import com.google.android.material.card.MaterialCardView
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val sharedViewModel: SharedFilterLoanViewModel by activityViewModels()

    // Khai báo các Views
    private lateinit var cvTotalBooks: MaterialCardView
    private lateinit var cvTotalBorrowing: MaterialCardView
    private lateinit var cvTotalReader: MaterialCardView
    private lateinit var cvTotalDelayed: MaterialCardView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Header
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi")).format(Date())
        setupCustomHeader(
            view = view,
            title = "Trang chủ",
            subtitle = currentDate
        )

        initViews(view)
        handleEvents()
    }

    private fun initViews(view: View) {
        cvTotalBooks = view.findViewById(R.id.cvTotalBooks)
        cvTotalBorrowing = view.findViewById(R.id.cvTotalBorrowing)
        cvTotalReader=view.findViewById(R.id.cvTotalReader)
        cvTotalDelayed=view.findViewById(R.id.cvTotalDelayed)
    }

    private fun handleEvents() {


        cvTotalBooks.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_bookList)
        }

        cvTotalReader.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_readerList)
        }

        // Sử dụng SharedViewModel để truyền tín hiệu thay vì dùng Bundle.
        // Tránh lỗi kẹt bộ lọc do app:restoreState="true" tự động khôi phục UI cũ và ghi đè Bundle mới.
        // Lưu ý: Đích đến (BorrowPay) phải gọi clearFilter() ngay sau khi nhận được lệnh.
        cvTotalBorrowing.setOnClickListener {
            sharedViewModel.setFilter("BORROWING") // Cập nhật ViewModel
            findNavController().navigate(R.id.action_dashboard_to_borrowPay)
        }

        cvTotalDelayed.setOnClickListener {
            sharedViewModel.setFilter("DELAYED") // Cập nhật ViewModel
            findNavController().navigate(R.id.action_dashboard_to_borrowPay)
        }


    }
}