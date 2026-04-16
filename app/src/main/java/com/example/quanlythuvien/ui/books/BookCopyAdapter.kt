package com.example.quanlythuvien.ui.books

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
    private val allowEdit: Boolean,
    private val allowDelete: Boolean,
    private val onEditClick: (BookCopyItem, Int) -> Unit,
    private val onDeleteClick: (BookCopyItem, Int) -> Unit
) : RecyclerView.Adapter<BookCopyAdapter.CopyViewHolder>() {

    private fun displayCondition(rawCondition: String): String = when (rawCondition.uppercase()) {
        "NEW" -> "Mới"
        "GOOD" -> "Tốt"
        "FAIR" -> "Khá"
        "POOR" -> "Kém"
        else -> rawCondition
    }

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
            "BORROWED", "ĐANG MƯỢN" -> ContextCompat.getColor(holder.itemView.context, R.color.btn_primary)
            "LOST", "ĐÃ MẤT" -> ContextCompat.getColor(holder.itemView.context, R.color.red)
            "DAMAGED", "HƯ HỎNG" -> ContextCompat.getColor(holder.itemView.context, R.color.yellow)
            else -> fallbackColor
        }
    }

    private fun conditionColor(holder: CopyViewHolder, rawCondition: String, fallbackColor: Int): Int {
        return when (rawCondition.uppercase()) {
            "NEW", "MỚI" -> ContextCompat.getColor(holder.itemView.context, R.color.green)
            "GOOD", "TỐT" -> ContextCompat.getColor(holder.itemView.context, R.color.yellow)
            "FAIR", "KHÁ" -> ContextCompat.getColor(holder.itemView.context, R.color.purple)
            "POOR", "KÉM" -> ContextCompat.getColor(holder.itemView.context, R.color.red)
            else -> fallbackColor
        }
    }

    private fun conditionLabelSpan(holder: CopyViewHolder, rawCondition: String): CharSequence {
        val label = displayCondition(rawCondition)
        val color = conditionColor(holder, rawCondition, holder.tvCopyStatus.currentTextColor)
        return SpannableStringBuilder(label).apply {
            setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun statusLabelSpan(holder: CopyViewHolder, rawStatus: String): CharSequence {
        val label = displayStatus(rawStatus)
        val color = statusColor(holder, rawStatus, holder.tvCopyStatus.currentTextColor)
        return SpannableStringBuilder(label).apply {
            setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    class CopyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCopyId: TextView = view.findViewById(R.id.tvCopyId)
        val tvCopyStatus: TextView = view.findViewById(R.id.tvCopyStatus)
        val ivEditCopy: ImageView = view.findViewById(R.id.ivEditCopy)
        val ivDeleteCopy: ImageView = view.findViewById(R.id.ivDeleteCopy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_copy, parent, false)
        return CopyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CopyViewHolder, position: Int) {
        val item = copyList[position]
        holder.tvCopyId.text = item.copyId
        val statusText = statusLabelSpan(holder, item.statusText)
        val conditionText = conditionLabelSpan(holder, item.conditionText)
        holder.tvCopyStatus.text = SpannableStringBuilder().apply {
            append(statusText)
            append(" · ")
            append("Tình trạng: ")
            append(conditionText)
        }
        holder.tvCopyStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))

        if (allowEdit) {
            holder.ivEditCopy.visibility = View.VISIBLE
            holder.ivEditCopy.setOnClickListener {
                val adapterPos = holder.bindingAdapterPosition
                if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener
                onEditClick(item, adapterPos)
            }
        } else {
            holder.ivEditCopy.visibility = View.GONE
            holder.ivEditCopy.setOnClickListener(null)
        }

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

