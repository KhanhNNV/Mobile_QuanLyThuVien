package com.example.quanlythuvien.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class SettingFragment : Fragment(R.layout.fragment_setting) {


    private lateinit var switchStudentDiscount: MaterialSwitch
    private lateinit var layoutStudentDiscountContainer: LinearLayout
    private lateinit var edtStudentDiscount: TextInputEditText


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        handleswitchStudentDiscountEvents()
    }

    private fun initViews(view: View) {
        switchStudentDiscount = view.findViewById(R.id.switchStudentDiscount)
        layoutStudentDiscountContainer = view.findViewById(R.id.layoutStudentDiscountContainer)
        edtStudentDiscount = view.findViewById(R.id.edtStudentDiscount)
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


}