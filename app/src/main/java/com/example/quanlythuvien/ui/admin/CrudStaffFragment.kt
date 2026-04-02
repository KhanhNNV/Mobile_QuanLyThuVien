package com.example.quanlythuvien.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CrudStaffFragment: Fragment() {
    fun getMockStaffData(): List<StaffData> {
        return listOf(
            StaffData(
                userName = "admin_trungtam",
                passWord = "hash_e10adc3949ba59abbe56e057f20f883e", // Thực tế sẽ là chuỗi mã hóa
                libraryName = "Thư viện Trung tâm",
                is_active = true
            ),
            StaffData(
                userName = "nv_nguyenvana",
                passWord = "hash_8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                libraryName = "Thư viện Cơ sở 1",
                is_active = true
            ),
            StaffData(
                userName = "nv_lethib",
                passWord = "hash_c33367701511b4f6020ec61ded352059",
                libraryName = "Thư viện Cơ sở 2",
                is_active = false // Test trạng thái "Đã khóa"
            ),
            StaffData(
                userName = "nv_tranc",
                passWord = "hash_0cc175b9c0f1b6a831c399e269772661",
                libraryName = "Thư viện Trung tâm",
                is_active = true
            ),
            StaffData(
                userName = "nv_phamchid",
                passWord = "hash_92eb5ffee6ae2fec3ad71c777531578f",
                libraryName = "Thư viện Cơ sở 1",
                is_active = false // Test trạng thái "Đã khóa"
            ),
            StaffData(
                userName = "admin_coso2",
                passWord = "hash_4a8a08f09d37b73795649038408b5f33",
                libraryName = "Thư viện Cơ sở 2",
                is_active = true
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
                        1 -> item.is_active == true  // Chọn "Đang hoạt động" -> Giữ lại người có is_active là true
                        2 -> item.is_active == false // Chọn "Đã khóa" -> Giữ lại người có is_active là false
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


}