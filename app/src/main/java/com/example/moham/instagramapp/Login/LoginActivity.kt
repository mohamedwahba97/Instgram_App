package com.example.moham.instagramapp.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.example.moham.instagramapp.Home.HomeActivity
import com.example.moham.instagramapp.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity:AppCompatActivity() {

    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mContext:Context
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mEmail:EditText
    private lateinit var mPassword:EditText
    private lateinit var mPleaseWait:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mProgressBar = findViewById(R.id.progressBar)
        mPleaseWait = findViewById(R.id.pleaseWait)
        mEmail = findViewById(R.id.input_email)
        mPassword = findViewById(R.id.input_password)
        mContext = this@LoginActivity
        Log.d(TAG, "onCreate: started.")
        mPleaseWait.visibility = View.GONE
        mProgressBar.visibility = View.GONE
        setupFirebaseAuth()
        init()
    }
    private fun isStringNull(string:String):Boolean {
        Log.d(TAG, "isStringNull: checking string if null.")
        return string == ""
    }
   /*
   ------------------------------------ Firebase ---------------------------------------------
   */
    private fun init() {
        //initialize the button for logging in
       val btnLogin = findViewById(R.id.btn_login) as Button
       btnLogin.setOnClickListener(object:View.OnClickListener {
          override fun onClick(v:View) {
               Log.d(TAG, "onClick: attempting to log in.")
               val email = mEmail.getText().toString()
               val password = mPassword.getText().toString()
               if (isStringNull(email) && isStringNull(password))
               {
                   Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show()
               }
               else
               {
                   mProgressBar.setVisibility(View.VISIBLE)
                   mPleaseWait.setVisibility(View.VISIBLE)
                   mAuth.signInWithEmailAndPassword(email, password)
                           .addOnCompleteListener(this@LoginActivity, object: OnCompleteListener<AuthResult> {
                              override fun onComplete(@NonNull task: Task<AuthResult>) {
                                   Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful())
                                   val user = mAuth.getCurrentUser()
                                   // If sign in fails, display a message to the user. If sign in succeeds
                                   // the auth state listener will be notified and logic to handle the
                                   // signed in user can be handled in the listener.
                                   if (!task.isSuccessful())
                                   {
                                       Log.w(TAG, "signInWithEmail:failed", task.getException())
                                       Toast.makeText(this@LoginActivity, getString(R.string.auth_failed),
                                               Toast.LENGTH_SHORT).show()
                                       mProgressBar.setVisibility(View.GONE)
                                       mPleaseWait.setVisibility(View.GONE)
                                   }
                                   else
                                   {
                                       try
                                       {
                                           if (CHECK_IF_VERIFIED)
                                           {
                                               if (user!!.isEmailVerified())
                                               {
                                                   Log.d(TAG, "onComplete: success. email is verified.")
                                                   val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                                   startActivity(intent)
                                               }
                                               else
                                               {
                                                   Toast.makeText(mContext, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show()
                                                   mProgressBar.setVisibility(View.GONE)
                                                   mPleaseWait.setVisibility(View.GONE)
                                                   mAuth.signOut()
                                               }
                                           }
                                           else
                                           {
                                               Log.d(TAG, "onComplete: success. email is verified.")
                                               val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                               startActivity(intent)
                                           }
                                       }
                                       catch (e:NullPointerException) {
                                           Log.e(TAG, "onComplete: NullPointerException: " + e.message)
                                       }
                                   }
                                   // ...
                               }
                           })
               }
           }
       })
       val linkSignUp = findViewById(R.id.link_signup) as TextView
       linkSignUp.setOnClickListener(object:View.OnClickListener {
          override fun onClick(v:View) {
               Log.d(TAG, "onClick: navigating to register screen")
               val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
               startActivity(intent)
           }
       })
       /*
    If the user is logged in then navigate to HomeActivity and call 'finish()'
    */
       if (mAuth.getCurrentUser() != null)
       {
           val intent = Intent(this@LoginActivity, HomeActivity::class.java)
           startActivity(intent)
           finish()
       }
    }
    /**
     * Setup the firebase auth object
     */
    private fun setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.")
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            // ...
        }
    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }
    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }
    companion object {
        private const val TAG = "LoginActivity"
        private val CHECK_IF_VERIFIED = false
    }
}
