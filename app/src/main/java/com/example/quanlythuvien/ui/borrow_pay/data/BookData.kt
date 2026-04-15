package com.example.quanlythuvien.ui.borrow_pay.data

data class BookData (
    val copyId: Long = 0L,     // THÊM: Dùng để gửi xuống API Update
    val title: String,
    val author: String,
    val categoryName: String,
    val barcode: String = ""   // THÊM (Tùy chọn): Để hiển thị thêm mã vạch cho rõ ràng
) {
    // Override hàm toString() là cách NHANH NHẤT để Spinner hiển thị chữ đẹp mắt
    // Khi bạn đưa một Object vào ArrayAdapter, Spinner sẽ gọi toString() của Object đó để lấy chữ hiển thị.
    override fun toString(): String {
        return if (copyId == 0L) {
            title // Dành cho lựa chọn đầu tiên "Giữ nguyên (Không đổi)"
        } else {
            // Dành cho các cuốn sách thật: "Clean Code - Robert C. Martin (CC-001)"
            "$title - $author ($barcode)"
        }
    }
}