package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.UpdateLoanDetailRequest
import com.example.quanlythuvien.data.model.response.LoanResponse
import com.example.quanlythuvien.data.remote.LoanApiService
import com.example.quanlythuvien.data.remote.LoanDetailApiService
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.ui.borrow_pay.adapter.BookSpinnerAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.BookData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.loan_detail.LoanDetailState
import com.example.quanlythuvien.ui.loan_detail.LoanDetailViewModel
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LoanDetailFragment : Fragment() {

    private lateinit var viewModel: LoanDetailViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var bookAdapter: LoanDetailAdapter
    private var currentLoanId: Long = 0L

    // Tạo biến cục bộ để check quyền Admin
    private var checkIsAdmin: Boolean = false

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvReaderName: TextView
    private lateinit var tvLoanId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvBorrowDate: TextView
    private lateinit var ibtLoanMenu: ImageButton
    private lateinit var rvBooks: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loan_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        val idFromInvoice = arguments?.getLong("loanId") ?: 0L
        val idFromList = arguments?.getLong("EXTRA_LOAN_ID") ?: 0L

        // Ưu tiên lấy cái nào có giá trị lớn hơn 0
        currentLoanId = if (idFromInvoice != 0L) idFromInvoice else idFromList

        // LOGIC PHÂN QUYỀN
        val role = tokenManager.getRole()
        checkIsAdmin = (role == "ADMIN" || role == "ROLE_ADMIN")

        initViews(view)
        setupViewModel()
        setupHeader()
        setupRecyclerView()
        observeViewModel()

        if (currentLoanId != 0L) {
            viewModel.fetchLoanById(currentLoanId)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy mã phiếu mượn", Toast.LENGTH_SHORT).show()
        }
        viewModel.fetchAvailableBooks()
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
        tvReaderName = view.findViewById(R.id.tvDialogReaderName)
        tvLoanId = view.findViewById(R.id.tvDialogLoanId)
        tvStatus = view.findViewById(R.id.tvDialogStatus)
        tvBorrowDate = view.findViewById(R.id.tvDialogBorrowDate)
        ibtLoanMenu = view.findViewById(R.id.ibtSet)
        rvBooks = view.findViewById(R.id.rvBorrowedBooks)
    }

    private fun setupViewModel() {
        val loanApi = RetrofitClient.getInstance(requireContext()).create(LoanApiService::class.java)
        val detailApi = RetrofitClient.getInstance(requireContext()).create(LoanDetailApiService::class.java)
        val factory = GenericViewModelFactory {
            LoanDetailViewModel(LoanRepository(loanApi), LoanDetailRepository(detailApi))
        }
        viewModel = ViewModelProvider(this, factory)[LoanDetailViewModel::class.java]
    }

    private fun setupHeader() {
        tvHeaderTitle.text = "Chi tiết phiếu mượn"
        btnBack.setOnClickListener { findNavController().popBackStack() }

        if (checkIsAdmin) {
            ibtLoanMenu.visibility = View.VISIBLE
            ibtLoanMenu.setOnClickListener { view ->
                val popup = PopupMenu(requireContext(), view)
                popup.menu.add(0, 1, 0, "Xóa phiếu mượn")
                popup.setOnMenuItemClickListener {
                    if (it.itemId == 1) handleDeleteLoan()
                    true
                }
                popup.show()
            }
        } else {
            ibtLoanMenu.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        // 1. Lấy role trực tiếp từ TokenManager (nếu null thì để chuỗi rỗng)
        val role = tokenManager.getRole() ?: ""

        // 2. Truyền role vào Adapter thay vì checkIsAdmin
        bookAdapter = LoanDetailAdapter(role) { targetBook, action ->
            when (action) {
                "RETURN" -> handleReturnBook(targetBook)
                "EDIT" -> handleEditBook(targetBook)
                "DELETE" -> if (checkIsAdmin) handleDeleteBook(targetBook)
            }
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is LoanDetailState.Success -> {
                        bindDataToUI(state.loan)
                    }
                    is LoanDetailState.DeleteLoanSuccess -> {
                        setFragmentResult("REFRESH_LOAN_LIST", bundleOf("IS_CHANGED" to true))
                        findNavController().popBackStack()
                    }
                    is LoanDetailState.UpdateBookSuccess -> {
                        setFragmentResult("REFRESH_LOAN_LIST", bundleOf("IS_CHANGED" to true))
                        Toast.makeText(requireContext(), "Thao tác thành công", Toast.LENGTH_SHORT).show()
                    }
                    is LoanDetailState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun bindDataToUI(loan: LoanResponse) {
        tvReaderName.text = loan.readerName
        tvLoanId.text = loan.loanId.toString()
        tvBorrowDate.text = formatDisplayDate(loan.borrowDate) // Format cho đẹp nếu cần
        editStatusUI(loan.status, tvStatus)

        // Map danh sách sách mượn (sử dụng loanDetails thay cho bookDetails cũ)
        val uiDetails = loan.loanDetails?.map { detail ->
            LoanDetailItemData(
                loanDetailId = detail.loanDetailId, // Sử dụng ID chính xác để gọi API
                bookId = detail.copyId ?: 0L,
                title = detail.bookTitle ?: "Không rõ",
                author = detail.author ?: "Không rõ",
                categoryName = detail.category ?: "Không rõ",
                dueDate = formatDisplayDate(detail.dueDate).ifEmpty { "Chưa có" },
                returnDate = formatDisplayDate(detail.returnDate),
                bookBarcode = detail.bookBarcode ?: "Chưa có mã",
                status = detail.status
            )
        } ?: emptyList()

        bookAdapter.submitList(uiDetails)
    }

    private fun handleDeleteLoan() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn phiếu mượn này?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ -> viewModel.deleteWholeLoan(currentLoanId) }
            .show()
    }

    // ==========================================
// 1. LUỒNG TRẢ SÁCH (DÙNG 4 ENUM CONDITION)
// ==========================================
    private fun handleReturnBook(targetBook: LoanDetailItemData) {
        // Hiển thị tiếng Việt cho thân thiện, gửi giá trị tiếng Anh cho Server
        val displayConditions = arrayOf(
            "Mới (NEW) - Sách còn nguyên vẹn",
            "Tốt (GOOD) - Có dấu hiệu sử dụng nhẹ",
            "Trung bình (FAIR) - Bị nhăn, mòn góc",
            "Kém (POOR) - Rách, ướt, không thể tái sử dụng"
        )
        val serverConditions = arrayOf("NEW", "GOOD", "FAIR", "POOR")
        var selectedIndex = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xác nhận trả sách")
            .setMessage("Cuốn sách: ${targetBook.title}\nVui lòng đánh giá tình trạng sách khi thu hồi:")
            .setSingleChoiceItems(displayConditions, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Xác nhận Trả") { dialog, _ ->
                val condition = serverConditions[selectedIndex]
                // Khi gửi condition FAIR/POOR, Backend đã có sẵn logic đổi status thành DAMAGED
                viewModel.returnBook(targetBook.loanDetailId, condition, currentLoanId)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy bỏ", null)
            .show()
    }

    // ==========================================
// 2. LUỒNG SỬA DỮ LIỆU ADMIN (CÓ GIA HẠN NGÀY)
// ==========================================
    private fun handleEditBook(targetBook: LoanDetailItemData) {
        val dialog = BottomSheetDialog(requireContext())
        val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_loan_detail, null)
        dialog.setContentView(viewDialog)

        val spnStatus = viewDialog.findViewById<Spinner>(R.id.spnEditStatus)
        val edtDueDate = viewDialog.findViewById<EditText>(R.id.edtEditDueDate)
        val btnSave = viewDialog.findViewById<Button>(R.id.btnSaveEdit)
        val btnCancel = viewDialog.findViewById<Button>(R.id.btnCancelEdit)
        val spnReplacementBook = viewDialog.findViewById<Spinner>(R.id.spnSelectBookInLoan)

        // Khôi phục hiển thị và chức năng chọn ngày (Gia hạn)
        edtDueDate.visibility = View.VISIBLE
        edtDueDate.setText(if (targetBook.dueDate == "Chưa có") "" else targetBook.dueDate)
        edtDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val newDate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                    )
                    edtDueDate.setText(newDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Cài đặt 3 Trạng thái cho Admin
        val statusList = listOf("BORROWING", "RETURNED", "LOST")
        spnStatus.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)

        val currentStatusIndex = statusList.indexOf(targetBook.status)
        if (currentStatusIndex >= 0) {
            spnStatus.setSelection(currentStatusIndex)
        } else {
            spnStatus.setSelection(0)
        }

        val spinnerList = mutableListOf<BookData>()
        spinnerList.add(BookData(copyId = 0L, title = "Giữ nguyên (Không đổi)", author = "", barcode = ""))
        val availableCopies = viewModel.availableBooks.value
        spinnerList.addAll(availableCopies)

        val replacementAdapter = BookSpinnerAdapter(requireContext(), spinnerList)
        spnReplacementBook.adapter = replacementAdapter

        // ==========================================
        // XỬ LÝ NÚT LƯU THAY ĐỔI
        // ==========================================
        btnSave.setOnClickListener {
            val selectedStatus = spnStatus.selectedItem.toString()
            val selectedBookData = spnReplacementBook.selectedItem as BookData
            val newCopyId = if (selectedBookData.copyId == 0L) targetBook.bookId else selectedBookData.copyId
            val formattedDueDate = formatIsoDate(edtDueDate.text.toString())

            when (selectedStatus) {
                "RETURNED" -> {
                    // Chặn nếu sách đã được trả rồi
                    if (targetBook.status == "RETURNED") {
                        Toast.makeText(requireContext(), "Sách này đã được trả từ trước!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val displayConditions = arrayOf(
                        "Mới (NEW) - Sách còn nguyên vẹn",
                        "Tốt (GOOD) - Có dấu hiệu sử dụng nhẹ",
                        "Trung bình (FAIR) - Bị nhăn, mòn góc",
                        "Kém (POOR) - Rách, ướt, không thể tái sử dụng"
                    )
                    val serverConditions = arrayOf("NEW", "GOOD", "FAIR", "POOR")
                    var selectedConditionIndex = 0

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đánh giá tình trạng sách để Trả")
                        .setSingleChoiceItems(displayConditions, selectedConditionIndex) { _, which ->
                            selectedConditionIndex = which
                        }
                        .setPositiveButton("Xác nhận") { condDialog, _ ->
                            val condition = serverConditions[selectedConditionIndex]

                            viewModel.returnBook(targetBook.loanDetailId, condition, currentLoanId)

                            condDialog.dismiss()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Hủy bỏ", null)
                        .show()
                }

                "LOST" -> {
                    // Vẫn gọi API Update Admin. Backend hiện tại đã có logic tạo Violation cho LOST ở hàm updateDetailAdmin.
                    val request = UpdateLoanDetailRequest(
                        copyId = newCopyId,
                        status = "LOST",
                        dueDate = formattedDueDate,
                        condition = null
                    )
                    viewModel.updateBookInLoan(targetBook.loanDetailId, request, currentLoanId)
                    dialog.dismiss()
                }

                "BORROWING" -> {
                    // Mượn tiếp, gia hạn hoặc đổi sách mới
                    val request = UpdateLoanDetailRequest(
                        copyId = newCopyId,
                        status = "BORROWING",
                        dueDate = formattedDueDate,
                        condition = null
                    )
                    viewModel.updateBookInLoan(targetBook.loanDetailId, request, currentLoanId)
                    dialog.dismiss()
                }
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun handleDeleteBook(targetBook: LoanDetailItemData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa sách khỏi phiếu")
            .setMessage("Bạn muốn xóa cuốn sách '${targetBook.title}' khỏi phiếu mượn này?\nLưu ý: Thao tác này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                // Gọi API Xóa với ID của chi tiết sách
                viewModel.deleteBookFromLoan(targetBook.loanDetailId, currentLoanId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // --- HÀM CẬP NHẬT TRẠNG THÁI UI THEO ENUM MỚI CỦA PHIẾU GỐC ---
    private fun editStatusUI(status: String, tvStatus: TextView) {
        val (text, color, bg) = when (status.uppercase()) {
            "ACTIVE" -> Triple("Đang hoạt động", R.color.text_status_info, R.drawable.bg_status_info)
            "COMPLETED"-> Triple("Hoàn tất", R.color.text_status_success, R.drawable.bg_status_success)
            "OVERDUE" -> Triple("Quá hạn", R.color.text_status_error, R.drawable.bg_status_error)
            "VIOLATED" -> Triple("Vi phạm", R.color.text_status_error, R.drawable.bg_status_error)
            else -> Triple("Không xác định", R.color.text_secondary, R.drawable.bg_status_info)
        }
        tvStatus.text = text
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), color))
        tvStatus.setBackgroundResource(bg)
    }

    private fun formatDisplayDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        return try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdfOut.format(sdfIn.parse(isoDate)!!)
        } catch (e: Exception) {
            isoDate
        }
    }

    // Đã đổi String thành String? (có dấu hỏi chấm)
    private fun formatIsoDate(displayDate: String): String? {
        // Đổi return "" thành return null
        if (displayDate.isEmpty() || displayDate == "Chưa có") return null

        return try {
            val sdfIn = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfOut = SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault())
            sdfOut.format(sdfIn.parse(displayDate)!!)
        } catch (e: Exception) {
            null // Đổi displayDate thành null
        }
    }
}