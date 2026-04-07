package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BorrowPayAdapter(
    private val onItemClick: (LoanItemData) -> Unit
) : ListAdapter<LoanItemData, BorrowPayAdapter.BorrowPayViewHolder>(BorrowPayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowPayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrow_pay, parent, false)
        return BorrowPayViewHolder(view)
    }

    override fun onBindViewHolder(holder: BorrowPayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BorrowPayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các View từ XML
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        private val tvLoanStatus: TextView = itemView.findViewById(R.id.tvLoanStatus)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        // 1. Ánh xạ thêm Icon cảnh báo
        private val ivWarningOverdue: ImageView = itemView.findViewById(R.id.ivWarningOverdue)

        fun bind(item: LoanItemData) {
            tvName.text = item.readerName
            tvLoanId.text = "${item.loanId}"
            tvDueDate.text = item.dueDate
            val context = itemView.context

            // Xử lý màu sắc cho trạng thái
            if (item.overallStatus == "BORROWING") {
                tvLoanStatus.text = "Đang mượn"
                val textColor = ContextCompat.getColor(context, R.color.text_status_info)
                val bgColor = ContextCompat.getColor(context, R.color.status_info)
                tvLoanStatus.setTextColor(textColor)
                tvLoanStatus.backgroundTintList = ColorStateList.valueOf(bgColor)
            } else {
                tvLoanStatus.text = "Đã trả"
                val textColor = ContextCompat.getColor(context, R.color.text_status_success)
                val bgColor = ContextCompat.getColor(context, R.color.status_success)
                tvLoanStatus.setTextColor(textColor)
                tvLoanStatus.backgroundTintList = ColorStateList.valueOf(bgColor)
            }

            // 2. Gọi hàm kiểm tra trễ hạn để bật/tắt Icon
            checkOverdue(item.dueDate, item.overallStatus)

            // Sự kiện click để giao diện chi tiết của phiếu mượn
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        // 3. Hàm xử lý logic ẩn/hiện cảnh báo trễ hạn
        private fun checkOverdue(dueDateStr: String, status: String) {
            val context = itemView.context

            // Nếu phiếu đã trả xong, chắc chắn ẩn cảnh báo và trả lại màu chữ bình thường
            if (status != "BORROWING") {
                ivWarningOverdue.visibility = View.GONE
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                return
            }

            try {
                // Dịch chuỗi ngày tháng
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dueDate = sdf.parse(dueDateStr)

                // Lấy ngày hôm nay và xóa giờ/phút/giây để so sánh chính xác ngày
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                // Nếu trễ hạn thì hiện cảnh báo
                if (dueDate != null && dueDate.before(today)) {
                    ivWarningOverdue.visibility = View.VISIBLE
                    // Tô đỏ luôn dòng chữ ngày tháng cho đồng bộ với icon
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                } else {
                    ivWarningOverdue.visibility = View.GONE
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            } catch (e: Exception) {
                // Đề phòng lỗi định dạng chuỗi, mặc định ẩn đi
                ivWarningOverdue.visibility = View.GONE
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }
    }

    class BorrowPayDiffCallback : DiffUtil.ItemCallback<LoanItemData>() {
        override fun areItemsTheSame(oldItem: LoanItemData, newItem: LoanItemData): Boolean {
            return oldItem.loanId == newItem.loanId
        }

        override fun areContentsTheSame(oldItem: LoanItemData, newItem: LoanItemData): Boolean {
            return oldItem == newItem
        }
    }
}