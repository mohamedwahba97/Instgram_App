package com.example.moham.instagramapp.Utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.moham.instagramapp.Profile.AccountSettingsActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class ViewProfileFragment : Fragment() {
    private lateinit var mOnGridImageSelectedListener:OnGridImageSelectedListener
    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    //widgets
    private lateinit var mPosts:TextView
    private lateinit var mFollowers:TextView
    private lateinit var mFollowing:TextView
    private lateinit var mDisplayName:TextView
    private lateinit var mUsername:TextView
    private lateinit var mWebsite:TextView
    private lateinit var mDescription:TextView
    private lateinit var mFollow:TextView
    private lateinit var mUnfollow:TextView
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mProfilePhoto:CircleImageView
    private lateinit var gridView:GridView
    private lateinit var mBackArrow:ImageView
    private lateinit var bottomNavigationView:BottomNavigationViewEx
    private lateinit var mContext:Context
    private lateinit var editProfile:TextView
    //vars
    private lateinit var mUser:User
    private var mFollowersCount = 0
    private var mFollowingCount = 0
    private var mPostsCount = 0
    private val userFromBundle:User?
        get() {
            Log.d(TAG, "getUserFromBundle: arguments: $arguments")
            val bundle = this.arguments
            if (bundle != null)
            {
                return bundle.getParcelable(getString(R.string.intent_user))
            }
            else
            {
                return null
            }
        }


    interface OnGridImageSelectedListener {
        fun onGridImageSelected(photo: Photo, activityNumber: Int)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_view_profile, container, false)
        mDisplayName = view.findViewById<View>(R.id.display_name) as TextView
        mUsername = view.findViewById<View>(R.id.username) as TextView
        mWebsite = view.findViewById<View>(R.id.website) as TextView
        mDescription = view.findViewById<View>(R.id.description) as TextView
        mProfilePhoto = view.findViewById<View>(R.id.profile_photo) as CircleImageView
        mPosts = view.findViewById<View>(R.id.tvPosts) as TextView
        mFollowers = view.findViewById<View>(R.id.tvFollowers) as TextView
        mFollowing = view.findViewById<View>(R.id.tvFollowing) as TextView
        mProgressBar = view.findViewById<View>(R.id.profileProgressBar) as ProgressBar
        gridView = view.findViewById<View>(R.id.gridView) as GridView
        bottomNavigationView = view.findViewById<View>(R.id.bottomNavViewBar) as BottomNavigationViewEx
        mFollow = view.findViewById<View>(R.id.follow) as TextView
        mUnfollow = view.findViewById<View>(R.id.unfollow) as TextView
        editProfile = view.findViewById<View>(R.id.textEditProfile) as TextView
        mBackArrow = view.findViewById<View>(R.id.backArrow) as ImageView
        mContext = activity!!
        Log.d(TAG, "onCreateView: stared.")


        try {
            mUser = userFromBundle!!
            init()
        } catch (e: NullPointerException) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.message)
            Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show()
            activity!!.supportFragmentManager.popBackStack()
        }

        setupBottomNavigationView()
        setupFirebaseAuth()

        isFollowing()
        getFollowingCount()
        getFollowersCount()
        getPostsCount()



        mFollow.setOnClickListener {
            Log.d(TAG, "onClick: now following: " + mUser.username)

            FirebaseDatabase.getInstance().reference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(mUser.user_id)
                    .child(getString(R.string.field_user_id))
                    .setValue(mUser.user_id)

            FirebaseDatabase.getInstance().reference
                    .child(getString(R.string.dbname_followers))
                    .child(mUser.user_id)
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(getString(R.string.field_user_id))
                    .setValue(FirebaseAuth.getInstance().currentUser!!.uid)
            setFollowing()
        }


        mUnfollow.setOnClickListener {
            Log.d(TAG, "onClick: now unfollowing: " + mUser.username)

            FirebaseDatabase.getInstance().reference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(mUser.user_id)
                    .removeValue()

            FirebaseDatabase.getInstance().reference
                    .child(getString(R.string.dbname_followers))
                    .child(mUser.user_id)
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .removeValue()
            setUnfollowing()
        }

        //setupGridView();


        editProfile.setOnClickListener {
            Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment))
            val intent = Intent(activity, AccountSettingsActivity::class.java)
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity))
            startActivity(intent)
            activity!!.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        return view
    }


    private fun init() {

        //set the profile widgets
        val reference1 = FirebaseDatabase.getInstance().reference
        val query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.user_id)
        query1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue<UserAccountSettings>(UserAccountSettings::class.java)!!.toString())

                    val settings = UserSettings()
                    settings.user = mUser
                    settings.settings = singleSnapshot.getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                    setProfileWidgets(settings)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        //get the users profile photos

        val reference2 = FirebaseDatabase.getInstance().reference
        val query2 = reference2
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.user_id)
        query2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val photos = ArrayList<Photo>()
                for (singleSnapshot in dataSnapshot.children) {

                    val photo = Photo()
                    val objectMap = singleSnapshot.value as HashMap<*, *>

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
                }
                setupImageGrid(photos)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: query cancelled.")
            }
        })
    }

    private fun isFollowing() {
        Log.d(TAG, "isFollowing: checking if following this users.")
        setUnfollowing()

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.user_id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.value!!)

                    setFollowing()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getFollowersCount() {
        mFollowersCount = 0

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.user_id)
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
                .child(mUser.user_id)
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
                .child(mUser.user_id)
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

    private fun setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user")
        mFollow.visibility = View.GONE
        mUnfollow.visibility = View.VISIBLE
        editProfile.visibility = View.GONE
    }

    private fun setUnfollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user")
        mFollow.visibility = View.VISIBLE
        mUnfollow.visibility = View.GONE
        editProfile.visibility = View.GONE
    }

    private fun setCurrentUsersProfile() {
        Log.d(TAG, "setFollowing: updating UI for showing this user their own profile")
        mFollow.visibility = View.GONE
        mUnfollow.visibility = View.GONE
        editProfile.visibility = View.VISIBLE
    }

    private fun setupImageGrid(photos: ArrayList<Photo>) {
        //setup our image grid
        val gridWidth = resources.displayMetrics.widthPixels
        val imageWidth = gridWidth / NUM_GRID_COLUMNS
        gridView.columnWidth = imageWidth

        val imgUrls = ArrayList<String>()
        for (i in photos.indices) {
            imgUrls.add(photos[i].image_path)
        }
        val adapter = GridImageAdapter(this.activity!!, R.layout.layout_grid_imageview,
                "", imgUrls)
        gridView.adapter = adapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> mOnGridImageSelectedListener.onGridImageSelected(photos[position], ACTIVITY_NUM) }
    }

    override fun onAttach(context: Context?) {
        try {
            mOnGridImageSelectedListener = (activity as OnGridImageSelectedListener?)!!
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.message)
        }

        super.onAttach(context)
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
        mPosts.text = settings.posts.toString()
        mFollowing.text = settings.following.toString()
        mFollowers.text = settings.followers.toString()
        mProgressBar.visibility = View.GONE

        mBackArrow.setOnClickListener {
            Log.d(TAG, "onClick: navigating back")
            activity!!.supportFragmentManager.popBackStack()
            activity!!.finish()
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
        myRef = mFirebaseDatabase.reference

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

        private const val TAG = "ProfileFragment"

        private const val ACTIVITY_NUM = 4
        private const val NUM_GRID_COLUMNS = 3
    }

}
