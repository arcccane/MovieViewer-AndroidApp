package com.nyp.sit.movieviewer_intermediate_starter_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignUpResult
import com.nyp.sit.movieviewer_intermediate_starter_project.R
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityRegisterBinding
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityVerificationBinding
import kotlinx.android.synthetic.main.activity_verification.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class VerificationActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private lateinit var binding: ActivityVerificationBinding

    private var appCoroutineScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerificationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        appCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?){
                Log.d("Verify",result?.userState?.name.toString())
            }

            override fun onError(e: Exception?) {
                Log.d("Verify","There is an error - ${e.toString()}")
            }
        })
    }

    fun runVerificationCode(v: View) {

        val loginName = intent.getStringExtra("loginName")

        appCoroutineScope?.launch {
            AWSMobileClient.getInstance().confirmSignUp(
                loginName,
                binding.verifyET.text.toString(),
                object : Callback<com.amazonaws.mobile.client.results.SignUpResult> {

                    override fun onResult(result: com.amazonaws.mobile.client.results.SignUpResult?) {
                        Log.d("Verify","Sign up result - ${result?.confirmationState}")
                        val i = Intent(this@VerificationActivity, LoginActivity::class.java)
                        startActivity(i)
                        runOnUiThread {
                            displayToast("Account Verified")
                        }
                    }

                    override fun onError(e: Exception?) {
                        Log.d("Verify","Sign up result error - ${e.toString()}")
                    }
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?):Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}