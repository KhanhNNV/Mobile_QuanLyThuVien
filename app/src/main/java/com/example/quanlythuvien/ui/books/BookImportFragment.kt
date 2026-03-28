package com.example.quanlythuvien.ui.books

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.quanlythuvien.R

class BookImportFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện từ file fragment_book_import.xml
        return inflater.inflate(R.layout.fragment_book_import, container, false)
    }

}