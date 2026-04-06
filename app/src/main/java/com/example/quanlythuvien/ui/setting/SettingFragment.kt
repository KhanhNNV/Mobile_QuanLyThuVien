package com.example.quanlythuvien.ui.setting

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class SettingFragment : Fragment(R.layout.fragment_setting) {


    private lateinit var switchStudentDiscount: MaterialSwitch
    private lateinit var layoutStudentDiscountContainer: LinearLayout
    private lateinit var edtStudentDiscount: TextInputEditText
    private lateinit var cvManageStaff: MaterialCardView
    private lateinit var cvLoanPolicy: MaterialCardView
    private lateinit var cvCategory: MaterialCardView
    private lateinit var btnLogout: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(
            view = view,
            title = "Cài đặt",
            subtitle = "*chưa biết ghi gì"
        )

        initViews(view)
        handleswitchStudentDiscountEvents()
        handleCardViewManageStaffEvent()
        handleCardViewLoanPolicyEvent()
        handleCardViewCategoryEvent()
        handleButtonLogOutEvent()

    }

    private fun initViews(view: View) {
        switchStudentDiscount = view.findViewById(R.id.switchStudentDiscount)
        layoutStudentDiscountContainer = view.findViewById(R.id.layoutStudentDiscountContainer)
        edtStudentDiscount = view.findViewById(R.id.edtStudentDiscount)
        cvManageStaff=view.findViewById(R.id.cvManageStaff)
        cvLoanPolicy=view.findViewById(R.id.cvLoanPolicy)
        cvCategory=view.findViewById(R.id.cvCategory)
        btnLogout=view.findViewById(R.id.btnLogout)
    }

    // Xử lý bật/tắt công tắc giảm giá sinh viên
    private fun handleswitchStudentDiscountEvents() {
        switchStudentDiscount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutStudentDiscountContainer.visibility = View.VISIBLE
            } else {
                layoutStudentDiscountContainer.visibility = View.GONE
                edtStudentDiscount.text?.clear()
            }
        }

    }

    private fun handleCardViewManageStaffEvent(){
        cvManageStaff.setOnClickListener {
            findNavController().navigate(R.id.staffListFragment)
        }
    }

    private fun handleCardViewLoanPolicyEvent(){
        cvLoanPolicy.setOnClickListener {
            findNavController().navigate(R.id.loanPolicyFragment)
        }

    }

    private fun handleCardViewCategoryEvent(){
        cvCategory.setOnClickListener {
            findNavController().navigate(R.id.categoryListFragment)
        }

    }

    private fun handleButtonLogOutEvent(){
        btnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putBoolean("isLoggedIn", false)
                remove("username")
                remove("userRole")
                apply()
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()

            findNavController().navigate(R.id.welcomeFragment, null, navOptions)
        }
    }




}