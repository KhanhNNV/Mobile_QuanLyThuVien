package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.adapter.BorrowPayAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.setupCustomHeader
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.example.quanlythuvien.viewmodel.SharedFilterLoanViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class BorrowPayFragment : Fragment() {

    // (Hàm getSampleData giữ nguyên như cũ...)
    private fun getSampleData(): List<LoanItemData> {
        // Lưu ý: Ngày hiện tại giả định là 09/04/2026
        return listOf(
            // 1. Phiếu mượn bình thường (Còn hạn)
            LoanItemData(
                loanId = 1L,
                borrowDate = "01/04/2026",
                overallStatus = "BORROWING",
                readerName = "Nguyễn Văn A",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(201L, "Lập trình Android", "Google", "Lập trình", null, "15/04/2026", "BORROWING"),
                    LoanDetailItemData(202L, "Kotlin Design Patterns", "JetBrains", "Công nghệ", null, "20/04/2026", "BORROWING")
                )
            ),

            // 2. Phiếu mượn TRỄ HẠN (Status là BORROWING nhưng có sách quá hạn 05/04)
            LoanItemData(
                loanId = 2L,
                borrowDate = "25/03/2026",
                overallStatus = "BORROWING",
                readerName = "Trần Thị B (Trễ hạn)",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(203L, "Cấu trúc dữ liệu", "TG. Alpha", "Giáo trình", null, "05/04/2026", "BORROWING")
                )
            ),

            // 3. Phiếu ĐÃ TRẢ HOÀN TẤT
            LoanItemData(
                loanId = 3L,
                borrowDate = "10/03/2026",
                overallStatus = "RETURNED",
                readerName = "Lê Quang C",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(204L, "Giải thuật hiện đại", "TG. Beta", "Kỹ thuật", "18/03/2026", "20/03/2026", "RETURNED")
                )
            ),

            // 4. Phiếu QUÁ HẠN (Status OVERDUE từ server/database)
            LoanItemData(
                loanId = 4L,
                borrowDate = "15/03/2026",
                overallStatus = "OVERDUE",
                readerName = "Phạm Hoàng D",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(205L, "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học", null, "30/03/2026", "BORROWING")
                )
            ),

            // 5. Phiếu có SÁCH BỊ MẤT (LOST) - Tính là trễ hạn nếu quá ngày
            LoanItemData(
                loanId = 5L,
                borrowDate = "20/03/2026",
                overallStatus = "BORROWING",
                readerName = "Hoàng Văn E (Mất sách)",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(206L, "Clean Code", "Robert C. Martin", "Kỹ thuật", null, "01/04/2026", "LOST")
                )
            ),

            // 6. Phiếu mượn nhiều sách, một số đã trả, một số còn hạn
            LoanItemData(
                loanId = 6L,
                borrowDate = "05/04/2026",
                overallStatus = "BORROWING",
                readerName = "Ngô Thị F",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(207L, "Java Core", "Oracle", "Lập trình", "07/04/2026", "15/04/2026", "RETURNED"),
                    LoanDetailItemData(208L, "Spring Boot", "Pivotal", "Backend", null, "20/04/2026", "BORROWING")
                )
            ),

            // 7. Phiếu mượn cũ để test bộ lọc thời gian (Tháng 2)
            LoanItemData(
                loanId = 7L,
                borrowDate = "15/02/2026",
                overallStatus = "RETURNED",
                readerName = "Vũ Văn G",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(209L, "Tư duy hệ thống", "TG. Gamma", "Kỹ năng", "25/02/2026", "01/03/2026", "RETURNED")
                )
            ),

            // 8. Phiếu mượn trùng tên hoặc ID gần giống để test Search
            LoanItemData(
                loanId = 10L,
                borrowDate = "08/04/2026",
                overallStatus = "BORROWING",
                readerName = "Nguyễn Văn A (Người khác)",
                borrowedBooks = mutableListOf(
                    LoanDetailItemData(210L, "UI/UX Design", "Figma", "Thiết kế", null, "22/04/2026", "BORROWING")
                )
            )
        )
    }

    // =========================================================================
// KHAI BÁO CÁC VIEWMODEL (QUẢN LÝ DỮ LIỆU DÙNG CHUNG)
// =========================================================================

    /** ViewModel dùng để nhận tín hiệu lọc từ các màn hình khác (Ví dụ: Dashboard bấm vào) */
    private val sharedViewModel: SharedFilterLoanViewModel by activityViewModels()

    /** ViewModel "Người vận chuyển": Dùng để truyền dữ liệu giữa màn hình Danh sách và màn hình Chi tiết */
    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()


// =========================================================================
// KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (UI COMPONENTS)
// =========================================================================

    /** Nút nổi (Floating Action Button) để thêm một phiếu mượn mới */
    private lateinit var fasAddLoan: FloatingActionButton

    /** Ô nhập liệu để tìm kiếm nhanh (Tên độc giả, mã phiếu...) */
    private lateinit var autoSearch: AutoCompleteTextView

    /** Icon/Nút dùng để Ẩn hoặc Hiện bảng điều khiển bộ lọc */
    private lateinit var btnToggleFilter: ImageView

    /** Layout chứa toàn bộ các điều kiện lọc (Mặc định thường ẩn đi) */
    private lateinit var layoutFilterContainer: ConstraintLayout

