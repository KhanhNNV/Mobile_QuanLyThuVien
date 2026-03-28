package com.example.quanlythuvien.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var btnBack: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews(view: View) {
        rvNotifications = view.findViewById(R.id.rvNotifications)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        rvNotifications.layoutManager = LinearLayoutManager(requireContext())

        // Dữ liệu mẫu (Lưu ý: Thêm true/false ở cuối để hiện/ẩn dấu chấm đỏ)
        // false = chưa đọc (hiện chấm đỏ), true = đã đọc (ẩn chấm đỏ)
        val dummyData = listOf(
            Notification("Đơn hàng của bạn đã được giao thành công.", "27/03/2026", "10:00", false),
            Notification("Bạn có một tin nhắn mới từ hệ thống.", "26/03/2026", "15:45", true),
            Notification("Mã giảm giá 50% sắp hết hạn, dùng ngay!", "25/03/2026", "08:30", false)
        )

        adapter = NotificationAdapter(dummyData)
        rvNotifications.adapter = adapter
    }

    private fun setupListeners() {
        // Xử lý khi bấm nút Back
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}