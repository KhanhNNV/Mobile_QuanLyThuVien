package com.example.quanlythuvien.ui.admin

data class StaffData(
    var staffId: Long,          // Tương ứng user_id (PK)
    var libraryName: String,
    var userName: String,       // Tương ứng username (Unique)
    var passwordHash: String,   // Tương ứng password_hash
    var name: String,           // Tương ứng fullname
    var isActive: Boolean       // Trạng thái hoạt động (Boolean)
)