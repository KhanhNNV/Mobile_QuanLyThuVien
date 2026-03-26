package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quanlythuvien.R

class ReaderAddFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện fragment_add_reader.xml
        return inflater.inflate(R.layout.fragment_reader_add, container, false)
    }
}