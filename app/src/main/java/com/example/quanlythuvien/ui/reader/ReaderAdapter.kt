package com.example.quanlythuvien.ui.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Reader

open class ReaderDiffCallback {

}

class ReaderAdapter (
    private val onItemClick: (Reader) -> Unit
): ListAdapter<Reader, ReaderAdapter.ReaderViewHolder>(ReaderDiffCallback()) {

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

        fun bind(reader: Reader) {
            tvReaderName.text = reader.name
            tvReaderInfo.text = reader.phoneNumber
            tvReaderStatus.text = reader.readerType.name
            tvAvatar.text = reader.name.firstOrNull()?.uppercase()
        }
    }

    class ReaderDiffCallback : DiffUtil.ItemCallback<Reader>() {
        override fun areItemsTheSame(
            p0: Reader, p1: Reader
        ): Boolean {
            return p0.readerId == p1.readerId
        }

        override fun areContentsTheSame(
            p0: Reader, p1: Reader
        ): Boolean {
            return p0 == p1
        }

    }

}