package com.example.quanlythuvien.ui.reader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlythuvien.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.data.entity.enums.ReaderType

class ReaderAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện fragment_reader_add.xml
        return inflater.inflate(R.layout.fragment_reader_add, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ánh xạ nút "Lưu Độc Giả" từ file XML sang biến Kotlin
        val edtReaderCode = view.findViewById<TextInputEditText>(R.id.edtReaderCode)
        val edtReaderName = view.findViewById<TextInputEditText>(R.id.edtReaderName)
        val edtReaderPhone = view.findViewById<TextInputEditText>(R.id.edtReaderPhone)
        val spinnerReaderType = view.findViewById<AutoCompleteTextView>(R.id.spinnerReaderType)
        val btnSaveReader = view.findViewById<MaterialButton>(R.id.btnSaveReader)
        val btnCancelReader = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelReader)

        val displayTypes = ReaderType.entries.map { it.value }.toTypedArray()

        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayTypes)
        spinnerReaderType.setAdapter(adapter)


        btnCancelReader.setOnClickListener {
            findNavController().popBackStack()
        }

        // 2. Lắng nghe sự kiện người dùng bấm vào nút Lưu
        btnSaveReader.setOnClickListener {

            val code = edtReaderCode.text.toString().trim()
            val name = edtReaderName.text.toString().trim()
            val phone = edtReaderPhone.text.toString().trim()
            val type = spinnerReaderType.text.toString().trim()

            if (code.isEmpty() || name.isEmpty() || phone.isEmpty() || type.isEmpty())
            {
                Toast.makeText(requireContext(),"Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //

            // 3. Gọi MaterialAlertDialogBuilder để tạo Popup thông báo
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Lưu thành công!")
                .setMessage("Độc giả đã được thêm vào hệ thống. Bạn có muốn xuất thông tin này ra file PDF để in thẻ độc giả không?")

                .setPositiveButton("In PDF") { dialog, _ ->
                    createPdf(code,name,phone,type)
                    dialog.dismiss() // Đóng popup lại
                    findNavController().popBackStack()
                }

                .setNegativeButton("Để sau") { dialog, _ ->
                    dialog.dismiss() // Chỉ đóng popup lại thôi
                    findNavController().popBackStack()
                }

                .show()
        }

    }
    private fun createPdf(code : String, name: String,phone: String ,type: String){

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(400,300,1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas : Canvas = page.canvas

        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 16f
        //
        paint.isFakeBoldText = true // chữ in đạm cho tiêu đề
        canvas.drawText("THẺ ĐỘC GIẢ THƯ VIỆN", 100f, 50f, paint)
        paint.isFakeBoldText = false // Chữ thường cho thông tin
        canvas.drawText("Mã thẻ: $code", 50f, 100f, paint)
        canvas.drawText("Họ và tên: $name", 50f, 140f, paint)
        canvas.drawText("Số điện thoại: $phone", 50f, 180f, paint)
        canvas.drawText("Loại độc giả: $type", 50f, 220f, paint)
        pdfDocument.finishPage(page)
        try {
            // Lưu file vào thư mục Documents riêng của App
            val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, "TheDocGia_$code.pdf")

            pdfDocument.writeTo(FileOutputStream(file))

            Toast.makeText(requireContext(), "Đã in PDF! Bạn vào thư mục máy để xem.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi khi tạo PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}