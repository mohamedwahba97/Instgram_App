package com.example.moham.instagramapp.Profile

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

import com.example.moham.instagramapp.Login.LoginActivity
import com.example.moham.instagramapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignOutFragment : Fragment() {

    //firebase
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private var mProgressBar: ProgressBar? = null
    private var tvSignout: TextView? = null
    private var tvSigningOut: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signout, container, false)
        tvSignout = view.findViewById(R.id.tvConfirmSignout)
        mProgressBar = view.findViewById(R.id.progressBar)
        tvSigningOut = view.findViewById(R.id.tvSigningOut)
        val btnConfirmSignout = view.findViewById<Button>(R.id.btnConfirmSignout)

        mProgressBar!!.visibility = View.GONE
        tvSigningOut!!.visibility = View.GONE

        setupFirebaseAuth()

        btnConfirmSignout.setOnClickListener {
            Log.d(TAG, "onClick: attempting to sign out.")
            mProgressBar!!.visibility = View.VISIBLE
            tvSigningOut!!.visibility = View.VISIBLE

            mAuth!!.signOut()
            activity!!.finish()
        }

        return view
    }

    /*
    ------------------------------------ Firebase ---------------------------------------------
     */


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

                Log.d(TAG, "onAuthStateChanged: navigating back to login screen.")
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            // ...
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

    companion object {

        private val TAG = "SignOutFragment"
    }

}
