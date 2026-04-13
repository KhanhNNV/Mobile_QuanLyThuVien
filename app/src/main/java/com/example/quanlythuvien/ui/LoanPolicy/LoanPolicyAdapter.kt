package com.example.quanlythuvien.ui.LoanPolicy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
// Thêm dòng import model LoanPolicyResponse
import com.example.quanlythuvien.data.model.response.LoanPolicyResponse

class LoanPolicyAdapter(
    private var policyList: MutableList<LoanPolicyResponse>,
    private val onEditClick: (LoanPolicyResponse) -> Unit,
    private val onDeleteClick: (LoanPolicyResponse, Int) -> Unit
) : RecyclerView.Adapter<LoanPolicyAdapter.PolicyViewHolder>() {

    class PolicyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemCategory: TextView = view.findViewById(R.id.tvItemCategory)
        val tvLoanObject: TextView = view.findViewById(R.id.tvLoanObject)
        val tvLoanExp: TextView = view.findViewById(R.id.tvLoanExp)
        val btnEditPolicy: ImageButton = view.findViewById(R.id.btnEditPolicy)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_policy, parent, false)
        return PolicyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        val policy = policyList[position]
       holder.tvItemCategory.text = policy.categoryName ?: "Mặc định"

        if (policy.applyForStudent) {
            holder.tvLoanObject.text = "Sinh viên"
        } else {
            holder.tvLoanObject.text = "Thông thường"
        }

        holder.tvLoanExp.text = "${policy.maxBorrowDays} ngày"


        holder.btnEditPolicy.setOnClickListener {
            onEditClick(policy)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(policy, position)
        }
    }

    override fun getItemCount() = policyList.size

    fun updateData(newList: List<LoanPolicyResponse>) {
        policyList.clear()
        policyList.addAll(newList)
        notifyDataSetChanged()
    }
}