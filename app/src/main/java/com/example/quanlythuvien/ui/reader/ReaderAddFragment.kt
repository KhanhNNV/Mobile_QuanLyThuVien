package com.example.quanlythuvien.ui.reader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

class ReaderAddFragment : Fragment(R.layout.fragment_reader_add) {

    // 1. Khai báo các View
    private lateinit var tvTitle: TextView
    private lateinit var edtReaderName: TextInputEditText
    private lateinit var edtReaderPhone: TextInputEditText
    private lateinit var btnCancelReader: MaterialButton
    private lateinit var btnSaveReader: MaterialButton

    private lateinit var viewModel: ReaderAddViewModel
    private lateinit var edtMembershipMonths: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        setupEditMode() // Tách logic chế độ Edit ra một hàm cho sạch sẽ
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tvTitle)
        edtReaderName = view.findViewById(R.id.edtReaderName)
        edtReaderPhone = view.findViewById(R.id.edtReaderPhone)
        btnCancelReader = view.findViewById(R.id.btnCancelReader)
        btnSaveReader = view.findViewById(R.id.btnSaveReader)
        edtMembershipMonths = view.findViewById(R.id.edtMembershipMonths)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(ReaderApiService::class.java)
        val repository = ReaderRepository(apiService)

        val factory = GenericViewModelFactory { ReaderAddViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[ReaderAddViewModel::class.java]
    }

    private fun setupEditMode() {
        val editName = arguments?.getString("readerName")
        val editPhone = arguments?.getString("readerPhone")
        val isModeEdit = !editName.isNullOrEmpty()

        if (isModeEdit) {
            tvTitle.text = "Cập nhật Độc giả"
            btnSaveReader.text = "CẬP NHẬT"
            edtReaderName.setText(editName)
            edtReaderPhone.setText(editPhone)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addReaderState.collectLatest { state ->
                when (state) {
                    is ReaderAddState.Idle -> {
                        btnSaveReader.isEnabled = true
                        // Check xem đang ở chế độ thêm hay sửa để set chữ cho đúng
                        btnSaveReader.text = if (arguments?.getString("readerName").isNullOrEmpty()) "LƯU ĐỘC GIẢ" else "CẬP NHẬT"
                    }
                    is ReaderAddState.Loading -> {
                        btnSaveReader.isEnabled = false
                        btnSaveReader.text = "Đang xử lý..."
                    }
                    is ReaderAddState.Success -> {
                        btnSaveReader.isEnabled = true
                        btnSaveReader.text = "LƯU ĐỘC GIẢ"

                        Toast.makeText(requireContext(), "Thêm độc giả thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is ReaderAddState.Error -> {
                        btnSaveReader.isEnabled = true
                        btnSaveReader.text = "LƯU ĐỘC GIẢ"
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        btnCancelReader.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSaveReader.setOnClickListener {
            val name = edtReaderName.text.toString().trim()
            val phone = edtReaderPhone.text.toString().trim()
            val monthsStr = edtMembershipMonths.text.toString().trim()

            // Validate báo lỗi từng ô giống AddBookFragment
            if (name.isEmpty()) {
                edtReaderName.error = "Vui lòng nhập họ tên!"
                edtReaderName.requestFocus()
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                edtReaderPhone.error = "Vui lòng nhập số điện thoại!"
                edtReaderPhone.requestFocus()
                return@setOnClickListener
            }
            if (monthsStr.isEmpty()) {
                edtMembershipMonths.error = "Vui lòng nhập số tháng!"
                edtMembershipMonths.requestFocus()
                return@setOnClickListener
            }
            val months = monthsStr.toLongOrNull()
            if (months == null || months <= 0) {
                edtMembershipMonths.error = "Số tháng không hợp lệ!"
                edtMembershipMonths.requestFocus()
                return@setOnClickListener
            }

            // Lấy ID thư viện từ Token
            val libraryId = TokenManager(requireContext()).getLibraryId()
            if (libraryId == null) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin thư viện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            //Tạo một cái Lịch (Calendar) ngay thời điểm hiện tại
            val calendar = Calendar.getInstance()

            //  Cộng thêm số tháng user vừa nhập vào cái Lịch đó
            calendar.add(Calendar.MONTH, months.toInt())

            // Chuẩn bị một cái Khuôn (Format) : "Năm-Tháng-NgàyTHiờ:Phút:Giây"
            // Locale.getDefault() để đảm bảo định dạng không bị lỗi múi giờ
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            //  In cái Lịch ra thành chuỗi dựa theo cái Khuôn đó
            val expiryDate = formatter.format(calendar.time)

            val request = ReaderRequest(
                fullName = name,
                phone = phone,
                barcode = "",
                libraryId = libraryId,
                membershipExpiry = expiryDate
            )

            viewModel.addReader(request)
        }
    }
}