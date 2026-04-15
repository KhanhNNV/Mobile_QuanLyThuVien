package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ReaderListFragment : Fragment(R.layout.fragment_reader_list) {
    private lateinit var readerAdapter: ReaderAdapter

    // Này để khi xoay màn hình thì , android sẽ phải xây lại fragment nhưng dữ liệu không mất
    private val viewModel: ReaderListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCustomHeader(
            view = view,
            title = "Độc giả",
            subtitle = "*Lấy tổng số độc giả"
        )

        val fasAddReader = view.findViewById<FloatingActionButton>(R.id.fasAddReader)
        fasAddReader.setOnClickListener {
            // Chuyển sang màn hình form nhập độc giả
            findNavController().navigate(R.id.readerAddFragment)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvReaders)
        val layoutManager = LinearLayoutManager(requireContext())
        readerAdapter = ReaderAdapter { readerResponse ->
            val bundle = Bundle().apply {
                // Ép kiểu Long sang Int nếu màn hình Detail của bạn đang dùng Int để hứng Id
                putInt("readerId", readerResponse.readerId.toInt())

                // Dùng fullName và phone thay vì name và phoneNumber
                putString("readerName", readerResponse.fullName)
                putString("readerPhone", readerResponse.phone)
            }

            findNavController().navigate(R.id.readerDetailFragment, bundle)
        }

        recyclerView.adapter = readerAdapter
        recyclerView.layoutManager = layoutManager

        viewModel.allReader.observe(viewLifecycleOwner) { readers ->
            readerAdapter.submitList(readers)
        }

        // Bắt sự kiện cuộn để xử lý phân trang
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.fetchReaders()
                }
            }
        })
    }
}