package com.example.quanlythuvien.ui.borrow_pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoanDetailAdapter(
    private val userRole: String,
    private val onMenuActionClick: (LoanDetailItemData, String) -> Unit
) : ListAdapter<LoanDetailItemData, LoanDetailAdapter.BookViewHolder>(BookDiffCallback()) {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory) // Đã sửa ID cho khớp XML
        private val tvBarcode: TextView = itemView.findViewById(R.id.tvBarcode)   // Khai báo thêm Barcode
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDetailStatus)
        private val ibtSet: ImageButton = itemView.findViewById(R.id.ibtSet)
        private val tvReturnDateTitle: TextView = itemView.findViewById(R.id.tvReturnDateTitle) // Đã sửa ID cho khớp XML
        private val tvReturnDate: TextView = itemView.findViewById(R.id.tvReturnDate)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDetailDueDate)
        private val ivWarningOverdue: ImageView = itemView.findViewById(R.id.ivWarningOverdueDetail)

        fun bind(item: LoanDetailItemData) {
            val context = itemView.context

            // 1. Đổ dữ liệu văn bản
            tvTitle.text = item.title
            tvAuthor.text = item.author
            tvCategory.text = item.categoryName
            tvDueDate.text = item.dueDate
            tvBarcode.text = item.bookBarcode

            val currentStatus = item.status

            // 2. Map Color và Text tương ứng với trạng thái (Khớp Enum Backend)
            val (statusText, textColorRes, bgDrawableRes) = when (currentStatus) {
                "RETURNED" -> Triple("Đã trả", R.color.text_status_success, R.drawable.bg_status_success)
                "BORROWING" -> Triple("Đang mượn", R.color.text_status_info, R.drawable.bg_status_info)
                "OVERDUE" -> Triple("Quá hạn", R.color.text_status_error, R.drawable.bg_status_error)
                "LOST" -> Triple("Bị mất", R.color.text_status_error, R.drawable.bg_status_error)
                "DAMAGED" -> Triple("Hư hỏng", R.color.text_status_error, R.drawable.bg_status_error)
                else -> Triple("Không xác định", R.color.text_status_error, R.drawable.bg_status_error)
            }

            tvStatus.text = statusText
            tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            tvStatus.setBackgroundResource(bgDrawableRes)

            // 3. Xử lý ẩn hiện Ngày Trả
            val isReturnedState = currentStatus in listOf("RETURNED", "LOST", "DAMAGED")
            if (isReturnedState && !item.returnDate.isNullOrEmpty()) {
                tvReturnDateTitle.visibility = View.VISIBLE
                tvReturnDate.visibility = View.VISIBLE
                tvReturnDate.text = item.returnDate
            } else {
                tvReturnDateTitle.visibility = View.GONE
                tvReturnDate.visibility = View.GONE
            }

            // 4. Xử lý Cảnh báo Trễ hạn
            when (currentStatus) {
                "OVERDUE" -> {
                    ivWarningOverdue.visibility = View.VISIBLE
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                }
                "BORROWING" -> {
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dueDate = sdf.parse(item.dueDate)
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.time

                        if (dueDate != null && dueDate.before(today)) {
                            ivWarningOverdue.visibility = View.VISIBLE
                            tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                        } else {
                            ivWarningOverdue.visibility = View.GONE
                            tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        }
                    } catch (e: Exception) {
                        ivWarningOverdue.visibility = View.GONE
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    }
                }
                else -> {
                    ivWarningOverdue.visibility = View.GONE
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }

            // ==========================================
            // 5. HIỂN THỊ HOẶC ẨN MENU POPUP (NÚT 3 CHẤM)
            // ==========================================
            val isAdmin = userRole == "ADMIN" || userRole == "ROLE_ADMIN"
            val isStaff = userRole == "STAFF" || userRole == "ROLE_STAFF"
            val isBorrowing = currentStatus == "BORROWING"

            // BƯỚC 1: QUYẾT ĐỊNH HIỂN THỊ HAY ẨN NÚT 3 CHẤM
            if (isAdmin || (isStaff && isBorrowing)) {
                ibtSet.visibility = View.VISIBLE

                // BƯỚC 2: GÁN SỰ KIỆN CLICK (Chỉ gán 1 lần)
                ibtSet.setOnClickListener { view ->
                    val popup = PopupMenu(context, view)

                    // Thêm lựa chọn "Sửa thông tin"
                    if (isAdmin || (isStaff && isBorrowing)) {
                        popup.menu.add(0, 1, 0, "Sửa thông tin")
                    }

                    // Thêm lựa chọn "Xóa" (CHỈ DÀNH CHO ADMIN)
                    if (isAdmin) {
                        popup.menu.add(0, 2, 0, "Xóa chi tiết")
                    }

                    // Bắt sự kiện khi click vào menu
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.title) {
                            "Sửa thông tin" -> onMenuActionClick(item, "EDIT")
                            "Xóa chi tiết" -> onMenuActionClick(item, "DELETE")
                        }
                        true
                    }
                    popup.show()
                }
            } else {
                // Nếu là Staff và sách ĐÃ TRẢ / QUÁ HẠN / MẤT... -> Ẩn luôn nút 3 chấm
                ibtSet.visibility = View.GONE
            }

            // ==========================================
            // 6. XỬ LÝ CLICK VÀO TOÀN BỘ ITEM (TRẢ SÁCH)
            // ==========================================
            itemView.setOnClickListener {
                if (currentStatus == "BORROWING" || currentStatus == "OVERDUE") {
                    onMenuActionClick(item, "RETURN")
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_detail, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookDiffCallback : DiffUtil.ItemCallback<LoanDetailItemData>() {
        override fun areItemsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData): Boolean {
            return oldItem.loanDetailId == newItem.loanDetailId
        }

        override fun areContentsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData): Boolean {
            return oldItem == newItem
        }
    }
}