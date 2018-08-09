package com.example.moham.instagramapp.Login

import android.content.Context
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.FirebaseMethods
import com.example.moham.instagramapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"
    private lateinit var mContext:Context
    private lateinit var email:String
    private lateinit var username:String
    private lateinit var password:String
    private lateinit var mEmail:EditText
    private lateinit var mPassword:EditText
    private lateinit var mUsername:EditText
    private lateinit var loadingPleaseWait:TextView
    private lateinit var btnRegister:Button
    private lateinit var mProgressBar:ProgressBar
    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var firebaseMethods:FirebaseMethods
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    private var append = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mContext = this@RegisterActivity
        //FirebaseMethods(mContext)
        Log.d(TAG, "onCreate: started.")
        initWidgets()
        setupFirebaseAuth()
        init()
    }

    private fun init() {
        btnRegister.setOnClickListener(object:View.OnClickListener {
           override fun onClick(v:View) {
                email = mEmail.getText().toString()
                username = mUsername.getText().toString()
                password = mPassword.getText().toString()
                if (checkInputs(email, username, password))
                {
                    mProgressBar.setVisibility(View.VISIBLE)
                    loadingPleaseWait.setVisibility(View.VISIBLE)
                    firebaseMethods.registerNewEmail(email, password, username)
                }
            }
        })
    }
    private fun checkInputs(email:String, username:String, password:String):Boolean {
        Log.d(TAG, "checkInputs: checking inputs for null values.")
        if (email == "" || username == "" || password == "")
        {
            Toast.makeText(mContext, "All fields must be filled out.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    /**
     * Initialize the activity widgets
     */
    private fun initWidgets() {
        Log.d(TAG, "initWidgets: Initializing Widgets.")
        mEmail = findViewById(R.id.input_email) as EditText
        mUsername = findViewById(R.id.input_username) as EditText
        btnRegister = findViewById(R.id.btn_register) as Button
        mProgressBar = findViewById(R.id.progressBar) as ProgressBar
        loadingPleaseWait = findViewById(R.id.loadingPleaseWait) as TextView
        mPassword = findViewById(R.id.input_password) as EditText
        mContext = this@RegisterActivity
        mProgressBar.setVisibility(View.GONE)
        loadingPleaseWait.setVisibility(View.GONE)
    }
    private fun isStringNull(string:String):Boolean {
        Log.d(TAG, "isStringNull: checking string if null.")
        if (string == "")
        {
            return true
        }
        else
        {
            return false
        }
    }
    /**
     * Check is @param username already exists in teh database
     * @param username
     */
    private fun checkIfUsernameExists(username:String) {
        Log.d(TAG, "checkIfUsernameExists: Checking if " + username + " already exists.")
        val reference = FirebaseDatabase.getInstance().getReference()
        val query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
          override  fun onDataChange(dataSnapshot:DataSnapshot) {
                for (singleSnapshot in dataSnapshot.getChildren())
                {
                    if (singleSnapshot.exists())
                    {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User::class.java)!!.username)
                        append = myRef.push().getKey()!!.substring(3, 10)
                        Log.d(TAG, "onDataChange: username already exists. Appending random string to name: " + append)
                    }
                }
                var mUsername = ""
                mUsername = username + append
                //add new user to the database
                firebaseMethods.addNewUser(email, mUsername, "", "", "")
                Toast.makeText(mContext, "Signup successful. Sending verification email.", Toast.LENGTH_SHORT).show()
                mAuth.signOut()
            }
            override fun onCancelled(databaseError:DatabaseError) {
            }
        })
    }
    /**
     * Setup the firebase auth object
     */
    private fun setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.")
        mAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase.getReference()
        mAuthListener = object:FirebaseAuth.AuthStateListener {
           override fun onAuthStateChanged(@NonNull firebaseAuth:FirebaseAuth) {
                val user = firebaseAuth.getCurrentUser()
                if (user != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid())
                    myRef.addListenerForSingleValueEvent(object:ValueEventListener {
                      override  fun onDataChange(dataSnapshot:DataSnapshot) {
                            checkIfUsernameExists(username)
                        }
                      override  fun onCancelled(databaseError:DatabaseError) {
                        }
                    })
                    finish()
                }
                else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out")
                }
                // ...
            }
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

        private const val TAG = "RegisterActivity"
    }

}
