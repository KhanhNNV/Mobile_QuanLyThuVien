package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanStatus
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BorrowPayAdapter(
    private val onItemClick: (LoanItemData) -> Unit
) : ListAdapter<LoanItemData, BorrowPayAdapter.BorrowPayViewHolder>(BorrowPayDiffCallback()) {


    //Hàm này tạo khuôn cho item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowPayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrow_pay, parent, false)
        return BorrowPayViewHolder(view)
    }


    //Hàm này đưa dữ liệu vào khuôn
    override fun onBindViewHolder(holder: BorrowPayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BorrowPayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        private val tvLoanStatus: TextView = itemView.findViewById(R.id.tvLoanStatus)
        private val tvBorrowDate: TextView = itemView.findViewById(R.id.tvBorrowDate)

        fun bind(item: LoanItemData) {
            tvName.text = item.readerName
            tvLoanId.text = "${item.loanId}"
            tvBorrowDate.text = item.borrowDate

            val context = itemView.context

            // --- BƯỚC 1: XÁC ĐỊNH TRẠNG THÁI THỰC TẾ DỰA TRÊN LOGIC KIỂM TRA SÁCH ---
            val finalStatus = determineFinalStatus(item)

            // --- BƯỚC 2: HIỂN THỊ UI DỰA TRÊN TRẠNG THÁI ĐÃ TÍNH TOÁN ---
            when (finalStatus) {
                LoanStatus.BORROWING -> {
                    tvLoanStatus.text = "Đang mượn"
                    updateStatusStyle(context, R.color.text_status_info, R.drawable.bg_status_info)
                }
                LoanStatus.RETURNED -> {
                    tvLoanStatus.text = "Đã trả"
                    updateStatusStyle(context, R.color.text_status_success, R.drawable.bg_status_success)
                }
                LoanStatus.OVERDUE -> {
                    tvLoanStatus.text = "Quá hạn"
                    // Màu đỏ nổi bật cho trạng thái quá hạn
                    updateStatusStyle(context, R.color.text_status_error, R.drawable.bg_status_error)
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        /**
         * Hàm logic: Kiểm tra xem phiếu này có thực sự bị quá hạn hay không
         */
        private fun determineFinalStatus(item: LoanItemData): LoanStatus {
            // 1. Nếu trạng thái gốc từ Database là Đã trả -> Giữ nguyên Đã trả
            if (item.overallStatus == "RETURNED") return LoanStatus.RETURNED

            // 2. Nếu đang mượn, tiến hành quét danh sách sách để tìm sách trễ hạn
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time

                // Kiểm tra xem có cuốn sách nào chưa trả (BORROWING/LOST) mà ngày hết hạn < hôm nay không
                val isAnyBookOverdue = item.borrowedBooks.any { book ->
                    if (book.status != "RETURNED") {
                        val dueDate = sdf.parse(book.dueDate)
                        dueDate != null && dueDate.before(today)
                    } else false
                }

                // Nếu tìm thấy ít nhất 1 cuốn trễ hạn -> Trả về trạng thái OVERDUE
                if (isAnyBookOverdue) return LoanStatus.OVERDUE

            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Mặc định nếu không trễ hạn thì giữ nguyên trạng thái gốc (thường là BORROWING)
            return LoanStatus.fromValue(item.overallStatus)
        }


        //Hàm này giúp edit màu chữ, màu nền của trạng thái thuận tiện hơn
        private fun updateStatusStyle(context: Context, textColorRes: Int, bgDrawableRes: Int) {
            // 1. Cập nhật màu chữ đúng cách
            tvLoanStatus.setTextColor(ContextCompat.getColor(context, textColorRes))

            // 2. Thay đổi toàn bộ Resource nền thay vì nhuộm màu (tint)
            // Điều này giúp tránh lỗi Resources.NotFoundException và giữ đúng bo góc/viền của XML
            tvLoanStatus.setBackgroundResource(bgDrawableRes)
        }
    }

    class BorrowPayDiffCallback : DiffUtil.ItemCallback<LoanItemData>() {
        override fun areItemsTheSame(oldItem: LoanItemData, newItem: LoanItemData) = oldItem.loanId == newItem.loanId
        override fun areContentsTheSame(oldItem: LoanItemData, newItem: LoanItemData) = oldItem == newItem
    }
}