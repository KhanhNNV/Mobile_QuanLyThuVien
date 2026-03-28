package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlythuvien.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReaderAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện fragment_reader_add.xml
        return inflater.inflate(R.layout.fragment_reader_add, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ánh xạ nút "Lưu Độc Giả" từ file XML sang biến Kotlin
        val btnSaveReader = view.findViewById<MaterialButton>(R.id.btnSaveReader)

        // 2. Lắng nghe sự kiện người dùng bấm vào nút Lưu
        btnSaveReader.setOnClickListener {

            // TODO: (Sau này) Gọi lệnh lưu dữ liệu vào Database ở đây

            // 3. Gọi MaterialAlertDialogBuilder để tạo Popup thông báo
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Lưu thành công!")
                .setMessage("Độc giả đã được thêm vào hệ thống. Bạn có muốn xuất thông tin này ra file PDF để in thẻ độc giả không?")

                .setPositiveButton("In PDF") { dialog, _ ->
                    // Tính năng chưa làm, hiện thông báo Toast nhắc nhở
                    Toast.makeText(requireContext(), "Tính năng xuất PDF đang được phát triển!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss() // Đóng popup lại
                }

                .setNegativeButton("Để sau") { dialog, _ ->
                    dialog.dismiss() // Chỉ đóng popup lại thôi
                }

                .show()
        }
    }
}