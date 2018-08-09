package com.example.moham.instagramapp.Profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.BottomNavigationViewHelper
import com.example.moham.instagramapp.Utils.FirebaseMethods
import com.example.moham.instagramapp.Utils.GridImageAdapter
import com.example.moham.instagramapp.Utils.UniversalImageLoader
import com.example.moham.instagramapp.models.Comment
import com.example.moham.instagramapp.models.Like
import com.example.moham.instagramapp.models.Photo
import com.example.moham.instagramapp.models.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ProfileFragment : Fragment() {
    internal lateinit var mOnGridImageSelectedListener:OnGridImageSelectedListener
    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    private  var mFirebaseMethods:FirebaseMethods? = null
    //widgets
    private lateinit var mPosts:TextView
    private lateinit var mFollowers:TextView
    private lateinit var mFollowing:TextView
    private lateinit var mDisplayName:TextView
    private lateinit var mUsername:TextView
    private lateinit var mWebsite:TextView
    private lateinit var mDescription:TextView
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mProfilePhoto:CircleImageView
    private lateinit var gridView:GridView
    private lateinit var toolbar:Toolbar
    private lateinit var profileMenu:ImageView
    private lateinit var bottomNavigationView:BottomNavigationViewEx
    private lateinit var mContext:Context
    //vars
    private var mFollowersCount = 0
    private var mFollowingCount = 0
    private var mPostsCount = 0

    interface OnGridImageSelectedListener {
        fun onGridImageSelected(photo: Photo, activityNumber: Int)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        mDisplayName = view.findViewById(R.id.display_name) as TextView
        mUsername = view.findViewById(R.id.username) as TextView
        mWebsite = view.findViewById(R.id.website) as TextView
        mDescription = view.findViewById(R.id.description) as TextView
        mProfilePhoto = view.findViewById(R.id.profile_photo) as CircleImageView
        mPosts = view.findViewById(R.id.tvPosts) as TextView
        mFollowers = view.findViewById(R.id.tvFollowers) as TextView
        mFollowing = view.findViewById(R.id.tvFollowing) as TextView
        mProgressBar = view.findViewById(R.id.profileProgressBar) as ProgressBar
        gridView = view.findViewById(R.id.gridView)
        toolbar = view.findViewById(R.id.profileToolBar) as Toolbar
        profileMenu = view.findViewById(R.id.profileMenu) as ImageView
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar) as BottomNavigationViewEx
        mContext = activity!!
        //mFirebaseMethods = FirebaseMethods(activity!!)
        Log.d(TAG, "onCreateView: stared.")
        setupBottomNavigationView()
        setupToolbar()
        setupFirebaseAuth()
        setupGridView()
        getFollowersCount()
        getFollowingCount()
        getPostsCount()

        val editProfile = view.findViewById<View>(R.id.textEditProfile) as TextView
        editProfile.setOnClickListener {
            Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment))
            val intent = Intent(activity, AccountSettingsActivity::class.java)
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity))
            startActivity(intent)
            activity!!.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        return view
    }

    override fun onAttach(context: Context?) {
        try {
            mOnGridImageSelectedListener = (activity as OnGridImageSelectedListener?)!!
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.message)
        }

        super.onAttach(context)
    }

    private fun setupGridView() {
        Log.d(TAG, "setupGridView: Setting up image grid.")

        val photos = ArrayList<Photo>()
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {

                    val photo = Photo()
                    val objectMap = singleSnapshot.value as HashMap<*, *>

                    try {
                        photo.caption = objectMap[getString(R.string.field_caption)].toString()
                        photo.tags = objectMap[getString(R.string.field_tags)].toString()
                        photo.photo_id = objectMap[getString(R.string.field_photo_id)].toString()
                        photo.user_id = objectMap[getString(R.string.field_user_id)].toString()
                        photo.date_created = objectMap[getString(R.string.field_date_created)].toString()
                        photo.image_path = objectMap[getString(R.string.field_image_path)].toString()

                        val comments = ArrayList<Comment>()
                        for (dSnapshot in singleSnapshot
                                .child(getString(R.string.field_comments)).children) {
                            val comment = Comment()
                            comment.user_id = dSnapshot.getValue<Comment>(Comment::class.java)!!.user_id
                            comment.comment = dSnapshot.getValue<Comment>(Comment::class.java)!!.comment
                            comment.date_created = dSnapshot.getValue<Comment>(Comment::class.java)!!.date_created
                            comments.add(comment)
                        }

                        photo.comments = comments

                        val likesList = ArrayList<Like>()
                        for (dSnapshot in singleSnapshot
                                .child(getString(R.string.field_likes)).children) {
                            val like = Like()
                            like.user_id = dSnapshot.getValue<Like>(Like::class.java)!!.user_id
                            likesList.add(like)
                        }
                        photo.likes = likesList
                        photos.add(photo)
                    } catch (e: NullPointerException) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.message)
                    }

                }

                //setup our image grid
                val gridWidth = resources.displayMetrics.widthPixels
                val imageWidth = gridWidth / NUM_GRID_COLUMNS
                gridView.columnWidth = imageWidth

                val imgUrls = ArrayList<String>()
                for (i in photos.indices) {
                    imgUrls.add(photos[i].image_path)
                }
                val adapter = GridImageAdapter(activity!!, R.layout.layout_grid_imageview,
                        "", imgUrls)
                gridView.adapter = adapter

                gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> mOnGridImageSelectedListener.onGridImageSelected(photos[position], ACTIVITY_NUM) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: query cancelled.")
            }
        })
    }

    private fun getFollowersCount() {
        mFollowersCount = 0

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found follower:" + singleSnapshot.value!!)
                    mFollowersCount++
                }
                mFollowers.text = mFollowersCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getFollowingCount() {
        mFollowingCount = 0

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found following user:" + singleSnapshot.value!!)
                    mFollowingCount++
                }
                mFollowing.text = mFollowingCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getPostsCount() {
        mPostsCount = 0

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.value!!)
                    mPostsCount++
                }
                mPosts.text = mPostsCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun setProfileWidgets(userSettings: UserSettings) {
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getSettings().getUsername());


        //User user = userSettings.getUser();
        val settings = userSettings.settings

        UniversalImageLoader.setImage(settings.profile_photo, this.mProfilePhoto, null, "")

        mDisplayName.text = settings.display_name
        mUsername.text = settings.username
        mWebsite.text = settings.website
        mDescription.text = settings.description
        mProgressBar.visibility = View.GONE
    }


    /**
     * Responsible for setting up the profile toolbar
     */
    private fun setupToolbar() {

        (activity as ProfileActivity).setSupportActionBar(toolbar)

        profileMenu.setOnClickListener {
            Log.d(TAG, "onClick: navigating to account settings.")
            val intent = Intent(mContext, AccountSettingsActivity::class.java)
            startActivity(intent)
            activity!!.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    /**
     * BottomNavigationView setup
     */
    private fun setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView")
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView)
        BottomNavigationViewHelper.enableNavigation(mContext, activity!!, bottomNavigationView)
        val menu = bottomNavigationView.menu
        val menuItem = menu.getItem(ACTIVITY_NUM)
        menuItem.isChecked = true
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
        myRef = mFirebaseDatabase.getReference()
        mAuthListener = object:FirebaseAuth.AuthStateListener {
          override  fun onAuthStateChanged(@NonNull firebaseAuth:FirebaseAuth) {
                val user = firebaseAuth.getCurrentUser()
                if (user != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid())
                }
                else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out")
                }
                // ...
            }
        }
        myRef.addValueEventListener(object:ValueEventListener {
          override  fun onDataChange(dataSnapshot:DataSnapshot) {
                //retrieve user information from the database
                //setProfileWidgets(mFirebaseMethods!!.getUserSettings(dataSnapshot))
                //retrieve images for the user in question
            }
         override   fun onCancelled(databaseError:DatabaseError) {
            }
        })
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

        private const val TAG = "ProfileFragment"

        private const val ACTIVITY_NUM = 4
        private const val NUM_GRID_COLUMNS = 3
    }

}
