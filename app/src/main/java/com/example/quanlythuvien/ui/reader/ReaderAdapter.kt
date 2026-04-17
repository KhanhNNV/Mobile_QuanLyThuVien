package com.example.quanlythuvien.ui.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.ReaderResponse

class ReaderAdapter(
    private val onItemClick: (ReaderResponse) -> Unit
) : RecyclerView.Adapter<ReaderAdapter.ReaderViewHolder>() {

    // Khai báo danh sách MutableList để có thể thêm/xóa dữ liệu dễ dàng
    private val readerList = mutableListOf<ReaderResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reader, parent, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        val currentReader = readerList[position]
        holder.bind(currentReader)

        holder.itemView.setOnClickListener {
            onItemClick(currentReader)
        }
    }

    override fun getItemCount(): Int = readerList.size

    /**
     * Dùng cho lần load đầu tiên (trang 0) hoặc khi bạn làm mới (Refresh) danh sách.
     */
    fun setData(newList: List<ReaderResponse>) {
        this.readerList.clear()
        this.readerList.addAll(newList)
        notifyDataSetChanged() // Làm mới toàn bộ danh sách
    }


    class ReaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReaderName: TextView = itemView.findViewById(R.id.tvReaderName)
        private val tvReaderPhone: TextView = itemView.findViewById(R.id.tvReaderInfo)
        private val tvReaderStatus: TextView = itemView.findViewById(R.id.tvReaderStatus)
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)

        fun bind(reader: ReaderResponse) {
            tvReaderName.text = reader.fullName
            val barcodeShow = reader.barcode.ifBlank { "—" }
            tvReaderPhone.text = "$barcodeShow · ${reader.phone}"
            tvAvatar.text = reader.fullName.firstOrNull()?.uppercase() ?: ""

            if (reader.isBlocked) {
                tvReaderStatus.visibility = View.VISIBLE
                tvReaderStatus.text = "Đã chặn"
                tvReaderStatus.setBackgroundResource(R.drawable.bg_chip_error)
                tvReaderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.chip_error))
            } else {
                tvReaderStatus.visibility = View.GONE
            }
        }
    }
}