package com.example.quanlythuvien.ui.violation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.ViolationResponse

class ViolationAdapter(
    private var violationList: MutableList<ViolationResponse>,
    private val isAdmin: Boolean,
    private val onEditClick: (ViolationResponse) -> Unit,
    private val onDeleteClick: (ViolationResponse) -> Unit,
    private val onViewLoanClick: (Long) -> Unit
) : RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    fun updateData(newList: List<ViolationResponse>) {
        violationList.clear()
        violationList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_violation, parent, false)
        return ViolationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        holder.bind(violationList[position])
    }

    override fun getItemCount(): Int = violationList.size

    inner class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvViolationId: TextView = itemView.findViewById(R.id.tvViolationId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvReaderName: TextView = itemView.findViewById(R.id.tvReaderName)
        private val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        private val tvDates: TextView = itemView.findViewById(R.id.tvDates)

        private val btnViewLoan: Button = itemView.findViewById(R.id.btnViewLoan)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(violation: ViolationResponse) {
            tvViolationId.text = "Vi phạm #${violation.violationId}"

            // Cập nhật trạng thái
            tvStatus.text = violation.status
            if (violation.status == "ACTIVE") {
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context,R.color.text_status_error))
                tvStatus.setBackgroundResource(R.drawable.bg_status_error)
            } else {
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context,R.color.text_status_success))
                tvStatus.setBackgroundResource(R.drawable.bg_status_success)
            }

            // Thông tin độc giả và lý do
            tvReaderName.text = "Độc giả: ${violation.readerName ?: "Không xác định"} ${if (violation.barcode != null) "(${violation.barcode})" else ""}"
            tvReason.text = violation.reason ?: "Không có lý do cụ thể"

            // Ngày tháng
            val created = violation.createdAt?.substringBefore("T") ?: "Chưa có"
            val updated = violation.updatedAt?.substringBefore("T") ?: "Chưa có"
            tvDates.text = "Tạo: $created | Cập nhật: $updated"

            // Xử lý nút Xem Phiếu Mượn
            if (violation.loanId != null) {
                btnViewLoan.visibility = View.VISIBLE
                btnViewLoan.setOnClickListener {
                    onViewLoanClick(violation.loanId)
                }
            } else {
                btnViewLoan.visibility = View.GONE // Ẩn nút nếu không gắn với phiếu mượn nào
            }

            // Nút sửa và xóa
            if (isAdmin) {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener { onEditClick(violation) }
                btnDelete.setOnClickListener { onDeleteClick(violation) }
            } else {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
        }
    }
}