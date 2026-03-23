package com.example.quanlythuvien.ui.borrow_pay
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Loan



class BorrowPayAdapter : ListAdapter<Loan, BorrowPayAdapter.BorrowPayViewHolder>(BorrowPayDiffCallback()){
    //Tạo ViewHolder: Nơi chứa và ánh xạ các View từ file XML
    class BorrowPayViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvId: TextView = itemView.findViewById(R.id.tvId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)

    }

    // Hàm này dùng để đổ dữ liệu  vào các TextView


}