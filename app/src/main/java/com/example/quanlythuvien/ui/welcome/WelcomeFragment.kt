package com.example.quanlythuvien.ui.welcome

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private lateinit var btnCreateLibrary: Button
    private lateinit var btnLogin: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        handleButtonCreateLibraryEvent()
        handleButtonLoginEvent()
    }

    private fun initViews(view: View){
        btnCreateLibrary = view.findViewById(R.id.btnCreateLibrary)
        btnLogin=view.findViewById(R.id.btnLogin)
    }

    private fun handleButtonCreateLibraryEvent(){
        btnCreateLibrary.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun handleButtonLoginEvent(){
        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }
}