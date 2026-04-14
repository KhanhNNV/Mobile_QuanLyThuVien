package com.example.quanlythuvien.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R

class BookCopyAdapter(
    private val copyList: MutableList<BookCopyItem>,
    private val allowDelete: Boolean,
    private val onDeleteClick: (BookCopyItem, Int) -> Unit
) : RecyclerView.Adapter<BookCopyAdapter.CopyViewHolder>() {

    private fun displayStatus(rawStatus: String): String = when (rawStatus.uppercase()) {
        "AVAILABLE" -> "Có sẵn"
        "BORROWED" -> "Đang mượn"
        "LOST" -> "Đã mất"
        "DAMAGED" -> "Hư hỏng"
        else -> rawStatus
    }

    private fun statusColor(holder: CopyViewHolder, rawStatus: String, fallbackColor: Int): Int {
        return when (rawStatus.uppercase()) {
            "AVAILABLE", "CÓ SẴN" -> ContextCompat.getColor(holder.itemView.context, R.color.green)
            "BORROWED", "ĐANG MƯỢN" -> ContextCompat.getColor(holder.itemView.context, R.color.yellow)
            "LOST", "ĐÃ MẤT", "DAMAGED", "HƯ HỎNG" -> ContextCompat.getColor(holder.itemView.context, R.color.red)
            else -> fallbackColor
        }
    }

    class CopyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCopyId: TextView = view.findViewById(R.id.tvCopyId)
        val tvCopyStatus: TextView = view.findViewById(R.id.tvCopyStatus)
        val ivDeleteCopy: ImageView = view.findViewById(R.id.ivDeleteCopy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_copy, parent, false)
        return CopyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CopyViewHolder, position: Int) {
        val item = copyList[position]
        holder.tvCopyId.text = item.copyId
        holder.tvCopyStatus.text = displayStatus(item.statusText)
        holder.tvCopyStatus.setTextColor(statusColor(holder, item.statusText, item.statusColor))

        if (allowDelete) {
            holder.ivDeleteCopy.visibility = View.VISIBLE
            holder.ivDeleteCopy.setOnClickListener {
                val adapterPos = holder.bindingAdapterPosition
                if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener
                onDeleteClick(item, adapterPos)
            }
        } else {
            holder.ivDeleteCopy.visibility = View.GONE
            holder.ivDeleteCopy.setOnClickListener(null)
        }
    }

    override fun getItemCount() = copyList.size

    fun removeAt(position: Int) {
        if (position < 0 || position >= copyList.size) return
        copyList.removeAt(position)
        notifyItemRemoved(position)
    }
}

