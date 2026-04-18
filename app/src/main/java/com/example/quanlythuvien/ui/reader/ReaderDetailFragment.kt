package com.example.quanlythuvien.ui.reader

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import java.util.Locale
import java.time.format.DateTimeFormatter
import com.example.quanlythuvien.data.model.request.ExtendMembershipExpiryRequest
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.LoanDetailApiService
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.tabs.TabLayout
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.getValue

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {

    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<LoanDetailRepository> = emptyList()

    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    private val detailViewModel: ReaderDetailViewModel by viewModels {
        GenericViewModelFactory {
            val app = requireContext().applicationContext as Application

            // Dùng chung instance của RetrofitClient theo ý "lãnh đạo"
            val retrofit = RetrofitClient.getInstance(app)

            // Nhưng tạo ra 2 ApiService riêng biệt
            val readerApi = retrofit.create(ReaderApiService::class.java)
            val loanDetailApi = retrofit.create(LoanDetailApiService::class.java)

            // Khởi tạo các Repository tương ứng
            val readerRepo = ReaderRepository(readerApi)
            val loanDetailRepo = LoanDetailRepository(loanDetailApi)

            // Cuối cùng truyền cả 2 vào ViewModel
            ReaderDetailViewModel(readerRepo, loanDetailRepo)
        }
    }

    // Biến tạm để nhớ thông tin chờ lưu PDF
    private var pendingPdfCode = ""
    private var pendingPdfName = ""
    private var pendingPdfPhone = ""

    private var currentReaderName = ""
    private var currentReaderPhone = ""
    private var currentReaderId = -1L
    private var currentMembershipExpiry: String = ""

    private var currentReaderTotalBorrowBooks = 0
    private var currentReaderTotalReturnBooks = 0
    private var currentReaderTotalOverdueBooks = 0
    private var currentReaderDebt = BigDecimal.ZERO
    private var currentReaderBarcode = ""

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentReaderId = arguments?.getLong("readerId", -1L) ?: -1L
        currentReaderName = arguments?.getString("readerName").orEmpty()
        currentReaderPhone = arguments?.getString("readerPhone").orEmpty()
        currentMembershipExpiry = arguments?.getString("membershipExpiry") ?: ""
        currentReaderBarcode = arguments?.getString("readerBarcode")?: ""
        currentReaderDebt = (arguments?.getSerializable("readerDebt") as? BigDecimal) ?: BigDecimal.ZERO
        currentReaderTotalBorrowBooks = arguments?.getInt("readerTotalBorrowBooks")?: 0
        currentReaderTotalOverdueBooks = arguments?.getInt("readerTotalOverdueBooks")?: 0
        currentReaderTotalReturnBooks = arguments?.getInt("readerTotalReturnBooks")?: 0



        bindHeader(view, currentReaderName, currentReaderPhone, currentReaderId, currentReaderDebt, currentReaderTotalBorrowBooks,currentReaderTotalOverdueBooks, currentReaderTotalReturnBooks )
        setupRecycler(view, currentReaderName)
        setupTabs(view)
        observeViewModel(view)
    }

    override fun onResume() {
        super.onResume()
        if (currentReaderId > 0) {
            detailViewModel.getReaderDetail(currentReaderId)
        }
    }

    private fun bindHeader(
        view: View,
        readerName: String,
        readerPhone: String,
        readerId: Long,
        readerDebt: BigDecimal,
        readerTotalBorrowBooks: Int,
        readerTotalOverdueBooks: Int,
        readerTotalReturnBooks: Int
    ) {
        view.findViewById<TextView>(R.id.tvReaderName).text = readerName
        view.findViewById<TextView>(R.id.tvReaderPhone).text = readerPhone
        view.findViewById<TextView>(R.id.tvAvatar).text = readerName.firstOrNull()?.uppercase() ?: ""
        view.findViewById<TextView>(R.id.tvHeaderTitle).text = "Chi tiết độc giả"
        view.findViewById<TextView>(R.id.tvDebtAmount).text = readerDebt.toString() + "VNĐ"
        view.findViewById<TextView>(R.id.tvStatBorrowing).text = readerTotalBorrowBooks.toString()
        view.findViewById<TextView>(R.id.tvTotalBorrowed).text = readerTotalReturnBooks.toString()
        view.findViewById<TextView>(R.id.tvStatOverdue).text = readerTotalOverdueBooks.toString()



        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.ivMoreOption).setOnClickListener { anchor ->
            showOptionMenu(
                anchorView = anchor,
                readerId = currentReaderId,
                readerName = currentReaderName,
                readerPhone = currentReaderPhone,
                role = getCurrentRole()
            )
        }
    }

    private fun setupRecycler(view: View, readerName: String) {
        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)
        bookAdapter = ReaderDetailAdapter { selectedBook ->
//            loanSharedViewModel.selectedLoanToView.value = selectedBook
            findNavController().navigate(R.id.loanFragment)
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
    }

    private fun setupTabs(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val statusString = when (tab?.position) {
                    0 -> "BORROWING"
                    1 -> "RETURNED"
                    2 -> "OVERDUE"
                    else -> "BORROWING"
                }
                // Gọi API thông qua ViewModel
                detailViewModel.fetchReaderLoans(currentReaderId, statusString)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        view.post { filterBooks(0) }
    }

    private fun showOptionMenu(
        anchorView: View,
        readerId: Long,
        readerName: String,
        readerPhone: String,
        role: String
    ) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_reader_options, popup.menu)

        // STAFF: ẩn xóa
        if (role == "STAFF") {
            popup.menu.findItem(R.id.menuDeleteReader)?.isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuEditReader -> {
                    val bundle = Bundle().apply {
                        putString("readerName", readerName)
                        putString("readerPhone", readerPhone)
                        putLong("readerId", readerId)
                    }
                    findNavController().navigate(R.id.actionReaderDetailToReaderAdd, bundle)
                    true
                }
                //  BẮT SỰ KIỆN IN PDF
                R.id.menuPrintPdf -> {

                    pendingPdfCode = currentReaderBarcode
                    pendingPdfName = readerName
                    pendingPdfPhone = readerPhone

                    // Mở hộp thoại chọn thư mục lưu file với tên mặc định
                    createPdfLauncher.launch("TheDocGia_${pendingPdfCode}.pdf")
                    true
                }
                // Xử lý khi nhấn vào nút gia hạn
                R.id.menuExtendReader -> {
                    showExtendMembershipDialog(readerName, readerPhone)
                    true
                }
                //Xữ lý khi nhấn vào nút xóa reader
                R.id.menuDeleteReader -> {
                    showDeleteConfirmationDialog(readerId)
                    true
                }

                else -> false
            }
        }
        popup.setForceShowIcon(true)
        popup.show()
    }

    /**
     * Hiển thị Dialog gia hạn thẻ độc giả sử dụng NumberPicker
     */
    private fun showExtendMembershipDialog(readerName: String, readerPhone: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_extend_membership)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvExtendReaderInfo = dialog.findViewById<TextView>(R.id.tvExtendReaderInfo)
        val tvCurrentExpiryDate = dialog.findViewById<TextView>(R.id.tvCurrentExpiryDate)
        val numberPickerMonth = dialog.findViewById<NumberPicker>(R.id.numberPickerMonth)
        val tvNewExpiryDate = dialog.findViewById<TextView>(R.id.tvNewExpiryDate)
        val btnCancelExtend = dialog.findViewById<Button>(R.id.btnCancelExtend)
        val btnConfirmExtend = dialog.findViewById<Button>(R.id.btnConfirmExtend)

        // Hiển thị thông tin độc giả
        tvExtendReaderInfo.text = "$readerName - $readerPhone"

        // Hiển thị ngày hết hạn hiện tại
        val currentExpiryDisplay = if (currentMembershipExpiry.isNotEmpty()) {
            formatDisplayDate(currentMembershipExpiry)
        } else {
            "Chưa có thông tin"
        }
        tvCurrentExpiryDate.text = currentExpiryDisplay

        // Cấu hình NumberPicker
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 24
        numberPickerMonth.value = 2
        numberPickerMonth.wrapSelectorWheel = false

        // Hàm cập nhật ngày hết hạn mới
        fun updateNewExpiryDate(months: Int) {
            if (currentMembershipExpiry.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val currentDate = sdf.parse(currentMembershipExpiry)
                    if (currentDate != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = currentDate
                        calendar.add(Calendar.MONTH, months)
                        val newDate = calendar.time
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        tvNewExpiryDate.text = "Ngày hết hạn mới: ${outputFormat.format(newDate)}"
                    } else {
                        tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
                    }
                } catch (e: Exception) {
                    tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
                }
            } else {
                tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
            }
        }

        // Khởi tạo hiển thị ban đầu
        updateNewExpiryDate(2)

        // Lắng nghe sự kiện thay đổi của NumberPicker
        numberPickerMonth.setOnValueChangedListener { _, _, newVal ->
            updateNewExpiryDate(newVal)
        }

        // Xử lý nút Hủy
        btnCancelExtend.setOnClickListener {
            dialog.dismiss()
        }

        // Xử lý nút Xác nhận
        btnConfirmExtend.setOnClickListener {
            val months = numberPickerMonth.value.toLong()
            btnConfirmExtend.isEnabled = false
            btnConfirmExtend.text = "Đang xử lý..."
            extendMembership(currentReaderId, months, dialog, btnConfirmExtend)
        }

        dialog.show()
    }

    /**
     * Gọi API gia hạn thẻ độc giả
     */
    private fun extendMembership(readerId: Long, months: Long, dialog: Dialog, confirmButton: Button) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val retrofit = RetrofitClient.getInstance(requireContext())
                val apiService = retrofit.create(ReaderApiService::class.java)
                val repository = ReaderRepository(apiService)

                val request = ExtendMembershipExpiryRequest(monthRegis = months)
                val response = repository.extendMembershipExpiry(readerId, request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Gia hạn thành công! Đã gia hạn thêm $months tháng.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Cập nhật lại thông tin hiển thị
                    response.body()?.let { updatedReader ->
                        currentMembershipExpiry = updatedReader.membershipExpiry ?: ""
                        view?.findViewById<TextView>(R.id.tvExpireDate)?.text = formatDisplayDate(currentMembershipExpiry)
                    }

                    dialog.dismiss()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Dữ liệu không hợp lệ"
                        404 -> "Không tìm thấy độc giả"
                        403 -> "Bạn không có quyền thực hiện"
                        else -> "Lỗi: ${response.code()}"
                    }
                    Toast.makeText(requireContext(), "Gia hạn thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                    confirmButton.isEnabled = true
                    confirmButton.text = "XÁC NHẬN"
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                confirmButton.isEnabled = true
                confirmButton.text = "XÁC NHẬN"
            }
        }
    }

    /**
     * Format ngày tháng để hiển thị
     */
    private fun formatDisplayDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Hiển thị Dialog xác nhận xóa Reader.
     */
    private fun showDeleteConfirmationDialog(readerId: Long){
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa Độc Giả")
            .setMessage("Bạn có chắc chắn muốn xóa độc giả này?")
            //Nút xác nhận xóa
            .setPositiveButton("Có") { dialog, _ ->
                if (readerId <= 0L) {
                    Toast.makeText(requireContext(), "Không tìm thấy mã độc giả", Toast.LENGTH_SHORT).show()
                } else {
                    detailViewModel.deleteReader(readerId)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel(rootView: View) {

        detailViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            rootView.findViewById<View>(R.id.ivMoreOption).isEnabled = !loading
        }

        detailViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        detailViewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Đã xóa độc giả", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        detailViewModel.loanList.observe(viewLifecycleOwner) { loans ->
            // Cập nhật Adapter với danh sách từ API thay vì Mock
            bookAdapter.submitList(loans)
        }

        detailViewModel.readerData.observe(viewLifecycleOwner) { data ->
            currentReaderName = data.fullName
            currentReaderPhone = data.phone

            rootView.findViewById<TextView>(R.id.tvReaderName).text = currentReaderName
            val barcodeDisplay = data.barcode.ifBlank { "—" }
            rootView.findViewById<TextView>(R.id.tvReaderPhone).text =
                "${data.phone}  ·  $barcodeDisplay"
            rootView.findViewById<TextView>(R.id.tvAvatar).text = currentReaderName.firstOrNull()?.uppercase() ?: ""
            rootView.findViewById<TextView>(R.id.tvJoinDate).text = formatServerDate(data.createdAt)
            rootView.findViewById<TextView>(R.id.tvExpireDate).text = formatServerDate(data.membershipExpiry)

            val daysChip = rootView.findViewById<TextView>(R.id.tvMembershipDaysRemaining)
            daysChip.text = formatMembershipDaysRemaining(data.membershipExpiry)
            if (membershipDaysChipIsWarning(data.membershipExpiry)) {
                daysChip.setBackgroundResource(R.drawable.bg_chip_warning)
                daysChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_warning))
            } else {
                daysChip.setBackgroundResource(R.drawable.bg_chip_primary)
                daysChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_primary))
            }

            val statusView = rootView.findViewById<TextView>(R.id.tvReaderStatus)
            if (data.isBlocked) {
                statusView.visibility = View.VISIBLE
                statusView.text = "Đã chặn"
                statusView.setBackgroundResource(R.drawable.bg_chip_error)
                statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_error))
            } else {
                statusView.visibility = View.GONE
            }
        }
    }

    private fun getCurrentRole(): String {
        val rawRole = TokenManager(requireContext()).getRole().orEmpty()
        return when {
            rawRole.contains("ADMIN", ignoreCase = true) -> "ADMIN"
            rawRole.contains("STAFF", ignoreCase = true) -> "STAFF"
            else -> ""
        }
    }


    private fun filterBooks(tabPosition: Int) {
        val statusString = when (tabPosition) {
            0 -> "BORROWING"
            1 -> "RETURNED"
            2 -> "OVERDUE"
            else -> "BORROWING"
        }
        // Gọi ViewModel để lấy dữ liệu thật từ Server
        detailViewModel.fetchReaderLoans(currentReaderId, statusString)
    }

    //HÀM TẠO VÀ LƯU PDF
    private fun writePdfToUri(uri: android.net.Uri, code: String, name: String, phone: String) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(400, 300, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 16f

        paint.isFakeBoldText = true
        canvas.drawText("THẺ ĐỘC GIẢ THƯ VIỆN", 100f, 50f, paint)
        paint.isFakeBoldText = false

        canvas.drawText("Mã thẻ: $code", 50f, 100f, paint)
        canvas.drawText("Họ và tên: $name", 50f, 140f, paint)
        canvas.drawText("Số điện thoại: $phone", 50f, 180f, paint)

        pdfDocument.finishPage(page)
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(requireContext(), "Lưu PDF thành công! Mở mục Tệp (Files) để xem.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi khi lưu PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
    private val createPdfLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            // Nếu người dùng chọn chỗ lưu thành công, tiến hành vẽ và ghi PDF vào chỗ đó
            writePdfToUri(uri, pendingPdfCode, pendingPdfName, pendingPdfPhone)
        } else {
            Toast.makeText(requireContext(), "Đã hủy lưu file PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatServerDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "--/--/----"
        return runCatching { LocalDateTime.parse(raw).format(dateFormatter) }
            .getOrDefault(raw)
    }

    private fun expiryToLocalDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(raw).toLocalDate() }
            .recoverCatching { LocalDate.parse(raw) }
            .getOrNull()
    }

    private fun formatMembershipDaysRemaining(membershipExpiry: String?): String {
        val expiryDate = expiryToLocalDate(membershipExpiry)
        if (expiryDate == null) return "Thẻ: chưa có hạn"
        val days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
        return when {
            days < 0 -> "Quá hạn: ${-days} ngày"
            days == 0L -> "Còn hạn: hôm nay"
            else -> "Còn hạn: $days ngày"
        }
    }

    /** Chip vàng: quá hạn, hết hạn trong ngày, hoặc chưa có ngày hết hạn. */
    private fun membershipDaysChipIsWarning(membershipExpiry: String?): Boolean {
        val expiryDate = expiryToLocalDate(membershipExpiry)
        if (expiryDate == null) return true
        val days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
        return days <= 0L
    }

}