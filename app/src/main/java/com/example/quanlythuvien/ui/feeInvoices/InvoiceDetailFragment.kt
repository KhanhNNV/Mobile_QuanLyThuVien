// File: app/src/main/java/com/example/quanlythuvien/ui/feeInvoices/InvoiceDetailFragment.kt
package com.example.quanlythuvien.ui.feeInvoices

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.FeeInvoiceApiService
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.FeeInvoiceRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupHeaderWithBack
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceDetailFragment : Fragment(R.layout.fragment_invoice_detail) {

    private lateinit var viewModel: InvoiceViewModel
    private lateinit var sharedViewModel: InvoiceSharedViewModel

    private lateinit var tvDetailInvoiceId: TextView
    private lateinit var tvDetailReader: TextView
    private lateinit var tvDetailType: TextView
    private lateinit var tvDetailAmount: TextView
    private lateinit var tvDetailCreatedAt: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerPaymentMethod: Spinner
    private lateinit var btnSave: Button

    private var hasLoadedInvoice = false // Flag để tránh load lại nhiều lần

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Chi tiết hóa đơn")

        initViews(view)
        setupViewModels()
        setupSpinners()
        observeViewModels()
        handleEvents()

        // Lấy ID từ SharedViewModel và load dữ liệu
        loadInvoiceFromSharedViewModel()
    }

    private fun initViews(view: View) {
        tvDetailInvoiceId = view.findViewById(R.id.tvDetailInvoiceId)
        tvDetailReader = view.findViewById(R.id.tvDetailReader)
        tvDetailType = view.findViewById(R.id.tvDetailType)
        tvDetailAmount = view.findViewById(R.id.tvDetailAmount)
        tvDetailCreatedAt = view.findViewById(R.id.tvDetailCreatedAt)
        spinnerStatus = view.findViewById(R.id.spnUpdateStatus)
        spinnerPaymentMethod = view.findViewById(R.id.spnUpdatePaymentMethod)
        btnSave = view.findViewById(R.id.btnSaveInvoice)
    }

    private fun setupViewModels() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        val invoiceApiService = retrofit.create(FeeInvoiceApiService::class.java)

        val invoiceRepository = FeeInvoiceRepository(invoiceApiService)

        val factory = GenericViewModelFactory {
            InvoiceViewModel(invoiceRepository)
        }
        viewModel = ViewModelProvider(this, factory)[InvoiceViewModel::class.java]

        // SharedViewModel để lấy ID được chọn
        sharedViewModel = ViewModelProvider(requireActivity())[InvoiceSharedViewModel::class.java]
    }

    private fun setupSpinners() {
        val statuses = arrayOf("PAID", "UNPAID")
        val statusDisplayNames = statuses.map { viewModel.getStatusDisplayName(it) }
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusDisplayNames
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val paymentMethods = PaymentMethod.values().map { it.displayName }
        val paymentAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            paymentMethods
        )
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentMethod.adapter = paymentAdapter
    }

    private fun loadInvoiceFromSharedViewModel() {
        // Lấy ID từ value
        val invoiceId = sharedViewModel.selectedInvoiceId.value

        if (invoiceId != null && !hasLoadedInvoice) {
            hasLoadedInvoice = true
            viewModel.fetchInvoiceDetail(invoiceId)
        } else if (invoiceId == null) {
            // Nếu ko có ID, quay về list
            Toast.makeText(requireContext(), "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedInvoice.collectLatest { invoice ->
                        invoice?.let {
                            updateUI(it)
                        }
                    }
                }

                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is InvoiceState.Loading -> {
                                btnSave.isEnabled = false
                            }
                            is InvoiceState.SuccessDetail -> {
                                btnSave.isEnabled = true
                            }
                            is InvoiceState.SuccessAction -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                // Yêu cầu refresh list trước khi quay lại
                                sharedViewModel.requestRefreshList()
                                findNavController().popBackStack()
                            }
                            is InvoiceState.Error -> {
                                btnSave.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                // Nếu lỗi 404 hoặc không tìm thấy, quay về list
                                if (state.message.contains("404") || state.message.contains("Không tìm thấy")) {
                                    findNavController().popBackStack()
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(invoice: com.example.quanlythuvien.data.model.response.FeeInvoiceResponse) {
        tvDetailInvoiceId.text = "Mã hóa đơn: ${invoice.invoiceId}"
        tvDetailReader.text = "Độc giả: ${invoice.readerName}"
        tvDetailType.text = "Loại hóa đơn: ${getInvoiceTypeDisplay(invoice.type)}"
        tvDetailAmount.text = "Tổng tiền: ${viewModel.formatCurrency(invoice.totalAmount)}"
        tvDetailCreatedAt.text = "Ngày tạo: ${formatDate(invoice.createdAt)}"

        // Set status
        val statusIndex = when (invoice.status) {
            "PAID" -> 0
            "UNPAID" -> 1
            else -> 1
        }
        spinnerStatus.setSelection(statusIndex)

        // Set payment method (nếu có)
        val paymentMethodIndex = when (invoice.paymentMethod) {
            "CASH" -> 0
            "BANK_TRANSFER" -> 1
            else -> 0
        }
        spinnerPaymentMethod.setSelection(paymentMethodIndex)

        // Disable editing if already paid
        if (invoice.status == "PAID") {
            spinnerStatus.isEnabled = false
            spinnerPaymentMethod.isEnabled = false
            btnSave.isEnabled = false
            btnSave.text = "ĐÃ THANH TOÁN"
        } else {
            spinnerStatus.isEnabled = true
            spinnerPaymentMethod.isEnabled = true
            btnSave.isEnabled = true
            btnSave.text = "CẬP NHẬT HÓA ĐƠN"
        }
    }

    private fun handleEvents() {
        btnSave.setOnClickListener {
            val invoice = viewModel.selectedInvoice.value ?: return@setOnClickListener

            val selectedStatusIndex = spinnerStatus.selectedItemPosition
            val status = when (selectedStatusIndex) {
                0 -> "PAID"
                1 -> "UNPAID"
                else -> "UNPAID"
            }

            val selectedPaymentIndex = spinnerPaymentMethod.selectedItemPosition
            val paymentMethod = when (selectedPaymentIndex) {
                0 -> "CASH"
                1 -> "BANK_TRANSFER"
                2 -> "MOMO"
                3 -> "ZALOPAY"
                else -> "CASH"
            }

            viewModel.updateInvoice(invoice.invoiceId, status, paymentMethod)
        }
    }

    private fun getInvoiceTypeDisplay(type: String): String {
        return when (type) {
            "REGISTRATION" -> "Phí đăng ký thẻ"
            "RENEWAL" -> "Phí gia hạn"
            "PENALTY" -> "Phí phạt"
            "LATE_FEE" -> "Phí trả trễ"
            "DAMAGE_FEE" -> "Phí hư hỏng"
            "MEMBERSHIP_FEE" -> "Phí thành viên"
            else -> type
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear selected invoice khi fragment bị destroy
        sharedViewModel.clearSelectedInvoice()
        hasLoadedInvoice = false
    }
}