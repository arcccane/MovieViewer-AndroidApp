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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityRegisterBinding
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private lateinit var binding: ActivityRegisterBinding

    private var appCoroutineScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        appCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?){
                Log.d("Register",result?.userState?.name.toString())
            }

            override fun onError(e: Exception?) {
                Log.d("Register","There is an error - ${e.toString()}")
            }
        })
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

    fun runRegister(v: View) {

        val loginName = binding.regNameET.text.toString()
        val password = binding.regPassET.text.toString()
        val email = binding.regEmailET.text.toString()
        val admin = binding.regAdmET.text.toString()
        val pem = binding.regPemET.text.toString()

        appCoroutineScope?.launch(Dispatchers.IO) {

            val userPool = CognitoUserPool(v.context, AWSMobileClient.getInstance().configuration)
            val userAttributes = CognitoUserAttributes()
            userAttributes.addAttribute("custom:PemGrp",pem)
            userAttributes.addAttribute("custom:AdminNumber",admin)
            userAttributes.addAttribute("email",email)

            userPool.signUp(
                loginName,
                password,
                userAttributes,
                null, object : SignUpHandler {
                    override fun onSuccess(user: CognitoUser?, signUpResult: SignUpResult?) {
                        Log.d("Register", "Sign up success ${signUpResult?.userConfirmed}")
                        val i = Intent(this@RegisterActivity, VerificationActivity::class.java)
                        i.putExtra("loginName",loginName)
                        startActivity(i)
                        runOnUiThread {
                            displayToast("Login Name : " + regNameET.text.toString() + "\n"
                                    + "Password : " + regPassET.text.toString() + "\n"
                                    + "Email : " + regEmailET.text.toString() + "\n"
                                    + "Admin Number : " + regAdmET.text.toString() + "\n"
                                    + "PEM Grp : " + regPemET.text.toString())
                        }
                    }

                    override fun onFailure(exception: Exception?) {
                        Log.d("Register","Exception : ${exception?.message}")
                    }
                }
            )
        }
    }
    fun runBack(v: View) {
        finish()
    }
}