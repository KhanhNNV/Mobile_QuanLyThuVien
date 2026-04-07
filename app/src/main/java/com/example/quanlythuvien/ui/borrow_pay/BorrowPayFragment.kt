package com.example.quanlythuvien.ui.borrow_pay
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus
import com.example.quanlythuvien.ui.borrow_pay.adapter.BorrowPayAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import android.content.res.ColorStateList
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.example.quanlythuvien.viewmodel.SharedFilterLoanViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BorrowPayFragment :Fragment(){

    private fun getSampleData(): List<LoanItemData> {
        return listOf(
            LoanItemData(
                1, "10/03/2026", "20/03/2026", "BORROWING", "Nguyễn Văn A",
                mutableListOf(
                    LoanDetailItemData("Lập trình Java", "James Gosling", "Kỹ thuật", null, "BORROWING"),
                            LoanDetailItemData("Lập trình Python", "James Gosling", "Kỹ thuật", null, "BORROWING")
                )
            ),
            LoanItemData(
                2, "05/03/2026", "15/03/2026", "RETURNED", "Trần Thị B",
                mutableListOf(
                    LoanDetailItemData("Android nâng cao", "Google", "Lập trình", "14/03/2026", "RETURNED")
                )
            ),
            LoanItemData(
                3, "01/03/2026", "10/03/2026", "BORROWING", "Người Trễ Hạn",
                mutableListOf(
                    LoanDetailItemData("Clean Code", "Robert C. Martin", "Kỹ thuật", null, "BORROWING")
                )
            )
        )
    }


    private val sharedViewModel: SharedFilterLoanViewModel by activityViewModels()

    // Khai báo thêm "người vận chuyển"
    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    private lateinit var fasAddLoan: FloatingActionButton
// Nút thêm phiếu mượn

    private lateinit var autoSearch: AutoCompleteTextView
// Ô tìm kiếm có gợi ý (search phiếu / người mượn / sách)

    private lateinit var btnToggleFilter: ImageView
// Nút mở/đóng phần bộ lọc

    private lateinit var layoutFilterContainer: ConstraintLayout
// Layout chứa toàn bộ bộ lọc (ẩn/hiện)

    private lateinit var rgStatus: RadioGroup
// Nhóm chọn trạng thái phiếu

    private lateinit var rbOption1: RadioButton
// Radio: Tất cả

    private lateinit var rbOption2: RadioButton
// Radio: Đang mượn

    private lateinit var rbOption3: RadioButton
// Radio: Trễ hạn

    private lateinit var edtFromDate: EditText
// Ô chọn ngày bắt đầu lọc

    private lateinit var edtToDate: EditText
// Ô chọn ngày kết thúc lọc

    private lateinit var btnResetFilter: Button
// Nút reset bộ lọc về mặc định

    private lateinit var btnConfirmFilter: Button
// Nút xác nhận áp dụng bộ lọc

    private lateinit var recyclerView: RecyclerView
// Danh sách hiển thị các phiếu mượn


    //List chưa dữ liệu phiếu mượn
    private lateinit var sampleDataList: MutableList<LoanItemData>

    //Adapter cho RecyclerView
    private lateinit var adapter: BorrowPayAdapter

    private var lastFilter: String? = null



    //Hàm chuyển file layout xml thành UI
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_borrow_pay, container, false)
    }
    private fun initViews(view: View) {
        fasAddLoan = view.findViewById(R.id.fasAddLoan)

        autoSearch = view.findViewById(R.id.autoSearch)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)

        layoutFilterContainer = view.findViewById(R.id.layoutFilterContainer)

        rgStatus = view.findViewById(R.id.rgStatus)
        rbOption1 = view.findViewById(R.id.rbOption1)
        rbOption2 = view.findViewById(R.id.rbOption2)
        rbOption3 = view.findViewById(R.id.rbOption3)

        edtFromDate = view.findViewById(R.id.edtFromDate)
        edtToDate = view.findViewById(R.id.edtToDate)

        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnConfirmFilter = view.findViewById(R.id.btnConfirmFilter)

        recyclerView = view.findViewById(R.id.recyclerView)
    }

    //Hàm sử dụng view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Custom lại header
        setupCustomHeader(
            view = view,
            title = "Mượn/Trả",
            subtitle = "*Lấy tổng số phiếu - số trễ hạn"
        )

        initViews(view)
        //Gán List data mẫu
        sampleDataList=  getSampleData().toMutableList()

        //Khi có click vào item sẽ mở Dialog thông tin chi tiết
        adapter = BorrowPayAdapter { clickedItem ->
            // Dùng loanId để tìm ra phiên bản dữ liệu mới nhất
            val freshItem = sampleDataList.find { it.loanId == clickedItem.loanId } ?: clickedItem

            // 1. Đưa dữ liệu cho ViewModel mang đi
            loanSharedViewModel.selectedLoanToView.value = freshItem

            // 2. Chuyển trang qua LoanFragment dựa vào cái ID action vừa tạo trong XML
            findNavController().navigate(R.id.action_borrowPay_to_loanDetail)
        }
        //Đổ dữ liệu vào RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(sampleDataList)

        // 3. Đón lõng dữ liệu trả về (khi người dùng tắt LoanFragment)
        setupLoanResultObserver()


        //Xử lý sự kiện nhấn nút lọc
        btnToggleFilter.setOnClickListener {
            if (layoutFilterContainer.visibility == View.GONE) {
                // 1. Hiển thị bộ lọc
                layoutFilterContainer.visibility = View.VISIBLE

                // 2. Đổi màu NỀN của nút
                val activeBgColor = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.backgroundTintList = activeBgColor

                // 3. Đổi màu ICON bên trong
                val activeIconColor = ContextCompat.getColorStateList(requireContext(), R.color.white)
                btnToggleFilter.imageTintList = activeIconColor

            } else {
                // 1. Ẩn bộ lọc
                layoutFilterContainer.visibility = View.GONE

                // 2. Trả NỀN về mặc định
                // Gán null để xóa lớp màu phủ, giúp nút lấy lại màu gốc của @drawable/bg_filter
                btnToggleFilter.backgroundTintList = null

                // 3. Trả ICON về màu mặc định
                // Theo như XML của bạn, màu gốc của Icon là btn_primary
                val defaultIconColor = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = defaultIconColor
            }
        }

        edtFromDate.setOnClickListener { showDatePickerForFilter(edtFromDate) }
        edtToDate.setOnClickListener { showDatePickerForFilter(edtToDate) }
        // Nút XÁC NHẬN LỌC
        btnConfirmFilter.setOnClickListener {
            applyFilter()
        }

        // Nút ĐẶT LẠI (RESET)
        btnResetFilter.setOnClickListener {
            // Xóa chữ 2 ô ngày
            edtFromDate.text.clear()
            edtToDate.text.clear()
            // Đưa Radio về "Tất cả"
            rgStatus.check(R.id.rbOption1)
            // Hiển thị lại toàn bộ danh sách
            adapter.submitList(sampleDataList)
        }

        setupViewModelObserver()

    }


    //Hàm dùng để hiển thị DatePicker để chọn ngày
    private fun showDatePickerForFilter(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    //Hàm xử lý bộ lọc
    private fun applyFilter() {
        val fromDateStr = edtFromDate.text.toString()
        val toDateStr = edtToDate.text.toString()
        //Dịch ngày tháng năm thành chuỗi và ngược lại
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Gán thời gian thực cho today và chỉ lấy ngày bỏ qua thời gian
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time



        val filteredList = sampleDataList.filter { item ->
            // A. Lọc theo TRẠNG THÁI
            val statusMatch = when (rgStatus.checkedRadioButtonId) {
                R.id.rbOption2 -> item.overallStatus == "BORROWING"
                R.id.rbOption3 -> {
                    // Logic TRỄ HẠN: Đang mượn VÀ Hạn trả < Hôm nay
                    try {
                        //kiểm tra phiếu (Phải đang mượn, dueDate phải được dịch thành công và kiểm tra xem đã vượt ngày trả hay chưa)
                        val dueDate = sdf.parse(item.dueDate)
                        item.overallStatus == "BORROWING" && dueDate != null && dueDate.before(today)
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> true // Tất cả
            }

            // B. Lọc theo THỜI GIAN HẠN TRẢ
            var dateMatch = true
            try {
                //Dịch dueDate
                val itemDueDate = sdf.parse(item.dueDate)

                if (itemDueDate != null) {
                    //Nếu dịch thành công kiểm tra xem chuỗi fromDate và toDate cs tồn tại không
                    if (fromDateStr.isNotEmpty() && toDateStr.isNotEmpty()) {
                        //Nếu có thì dịch hai chuỗi này
                        val fromDate = sdf.parse(fromDateStr)
                        val toDate = sdf.parse(toDateStr)

                        if (fromDate != null && toDate != null) {
                            //Nếu dịch thành công thì kiểm tra xem hạn trả có nằm giữa fromDate và toDate
                            dateMatch = !itemDueDate.before(fromDate) && !itemDueDate.after(toDate)
                        }
                        //Nếu chỉ fromDate tồn tại Thì DueDate phải nằm sau FromDate
                    } else if (fromDateStr.isNotEmpty()) {
                        val fromDate = sdf.parse(fromDateStr)
                        if (fromDate != null) dateMatch = !itemDueDate.before(fromDate)
                    } //Nếu chỉ toDate tồn tại Thì DueDate phải nằm trước toDate
                    else if (toDateStr.isNotEmpty()) {
                        val toDate = sdf.parse(toDateStr)
                        if (toDate != null) dateMatch = !itemDueDate.after(toDate)
                    }
                }
            } catch (e: Exception) {
                //Nếu itemDueDate dịch lỗi thì hiển thị ra chuỗi báo lỗi
                e.printStackTrace()
            }

            // Kết hợp cả hai điều kiện
            statusMatch && dateMatch
        }

        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy phiếu mượn phù hợp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModelObserver() {
        sharedViewModel.filterType.observe(viewLifecycleOwner) { type ->
            if (type != null) {
                // Mở layout bộ lọc
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)

                // Check vào RadioButton
                when (type) {
                    "BORROWING" -> rgStatus.check(R.id.rbOption2)
                    "DELAYED" -> rgStatus.check(R.id.rbOption3)
                }

                applyFilter()

                // Đọc xong thì phải xóa để không bị lặp lại
                sharedViewModel.clearFilter()
            }
        }
    }

    //Hàm này kiểm giúp kiểm tra dữ liệu gửi về có thay đổi hay không nếu có thì cập nhật lại dữ liệu cho sampleDataList
    private fun setupLoanResultObserver() {
        loanSharedViewModel.updatedLoanToSave.observe(viewLifecycleOwner) { updatedItem ->
            if (updatedItem != null) {
                // Nếu có dữ liệu trả về, tiến hành cập nhật vào danh sách gốc
                val index = sampleDataList.indexOfFirst { it.loanId == updatedItem.loanId }
                if (index != -1) {
                    sampleDataList[index] = updatedItem
                }

                // Lọc và hiển thị lại lên màn hình
                applyFilter()

                // XÓA dữ liệu trong ViewModel để không bị lặp lại thao tác này khi xoay màn hình
                loanSharedViewModel.updatedLoanToSave.value = null
            }
        }
    }

}