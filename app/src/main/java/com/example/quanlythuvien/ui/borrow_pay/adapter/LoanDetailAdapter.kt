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
import com.example.quanlythuvien.data2.entity.enums.LoanDetailStatus
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoanDetailAdapter(
    // Kênh liên lạc để báo cáo với Fragment khi người dùng chọn SỬA hoặc XÓA
    private val isAdmin: Boolean,
    private val onMenuActionClick: (LoanDetailItemData, String) -> Unit
) : ListAdapter<LoanDetailItemData, LoanDetailAdapter.BookViewHolder>(BookDiffCallback()) {

    // ==========================================
    // LỚP KHUÔN (VIEW HOLDER) - Quản lý 1 thẻ sách
    // ==========================================
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 1. Ánh xạ các View cơ bản
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCatetory)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDetailStatus)
        private val ibtSet: ImageButton = itemView.findViewById(R.id.ibtSet)

        // 2. Ánh xạ các View liên quan đến thời gian
        private val tvNgayTraTitle: TextView = itemView.findViewById(R.id.tvNgayTraTitle)
        private val tvReturnDate: TextView = itemView.findViewById(R.id.tvReturnDate)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDetailDueDate)
        private val ivWarningOverdue: ImageView = itemView.findViewById(R.id.ivWarningOverdueDetail)

        // HÀM BƠM DỮ LIỆU TỪ OBJECT VÀO GIAO DIỆN
        fun bind(item: LoanDetailItemData) {
            val context = itemView.context

            // --- BƯỚC 1: Đổ dữ liệu văn bản cơ bản ---
            tvTitle.text = item.title
            tvAuthor.text = item.author
            tvCategory.text = item.categoryName
            tvDueDate.text = item.dueDate

            val currentStatus = LoanDetailStatus.fromValue(item.status)

            // --- BƯỚC 2: Xử lý màu sắc và nội dung cho Tag Trạng Thái ---
            val (text, textColorRes, bgDrawableRes) = when (currentStatus) {
                LoanDetailStatus.RETURNED -> Triple("Đã trả", R.color.text_status_success, R.drawable.bg_status_success)
                LoanDetailStatus.LOST -> Triple("Bị mất", R.color.text_status_error, R.drawable.bg_status_error)
                else -> Triple("Đang mượn", R.color.text_status_info, R.drawable.bg_status_info)
            }

            tvStatus.text = text
            tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            tvStatus.setBackgroundResource(bgDrawableRes)

            // --- BƯỚC 3: Xử lý ẩn/hiện Ngày Trả thực tế ---
            if (currentStatus == LoanDetailStatus.RETURNED && !item.returnDate.isNullOrEmpty()) {
                tvNgayTraTitle.visibility = View.VISIBLE
                tvReturnDate.visibility = View.VISIBLE
                tvReturnDate.text = item.returnDate
            } else {
                tvNgayTraTitle.visibility = View.GONE
                tvReturnDate.visibility = View.GONE
            }

            // --- BƯỚC 4: Xử lý Cảnh báo Trễ hạn ---
            if (currentStatus != LoanDetailStatus.RETURNED) {
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
            } else {
                ivWarningOverdue.visibility = View.GONE
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            // --- BƯỚC 5: XỬ LÝ PHÂN QUYỀN TRONG MENU 3 CHẤM ---
            ibtSet.setOnClickListener { view ->
                val popup = PopupMenu(context, view)

                // Mọi người (Admin và Staff) đều có quyền Sửa
                popup.menu.add(0, 1, 0, "Sửa")

                // Chỉ ADMIN mới thấy nút Xóa hiện lên trong Menu
                if (isAdmin) {
                    popup.menu.add(0, 2, 0, "Xóa")
                }

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> onMenuActionClick(item, "EDIT")
                        2 -> onMenuActionClick(item, "DELETE")
                    }
                    true
                }
                popup.show()
            }

            // Tiện ích: Bấm thẳng vào dòng sách cũng kích hoạt chức năng Sửa (Rất tiện cho người dùng)
            itemView.setOnClickListener {
                onMenuActionClick(item, "EDIT")
            }
        }
    }

    // ==========================================
    // CÁC HÀM BẮT BUỘC CỦA LIST ADAPTER
    // ==========================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_detail, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // Chỉ gọi hàm bind() là đủ, toàn bộ logic đã được đưa vào bên trong bind()
        holder.bind(getItem(position))
    }

    // Công cụ giúp RecyclerView biết item nào thay đổi để cập nhật mượt mà
    class BookDiffCallback : DiffUtil.ItemCallback<LoanDetailItemData>() {
        override fun areItemsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData) = oldItem.bookId == newItem.bookId
        override fun areContentsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData) = oldItem == newItem
    }
}