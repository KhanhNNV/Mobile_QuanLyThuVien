package com.example.quanlythuvien.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CrudStaffFragment: Fragment() {
    fun getMockStaffData(): List<StaffData> {
        return listOf(
            StaffData(
                name = "Nguyễn Trung",
                staffId = 1001L,
                userName = "admin_trungtam",
                isActive = true
            ),
            StaffData(
                name = "Nguyễn Văn A",
                staffId = 1002L,
                userName = "nv_nguyenvana",
                isActive = true
            ),
            StaffData(
                name = "Lê Thị B",
                staffId = 1003L,
                userName = "nv_lethib",
                isActive = false // Đã khóa
            ),
            StaffData(
                name = "Trần Văn C",
                staffId = 1004L,
                userName = "nv_tranc",
                isActive = true
            ),
            StaffData(
                name = "Phạm Chí D",
                staffId = 1005L,
                userName = "nv_phamchid",
                isActive = false // Đã khóa
            ),
            StaffData(
                name = "Admin Cơ Sở 2",
                staffId = 1006L,
                userName = "admin_coso2",
                isActive = true
            )
        )
    }
    // Khai báo các View
    private lateinit var autoSearchStaff: AutoCompleteTextView
    private lateinit var spnStatusFilter: Spinner
    private lateinit var recyclerViewStaff: RecyclerView

    //Khai báo nút thêm Staff
    private lateinit var fabAddStaff: FloatingActionButton


    //Khai báo list mẫu
    private lateinit var listStaff: MutableList<StaffData>

    //Apdapter cho RecyclerView
    private lateinit var adapter: CrudStaffAdapter

    private lateinit var btnBack: ImageButton
    //Hàm tạo giao diện
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_manager_staff_of_admin, container, false)
    }


    //Hàm ánh xạ view
    // Bạn đặt hàm này ở bên dưới, trong class Fragment của bạn
    private fun initViews(view: View) {
        // 1. Ánh xạ các View trên layout chính
        autoSearchStaff = view.findViewById(R.id.autoSearchStaff)
        spnStatusFilter = view.findViewById(R.id.spnStatusFilter)
        recyclerViewStaff = view.findViewById(R.id.recyclerViewStaff)

        // 2. Ánh xạ nút FAB từ file include
        fabAddStaff = view.findViewById(R.id.fasAddStaff)

        // 3. Cài đặt LayoutManager cho RecyclerView (Bắt buộc để danh sách hiển thị dạng dọc)
        recyclerViewStaff=view.findViewById(R.id.recyclerViewStaff)
        btnBack=view.findViewById(R.id.btnBack)
    }

    //Hàm sử dụng view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomHeader(
            view = view,
            title = "Quản lý nhân viên",
            subtitle = "Tổng số nhân viên"
        )

        initViews(view)

        listStaff=getMockStaffData().toMutableList()
        adapter = CrudStaffAdapter { staffItem, action ->
            when (action) {
                "EDIT" -> {
                    Toast.makeText(requireContext(), "Bạn muốn SỬA: ${staffItem.userName}", Toast.LENGTH_SHORT).show()
                    // TODO: Mở màn hình/Dialog sửa nhân viên
                }
                "DELETE" -> {
                    Toast.makeText(requireContext(), "Bạn muốn XÓA: ${staffItem.userName}", Toast.LENGTH_SHORT).show()
                    // TODO: Hiển thị Dialog xác nhận xóa
                }
            }
        }


        recyclerViewStaff.adapter = adapter
        recyclerViewStaff.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(listStaff)

        setupSpinner()
        handleSpinnerEvent()
        handleButtonBackEvent()

    }

    //Hàm đổ dữ liệu vào spinner
    private fun setupSpinner() {
        // 1. Sửa 'this' thành 'requireContext()'
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.staff_status_options,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnStatusFilter.adapter = adapter

    }
    //Hàm xử lý Spinner
    private fun handleSpinnerEvent() {
        spnStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val listFilter = listStaff.filter { item ->
                    when (position) {
                        1 -> item.isActive == true  // Chọn "Đang hoạt động" -> Giữ lại người có is_active là true
                        2 -> item.isActive == false // Chọn "Đã khóa" -> Giữ lại người có is_active là false
                        else -> true                 // Chọn "Tất cả" (position = 0) -> return true để giữ lại hết
                    }
                }
                adapter.submitList(listFilter)

                if (listFilter.isEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy nhân viên phù hợp!", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không cần xử lý
            }
        }
    }

    private fun handleButtonBackEvent(){
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}