// --- NHÓM LỌC THEO TRẠNG THÁI (RADIO GROUP) ---
    /** Nhóm chứa các nút chọn trạng thái (Chỉ được chọn 1 tại một thời điểm) */
    private lateinit var rgStatus: RadioGroup
    /** Lựa chọn: Chỉ hiển thị các phiếu đã quá hạn trả sách */
    private lateinit var rbOption1: RadioButton
    /** Lựa chọn: Chỉ hiển thị các phiếu đang trong quá trình mượn sách */
    private lateinit var rbOption2: RadioButton
    /** Lựa chọn: Chỉ hiển thị các phiếu đã hoàn tất việc trả sách */
    private lateinit var rbOption3: RadioButton

// --- NHÓM LỌC THEO KHOẢNG THỜI GIAN (DATE PICKER) ---
    /** Ô hiển thị/chọn ngày bắt đầu của khoảng thời gian cần lọc */
    private lateinit var edtFromDate: EditText
    /** Ô hiển thị/chọn ngày kết thúc của khoảng thời gian cần lọc */
    private lateinit var edtToDate: EditText

// --- NHÓM NÚT ĐIỀU KHIỂN BỘ LỌC ---
    /** Nút để xóa sạch mọi điều kiện lọc hiện tại, đưa danh sách về mặc định "Tất cả" */
    private lateinit var btnResetFilter: Button
    /** Nút để xác nhận và thực hiện việc lọc dữ liệu theo các tiêu chí đã chọn */
    private lateinit var btnConfirmFilter: Button

    /** Danh sách cuộn để hiển thị các thẻ (Card) phiếu mượn */
    private lateinit var recyclerView: RecyclerView


