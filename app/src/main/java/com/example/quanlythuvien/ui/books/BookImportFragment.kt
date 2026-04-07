package com.example.quanlythuvien.ui.books

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.quanlythuvien.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class BookImportFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện từ file fragment_book_import.xml
        return inflater.inflate(R.layout.fragment_book_import, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val edtBookName = view.findViewById<TextInputEditText>(R.id.edtBookName)
        val edtAuthor = view.findViewById<TextInputEditText>(R.id.edtAuthor)
        val edtIsbn = view.findViewById<TextInputEditText>(R.id.edtIsbn)
        val spinnerCategory =view.findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val edtCost = view.findViewById<TextInputEditText>(R.id.edtCost)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        btnSave.setOnClickListener {
            val bookName = edtBookName.text.toString().trim()
            val author = edtAuthor.text.toString().trim()
            val isbn = edtIsbn.text.toString().trim()
            val spCategory = spinnerCategory.text.toString().trim()
            val cost = edtCost.text.toString().trim()

            if (bookName.isEmpty())
            {
                edtBookName.error = "Vui lòng nhập tên sách!"
                edtBookName.requestFocus()
            }else if(author.isEmpty())
            {
                edtAuthor.error = "Vui lòng nhập tên tác giả!"
                edtAuthor.requestFocus()
            }else if (isbn.isEmpty()) {
                edtIsbn.error = "Vui lòng nhập mã ISBN!"
                edtIsbn.requestFocus()

            } else if (cost.isEmpty() || cost == "VND")
            {
                edtCost.error = "Vui lòng nhập giá gốc của sách!"
                edtCost.requestFocus()

            } else {
                Toast.makeText(requireContext(), "Đã lưu sách: $bookName", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

    }

}