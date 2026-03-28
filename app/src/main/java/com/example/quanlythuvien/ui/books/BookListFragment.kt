package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.AppDatabase
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookListFragment : Fragment() {

    private val viewModel: BookListViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter
    
    private lateinit var etSearch: EditText

    private lateinit var btnToggleFilter: ImageButton
    private lateinit var llFilterContainer: LinearLayout
    private lateinit var rgStatusFilter: RadioGroup
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnResetFilter: Button
    private lateinit var btnApplyFilter: Button
    private lateinit var recyclerViewBooks: RecyclerView
    private lateinit var btnAddBook: Button
    
    private var allBooksList: List<Book> = emptyList()
    private var categoryList: List<Category> = emptyList()
    
    private var currentSearchQuery: String = ""
    private var currentFilterType: Int = 0 
    private var currentCategoryId: Int = 0
    
    private var tempFilterType: Int = 0 
    private var tempCategoryId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()

        loadCategoryMap()

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books ?: emptyList()
            applyFilters()
        }
    }

    private fun initViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)

        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        rgStatusFilter = view.findViewById(R.id.rgStatusFilter)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        recyclerViewBooks = view.findViewById(R.id.recyclerViewBooks)
        btnAddBook = view.findViewById(R.id.btnAddBook)
        
        bookAdapter = BookAdapter()
        recyclerViewBooks.adapter = bookAdapter
        recyclerViewBooks.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        // Toggle Filter Container
        btnToggleFilter.setOnClickListener {
            val isExpanded = llFilterContainer.visibility == View.VISIBLE
            if (isExpanded) {
                llFilterContainer.visibility = View.GONE
                btnToggleFilter.animate().rotation(0f).setDuration(200).start()
            } else {
                llFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.animate().rotation(90f).setDuration(200).start()
            }
        }

        // Instant search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Track temp states for radio buttons
        rgStatusFilter.setOnCheckedChangeListener { _, checkedId ->
            tempFilterType = when (checkedId) {
                R.id.rbAll -> 0
                R.id.rbBorrowed -> 1
                R.id.rbLost -> 2
                else -> 0
            }
        }

        // Track temp states for category spinner
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    tempCategoryId = 0 // Tất cả thể loại
                } else if (position > 0 && position - 1 < categoryList.size) {
                    tempCategoryId = categoryList[position - 1].categoryId
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Apply filters button
        btnApplyFilter.setOnClickListener {
            currentFilterType = tempFilterType
            currentCategoryId = tempCategoryId
            applyFilters()
            
            // Auto close filter panel after apply
            llFilterContainer.visibility = View.GONE
            btnToggleFilter.animate().rotation(0f).setDuration(200).start()
        }
        
        // Reset filters button
        btnResetFilter.setOnClickListener {
            rgStatusFilter.check(R.id.rbAll)
            spinnerCategory.setSelection(0)
            
            tempFilterType = 0
            tempCategoryId = 0
            currentFilterType = 0
            currentCategoryId = 0
            
            applyFilters()
        }

        btnAddBook.setOnClickListener {
            // TODO: Navigate to Add Book (e.g., findNavController().navigate(R.id.createBookFragment))
        }
    }

    private fun loadCategoryMap() {
        GlobalScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val categories = db.libraryDao().getAllCategories()
                categoryList = categories
                val map = categories.associate { it.categoryId to it.name }
                
                val spinnerItems = mutableListOf("Tất cả thể loại")
                spinnerItems.addAll(categories.map { it.name })
                
                requireActivity().runOnUiThread {
                    bookAdapter.updateCategories(map)
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        spinnerItems
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
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
            
            val matchesCategory = if (currentCategoryId == 0) true else {
                book.categoryId == currentCategoryId
            }
            
            val matchesFilter = when (currentFilterType) {
                0 -> true // Tất cả
                1 -> (book.totalQuantity - book.availableQuantity - book.lostQuantity) > 0 // Đang mượn 
                2 -> book.lostQuantity > 0 // Đã mất
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesFilter
        }
        
        bookAdapter.submitList(filtered)
    }
}