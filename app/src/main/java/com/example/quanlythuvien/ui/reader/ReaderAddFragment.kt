package com.example.quanlythuvien.ui.reader

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReaderAddFragment : Fragment(R.layout.fragment_reader_add) {

    private lateinit var tvTitle: TextView
    private lateinit var edtReaderName: TextInputEditText
    private lateinit var edtReaderPhone: TextInputEditText
    private lateinit var edtReaderBarcode: TextInputEditText
    private lateinit var edtMembershipMonths: TextInputEditText
    private lateinit var edtMembershipExpiry: TextInputEditText
    private lateinit var layoutMembershipMonths: LinearLayout
    private lateinit var layoutMembershipExpiry: LinearLayout
    private lateinit var layoutReaderBarcode: LinearLayout
    private lateinit var btnToggleBlockReader: MaterialButton
    private lateinit var btnCancelReader: MaterialButton
    private lateinit var btnSaveReader: MaterialButton
    private lateinit var viewModel: ReaderAddViewModel

    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    private var editReaderId: Long = -1L
    private var isModeEdit = false
    private var selectedExpiryDate: LocalDate? = null
    private var isBlocked = false
    private var detailSnapshot: ReaderResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupViewModel()
        setupEditMode()  // Tách logic chế độ Edit ra một hàm cho sạch sẽ
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tvTitle)
        edtReaderName = view.findViewById(R.id.edtReaderName)
        edtReaderPhone = view.findViewById(R.id.edtReaderPhone)
        edtReaderBarcode = view.findViewById(R.id.edtReaderBarcode)
        edtMembershipMonths = view.findViewById(R.id.edtMembershipMonths)
        edtMembershipExpiry = view.findViewById(R.id.edtMembershipExpiry)
        layoutMembershipMonths = view.findViewById(R.id.layoutMembershipMonths)
        layoutMembershipExpiry = view.findViewById(R.id.layoutMembershipExpiry)
        layoutReaderBarcode = view.findViewById(R.id.layoutReaderBarcode)
        btnToggleBlockReader = view.findViewById(R.id.btnToggleBlockReader)
        btnCancelReader = view.findViewById(R.id.btnCancelReader)
        btnSaveReader = view.findViewById(R.id.btnSaveReader)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(ReaderApiService::class.java)
        val repository = ReaderRepository(apiService)
        val factory = GenericViewModelFactory { ReaderAddViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[ReaderAddViewModel::class.java]
    }

    private fun isAdminRole(): Boolean {
        val raw = TokenManager(requireContext()).getRole().orEmpty()
        return raw.contains("ADMIN", ignoreCase = true)
    }

    private fun setupEditMode() {
        val editName = arguments?.getString("readerName")
        val editPhone = arguments?.getString("readerPhone")
        editReaderId = arguments?.getLong("readerId", -1L) ?: -1L
        isModeEdit = editReaderId > 0L && !editName.isNullOrEmpty()

        if (isModeEdit) {
            tvTitle.text = "Cập nhật Độc giả"
            edtReaderName.setText(editName)
            edtReaderPhone.setText(editPhone)
            layoutMembershipMonths.visibility = View.GONE
            if (isAdminRole()) {
                layoutMembershipExpiry.visibility = View.VISIBLE
                layoutReaderBarcode.visibility = View.VISIBLE
            } else {
                layoutMembershipExpiry.visibility = View.GONE
                layoutReaderBarcode.visibility = View.GONE
            }
            btnToggleBlockReader.visibility = View.VISIBLE
            updateBlockButton()
            viewModel.getReaderDetail(editReaderId)
        } else {
            layoutMembershipMonths.visibility = View.VISIBLE
            layoutMembershipExpiry.visibility = View.GONE
            layoutReaderBarcode.visibility = View.GONE
            btnToggleBlockReader.visibility = View.GONE
        }

        updateSaveButtonText()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addReaderState.collectLatest { state ->
                when (state) {
                    is ReaderAddState.Idle -> {
                        btnSaveReader.isEnabled = true
                        updateSaveButtonText()
                    }
                    is ReaderAddState.Loading -> {
                        btnSaveReader.isEnabled = false
                        btnSaveReader.text = "Đang xử lý..."
                    }
                    is ReaderAddState.Success -> {
                        btnSaveReader.isEnabled = true
                        updateSaveButtonText()
                        val successMsg = if (isModeEdit) "Cập nhật độc giả thành công!" else "Thêm độc giả thành công!"
                        Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is ReaderAddState.Error -> {
                        btnSaveReader.isEnabled = true
                        updateSaveButtonText()
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.readerDetail.collectLatest { reader ->
                if (reader != null && isModeEdit) {
                    detailSnapshot = reader
                    selectedExpiryDate = parseServerDate(reader.membershipExpiry)
                    edtMembershipExpiry.setText(selectedExpiryDate?.format(displayDateFormatter).orEmpty())
                    edtReaderBarcode.setText(reader.barcode)
                    isBlocked = reader.isBlocked
                    updateBlockButton()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detailLoadError.collect { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnCancelReader.setOnClickListener {
            findNavController().popBackStack()
        }

        edtMembershipExpiry.setOnClickListener {
            if (isModeEdit && isAdminRole()) showExpiryDatePicker()
        }

        btnToggleBlockReader.setOnClickListener {
            if (!isBlocked) {
                if (hasActiveMembershipForBlockConfirm()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận khóa")
                        .setMessage("Độc giả vẫn còn thời hạn thẻ. Bạn có chắc chắn muốn khóa người này không?")
                        .setPositiveButton("Khóa") { _, _ ->
                            isBlocked = true
                            updateBlockButton()
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                } else {
                    isBlocked = true
                    updateBlockButton()
                }
            } else {
                isBlocked = false
                updateBlockButton()
            }
        }

        btnSaveReader.setOnClickListener {
            val name = edtReaderName.text.toString().trim()
            val phone = edtReaderPhone.text.toString().trim()

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

            val libraryId = TokenManager(requireContext()).getLibraryId()
            if (libraryId == null) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin thư viện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isModeEdit) {
                val monthsStr = edtMembershipMonths.text.toString().trim()
                val months = monthsStr.toLongOrNull() ?: -1L
                if (months <= 0L) {
                    edtMembershipMonths.error = "Vui lòng nhập số tháng hợp lệ!"
                    edtMembershipMonths.requestFocus()
                    return@setOnClickListener
                }

                val request = ReaderRequest(
                    fullName = name,
                    phone = phone,
                    barcode = "",
                    libraryId = libraryId,
                    monthRegis = months
                )
                viewModel.addReader(request)
                return@setOnClickListener
            }

            if (isAdminRole()) {
                if (selectedExpiryDate == null) {
                    edtMembershipExpiry.error = "Vui lòng chọn ngày hết hạn!"
                    edtMembershipExpiry.requestFocus()
                    return@setOnClickListener
                }

                val barcodeTrim = edtReaderBarcode.text.toString().trim()
                if (barcodeTrim.isEmpty()) {
                    edtReaderBarcode.error = "Vui lòng nhập mã thẻ (barcode)!"
                    edtReaderBarcode.requestFocus()
                    return@setOnClickListener
                }

                val request = ReaderRequest(
                    fullName = name,
                    phone = phone,
                    barcode = barcodeTrim,
                    libraryId = libraryId,
                    membershipExpiry = selectedExpiryDate
                        ?.atTime(23, 59, 59)
                        ?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    isBlocked = isBlocked
                )
                viewModel.updateReader(editReaderId, request)
            } else {
                val snap = detailSnapshot
                if (snap == null) {
                    Toast.makeText(requireContext(), "Đang tải dữ liệu độc giả, thử lại sau.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val request = ReaderRequest(
                    fullName = name,
                    phone = phone,
                    barcode = snap.barcode,
                    libraryId = libraryId,
                    isBlocked = isBlocked
                )
                viewModel.updateReader(editReaderId, request)
            }
        }
    }

    private fun hasActiveMembershipForBlockConfirm(): Boolean {
        val expiry = if (isAdminRole()) {
            selectedExpiryDate
        } else {
            expiryLocalDate(detailSnapshot?.membershipExpiry)
        }
        if (expiry == null) return false
        return !LocalDate.now().isAfter(expiry)
    }

    private fun expiryLocalDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(raw).toLocalDate() }
            .recoverCatching { LocalDate.parse(raw) }
            .getOrNull()
    }

    private fun showExpiryDatePicker() {
        val initialDate = selectedExpiryDate ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedExpiryDate = LocalDate.of(year, month + 1, dayOfMonth)
                edtMembershipExpiry.error = null
                edtMembershipExpiry.setText(selectedExpiryDate?.format(displayDateFormatter).orEmpty())
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).show()
    }

    private fun updateSaveButtonText() {
        btnSaveReader.text = if (isModeEdit) "CẬP NHẬT" else "LƯU ĐỘC GIẢ"
    }

    private fun updateBlockButton() {
        val colorRes = if (isBlocked) R.color.blue else R.color.red
        btnToggleBlockReader.text = if (isBlocked) "UNBLOCK" else "BLOCK"
        btnToggleBlockReader.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        btnToggleBlockReader.strokeColor = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), colorRes)
        )
    }

    private fun parseServerDate(rawValue: String?): LocalDate? {
        if (rawValue.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(rawValue).toLocalDate() }
            .recoverCatching { LocalDate.parse(rawValue) }
            .getOrNull()
    }
}
