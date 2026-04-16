package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.BookData

class BookSpinnerAdapter(context: Context, books: List<BookData>) :
    ArrayAdapter<BookData>(context, 0, books) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_custom_spinner_book, parent, false)

        val book = getItem(position)

        val tvBookName = view.findViewById<TextView>(R.id.tvBookName)
        val tvAuthorCategory = view.findViewById<TextView>(R.id.tvAuthorCategory)

        if (book != null) {
            tvBookName.text = book.title

            // Xử lý hiển thị cho dòng phụ
            if (book.copyId == 0L) {
                tvAuthorCategory.text = "Giữ nguyên bản gốc"
            } else {
                // ĐÃ SỬA: Thay thế Thể loại (categoryName) bằng Barcode (Mã vạch)
                tvAuthorCategory.text = "TG: ${book.author} | Mã vạch: ${book.barcode}"
            }
        }

        return view
    }
}