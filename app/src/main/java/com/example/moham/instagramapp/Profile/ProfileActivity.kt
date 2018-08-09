package com.example.moham.instagramapp.Profile

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.ViewCommentsFragment
import com.example.moham.instagramapp.Utils.ViewPostFragment
import com.example.moham.instagramapp.Utils.ViewProfileFragment
import com.example.moham.instagramapp.models.Photo
import com.example.moham.instagramapp.models.User
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity(), ProfileFragment.OnGridImageSelectedListener, ViewPostFragment.OnCommentThreadSelectedListener, ViewProfileFragment.OnGridImageSelectedListener {

    private val mContext = this@ProfileActivity
    private lateinit var mProgressBar:ProgressBar
    private lateinit var profilePhoto:ImageView

    override fun onCommentThreadSelectedListener(photo: Photo?) {
        Log.d(TAG, "onCommentThreadSelectedListener:  selected a comment thread")

        val fragment = ViewCommentsFragment()
        val args = Bundle()
        args.putParcelable(getString(R.string.photo), photo)
        fragment.arguments = args

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(getString(R.string.view_comments_fragment))
        transaction.commit()
    }

    override fun onGridImageSelected(photo: Photo, activityNumber: Int) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString())

        val fragment = ViewPostFragment()
        val args = Bundle()
        args.putParcelable(getString(R.string.photo), photo)
        args.putInt(getString(R.string.activity_number), activityNumber)

        fragment.arguments = args

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(getString(R.string.view_post_fragment))
        transaction.commit()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate: started.")

        init()


    }

    private fun init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment))

        val intent = intent
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: searching for user object attached as intent extra")
            if (intent.hasExtra(getString(R.string.intent_user))) {
                val user = intent.getParcelableExtra<User>(getString(R.string.intent_user))
                if (user.user_id != FirebaseAuth.getInstance().currentUser!!.uid) {
                    Log.d(TAG, "init: inflating view profile")
                    val fragment = ViewProfileFragment()
                    val args = Bundle()
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra<Parcelable>(getString(R.string.intent_user)))
                    fragment.arguments = args

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.container, fragment)
                    transaction.addToBackStack(getString(R.string.view_profile_fragment))
                    transaction.commit()
                } else {
                    Log.d(TAG, "init: inflating Profile")
                    val fragment = ProfileFragment()
                    val transaction = this@ProfileActivity.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.container, fragment)
                    transaction.addToBackStack(getString(R.string.profile_fragment))
                    transaction.commit()
                }
            } else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show()
            }

        } else {
            Log.d(TAG, "init: inflating Profile")
            val fragment = ProfileFragment()
            val transaction = this@ProfileActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(getString(R.string.profile_fragment))
            transaction.commit()
        }

    }

    companion object {

        private const val TAG = "ProfileActivity"


        private const val ACTIVITY_NUM = 4
        private const val NUM_GRID_COLUMNS = 3
    }

}
