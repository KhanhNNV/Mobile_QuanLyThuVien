package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R

class BookListFragment : Fragment() {

    // Khởi tạo ViewModel
    private val viewModel: BookListViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter // Bạn sẽ cần tạo Adapter này

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp layout cho Fragment (layout này chỉ cần chứa 1 cái RecyclerView)
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewBooks)
        bookAdapter = BookAdapter()
        recyclerView.adapter = bookAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // "Lắng nghe" dữ liệu từ ViewModel
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            // Cập nhật dữ liệu cho Adapter mỗi khi danh sách thay đổi
            books?.let {
                bookAdapter.submitList(it)
            }
        }
    }
}