// =========================================================================
// KHAI BÁO DỮ LIỆU VÀ BỘ ĐIỀU PHỐI (DATA & ADAPTER)
// =========================================================================

    /** Danh sách gốc (Master List) chứa toàn bộ dữ liệu phiếu mượn được nạp vào App */
    private lateinit var sampleDataList: MutableList<LoanItemData>

    /** Cầu nối dùng để vẽ dữ liệu từ sampleDataList lên giao diện của từng dòng trong RecyclerView */
    private lateinit var adapter: BorrowPayAdapter


    //Chuyển đổi file xml thành UI
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_borrow_pay, container, false)
    }


    //Ánh xạ các vỉew
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Chỉnh sửa lại phần header cho phù hợp vs trang
        setupCustomHeader(view, "Mượn/Trả", "*Quản lý danh sách phiếu mượn")

        //Gọi hàm ánh xạ
        initViews(view)

        // Khởi tạo dữ liệu thông qua ViewModel (Chỉ nạp lần đầu)
        loanSharedViewModel.loadData(getSampleData())

        // Gán biến tham chiếu để dùng trong hàm applyFilter()
        // Lưu ý: Bây giờ sampleDataList trỏ thẳng vào Master List trong ViewModel
        sampleDataList = loanSharedViewModel.masterLoanList

        adapter = BorrowPayAdapter { clickedItem ->
            val freshItem = sampleDataList.find { it.loanId == clickedItem.loanId } ?: clickedItem
            loanSharedViewModel.selectedLoanToView.value = freshItem
            findNavController().navigate(R.id.action_borrowPay_to_loanDetail)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter.submitList(sampleDataList)

        //Lắng nghe xem thử data đã update hay delete gì không
        setupLoanResultObserver()


        //Lắng nghe sự thay đổi của ô tìm kiếm để lọc danh sách dữ liệu cho phù hợp
        autoSearch.addTextChangedListener { applyFilter() }


        //Ẩn hiện bộ lọc
        btnToggleFilter.setOnClickListener {
            if (layoutFilterContainer.visibility == View.GONE) {
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            } else {
                layoutFilterContainer.visibility = View.GONE
                btnToggleFilter.backgroundTintList = null
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
            }
        }

        //Hiện datepicker khi muốn chọn ngày
        edtFromDate.setOnClickListener { showDatePickerForFilter(edtFromDate) }
        edtToDate.setOnClickListener { showDatePickerForFilter(edtToDate) }

        btnConfirmFilter.setOnClickListener { applyFilter() }


        btnResetFilter.setOnClickListener {
            edtFromDate.text.clear()
            edtToDate.text.clear()
            rgStatus.clearCheck()

            applyFilter()
        }

        setupViewModelObserver()
    }


    //Hàm hiện thị DatePicker để chọn ngày
    private fun showDatePickerForFilter(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

   //Hàm lọc danh sách khi có sự kiện thay đổi danh sách
    private fun applyFilter() {
        //Lấy chuỗi ngày mà người dùng chọn
        val fromDateStr = edtFromDate.text.toString()
        val toDateStr = edtToDate.text.toString()
       //Lấy dữ liệu ô tìm kiếm
        val searchQuery = autoSearch.text.toString().trim().lowercase()
       //Dịch ngày thành chuỗi và ngược lại
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Lấy thời gian hiện tại (chỉ lấy ngày, bỏ qua giờ phút giây) để so sánh trễ hạn
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val filteredList = sampleDataList.filter { item ->

            // -----------------------------------------------------------------------
            // BƯỚC 1: XÁC ĐỊNH TRẠNG THÁI THỰC TẾ
            // -----------------------------------------------------------------------
            val actualStatus = if (item.overallStatus == "RETURNED") {
                "RETURNED"
            } else {
                // Kiểm tra xem trong list sách mượn có cuốn nào chưa trả mà đã quá hạn không
                val hasOverdueBook = item.borrowedBooks.any { book ->
                    if (book.status != "RETURNED") {
                        val dueDate = sdf.parse(book.dueDate)
                        dueDate != null && dueDate.before(today)
                    } else false
                }
                if (hasOverdueBook) "OVERDUE" else "BORROWING"
            }

            // -----------------------------------------------------------------------
            // BƯỚC 2: ĐIỀU KIỆN 1 - LỌC THEO TRẠNG THÁI (Dựa trên trạng thái thực tế)
            // -----------------------------------------------------------------------
            val statusMatch = when (rgStatus.checkedRadioButtonId) {
                R.id.rbOption1 -> actualStatus == "OVERDUE"   // Lọc Quá hạn
                R.id.rbOption2 -> actualStatus == "BORROWING" // Lọc Đang mượn (đúng nghĩa)
                R.id.rbOption3 -> actualStatus == "RETURNED"  // Lọc Đã trả
                else -> true // Không chọn radio nào (checkedId == -1) -> Mặc định là TẤT CẢ
            }

            // -----------------------------------------------------
            // BƯỚC 3: ĐIỀU KIỆN 2 - LỌC THEO THỜI GIAN MƯỢN
            // -----------------------------------------------------
            var dateMatch = true
            try {
                val itemBorrowDate = sdf.parse(item.borrowDate)
                if (itemBorrowDate != null) {
                    if (fromDateStr.isNotEmpty() && toDateStr.isNotEmpty()) {
                        val fromDate = sdf.parse(fromDateStr)
                        val toDate = sdf.parse(toDateStr)
                        if (fromDate != null && toDate != null) {
                            dateMatch = !itemBorrowDate.before(fromDate) && !itemBorrowDate.after(toDate)
                        }
                    } else if (fromDateStr.isNotEmpty()) {
                        val fromDate = sdf.parse(fromDateStr)
                        if (fromDate != null) dateMatch = !itemBorrowDate.before(fromDate)
                    } else if (toDateStr.isNotEmpty()) {
                        val toDate = sdf.parse(toDateStr)
                        if (toDate != null) dateMatch = !itemBorrowDate.after(toDate)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }

            // -----------------------------------------------------
            // BƯỚC 4: ĐIỀU KIỆN 3 - LỌC THEO TỪ KHÓA TÌM KIẾM
            // -----------------------------------------------------
            val searchMatch = if (searchQuery.isEmpty()) true else {
                item.loanId.toString().contains(searchQuery) || item.readerName.lowercase().contains(searchQuery)
            }

            // Kết hợp cả 3 điều kiện
            statusMatch && dateMatch && searchMatch
        }

        // Cập nhật lên RecyclerView thông qua Adapter
        adapter.submitList(filteredList)

        // Thông báo nếu không có kết quả nào thỏa mãn
        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy phiếu phù hợp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModelObserver() {
        sharedViewModel.filterType.observe(viewLifecycleOwner) { type ->
            if (type != null) {
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                when (type) {
                    "OVERDUE" -> rgStatus.check(R.id.rbOption1)
                    "BORROWING" -> rgStatus.check(R.id.rbOption2)
                    "RETURNED" -> rgStatus.check(R.id.rbOption3)
                }
                applyFilter()
                sharedViewModel.clearFilter()
            }
        }
    }


    //Hàm lắng nghe những thay đổi khi LoanFragment thay đổi dữ liệu
    private fun setupLoanResultObserver() {
        // Lắng nghe tín hiệu XÓA
        loanSharedViewModel.deletedLoanId.observe(viewLifecycleOwner) { deletedId ->
            if (deletedId != null) {
                // Xóa trực tiếp trong Master List của ViewModel
                loanSharedViewModel.masterLoanList.removeAll { it.loanId == deletedId }

                // Cập nhật giao diện
                applyFilter()

                loanSharedViewModel.deletedLoanId.value = null
            }
        }

        // Lắng nghe tín hiệu CẬP NHẬT
        loanSharedViewModel.updatedLoanToSave.observe(viewLifecycleOwner) { updatedItem ->
            if (updatedItem != null) {
                val index = loanSharedViewModel.masterLoanList.indexOfFirst { it.loanId == updatedItem.loanId }
                if (index != -1) {
                    loanSharedViewModel.masterLoanList[index] = updatedItem
                }
                applyFilter()
                loanSharedViewModel.updatedLoanToSave.value = null
            }
        }
    }
}