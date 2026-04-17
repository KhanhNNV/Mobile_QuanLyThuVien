package com.example.quanlythuvien.ui.reader

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.tabs.TabLayout
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {

    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<MockReaderBook> = emptyList()

    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    private val detailViewModel: ReaderDetailViewModel by viewModels {
        GenericViewModelFactory {
            val app = requireContext().applicationContext as Application
            val apiService = RetrofitClient.getInstance(app).create(ReaderApiService::class.java)
            ReaderDetailViewModel(ReaderRepository(apiService))
        }
    }

    // Biến tạm để nhớ thông tin chờ lưu PDF
    private var pendingPdfCode = ""
    private var pendingPdfName = ""
    private var pendingPdfPhone = ""
    
    private var currentReaderName = ""
    private var currentReaderPhone = ""
    private var currentReaderId = -1L
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentReaderId = arguments?.getLong("readerId", -1L) ?: -1L
        currentReaderName = arguments?.getString("readerName").orEmpty()
        currentReaderPhone = arguments?.getString("readerPhone").orEmpty()


        bindHeader(view, currentReaderName, currentReaderPhone, currentReaderId)
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
        readerId: Long
    ) {
        view.findViewById<TextView>(R.id.tvReaderName).text = readerName
        view.findViewById<TextView>(R.id.tvReaderPhone).text = readerPhone
        view.findViewById<TextView>(R.id.tvAvatar).text = readerName.firstOrNull()?.uppercase() ?: ""
        view.findViewById<TextView>(R.id.tvHeaderTitle).text = "Chi tiết độc giả"

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
        setupMockData()

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)
        bookAdapter = ReaderDetailAdapter { selectedBook ->
            val mockLoan = LoanItemData(
                loanId = 12345,
                readerName = readerName.ifBlank { "Độc giả" },
                borrowDate = selectedBook.borrowDate,
                overallStatus = if (selectedBook.isReturned) "RETURNED" else "BORROWING",
                borrowedBooks = mutableListOf()
            )
            loanSharedViewModel.selectedLoanToView.value = mockLoan
            findNavController().navigate(R.id.loanFragment)
        }

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
    }

    private fun setupTabs(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterBooks(0) // đang mượn
                    1 -> filterBooks(1) // đã trả
                    2 -> filterBooks(2) // quá hạn
                }
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

                    pendingPdfCode = readerPhone
                    pendingPdfName = readerName
                    pendingPdfPhone = readerPhone

                    // Mở hộp thoại chọn thư mục lưu file với tên mặc định
                    createPdfLauncher.launch("TheDocGia_${pendingPdfCode}.pdf")
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

    private fun showDeleteConfirmationDialog(readerId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa Độc Giả")
            .setMessage("Bạn có chắc chắn muốn xóa độc giả này?")
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

    private fun setupMockData() {
        allDataMockReaderBook = listOf(
            MockReaderBook("Lập trình Java căn bản", "Trần Văn B", "978-111", "01/10/2025", "15/10/2025", isOverdue = true, isReturned = false),
            MockReaderBook("Kotlin Coroutines", "JetBrains", "978-222", "10/10/2025", "24/10/2025", isOverdue = false, isReturned = false),
            MockReaderBook("Cấu trúc dữ liệu & Giải thuật", "Nguyễn C", "978-333", "01/09/2025", "15/09/2025", isOverdue = false, isReturned = true),
            MockReaderBook("Clean Code", "Robert C. Martin", "978-444", "15/08/2025", "30/08/2025", isOverdue = false, isReturned = true)
        )
    }

    private fun filterBooks(status: Int) {
        val filtered = when (status) {
            0 -> allDataMockReaderBook.filter { !it.isReturned && !it.isOverdue }
            1 -> allDataMockReaderBook.filter { it.isReturned }
            2 -> allDataMockReaderBook.filter { !it.isReturned && it.isOverdue }
            else -> allDataMockReaderBook
        }
        bookAdapter.submitList(filtered)
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