package com.example.moham.instagramapp.Utils

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import com.example.moham.instagramapp.Home.HomeActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.Comment
import com.example.moham.instagramapp.models.Photo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class ViewCommentsFragment : Fragment() {

    ///firebase
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    //widgets
    private lateinit var mBackArrow:ImageView
    private lateinit var mCheckMark:ImageView
    private lateinit var mComment:EditText
    private lateinit var mListView:ListView
    //vars
    private  lateinit var mPhoto:Photo
    private lateinit var mComments:ArrayList<Comment>
    private lateinit var mContext:Context

    private val timestamp: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
            sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific")
            return sdf.format(Date())
        }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private val callingActivityFromBundle: String?
        get() {
            Log.d(TAG, "getPhotoFromBundle: arguments: $arguments")
            val bundle = this.arguments
            if (bundle != null)
            {
                return bundle.getString(getString(R.string.home_activity))
            }
            else
            {
                return null
            }
        }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private val photoFromBundle: Photo?
        get() {
            Log.d(TAG, "getPhotoFromBundle: arguments: $arguments")
            val bundle = this.arguments
            if (bundle != null)
            {
                return bundle.getParcelable(getString(R.string.photo))
            }
            else
            {
                return null
            }
        }
    init{
        arguments = Bundle()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_view_comments, container, false)
        mBackArrow = view.findViewById(R.id.backArrow)
        mCheckMark = view.findViewById(R.id.ivPostComment)
        mComment = view.findViewById(R.id.comment)
        mListView = view.findViewById(R.id.listView)
        mComments = ArrayList()
        mContext = activity!!


        try {
            mPhoto = photoFromBundle!!
            setupFirebaseAuth()

        } catch (e: NullPointerException) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.message)
        }

        return view
    }

    private fun setupWidgets() {

        val adapter = CommentListAdapter(mContext, R.layout.layout_comment, mComments)
        mListView.adapter = adapter

        mCheckMark.setOnClickListener {
            if (mComment.text.toString() != "") {
                Log.d(TAG, "onClick: attempting to submit new comment.")
                addNewComment(mComment.text.toString())

                mComment.setText("")
                closeKeyboard()
            } else {
                Toast.makeText(activity, "you can't post a blank comment", Toast.LENGTH_SHORT).show()
            }
        }

        mBackArrow.setOnClickListener {
            Log.d(TAG, "onClick: navigating back")
            if (callingActivityFromBundle == getString(R.string.home_activity)) {
                activity!!.supportFragmentManager.popBackStack()
                (activity as HomeActivity).showLayout()
            } else {
                activity!!.supportFragmentManager.popBackStack()
            }
        }
    }

    private fun closeKeyboard() {
        val view = activity!!.currentFocus
        if (view != null) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun addNewComment(newComment: String) {
        Log.d(TAG, "addNewComment: adding new comment: $newComment")

        val commentID = myRef.push().key

        val comment = Comment()
        comment.comment = newComment
        comment.date_created = timestamp
        comment.user_id = FirebaseAuth.getInstance().currentUser!!.uid

        //insert into photos node
        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.photo_id)
                .child(getString(R.string.field_comments))
                .child(commentID!!)
                .setValue(comment)

        //insert into user_photos node
        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.user_id) //should be mphoto.getUser_id()
                .child(mPhoto.photo_id)
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment)

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

        if (mPhoto.comments.isEmpty()) {
            mComments.clear()
            val firstComment = Comment()
            firstComment.comment = mPhoto.caption
            firstComment.user_id = mPhoto.user_id
            firstComment.date_created = mPhoto.date_created
            mComments.add(firstComment)
            mPhoto.comments = mComments
            setupWidgets()
        }


        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.photo_id)
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        Log.d(TAG, "onChildAdded: child added.")

                        val query = myRef
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.photo_id)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {

                                    val photo = Photo()
                                    val objectMap = singleSnapshot.value as HashMap<*, *>

                                    photo.caption = objectMap[mContext.getString(R.string.field_caption)].toString()
                                    photo.tags = objectMap[mContext.getString(R.string.field_tags)].toString()
                                    photo.photo_id = objectMap[mContext.getString(R.string.field_photo_id)].toString()
                                    photo.user_id = objectMap[mContext.getString(R.string.field_user_id)].toString()
                                    photo.date_created = objectMap[mContext.getString(R.string.field_date_created)].toString()
                                    photo.image_path = objectMap[mContext.getString(R.string.field_image_path)].toString()

                                    mComments.clear()
                                    val firstComment = Comment()
                                    firstComment.comment = mPhoto.caption
                                    firstComment.user_id = mPhoto.user_id
                                    firstComment.date_created = mPhoto.date_created
                                    mComments.add(firstComment)

                                    for (dSnapshot in singleSnapshot
                                            .child(mContext.getString(R.string.field_comments)).children) {
                                        val comment = Comment()
                                        comment.user_id = dSnapshot.getValue<Comment>(Comment::class.java)!!.user_id
                                        comment.comment = dSnapshot.getValue<Comment>(Comment::class.java)!!.comment
                                        comment.date_created = dSnapshot.getValue<Comment>(Comment::class.java)!!.date_created
                                        mComments.add(comment)
                                    }

                                    photo.comments = mComments

                                    mPhoto = photo

                                    setupWidgets()
                                    //                    List<Like> likesList = new ArrayList<Like>();
                                    //                    for (DataSnapshot dSnapshot : singleSnapshot
                                    //                            .child(getString(R.string.field_likes)).getChildren()){
                                    //                        Like like = new Like();
                                    //                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                    //                        likesList.add(like);
                                    //                    }

                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.d(TAG, "onCancelled: query cancelled.")
                            }
                        })
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

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

        private const val TAG = "ViewCommentsFragment"
    }

}
