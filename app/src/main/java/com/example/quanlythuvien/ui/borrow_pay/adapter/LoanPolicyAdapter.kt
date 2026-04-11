package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.LoanPolicy

class LoanPolicyAdapter(
    private var policyList: MutableList<LoanPolicy>,
    // Thêm hàm callback để xử lý sự kiện bấm nút Edit
    private val onEditClick: (LoanPolicy) -> Unit,
    private val onDeleteClick: (LoanPolicy, Int) -> Unit
) : RecyclerView.Adapter<LoanPolicyAdapter.PolicyViewHolder>() {

    class PolicyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemCategory: TextView = view.findViewById(R.id.tvItemCategory)
        val tvLoanObject: TextView = view.findViewById(R.id.tvLoanObject)
        val tvLoanExp: TextView = view.findViewById(R.id.tvLoanExp)
        val btnEditPolicy: ImageButton = view.findViewById(R.id.btnEditPolicy)

        val btnDelete: ImageButton= view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_policy, parent, false)
        return PolicyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        val policy = policyList[position]

        // Đổ dữ liệu vào các View
        holder.tvItemCategory.text = policy.categoryName
        holder.tvLoanObject.text = policy.targetCustomer
        holder.tvLoanExp.text = "${policy.maxDays} ngày"

        // Bắt sự kiện click nút Edit
        holder.btnEditPolicy.setOnClickListener {
            onEditClick(policy)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(policy, position)
        }
    }

    override fun getItemCount() = policyList.size

    fun updateData(newList: List<LoanPolicy>) {
        policyList.clear()
        policyList.addAll(newList)
        notifyDataSetChanged()
    }
}