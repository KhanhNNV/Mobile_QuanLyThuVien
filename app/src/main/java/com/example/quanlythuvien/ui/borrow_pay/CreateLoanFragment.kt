package com.example.quanlythuvien.ui.borrow_pay

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.BookCopyApiService
import com.example.quanlythuvien.data.remote.LoanApiService
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.BookCopyRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.ui.borrow_pay.adapter.SelectedBookAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.BookDropDownItem
import com.example.quanlythuvien.ui.borrow_pay.data.ReaderDropDownItem
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateLoanFragment : Fragment(R.layout.fragment_loan_add) {

    private lateinit var viewModel: CreateLoanViewModel
    private lateinit var adapter: SelectedBookAdapter

    // UI Components
    private lateinit var autoCompleteReader: AutoCompleteTextView
    private lateinit var readerDropdownLayout: TextInputLayout
    private lateinit var tvReaderName: TextView
    private lateinit var autoCompleteBook: AutoCompleteTextView
    private lateinit var bookDropdownLayout: TextInputLayout
    private lateinit var btnAddBook: MaterialButton
    private lateinit var rvSelectedBooks: RecyclerView
    private lateinit var tvEmptyBookList: TextView
    private lateinit var tvSelectedBooksTitle: TextView
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    // Adapter lưu trực tiếp Object
    private lateinit var readerAdapter: ArrayAdapter<ReaderDropDownItem>
    private lateinit var bookAdapter: ArrayAdapter<BookDropDownItem>

    // Biến tạm để giữ cuốn sách đang được chọn trên dropdown
    private var pendingSelectedBook: BookDropDownItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, getString(R.string.pm1))

        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupDropdowns()
        observeViewModel()
        handleEvents()
    }

    private fun initViews(view: View) {
        readerDropdownLayout = view.findViewById(R.id.readerDropdownLayout)
        autoCompleteReader = view.findViewById(R.id.autoCompleteReader)
        tvReaderName = view.findViewById(R.id.tvReaderName)

        bookDropdownLayout = view.findViewById(R.id.bookDropdownLayout)
        autoCompleteBook = view.findViewById(R.id.autoCompleteBook)
        btnAddBook = view.findViewById(R.id.btnAddBook)
        rvSelectedBooks = view.findViewById(R.id.rvSelectedBooks)
        tvEmptyBookList = view.findViewById(R.id.tvEmptyBookList)
        tvSelectedBooksTitle = view.findViewById(R.id.tvSelectedBooksTitle)

        btnCancel = view.findViewById(R.id.btnCancelLoan)
        btnSave = view.findViewById(R.id.btnSaveLoan)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        val loanApi = retrofit.create(LoanApiService::class.java)
        val readerApi = retrofit.create(ReaderApiService::class.java)
        val bookCopyApi = retrofit.create(BookCopyApiService::class.java)

        val loanRepo = LoanRepository(loanApi)
        val readerRepo = ReaderRepository(readerApi)
        val bookCopyRepo = BookCopyRepository(bookCopyApi)

        val factory = GenericViewModelFactory {
            CreateLoanViewModel(loanRepo, readerRepo, bookCopyRepo)
        }
        viewModel = ViewModelProvider(this, factory)[CreateLoanViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = SelectedBookAdapter { copyId ->
            viewModel.removeBookFromSelection(copyId)
        }
        rvSelectedBooks.layoutManager = LinearLayoutManager(requireContext())
        rvSelectedBooks.adapter = adapter
    }

    private fun setupDropdowns() {
        // Khởi tạo Adapter với Object
        readerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteReader.setAdapter(readerAdapter)

        bookAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteBook.setAdapter(bookAdapter)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->

                    // Cập nhật danh sách Độc giả
                    readerAdapter.clear()
                    readerAdapter.addAll(state.readers)
                    readerAdapter.notifyDataSetChanged()

                    // ập nhật danh sách Sách
                    bookAdapter.clear()
                    bookAdapter.addAll(state.availableBooks)
                    bookAdapter.notifyDataSetChanged()

                    btnSave.isEnabled = !state.isLoading

                    // Cập nhật RecyclerView mỗi khi danh sách sách đã chọn thay đổi
                    updateSelectedBooksUI(state.selectedBooksForLoan)

                    if (state.isCreateSuccess) {
                        Toast.makeText(requireContext(), "Tạo phiếu mượn thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@collectLatest // Dừng chạy tiếp để tránh lặp logic
                    }

                    if (state.violationMessages.isNotEmpty()) {
                        showValidationDialog(state.violationMessages)
                        // Bắt buộc gọi clear để tắt Dialog vĩnh viễn sau khi show
                        viewModel.clearErrorAndViolations()
                    }

                    // Xử lý thông báo lỗi nếu có
                    state.error?.let { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        viewModel.clearErrorAndViolations()
                    }
                }
            }
        }
    }

    private fun updateSelectedBooksUI(books: List<BookDropDownItem>) {
        adapter.submitList(books)
        val isEmpty = books.isEmpty()
        tvEmptyBookList.visibility = if (isEmpty) View.VISIBLE else View.GONE
        tvSelectedBooksTitle.visibility = if (isEmpty) View.GONE else View.VISIBLE
        rvSelectedBooks.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun handleEvents() {
        // Xử lý khi chọn độc giả từ danh sách xổ xuống
        autoCompleteReader.setOnItemClickListener { parent, _, position, _ ->
            val selectedReaderItem = parent.getItemAtPosition(position) as ReaderDropDownItem
            tvReaderName.text = "Tên độc giả: ${selectedReaderItem.reader.fullName}"

            // Lưu vào ViewModel để dùng khi tạo phiếu mượn
            viewModel.selectReader(selectedReaderItem)
        }

        // Xử lý khi chọn sách từ danh sách xổ xuống
        autoCompleteBook.setOnItemClickListener { parent, _, position, _ ->
            pendingSelectedBook = parent.getItemAtPosition(position) as BookDropDownItem
        }

        // Xử lý nút Thêm sách vào danh sách mượn
        btnAddBook.setOnClickListener {
            if (pendingSelectedBook != null) {
                // Thêm vào danh sách sách đang mượn
                viewModel.addBookToSelection(pendingSelectedBook!!)

                // Xóa ô nhập sau khi thêm
                autoCompleteBook.text.clear()
                pendingSelectedBook = null
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn sách từ danh sách", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSave.setOnClickListener {
            viewModel.createLoan()
        }
    }

    private fun showValidationDialog(violations: List<String>) {
        val message = violations.joinToString("\n")
        AlertDialog.Builder(requireContext())
            .setTitle("Thông báo lỗi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}