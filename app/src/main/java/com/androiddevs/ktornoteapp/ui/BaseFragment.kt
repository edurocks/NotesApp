package com.androiddevs.ktornoteapp.ui

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    fun showSnackbar(text: String) {
        Snackbar.make(requireActivity().rootLayout, text, Snackbar.LENGTH_LONG).show()
    }
}