package com.example.quanlythuvien.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.UserResponse

class StaffAdapter(
    private var staffList: MutableList<UserResponse>,
    private val onEditClick: (UserResponse) -> Unit,
    private val onDeleteClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvStatusActive: TextView = view.findViewById(R.id.tvStatusActive)
        val tvStaffId: TextView = view.findViewById(R.id.tvStaffId)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val ibtStaffOptions: ImageButton = view.findViewById(R.id.ibtStaffOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manager_staff_of_admin, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]

        holder.tvName.text = staff.fullname
        holder.tvStaffId.text = "#${staff.userId}"
        holder.tvUserName.text = "@${staff.username}"

        if (staff.isActive) {
            holder.tvStatusActive.text = "Hoạt động"
            holder.tvStatusActive.setBackgroundResource(R.drawable.bg_status_success)
            holder.tvStatusActive.setTextColor(holder.itemView.context.getColor(R.color.text_status_success))
        } else {
            holder.tvStatusActive.text = "Không hoạt động"
            holder.tvStatusActive.setBackgroundResource(R.drawable.bg_status_error)
            holder.tvStatusActive.setTextColor(holder.itemView.context.getColor(R.color.text_status_error))
        }

        if (staff.role == "ADMIN") {
            holder.tvName.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.outline_admin_panel_settings_24, 0
            )
        } else {
            holder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        holder.ibtStaffOptions.setOnClickListener {
            onEditClick(staff)
        }

        holder.itemView.setOnClickListener {
            onEditClick(staff)
        }
    }

    override fun getItemCount() = staffList.size

    fun updateData(newList: List<UserResponse>) {
        staffList.clear()
        staffList.addAll(newList)
        notifyDataSetChanged()
    }
}