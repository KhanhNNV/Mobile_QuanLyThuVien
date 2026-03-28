package com.example.quanlythuvien.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.google.android.material.card.MaterialCardView

class NotificationAdapter(private val notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val cvUnreadDot: MaterialCardView = itemView.findViewById(R.id.cvUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.tvContent.text = notification.content
        holder.tvDate.text = notification.date
        holder.tvTime.text = notification.time

        if (notification.isRead) {
            holder.cvUnreadDot.visibility = View.GONE // Ẩn
        } else {
            holder.cvUnreadDot.visibility = View.VISIBLE // Hiện
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}