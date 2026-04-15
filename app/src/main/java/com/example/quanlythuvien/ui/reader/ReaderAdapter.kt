package com.example.quanlythuvien.ui.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.data2.entity.Reader

class ReaderAdapter(
    private val onItemClick: (ReaderResponse) -> Unit
) : ListAdapter<ReaderResponse, ReaderAdapter.ReaderViewHolder>(ReaderDiffCallback()) {

    override fun onCreateViewHolder(
        p0: ViewGroup, p1: Int
    ): ReaderViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_reader, p0, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(
        p0: ReaderViewHolder, p1: Int
    ) {
        val currentReader = getItem(p1)
        p0.bind(currentReader)

        p0.itemView.setOnClickListener {
            onItemClick(currentReader)
        }
    }

    class ReaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReaderName: TextView = itemView.findViewById(R.id.tvReaderName)
        private val tvReaderInfo: TextView = itemView.findViewById(R.id.tvReaderInfo)
        private val tvReaderStatus: TextView = itemView.findViewById(R.id.tvReaderStatus)
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)

        fun bind(reader: ReaderResponse) {
            // Dùng đúng các biến trả về từ API
            tvReaderName.text = reader.fullName
            tvReaderInfo.text = reader.phone

            // Vì dự án bỏ "type" nên ta ẩn luôn phần text trạng thái/loại đi cho giao diện sạch
            tvReaderStatus.visibility = View.GONE

            tvAvatar.text = reader.fullName.firstOrNull()?.uppercase()
        }

    }

    class ReaderDiffCallback : DiffUtil.ItemCallback<ReaderResponse>() {
        override fun areItemsTheSame(
            p0: ReaderResponse, p1: ReaderResponse
        ): Boolean {
            return p0.readerId == p1.readerId
        }

        override fun areContentsTheSame(
            p0: ReaderResponse, p1: ReaderResponse
        ): Boolean {
            return p0 == p1
        }

    }

}