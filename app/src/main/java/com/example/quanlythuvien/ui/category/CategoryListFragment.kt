package com.example.quanlythuvien.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryListFragment : Fragment() {

    private lateinit var viewModel: CategoryListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val fabAddCategory = view.findViewById<FloatingActionButton>(R.id.fabAddCategory)
        recyclerView = view.findViewById(R.id.recyclerViewCategories)

        tvHeaderTitle?.text = "Thể loại"
        tvHeaderSubtitle?.text = "Quản lý thể loại sách"

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Khởi tạo Adapter với danh sách rỗng ban đầu
        adapter = CategoryAdapter(emptyList()) { selectedCategory ->
            // Bấm vào Item thì nhảy sang trang Sửa (Sẽ làm ở bước sau)
            val bundle = Bundle().apply {
                putLong("categoryId", selectedCategory.categoryId)
                putString("categoryName", selectedCategory.name)
            }
            findNavController().navigate(R.id.categoryEditFragment, bundle)
        }
        recyclerView.adapter = adapter

        // Setup ViewModel
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(CategoryApiService::class.java)
        val repository = CategoryRepository(apiService)
        val factory = GenericViewModelFactory { CategoryListViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[CategoryListViewModel::class.java]

        // Lắng nghe dữ liệu
        observeViewModel()

        // GỌI API LẤY DANH SÁCH (Mỗi lần Fragment này mở lên là nó tự gọi lại -> Luôn có data mới)
        viewModel.fetchCategories()

        fabAddCategory.setOnClickListener {
            findNavController().navigate(R.id.categoryAddFragment)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryListState.collectLatest { state ->
                when (state) {
                    is CategoryListState.Idle -> {}
                    is CategoryListState.Loading -> {
                        // (Tùy chọn) Hiện ProgressBar
                    }
                    is CategoryListState.Success -> {
                        // Cập nhật lại Adapter khi có data xịn
                        adapter.updateData(state.categories)
                    }
                    is CategoryListState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}