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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        return inflater.inflate(R.layout.fragment_manager_staff_of_admin, container, false)
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
        setupHeaderWithBack(
           view, "Quản lý nhân viên"
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
                    // 1. Luôn luôn hiển thị Dialog xác nhận trước khi xóa dữ liệu quan trọng
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa nhân viên ${staffItem.name} (${staffItem.userName}) khỏi hệ thống không?")
                        .setNegativeButton("Hủy", null) // Không làm gì cả nếu bấm Hủy
                        .setPositiveButton("Xóa") { _, _ ->

                            // 2. Xóa khỏi danh sách gốc (listStaff)
                            // Chúng ta dùng removeAll hoặc removeIf để tìm và xóa đúng ID
                            val isRemoved = listStaff.removeAll { it.staffId == staffItem.staffId }

                            if (isRemoved) {
                                // 3. CẬP NHẬT GIAO DIỆN
                                // Thay vì gọi adapter.submitList trực tiếp, chúng ta gọi applyFilter()
                                // để danh sách mới sau khi xóa vẫn giữ đúng bộ lọc (Search/Spinner) hiện tại.
                                applyFilter()

                                Toast.makeText(requireContext(), "Đã xóa nhân viên thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy nhân viên để xóa", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .show()
                }
            }
        }


        recyclerViewStaff.adapter = adapter
        recyclerViewStaff.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(listStaff)

        autoSearchStaff.addTextChangedListener {
            applyFilter() // Gọi hàm lọc mỗi khi có chữ thay đổi
        }

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

    // Hàm xử lý bộ lọc chung (Tìm kiếm + Trạng thái)
    private fun applyFilter() {
        // Lấy từ khóa tìm kiếm và đưa về chữ thường
        val searchQuery = autoSearchStaff.text.toString().trim().lowercase()
        // Lấy vị trí đang chọn của Spinner (0: Tất cả, 1: Đang hoạt động, 2: Đã khóa)
        val statusPosition = spnStatusFilter.selectedItemPosition

        val filteredList = listStaff.filter { item ->
            // 1. Kiểm tra điều kiện TRẠNG THÁI
            val statusMatch = when (statusPosition) {
                1 -> item.isActive == true
                2 -> item.isActive == false
                else -> true // Chọn "Tất cả"
            }

            // 2. Kiểm tra điều kiện TỪ KHÓA TÌM KIẾM
            val searchMatch = if (searchQuery.isEmpty()) {
                true // Nếu không gõ gì thì giữ lại hết
            } else {
                // Lọc theo Tên nhân viên
                val matchName = item.name.lowercase().contains(searchQuery)
                // Lọc theo Tên đăng nhập (Username)
                val matchUserName = item.userName.lowercase().contains(searchQuery)
                // Lọc theo Mã nhân viên
                val matchId = item.staffId.toString().contains(searchQuery)

                // Chỉ cần khớp 1 trong 3 thông tin là giữ lại
                matchName || matchUserName || matchId
            }

            // KẾT HỢP: Nhân viên đó phải thỏa mãn CẢ trạng thái VÀ từ khóa tìm kiếm
            statusMatch && searchMatch
        }

        // Gửi danh sách đã lọc cho Adapter hiển thị
        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy nhân viên phù hợp!", Toast.LENGTH_SHORT).show()
        }
    }


    //Hàm xử lý Spinner
    private fun handleSpinnerEvent() {
        spnStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
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