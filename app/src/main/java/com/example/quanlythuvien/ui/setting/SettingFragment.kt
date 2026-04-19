package com.example.quanlythuvien.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.enums.TypeFeeConfig
import com.example.quanlythuvien.data.model.response.FeeConfigResponse
import com.example.quanlythuvien.data.remote.FeeConfigApiService
import com.example.quanlythuvien.data.remote.LibraryApiService // Thêm import này
import com.example.quanlythuvien.data.repository.FeeConfigRepository
import com.example.quanlythuvien.data.repository.LibraryRepository // Thêm import này
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingFragment : Fragment(R.layout.fragment_setting) {

    // Views - Navigation
    private lateinit var cvManageStaff: MaterialCardView
    private lateinit var cvLoanPolicy: MaterialCardView
    private lateinit var cvCategory: MaterialCardView

    // Views - Fee Management
    private lateinit var layoutFeeHeader: LinearLayout
    private lateinit var cardFeeContainer: View
    private lateinit var imgToggleFee: ImageView
    private lateinit var edtRegistrationFee: TextInputEditText
    private lateinit var edtLateFee: TextInputEditText
    private lateinit var edtLostFeeExtra: TextInputEditText
    private lateinit var edtDamageFee: TextInputEditText
    private lateinit var btnSaveFees: Button

    // Views - Library Management
    private lateinit var layoutLibraryHeader: LinearLayout
    private lateinit var cardLibraryContainer: View
    private lateinit var imgToggleLibrary: ImageView
    private lateinit var edtManageLibraryName: TextInputEditText
    private lateinit var edtManageLibraryAddress: TextInputEditText
    private lateinit var btnSaveLibraryInfo: Button

    // Biến trạng thái Toggle
    private var isFeeExpanded = false
    private var isLibraryExpanded = false

    private lateinit var viewModel: SettingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(
            view = view,
            title = "Cài đặt",
            subtitle = "Dành cho thủ thư"
        )


        initViews(view)
        setupViewModel()
        observeViewModel()
        handleEvents()
    }

    private fun initViews(view: View) {
        cvManageStaff = view.findViewById(R.id.cvManageStaff)
        cvLoanPolicy = view.findViewById(R.id.cvLoanPolicy)
        cvCategory = view.findViewById(R.id.cvCategory)

        // Ánh xạ View Fee
        layoutFeeHeader = view.findViewById(R.id.layoutFeeHeader)
        cardFeeContainer = view.findViewById(R.id.cardFeeContainer)
        imgToggleFee = view.findViewById(R.id.imgToggleFee)
        edtRegistrationFee = view.findViewById(R.id.edtRegistrationFee)
        edtLateFee = view.findViewById(R.id.edtLateFee)
        edtLostFeeExtra = view.findViewById(R.id.edtLostFeeExtra)
        edtDamageFee = view.findViewById(R.id.edtDamageFee)
        btnSaveFees = view.findViewById(R.id.btnSaveFees)

        // Ánh xạ View Library
        layoutLibraryHeader = view.findViewById(R.id.layoutLibraryHeader)
        cardLibraryContainer = view.findViewById(R.id.cardLibraryContainer)
        imgToggleLibrary = view.findViewById(R.id.imgToggleLibrary)
        edtManageLibraryName = view.findViewById(R.id.edtManageLibraryName)
        edtManageLibraryAddress = view.findViewById(R.id.edtManageLibraryAddress)
        btnSaveLibraryInfo = view.findViewById(R.id.btnSaveLibraryInfo)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        val feeApiService = retrofit.create(FeeConfigApiService::class.java)
        val feeRepository = FeeConfigRepository(feeApiService)

        // Khởi tạo thêm LibraryRepository
        val libraryApiService = retrofit.create(LibraryApiService::class.java)
        val libraryRepository = LibraryRepository(libraryApiService)

        val factory = GenericViewModelFactory {
            SettingViewModel(feeRepository, libraryRepository)
        }
        viewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    when (state) {
                        is SettingState.Loading -> {
                            btnSaveFees.isEnabled = false
                            btnSaveLibraryInfo.isEnabled = false
                        }
                        // Trạng thái của Phí (Fees)
                        is SettingState.SuccessGetFees -> {
                            btnSaveFees.isEnabled = true
                            bindFeeDataToUI(state.fees)
                        }
                        is SettingState.SuccessUpdate -> {
                            btnSaveFees.isEnabled = true
                            Toast.makeText(requireContext(), "Cập nhật phí thành công!", Toast.LENGTH_SHORT).show()
                        }
                        // Trạng thái của Thư Viện (Library)
                        is SettingState.SuccessGetLibrary -> {
                            btnSaveLibraryInfo.isEnabled = true
                            edtManageLibraryName.setText(state.library.name)
                            edtManageLibraryAddress.setText(state.library.address)
                        }
                        is SettingState.SuccessUpdateLibrary -> {
                            btnSaveLibraryInfo.isEnabled = true
                            Toast.makeText(requireContext(), "Cập nhật thư viện thành công!", Toast.LENGTH_SHORT).show()
                        }
                        // Xử lý Lỗi chung
                        is SettingState.Error -> {
                            btnSaveFees.isEnabled = true
                            btnSaveLibraryInfo.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            btnSaveFees.isEnabled = true
                            btnSaveLibraryInfo.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun bindFeeDataToUI(configs: List<FeeConfigResponse>) {
        for (config in configs) {
            val amountStr = config.amount.toLong().toString()
            when (config.feeType) {
                TypeFeeConfig.REG_NORMAL -> edtRegistrationFee.setText(amountStr)
                TypeFeeConfig.LATE_PER_DAY -> edtLateFee.setText(amountStr)
                TypeFeeConfig.LOST_BOOK -> edtLostFeeExtra.setText(amountStr)
                TypeFeeConfig.DAMAGE_FEE -> edtDamageFee.setText(amountStr)
            }
        }
    }

    private fun handleEvents() {
        cvManageStaff.setOnClickListener { findNavController().navigate(R.id.staffListFragment) }
        cvLoanPolicy.setOnClickListener { findNavController().navigate(R.id.loanPolicyFragment) }
        cvCategory.setOnClickListener { findNavController().navigate(R.id.categoryListFragment) }

        openLayoutFeeHeaderEvent()
        handleBtnSaveFeesEvent()

        openLayoutLibraryHeaderEvent()
        handleBtnSaveLibraryEvent()
    }

    private fun openLayoutFeeHeaderEvent() {
        layoutFeeHeader.setOnClickListener {
            isFeeExpanded = !isFeeExpanded
            if (isFeeExpanded) {
                cardFeeContainer.visibility = View.VISIBLE
                imgToggleFee.animate().rotation(90f).setDuration(200).start()

                if (edtRegistrationFee.text.isNullOrEmpty() || edtRegistrationFee.text.toString() == "0") {
                    viewModel.fetchFeeConfigs()
                }
            } else {
                cardFeeContainer.visibility = View.GONE
                imgToggleFee.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun handleBtnSaveFeesEvent() {
        btnSaveFees.setOnClickListener {
            val regNormal = edtRegistrationFee.text.toString().toDoubleOrNull() ?: 0.0
            val lateFee = edtLateFee.text.toString().toDoubleOrNull() ?: 0.0
            val lostFee = edtLostFeeExtra.text.toString().toDoubleOrNull() ?: 0.0
            val damageFee = edtDamageFee.text.toString().toDoubleOrNull() ?: 0.0

            val updates = mapOf(
                TypeFeeConfig.REG_NORMAL to regNormal,
                TypeFeeConfig.LATE_PER_DAY to lateFee,
                TypeFeeConfig.LOST_BOOK to lostFee,
                TypeFeeConfig.DAMAGE_FEE to damageFee
            )
            viewModel.saveFeeConfigs(updates)
        }
    }


    private fun openLayoutLibraryHeaderEvent() {
        layoutLibraryHeader.setOnClickListener {
            isLibraryExpanded = !isLibraryExpanded
            if (isLibraryExpanded) {
                cardLibraryContainer.visibility = View.VISIBLE
                imgToggleLibrary.animate().rotation(90f).setDuration(200).start()

                if (edtManageLibraryName.text.isNullOrEmpty()) {
                    viewModel.fetchLibraryInfo()
                }
            } else {
                cardLibraryContainer.visibility = View.GONE
                imgToggleLibrary.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun handleBtnSaveLibraryEvent() {
        btnSaveLibraryInfo.setOnClickListener {
            val name = edtManageLibraryName.text.toString().trim()
            val address = edtManageLibraryAddress.text.toString().trim()

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ Tên và Địa chỉ thư viện", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateLibraryInfo(name, address)
        }
    }
}