package com.example.moham.instagramapp.Share

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.FirebaseMethods
import com.example.moham.instagramapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NextActivity : AppCompatActivity() {

    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    private lateinit var mFirebaseMethods:FirebaseMethods
    //widgets
    private lateinit var mCaption:EditText
    //vars
    private val mAppend = "file:/"
    private var imageCount = 0
    private lateinit var imgUrl:String
    private lateinit var bitmap:Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)
        mFirebaseMethods = FirebaseMethods(this@NextActivity)
        mCaption = findViewById(R.id.caption)

        setupFirebaseAuth()

        val backArrow = findViewById<ImageView>(R.id.ivBackArrow)
        backArrow.setOnClickListener {
            Log.d(TAG, "onClick: closing the activity")
            finish()
        }


        val share = findViewById<TextView>(R.id.tvShare)
        share.setOnClickListener {
            Log.d(TAG, "onClick: navigating to the final share screen.")
            //upload the image to firebase
            Toast.makeText(this@NextActivity, "Attempting to upload new photo", Toast.LENGTH_SHORT).show()
            val caption = mCaption.text.toString()

            if (intent.hasExtra(getString(R.string.selected_image))) {
                imgUrl = intent.getStringExtra(getString(R.string.selected_image))
                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null)
            } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                bitmap = intent.getParcelableExtra<Parcelable>(getString(R.string.selected_bitmap)) as Bitmap
                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null.toString(), bitmap)
            }
        }

        setImage()
    }

    private fun someMethod() {
        /*
            Step 1)
            Create a data model for Photos
            Step 2)
            Add properties to the Photo Objects (caption, date, imageUrl, photo_id, tags, user_id)
            Step 3)
            Count the number of photos that the user already has.
            Step 4)
            a) Upload the photo to Firebase Storage
            b) insert into 'photos' node
            c) insert into 'user_photos' node
         */

    }


    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private fun setImage() {
        intent = intent
        val image = findViewById<ImageView>(R.id.imageShare)

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image))
            Log.d(TAG, "setImage: got new image url: $imgUrl")
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend)
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap))
            Log.d(TAG, "setImage: got new bitmap")
            image.setImageBitmap(bitmap)
        }
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
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase.reference
        Log.d(TAG, "onDataChange: image count: $imageCount")

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


        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot)
                Log.d(TAG, "onDataChange: image count: $imageCount")

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    public override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    public override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    companion object {

        private const val TAG = "NextActivity"
    }

}
