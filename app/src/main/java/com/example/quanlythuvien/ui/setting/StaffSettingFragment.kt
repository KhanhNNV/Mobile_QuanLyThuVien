package com.example.quanlythuvien.ui.setting

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.example.quanlythuvien.data.remote.LibraryApiService
import com.example.quanlythuvien.data.repository.FeeConfigRepository
import com.example.quanlythuvien.data.repository.LibraryRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StaffSettingFragment : Fragment(R.layout.fragment_staff_setting) {

    private lateinit var cvLoanPolicy: MaterialCardView
    private lateinit var cvCategory: MaterialCardView
    private lateinit var btnLogout: Button

    private lateinit var tvRegistrationFee: TextView
    private lateinit var tvLateFee: TextView
    private lateinit var tvLostFeeExtra: TextView
    private lateinit var tvDamageFee: TextView

    private lateinit var viewModel: SettingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(
            view = view,
            title = "Cài đặt",
            subtitle = "Dành cho nhân viên"
        )

        initViews(view)
        setupViewModel()
        observeViewModel()
        handleEvents()

        // Gọi API lấy dữ liệu phí khi mở màn hình
        viewModel.fetchFeeConfigs()
    }

    private fun initViews(view: View) {
        cvLoanPolicy = view.findViewById(R.id.cvLoanPolicy)
        cvCategory = view.findViewById(R.id.cvCategory)
        btnLogout = view.findViewById(R.id.btnLogout)

        tvRegistrationFee = view.findViewById(R.id.tvRegistrationFee)
        tvLateFee = view.findViewById(R.id.tvLateFee)
        tvLostFeeExtra = view.findViewById(R.id.tvLostFeeExtra)
        tvDamageFee = view.findViewById(R.id.tvDamageFee)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(FeeConfigApiService::class.java)
        val repository = FeeConfigRepository(apiService)

        val libraryApiService = retrofit.create(LibraryApiService::class.java)
        val libraryRepository = LibraryRepository(libraryApiService)

        val factory = GenericViewModelFactory {
            SettingViewModel(repository,libraryRepository)
        }
        viewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    when (state) {
                        is SettingState.SuccessGetFees -> {
                            bindDataToUI(state.fees)
                        }
                        is SettingState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun bindDataToUI(configs: List<FeeConfigResponse>) {
        for (config in configs) {
            val amountFormatted = formatCurrency(config.amount)
            when (config.feeType) {
                TypeFeeConfig.REG_NORMAL -> tvRegistrationFee.text = amountFormatted
                TypeFeeConfig.LATE_PER_DAY -> tvLateFee.text = amountFormatted
                TypeFeeConfig.LOST_BOOK -> tvLostFeeExtra.text = amountFormatted
                TypeFeeConfig.DAMAGE_FEE -> tvDamageFee.text = amountFormatted
            }
        }
    }

    // Hàm hỗ trợ format tiền tệ (Ví dụ: 50000 -> "50.000 VNĐ")
    private fun formatCurrency(amount: Double): String {
        return "%,.0f VNĐ".format(amount).replace(",", ".")
    }

    private fun handleEvents() {
        // Navigation Events
        cvLoanPolicy.setOnClickListener { findNavController().navigate(R.id.loanPolicyFragment) }
        cvCategory.setOnClickListener { findNavController().navigate(R.id.categoryListFragment) }

        // Logout Event
        btnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putBoolean("isLoggedIn", false)
                remove("username")
                remove("userRole")
                apply()
            }

            // Xóa backstack và về trang welcome
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            findNavController().navigate(R.id.welcomeFragment, null, navOptions)
        }
    }
}