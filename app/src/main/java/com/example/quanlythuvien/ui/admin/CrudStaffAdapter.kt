package com.example.quanlythuvien.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R


class CrudStaffAdapter (
    private val onClickMenu: (StaffData, String) -> Unit
) : ListAdapter<StaffData, CrudStaffAdapter.StaffViewHolder>(StaffDiffCallback()) {

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val UserName: TextView = itemView.findViewById(R.id.tvUsername)
        private val PassWord: TextView = itemView.findViewById(R.id.tvPassword)
        private val LibraryName: TextView = itemView.findViewById(R.id.tvLibraryName)
        private val Active: TextView = itemView.findViewById(R.id.tvStatusActive)

        // Nút menu
        private val btnMenu: ImageButton = itemView.findViewById(R.id.ibtStaffOptions)

        // Hàm bơm dữ liệu
        fun bind(item: StaffData){
            UserName.text = item.userName
            PassWord.text = "********" // Khuyên dùng: Ẩn mật khẩu
            LibraryName.text = item.libraryName
            val status = item.is_active
            val context = itemView.context

            // Xử lý status cho tài khoản Staff
            if(status) {
                Active.text = "Đang hoạt động"
                Active.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_status_success))
                Active.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(context, R.color.status_success)
            } else {
                Active.text = "Đã khóa"
                Active.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_status_error))
                Active.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(context, R.color.status_error)
            }

            // XỬ LÝ MENU 3 CHẤM
            btnMenu.setOnClickListener {
                // Truyền btnMenu vào thay vì view để menu neo đúng ngay dưới nút bấm
                val popup = PopupMenu(context, btnMenu)
                popup.menu.add(0, 1, 0, "Sửa")
                popup.menu.add(0, 2, 0, "Xóa")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> {
                            // Gọi ra ngoài báo là chọn SỬA
                            onClickMenu.invoke(item, "EDIT")
                        }
                        2 -> {
                            // Gọi ra ngoài báo là chọn XÓA
                            onClickMenu.invoke(item, "DELETE")
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manager_staff_of_admin, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StaffDiffCallback : DiffUtil.ItemCallback<StaffData>() {
        override fun areItemsTheSame(oldItem: StaffData, newItem: StaffData): Boolean {
            return oldItem.userName == newItem.userName
        }

        override fun areContentsTheSame(oldItem: StaffData, newItem: StaffData): Boolean {
            return oldItem == newItem
        }
    }
}