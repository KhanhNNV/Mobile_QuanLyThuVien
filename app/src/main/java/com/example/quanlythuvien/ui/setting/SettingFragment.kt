package com.example.quanlythuvien.ui.setting

import android.content.Context
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.enums.TypeFeeConfig
import com.example.quanlythuvien.data.model.response.FeeConfigResponse
import com.example.quanlythuvien.data.remote.FeeConfigApiService
import com.example.quanlythuvien.data.repository.FeeConfigRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private lateinit var cvManageStaff: MaterialCardView
    private lateinit var cvLoanPolicy: MaterialCardView
    private lateinit var cvCategory: MaterialCardView
    private lateinit var btnLogout: Button

    private lateinit var layoutFeeHeader: LinearLayout
    private lateinit var cardFeeContainer: View
    private lateinit var imgToggleFee: ImageView

    private lateinit var edtRegistrationFee: TextInputEditText
    private lateinit var edtLateFee: TextInputEditText
    private lateinit var edtLostFeeExtra: TextInputEditText
    private lateinit var edtDamageFee: TextInputEditText
    private lateinit var btnSaveFees: Button

    private var isExpanded = false

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
        btnLogout = view.findViewById(R.id.btnLogout)

        layoutFeeHeader = view.findViewById(R.id.layoutFeeHeader)
        cardFeeContainer = view.findViewById(R.id.cardFeeContainer)
        imgToggleFee = view.findViewById(R.id.imgToggleFee)

        edtRegistrationFee = view.findViewById(R.id.edtRegistrationFee)
        edtLateFee = view.findViewById(R.id.edtLateFee)
        edtLostFeeExtra = view.findViewById(R.id.edtLostFeeExtra)
        edtDamageFee = view.findViewById(R.id.edtDamageFee)
        btnSaveFees = view.findViewById(R.id.btnSaveFees)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(FeeConfigApiService::class.java)
        val repository = FeeConfigRepository(apiService)

        val factory = GenericViewModelFactory {
            SettingViewModel(repository)
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
                            btnSaveFees.text = "ĐANG LƯU..."
                        }
                        is SettingState.SuccessGetFees -> {
                            btnSaveFees.isEnabled = true
                            btnSaveFees.text = "LƯU THAY ĐỔI"
                            bindDataToUI(state.fees)
                        }
                        is SettingState.SuccessUpdate -> {
                            btnSaveFees.isEnabled = true
                            btnSaveFees.text = "LƯU THAY ĐỔI"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        is SettingState.Error -> {
                            btnSaveFees.isEnabled = true
                            btnSaveFees.text = "LƯU THAY ĐỔI"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun bindDataToUI(configs: List<FeeConfigResponse>) {

        for (config in configs) {
            val amountStr = config.amount.toLong().toString() // Format số nguyên
            when (config.feeType) {
                TypeFeeConfig.REG_NORMAL -> edtRegistrationFee.setText(amountStr)
                TypeFeeConfig.LATE_PER_DAY -> edtLateFee.setText(amountStr)
                TypeFeeConfig.LOST_BOOK -> edtLostFeeExtra.setText(amountStr)
                TypeFeeConfig.DAMAGE_FEE -> edtDamageFee.setText(amountStr)
            }
        }
    }

    private fun handleEvents() {
        // Navigation Events
        cvManageStaff.setOnClickListener { findNavController().navigate(R.id.staffListFragment) }
        cvLoanPolicy.setOnClickListener { findNavController().navigate(R.id.loanPolicyFragment) }
        cvCategory.setOnClickListener { findNavController().navigate(R.id.categoryListFragment) }

        // Accordion (Toggle) Event
        openLayoutFeeHeaderEvent()

        // Save Fees Event
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

            viewModel.updateFeeConfigs(updates)
        }

        // Logout Event
        btnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putBoolean("isLoggedIn", false)
                remove("username")
                remove("userRole")
                apply()
            }

            val navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            findNavController().navigate(R.id.welcomeFragment, null, navOptions)
        }
    }

    private fun openLayoutFeeHeaderEvent(){
        layoutFeeHeader.setOnClickListener {
            isExpanded = !isExpanded

            if (isExpanded) {
                // Mở Accordion
                cardFeeContainer.visibility = View.VISIBLE
                imgToggleFee.animate()
                    .rotation(90f)
                    .setDuration(200)
                    .start()

                // Kiểm tra State hiện tại của ViewModel
                // CHỈ gọi API nếu chưa có dữ liệu (trạng thái Idle) hoặc lần trước bị lỗi (Error)
                val currentState = viewModel.state.value
                if (currentState is SettingState.Idle || currentState is SettingState.Error) {
                    viewModel.fetchFeeConfigs()
                }
            } else {
                // Đóng Accordion
                cardFeeContainer.visibility = View.GONE
                imgToggleFee.animate()
                    .rotation(0f)
                    .setDuration(200)
                    .start()
            }
        }
    }
}