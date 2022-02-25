package com.nyp.sit.movieviewer_intermediate_starter_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.google.android.material.textfield.TextInputLayout
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import android.view.ViewGroup
import org.w3c.dom.Text


class LoginActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private lateinit var binding: ActivityLoginBinding

    private var appCoroutineScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        appCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?){
                Log.d("Login",result?.userState?.name.toString())
                if (result?.userState?.name.toString() == "SIGNED_IN") {
                    val i = Intent(this@LoginActivity, ViewListOfMoviesActivity::class.java)
                    startActivity(i)
                    runOnUiThread {
                        displayToast("Logged In")
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.d("Login","There is an error - ${e.toString()}")
            }
        })

        binding.regBtn.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

    }
    fun runLogin(v: View) {

        appCoroutineScope?.launch {
            AWSMobileClient.getInstance().signIn(
                binding.loginNameET.text.toString(),
                binding.loginPassET.text.toString(),
                null, object : Callback<SignInResult> {
                    override fun onResult(result: SignInResult?) {
                        Log.d("Login","Sign in result : ${result.toString()}")
                        if (result?.signInState == SignInState.DONE){
                            val i = Intent(v.context,ViewListOfMoviesActivity::class.java)
                            startActivity(i)
                            runOnUiThread {
                                displayToast("Logged In")
                            }
                        }
                    }
                    override fun onError(e: Exception?) {
                        Log.d("Login","Sign in error : ${e.toString()}")
                    }
                }
            )
        }
    }
}