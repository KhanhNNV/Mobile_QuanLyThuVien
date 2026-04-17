package com.example.quanlythuvien.ui.feeInvoices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse

class InvoiceAdapter(
    private var invoiceList: MutableList<FeeInvoiceResponse>,
    private val onItemClick: (FeeInvoiceResponse) -> Unit,
    private val onOptionsClick: (FeeInvoiceResponse, View) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReaderName: TextView = view.findViewById(R.id.tvReaderName)
        val tvInvoiceStatus: TextView = view.findViewById(R.id.tvInvoiceStatus)
        val tvInvoiceId: TextView = view.findViewById(R.id.tvInvoiceId)
        val tvTotalAmount: TextView = view.findViewById(R.id.tvTotalAmount)
        val ibtInvoiceOptions: ImageButton = view.findViewById(R.id.ibtInvoiceOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fee_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoiceList[position]

        holder.tvReaderName.text = "${invoice.readerName}"
        holder.tvInvoiceId.text = invoice.invoiceId.toString()
        holder.tvTotalAmount.text = formatCurrency(invoice.amount)
        holder.tvInvoiceStatus.text = getStatusDisplayName(invoice.status)

        when (invoice.status) {
            "PAID" -> {
                holder.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_status_success)
                holder.tvInvoiceStatus.setTextColor(holder.itemView.context.getColor(R.color.text_status_success))
            }
            "UNPAID" -> {
                holder.tvInvoiceStatus.setBackgroundResource(R.drawable.bg_status_error)
                holder.tvInvoiceStatus.setTextColor(holder.itemView.context.getColor(R.color.text_status_error))
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(invoice)
        }

        holder.ibtInvoiceOptions.setOnClickListener { view ->
            onOptionsClick(invoice, view)
        }
    }

    override fun getItemCount() = invoiceList.size

    fun updateData(newList: List<FeeInvoiceResponse>) {
        invoiceList.clear()
        invoiceList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Double): String {
        return "%,.0f VND".format(amount)
    }

    private fun getStatusDisplayName(status: String): String {
        return when (status) {
            "PAID" -> "Đã thanh toán"
            "UNPAID" -> "Chưa thanh toán"
            else -> status
        }
    }
}