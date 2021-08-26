package com.androiddevs.ktornoteapp.ui.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.ktornoteapp.R
import com.androiddevs.ktornoteapp.data.other.Constants
import com.androiddevs.ktornoteapp.data.other.Status
import com.androiddevs.ktornoteapp.data.remote.BasicAuthInterceptor
import com.androiddevs.ktornoteapp.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_auth.*
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : BaseFragment(R.layout.fragment_auth) {

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var curEmail: String? = null
    private var curPassword: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isLoggedIn()) {
            authenticateApi(curEmail ?: "", curPassword ?: "")
            redirectLogin()
        }

        subscribeToObservers()

        btnRegister.setOnClickListener {
            viewModel.register(etRegisterEmail.text.toString(), etRegisterPassword.text.toString(),
                               etRegisterPasswordConfirm.text.toString())
        }

        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()
            curEmail = email
            curPassword = password
            viewModel.login(email, password)
        }
    }

    private fun isLoggedIn(): Boolean {
        curEmail = sharedPref.getString(Constants.KEY_LOGGED_IN_EMAIL, Constants.NO_EMAIL) ?: Constants.NO_EMAIL
        curPassword = sharedPref.getString(Constants.KEY_PASSWORD, Constants.NO_PASSWORD) ?: Constants.NO_PASSWORD
        return curEmail != Constants.NO_EMAIL && curPassword != Constants.NO_PASSWORD
    }

    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email = email
        basicAuthInterceptor.password = password
    }

    private fun redirectLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.authFragment, true)
            .build()

        findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToNotesFragment(), navOptions)
    }

    private fun subscribeToObservers() {
        viewModel.registerStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when(result.status) {
                    Status.SUCCESS -> {
                        registerProgressBar.visibility = View.GONE
                        showSnackbar(result.data ?: "Successfully registered an account")
                    }
                    Status.ERROR -> {
                        registerProgressBar.visibility = View.GONE
                        showSnackbar(result.message ?: "An unknown error occured")
                    }
                    Status.LOADING -> {
                      registerProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.loginStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when(result.status) {
                    Status.SUCCESS -> {
                        loginProgressBar.visibility = View.GONE
                        showSnackbar(result.data ?: "Successfully logged in")
                        sharedPref.edit().putString(Constants.KEY_LOGGED_IN_EMAIL, curEmail).apply()
                        sharedPref.edit().putString(Constants.KEY_PASSWORD, curPassword).apply()
                        authenticateApi(curEmail ?: "" , curPassword ?: "")
                        redirectLogin()
                    }
                    Status.ERROR -> {
                        loginProgressBar.visibility = View.GONE
                        showSnackbar(result.message ?: "An unknown error occured")
                    }
                    Status.LOADING -> {
                        loginProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        })
    }
}