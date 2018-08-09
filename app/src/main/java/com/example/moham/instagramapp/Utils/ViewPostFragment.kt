package com.example.moham.instagramapp.Utils


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ViewPostFragment : Fragment() {
    private lateinit var mOnCommentThreadSelectedListener:OnCommentThreadSelectedListener
    //firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    private lateinit var mFirebaseMethods:FirebaseMethods
    //widgets
    private lateinit var mPostImage:SquareImageView
    private lateinit var bottomNavigationView:BottomNavigationViewEx
    private lateinit var mBackLabel:TextView
    private lateinit var mCaption:TextView
    private lateinit var mUsername:TextView
    private lateinit var mTimestamp:TextView
    private lateinit var mLikes:TextView
    private lateinit var mComments:TextView
    private lateinit var mBackArrow:ImageView
    private lateinit var mEllipses:ImageView
    private lateinit var mHeartRed:ImageView
    private lateinit var mHeartWhite:ImageView
    private lateinit var mProfileImage:ImageView
    private lateinit var mComment:ImageView
    //vars
    private lateinit var mPhoto:Photo
    private var mActivityNumber = 0
    private val photoUsername = ""
    private val profilePhotoUrl = ""
    private lateinit var mUserAccountSettings:UserAccountSettings
    private lateinit var mGestureDetector:GestureDetector
    private lateinit var mHeart:Heart
    private var mLikedByCurrentUser : Boolean = false
    private lateinit var mUsers:StringBuilder
    private var mLikesString = ""
    private lateinit var mCurrentUser:User

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private//google 'android list of timezones'
    val timestampDifference: String
        get() {
            Log.d(TAG, "getTimestampDifference: getting timestamp difference.")

            var difference: String
            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
            sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific")
            val today = c.time
            sdf.format(today)
            val timestamp: Date
            val photoTimestamp = mPhoto.date_created
            try {
                timestamp = sdf.parse(photoTimestamp)
                difference = Math.round(((today.time - timestamp.time) / 1000 / 60 / 60 / 24).toFloat()).toString()
            } catch (e: ParseException) {
                Log.e(TAG, "getTimestampDifference: ParseException: " + e.message)
                difference = "0"
            }

            return difference
        }

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     * @return
     */
    private val activityNumFromBundle: Int
        get() {
            Log.d(TAG, "getActivityNumFromBundle: arguments: " + arguments!!)

            val bundle = this.arguments
            return bundle?.getInt(getString(R.string.activity_number)) ?: 0
        }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private val photoFromBundle: Photo?
        get() {
            Log.d(TAG, "getPhotoFromBundle: arguments: " + arguments!!)

            val bundle = this.arguments
            return bundle?.getParcelable(getString(R.string.photo))
        }


    interface OnCommentThreadSelectedListener {
        fun onCommentThreadSelectedListener(photo: Photo?)
    }

    init {
        arguments = Bundle()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_view_post, container, false)
        mPostImage = view.findViewById<View>(R.id.post_image) as SquareImageView
        bottomNavigationView = view.findViewById<View>(R.id.bottomNavViewBar) as BottomNavigationViewEx
        mBackArrow = view.findViewById<View>(R.id.backArrow) as ImageView
        mBackLabel = view.findViewById<View>(R.id.tvBackLabel) as TextView
        mCaption = view.findViewById<View>(R.id.image_caption) as TextView
        mUsername = view.findViewById<View>(R.id.username) as TextView
        mTimestamp = view.findViewById<View>(R.id.image_time_posted) as TextView
        mEllipses = view.findViewById<View>(R.id.ivEllipses) as ImageView
        mHeartRed = view.findViewById<View>(R.id.image_heart_red) as ImageView
        mHeartWhite = view.findViewById<View>(R.id.image_heart) as ImageView
        mProfileImage = view.findViewById<View>(R.id.profile_photo) as ImageView
        mLikes = view.findViewById<View>(R.id.image_likes) as TextView
        mComment = view.findViewById<View>(R.id.speech_bubble) as ImageView
        mComments = view.findViewById<View>(R.id.image_comments_link) as TextView

        mHeart = Heart(mHeartWhite, mHeartRed)
        mGestureDetector = GestureDetector(activity, GestureListener())

        setupFirebaseAuth()
        setupBottomNavigationView()


        return view
    }

    private fun init() {
        try {
            //mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(photoFromBundle!!.image_path, this.mPostImage, null, "")
            mActivityNumber = activityNumFromBundle
            val photo_id = photoFromBundle!!.photo_id

            val query = FirebaseDatabase.getInstance().reference
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val newPhoto = Photo()
                        val objectMap = singleSnapshot.value as HashMap<*, *>

                        newPhoto.caption = objectMap[getString(R.string.field_caption)].toString()
                        newPhoto.tags = objectMap[getString(R.string.field_tags)].toString()
                        newPhoto.photo_id = objectMap[getString(R.string.field_photo_id)].toString()
                        newPhoto.user_id = objectMap[getString(R.string.field_user_id)].toString()
                        newPhoto.date_created = objectMap[getString(R.string.field_date_created)].toString()
                        newPhoto.image_path = objectMap[getString(R.string.field_image_path)].toString()

                        val commentsList = ArrayList<Comment>()
                        for (dSnapshot in singleSnapshot
                                .child(getString(R.string.field_comments)).children) {
                            val comment = Comment()
                            comment.user_id = dSnapshot.getValue<Comment>(Comment::class.java)!!.user_id
                            comment.comment = dSnapshot.getValue<Comment>(Comment::class.java)!!.comment
                            comment.date_created = dSnapshot.getValue<Comment>(Comment::class.java)!!.date_created
                            commentsList.add(comment)
                        }
                        newPhoto.comments = commentsList

                        mPhoto = newPhoto

                        getCurrentUser()
                        getPhotoDetails()
                        //getLikesString();

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.")
                }
            })

        } catch (e: NullPointerException) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.message)
        }

    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            init()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mOnCommentThreadSelectedListener = (activity as OnCommentThreadSelectedListener?)!!
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.message)
        }

    }

    private fun getLikesString() {
        Log.d(TAG, "getLikesString: getting likes string")

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.photo_id)
                .child(getString(R.string.field_likes))
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers = StringBuilder()
                for (singleSnapshot in dataSnapshot.children) {

                    val reFerence = FirebaseDatabase.getInstance().reference
                    val quEry = reFerence
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue<Like>(Like::class.java)!!.user_id)
                    quEry.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (SingleSnapshot in dataSnapshot.children) {
                                Log.d(TAG, "onDataChange: found like: " + SingleSnapshot.getValue<User>(User::class.java)!!.username)

                                mUsers.append(SingleSnapshot.getValue<User>(User::class.java)!!.username)
                                mUsers.append(",")
                            }

                            val splitUsers = mUsers.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                            mLikedByCurrentUser = mUsers.toString().contains(mCurrentUser.username + ",")

                            val length = splitUsers.size
                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0]
                            } else if (length == 2) {
                                mLikesString = ("Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1])
                            } else if (length == 3) {
                                mLikesString = ("Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2])

                            } else if (length == 4) {
                                mLikesString = ("Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3])
                            } else if (length > 4) {
                                mLikesString = ("Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.size - 3) + " others")
                            }
                            Log.d(TAG, "onDataChange: likes string: $mLikesString")
                            setupWidgets()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
                if (!dataSnapshot.exists()) {
                    mLikesString = ""
                    mLikedByCurrentUser = false
                    setupWidgets()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    private fun getCurrentUser() {
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    mCurrentUser = singleSnapshot.getValue<User>(User::class.java)!!
                }
                getLikesString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: query cancelled.")
            }
        })
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d(TAG, "onDoubleTap: double tap detected.")

            val reference = FirebaseDatabase.getInstance().reference
            val query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.photo_id)
                    .child(getString(R.string.field_likes))
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {

                        val keyID = singleSnapshot.key

                        //case1: Then user already liked the photo
                        if (mLikedByCurrentUser && singleSnapshot.getValue<Like>(Like::class.java)!!.user_id == FirebaseAuth.getInstance().currentUser!!.uid) {

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.photo_id)
                                    .child(getString(R.string.field_likes))
                                    .child(keyID!!)
                                    .removeValue()
                            ///
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .child(mPhoto.photo_id)
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue()

                            mHeart.toggleLike()
                            getLikesString()
                        } else if ((!mLikedByCurrentUser)) {
                            //add new like
                            addNewLike()
                            break
                        }//case2: The user has not liked the photo
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            return true
        }
    }

    private fun addNewLike() {
        Log.d(TAG, "addNewLike: adding new like")

        val newLikeID = myRef.push().key
        val like = Like()
        like.user_id = FirebaseAuth.getInstance().currentUser!!.uid

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.photo_id)
                .child(getString(R.string.field_likes))
                .child(newLikeID!!)
                .setValue(like)

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.user_id)
                .child(mPhoto.photo_id)
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like)

        mHeart.toggleLike()
        getLikesString()
    }

    private fun getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details.")
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.user_id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    mUserAccountSettings = singleSnapshot.getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                }
                //setupWidgets();
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: query cancelled.")
            }
        })
    }


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun setupWidgets() {
        val timestampDiff = timestampDifference
        if (timestampDiff != "0") {
            mTimestamp.text = "$timestampDiff DAYS AGO"
        } else {
            mTimestamp.text = "TODAY"
        }
        UniversalImageLoader.setImage(mUserAccountSettings.profile_photo, this.mProfileImage, null, "")
        mUsername.text = mUserAccountSettings.username
        mLikes.text = mLikesString
        mCaption.text = mPhoto.caption

        if (mPhoto.comments.isNotEmpty()) {
            mComments.text = "View all " + mPhoto.comments.size + " comments"
        } else {
            mComments.text = ""
        }

        mComments.setOnClickListener {
            Log.d(TAG, "onClick: navigating to comments thread")

            mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto)
        }

        mBackArrow.setOnClickListener {
            Log.d(TAG, "onClick: navigating back")
            activity!!.supportFragmentManager.popBackStack()
        }

        mComment.setOnClickListener {
            Log.d(TAG, "onClick: navigating back")
            mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto)
        }

        if (mLikedByCurrentUser) {
            mHeartWhite.visibility = View.GONE
            mHeartRed.visibility = View.VISIBLE
            mHeartRed.setOnTouchListener { _, event ->
                Log.d(TAG, "onTouch: red heart touch detected.")
                mGestureDetector.onTouchEvent(event)
            }
        } else {
            mHeartWhite.visibility = View.VISIBLE
            mHeartRed.visibility = View.GONE
            mHeartWhite.setOnTouchListener { _, event ->
                Log.d(TAG, "onTouch: white heart touch detected.")
                mGestureDetector.onTouchEvent(event)
            }
        }


    }

    /**
     * BottomNavigationView setup
     */
    private fun setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView")
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView)
        BottomNavigationViewHelper.enableNavigation(this.activity!!, this.activity!!, bottomNavigationView)
        val menu = bottomNavigationView.menu
        val menuItem = menu.getItem(mActivityNumber)
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

        private const val TAG = "ViewPostFragment"
    }

}
