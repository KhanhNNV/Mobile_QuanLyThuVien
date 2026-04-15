package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.os.Bundle
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
import com.example.quanlythuvien.data.model.request.LoanDetailRequest
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
    private var currentLoanData: LoanResponse? = null

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
        currentLoanId = arguments?.getLong("EXTRA_LOAN_ID") ?: 0L

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
        }

        // ĐÃ THÊM: Gọi API lấy danh sách sách rảnh rỗi để nạp vào Spinner
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
        bookAdapter = LoanDetailAdapter(checkIsAdmin) { targetBook, action ->
            when (action) {
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
                        currentLoanData = state.loan
                        bindDataToUI(state.loan)
                    }
                    is LoanDetailState.DeleteLoanSuccess -> {
                        setFragmentResult("REFRESH_LOAN_LIST", bundleOf("IS_CHANGED" to true))
                        findNavController().popBackStack()
                    }
                    is LoanDetailState.UpdateBookSuccess -> {
                        setFragmentResult("REFRESH_LOAN_LIST", bundleOf("IS_CHANGED" to true))
                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
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
        tvBorrowDate.text = loan.borrowDate
        editStatusUI(loan.status, tvStatus)

        val uiDetails = loan.bookDetails?.map { detail ->
            LoanDetailItemData(
                bookId = detail.copyId ?: 0L,
                title = detail.title ?: "Không rõ",
                author = detail.author ?: "Không rõ",
                categoryName = detail.category ?: "Không rõ",
                dueDate = formatDisplayDate(detail.dueDate).ifEmpty { "Chưa có" },
                returnDate = formatDisplayDate(detail.returnDate),
                status = detail.status ?: "BORROWING"
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

    private fun handleEditBook(targetBook: LoanDetailItemData) {
        val dialog = BottomSheetDialog(requireContext())
        val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_loan_detail, null)
        dialog.setContentView(viewDialog)

        // 1. Ánh xạ các View
        val spnStatus = viewDialog.findViewById<Spinner>(R.id.spnEditStatus)
        val edtDueDate = viewDialog.findViewById<EditText>(R.id.edtEditDueDate)
        val btnSave = viewDialog.findViewById<Button>(R.id.btnSaveEdit)
        val btnCancel = viewDialog.findViewById<Button>(R.id.btnCancelEdit)
        val spnReplacementBook = viewDialog.findViewById<Spinner>(R.id.spnSelectBookInLoan)

        // 2. Cài đặt Spinner Trạng thái (Giữ nguyên cũ vì nó dùng item mặc định của Android)
        val statusList = listOf("BORROWING", "RETURNED_NORMAL", "LATE", "LOST", "DAMAGED")
        spnStatus.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
        val currentStatusIndex = statusList.indexOf(targetBook.status)
        if (currentStatusIndex >= 0) spnStatus.setSelection(currentStatusIndex)

        // 3. Cài đặt Spinner Sách thay thế với Custom Adapter
        val spinnerList = mutableListOf<BookData>()
        // Thêm option mặc định
        spinnerList.add(BookData(copyId = 0L, title = "Giữ nguyên (Không đổi)", author = "", categoryName = ""))

        // Lấy dữ liệu hiện tại từ ViewModel
        val availableCopies = viewModel.availableBooks.value
        spinnerList.addAll(availableCopies)

        // SỬ DỤNG CUSTOM ADAPTER BẠN VỪA TẠO
        val replacementAdapter = BookSpinnerAdapter(requireContext(), spinnerList)
        spnReplacementBook.adapter = replacementAdapter

        // 4. Cài đặt Hạn trả (DatePicker)
        edtDueDate.setText(if (targetBook.dueDate == "Chưa có") "" else targetBook.dueDate)
        edtDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            // Nếu đã có ngày trong ô, hãy parse nó để DatePicker mở đúng ngày đó
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val newDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                edtDueDate.setText(newDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 5. Xử lý nút Lưu thay đổi
        btnSave.setOnClickListener {
            // Lấy Object BookData từ Spinner (Cực kỳ an toàn vì dùng Custom Adapter)
            val selectedBookData = spnReplacementBook.selectedItem as BookData

            // Nếu người dùng chọn sách mới (copyId != 0), ta lấy ID mới, nếu không giữ ID cũ
            val newCopyId = if (selectedBookData.copyId == 0L) {
                targetBook.bookId
            } else {
                selectedBookData.copyId
            }

            val request = LoanDetailRequest(
                loanId = currentLoanId,
                copyId = newCopyId,
                dueDate = formatIsoDate(edtDueDate.text.toString()),
                status = spnStatus.selectedItem.toString()
            )

            // Thực hiện cập nhật qua ViewModel
            viewModel.updateBookInLoan(currentLoanId, targetBook.bookId, request)
            dialog.dismiss()
        }

        // 6. Xử lý nút Hủy bỏ
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
    private fun handleDeleteBook(targetBook: LoanDetailItemData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa sách")
            .setMessage("Xóa cuốn sách này khỏi phiếu mượn?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteBookFromLoan(currentLoanId, targetBook.bookId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun editStatusUI(status: String, tvStatus: TextView) {
        val (text, color, bg) = when (status) {
            "BORROWING" -> Triple("Đang mượn", R.color.text_status_info, R.drawable.bg_status_info)
            "RETURNED"-> Triple("Đã trả", R.color.text_status_success, R.drawable.bg_status_success)
            else -> Triple("Quá hạn", R.color.text_status_error, R.drawable.bg_status_error)
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

    private fun formatIsoDate(displayDate: String): String {
        if (displayDate.isEmpty() || displayDate == "Chưa có") return ""
        return try {
            val sdfIn = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfOut = SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault())
            sdfOut.format(sdfIn.parse(displayDate)!!)
        } catch (e: Exception) {
            displayDate
        }
    }
}