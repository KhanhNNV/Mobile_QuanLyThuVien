package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R


class ReaderListFragment : Fragment(R.layout.fragment_reader_list) {
    private lateinit var readerAdapter: ReaderAdapter

    // Này để khi xoay màn hình thì , android sẽ phải xây lại fragment nhưng dữ liệu không mất
    private val viewModel: ReaderListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvReaders)

        readerAdapter = ReaderAdapter()
        recyclerView.adapter = readerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allReaders.observe(viewLifecycleOwner) { readers ->
//            readers?.let {
//                readerAdapter.submitList(it)
//            }

            if (readers != null && readers.isEmpty()) {
                // Nếu DB đang rỗng -> Tự động chèn dữ liệu mẫu
                viewModel.insertMockData()
            } else {
                // Nếu đã có dữ liệu -> Đưa vào RecyclerView hiển thị
                readerAdapter.submitList(readers)
            }
        }
    }


}