package com.example.quanlythuvien.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CrudStaffFragment : Fragment() {

    private fun getMockStaffData(): List<StaffData> {
        return listOf(
            StaffData(1001L, "Thư viện Trung Tâm", "admin_trungtam", "hash123", "Nguyễn Trung", true),
            StaffData(1002L, "Thư viện Cơ sở 1", "nv_nguyenvana", "hash123", "Nguyễn Văn A", true),
            StaffData(1003L, "Thư viện Cơ sở 1", "nv_lethib", "hash123", "Lê Thị B", false),
            StaffData(1004L, "Thư viện Cơ sở 2", "nv_tranc", "hash123", "Trần Văn C", true)
        )
    }

    private lateinit var autoSearchStaff: AutoCompleteTextView
    private lateinit var spnStatusFilter: Spinner
    private lateinit var recyclerViewStaff: RecyclerView
    private lateinit var fabAddStaff: FloatingActionButton
    private lateinit var listStaff: MutableList<StaffData>
    private lateinit var adapter: CrudStaffAdapter
    private lateinit var btnBack: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_manager_staff_of_admin, container, false)
    }

    private fun initViews(view: View) {
        autoSearchStaff = view.findViewById(R.id.autoSearchStaff)
        spnStatusFilter = view.findViewById(R.id.spnStatusFilter)
        recyclerViewStaff = view.findViewById(R.id.recyclerViewStaff)
        fabAddStaff = view.findViewById(R.id.fasAddStaff)
        btnBack = view.findViewById(R.id.btnBack)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý nhân viên")
        initViews(view)

        fabAddStaff.setOnClickListener {
            findNavController().navigate(R.id.employeeAddFragment)
        }

        listStaff = getMockStaffData().toMutableList()

        adapter = CrudStaffAdapter { staffItem, action ->
            when (action) {
                "EDIT" -> handleEditStaff(staffItem)//Gọi hàm dialog edit
                "DELETE" -> handleDeleteStaff(staffItem)//Gọi hàm dialog delete
            }
        }

        recyclerViewStaff.adapter = adapter
        recyclerViewStaff.layoutManager = LinearLayoutManager(requireContext())

        // Luôn gửi bản sao list để đảm bảo DiffUtil hoạt động ổn định
        adapter.submitList(listStaff.toList())

        autoSearchStaff.addTextChangedListener { applyFilter() }
        setupSpinner()
        handleSpinnerEvent()

        btnBack.setOnClickListener { findNavController().popBackStack() }
    }


    //Hàm hiển thị dialog xử lý sửa thông tin Staff
    private fun handleEditStaff(staffItem: StaffData) {
        val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_staff, null)

        val edtName = viewDialog.findViewById<EditText>(R.id.edtEditFullname)
        val edtUser = viewDialog.findViewById<EditText>(R.id.edtEditUsername)
        val edtPass = viewDialog.findViewById<EditText>(R.id.edtEditPassword)
        // Ánh xạ Switch mới bổ sung
        val swActive = viewDialog.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.swEditActive)

        // Đổ dữ liệu hiện tại vào View
        edtName.setText(staffItem.name)
        edtUser.setText(staffItem.userName)
        swActive.isChecked = staffItem.isActive // Gán trạng thái hiện tại

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa tài khoản nhân viên")
            .setView(viewDialog)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu thay đổi") { _, _ ->
                val newName = edtName.text.toString().trim()
                val newUser = edtUser.text.toString().trim()
                val newPass = edtPass.text.toString().trim()
                val newStatus = swActive.isChecked // Lấy trạng thái từ Switch

                if (newName.isEmpty() || newUser.isEmpty()) {
                    Toast.makeText(requireContext(), "Thông tin không được để trống!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val index = listStaff.indexOfFirst { it.staffId == staffItem.staffId }
                if (index != -1) {
                    // Sử dụng .copy() để tạo object mới (Giải quyết lỗi UI không cập nhật)
                    val updatedStaff = listStaff[index].copy(
                        name = newName,
                        userName = newUser,
                        isActive = newStatus, // Cập nhật trạng thái mới
                        passwordHash = if (newPass.isNotEmpty()) newPass else listStaff[index].passwordHash
                    )

                    listStaff[index] = updatedStaff
                    applyFilter() // Làm mới danh sách
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    //Hàm hiển thị dialog xóa Staff
    private fun handleDeleteStaff(staffItem: StaffData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa nhân viên ${staffItem.name}?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                val isRemoved = listStaff.removeAll { it.staffId == staffItem.staffId }
                if (isRemoved) {
                    applyFilter()
                    Toast.makeText(requireContext(), "Đã xóa nhân viên", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    //Đổ dữ liệu lọc cho spinner
    private fun setupSpinner() {
        val adapterSpinner = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.staff_status_options,
            android.R.layout.simple_spinner_item
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnStatusFilter.adapter = adapterSpinner
    }

    //Hàm lọc để cập nhật lại danh sách khi có xử kiện thay đổi dữ liệu
    private fun applyFilter() {
        //Lấy dữ liệu của ô tìm kiếm
        val searchQuery = autoSearchStaff.text.toString().trim().lowercase()

        //Lấy dữ liệu người dùng đã chọn trong spinner
        val statusPosition = spnStatusFilter.selectedItemPosition

        //Lọc theo trạng thái (nếu người dùng không chọn thì all true)
        val filteredList = listStaff.filter { item ->
            val statusMatch = when (statusPosition) {
                1 -> item.isActive
                2 -> !item.isActive
                else -> true
            }
        //Lọc theo dữ liệu đã nhập
            val searchMatch = if (searchQuery.isEmpty()) true else {
                        //kiểm tra xem có dữ liệu trong tên staff nào không
                        item.name.lowercase().contains(searchQuery) ||
                                //kiểm tra xem có dữ liệu trong username của staff nào không
                        item.userName.lowercase().contains(searchQuery)
            }
            statusMatch && searchMatch
        }

        // CẬP NHẬT QUAN TRỌNG: Sử dụng .toList() để tạo bản sao danh sách mới
        // Điều này kích hoạt DiffUtil so sánh và vẽ lại các item bị đổi
        adapter.submitList(filteredList.toList())
    }


    //Hàm xử lý khi có sự kiện mở spinner
    private fun handleSpinnerEvent() {
        spnStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                applyFilter()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}