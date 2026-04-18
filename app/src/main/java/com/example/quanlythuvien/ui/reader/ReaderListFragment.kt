package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
    private lateinit var etSearch: EditText

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
        etSearch = view.findViewById(R.id.etSearch)
        setupRecyclerView(view)
        observeViewModel()
        setupSearchListener()
    }

    override fun onResume() {
        super.onResume()
        // Yêu cầu tải lại danh sách từ đầu mỗi khi quay lại màn hình này 
        // để cập nhật các thay đổi do xoá, sửa, thêm mới.
        viewModel.loadInitialReaders()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvReaders)
        val layoutManager = LinearLayoutManager(requireContext())

        readerAdapter = ReaderAdapter { reader ->
            val bundle = Bundle().apply {
                putLong("readerId", reader.readerId)
                putString("readerName", reader.fullName)
                putString("readerPhone", reader.phone)
                putString("readerBarcode", reader.barcode)
                putSerializable("readerDebt", reader.totalDebt)
                putInt("readerTotalBorrowBooks",reader.totalBorrowedBooks)
                putInt("readerTotalReturnBooks",reader.totalReturnBook)
                putInt("readerTotalOverdueBooks",reader.totalOverdueBooks)
            }
            findNavController().navigate(R.id.readerDetailFragment, bundle)
        }

        recyclerView.adapter = readerAdapter
        recyclerView.layoutManager = layoutManager

        // Xử lý cuộn
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Lấy thông tin về các vị trí item đang hiển thị
                val visibleItemCount = layoutManager.childCount // Số item đang hiện trên màn hình
                val totalItemCount = layoutManager.itemCount  // Tổng số item có trong Adapter
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition() // Vị trí item đầu tiên đang hiện

                //Kiểm tra hiện tại có đang tải không hoặc có đang trang cuối không
                if (!viewModel.isLoading() && !viewModel.isLastPage()) {
                    //(số item hiển thị + vị trí item đầu tiên) >= tổng số item
                    //  → nghĩa là người dùng đã cuộn đến gần hoặc chạm cuối danh sách.
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreReaders()
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        //Lắng nghe danh sách độc giả
        viewModel.allReader.observe(viewLifecycleOwner) { readers ->

            //Khi có danh sách mới gọi setData  sẽ cập nhật dữu liệu cho recycleView
            readerAdapter.setData(readers)
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher { //TextWatcher interface theo dõi sự thay đổi của văn bản trong EditText.

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString().orEmpty())  // Chỉ cập nhật Flow, không gọi API trực tiếp
            }
        })
    }

}