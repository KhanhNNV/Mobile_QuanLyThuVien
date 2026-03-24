package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.repository.LibraryRepository
import com.example.quanlythuvien.data.AppDatabase
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookListFragment : Fragment() {

    private val viewModel: BookListViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter
    
    private var allBooksList: List<Book> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentFilterType: Int = 0 

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBookStats = view.findViewById<TextView>(R.id.tvBookStats)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val chipGroupFilter = view.findViewById<ChipGroup>(R.id.chipGroupFilter)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewBooks)

        bookAdapter = BookAdapter()
        recyclerView.adapter = bookAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadCategoryMap()

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books ?: emptyList()
            applyFilters()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val checkedId = checkedIds.first()
            currentFilterType = when (checkedId) {
                R.id.chipAll -> 0
                R.id.chipLow -> 1
                R.id.chipOut -> 2
                R.id.chipLost -> 3
                else -> 0
            }
            applyFilters()
        }
    }

    private fun loadCategoryMap() {
        GlobalScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val categories = db.libraryDao().getAllCategories()
                val map = categories.associate { it.categoryId to it.name }
                
                requireActivity().runOnUiThread {
                    bookAdapter.updateCategories(map)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyFilters() {
        val filtered = allBooksList.filter { book ->
            val matchesSearch = if (currentSearchQuery.isEmpty()) true else {
                book.title.contains(currentSearchQuery, ignoreCase = true) ||
                book.author.contains(currentSearchQuery, ignoreCase = true) ||
                (book.isbnCode?.contains(currentSearchQuery, ignoreCase = true) ?: false)
            }
            
            val matchesFilter = when (currentFilterType) {
                0 -> true
                1 -> book.availableQuantity in 1..2
                2 -> book.availableQuantity <= 0
                3 -> book.lostQuantity > 0
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
        
        bookAdapter.submitList(filtered)
        
        val statsView = view?.findViewById<TextView>(R.id.tvBookStats)
        val outOfStock = allBooksList.count { it.availableQuantity <= 0 }
        statsView?.text = "${allBooksList.size} đầu sách - $outOfStock hết kho"
    }
}