package com.example.quanlythuvien.ui.feeInvoices

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse
import com.example.quanlythuvien.data.remote.FeeInvoiceApiService
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.FeeInvoiceRepository
import com.example.quanlythuvien.data.repository.ReaderRepository

import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupCustomHeader
import com.example.quanlythuvien.utils.setupHeaderWithBack
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InvoiceListFragment : Fragment(R.layout.fragment_invoice_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var viewModel: InvoiceViewModel
    private lateinit var sharedViewModel: InvoiceSharedViewModel
    private lateinit var autoSearch: AutoCompleteTextView
    private lateinit var spinnerStatus: Spinner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(view, "Quản lý hóa đơn","*số lượng hóa đơn")

        initViews(view)
        setupViewModels()
        setupRecyclerView()
        setupSearchAndFilter()
        observeViewModels()
        viewModel.fetchInvoices()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewInvoice)
        autoSearch = view.findViewById(R.id.autoSearchInvoice)
        spinnerStatus = view.findViewById(R.id.spnStatusFilter)
    }

    private fun setupViewModels() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        val invoiceApiService = retrofit.create(FeeInvoiceApiService::class.java)

        val invoiceRepository = FeeInvoiceRepository(invoiceApiService)

        val factory = GenericViewModelFactory {
            InvoiceViewModel(invoiceRepository)
        }
        viewModel = ViewModelProvider(this, factory)[InvoiceViewModel::class.java]

        // SharedViewModel cho việc chia sẻ dữ liệu giữa các fragment
        sharedViewModel = ViewModelProvider(requireActivity())[InvoiceSharedViewModel::class.java]
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        invoiceAdapter = InvoiceAdapter(
            invoiceList = mutableListOf(),
            onItemClick = { invoice ->
                // Dùng SharedViewModel để lưu ID và chuyển trang
                sharedViewModel.selectInvoice(invoice.invoiceId)
                findNavController().navigate(R.id.action_invoiceList_to_invoiceDetail)
            },
            onOptionsClick = { invoice, view ->
                showPopupMenu(invoice, view)
            }
        )
        recyclerView.adapter = invoiceAdapter
    }

    private fun setupSearchAndFilter() {
        val statusFilters = InvoiceStatusFilter.values().map { it.displayName }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusFilters
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = spinnerAdapter

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = InvoiceStatusFilter.values()[position]
                viewModel.setStatusFilter(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Search
        autoSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length >= 2) {
                    viewModel.searchInvoices(s.toString())
                } else if (s.isNullOrEmpty()) {
                    viewModel.searchInvoices("")
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredInvoices.collectLatest { invoices ->
                        invoiceAdapter.updateData(invoices)
                    }
                }

                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is InvoiceState.Loading -> {
                            }
                            is InvoiceState.SuccessList -> {
                                // Dữ liệu đã được cập nhật thông qua luồng filteredInvoices
                            }
                            is InvoiceState.SuccessAction -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            is InvoiceState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                // Lắng nghe sự kiện refresh từ SharedViewModel
                launch {
                    sharedViewModel.shouldRefreshList.collectLatest { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.fetchInvoices()
                            sharedViewModel.resetRefreshFlag()
                        }
                    }
                }
            }
        }
    }


    private fun showPopupMenu(invoice: FeeInvoiceResponse, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_invoice_options, popup.menu)



        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_detail -> {
                    // Dùng SharedViewModel để lưu ID và chuyển trang
                    sharedViewModel.selectInvoice(invoice.invoiceId)
                    findNavController().navigate(R.id.action_invoiceList_to_invoiceDetail)
                    true
                }
                R.id.action_print -> {
                    Toast.makeText(requireContext(), "In hóa đơn ${invoice.invoiceId}", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}