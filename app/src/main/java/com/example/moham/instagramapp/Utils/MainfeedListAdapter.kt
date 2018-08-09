package com.example.moham.instagramapp.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.moham.instagramapp.Home.HomeActivity
import com.example.moham.instagramapp.Profile.ProfileActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.Like
import com.example.moham.instagramapp.models.Photo
import com.example.moham.instagramapp.models.User
import com.example.moham.instagramapp.models.UserAccountSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainfeedListAdapter(@NonNull context:Context, @LayoutRes resource:Int, @NonNull objects:List<Photo>):ArrayAdapter<Photo>(context, resource, objects) {
    lateinit var mOnLoadMoreItemsListener:OnLoadMoreItemsListener
    private val mInflater:LayoutInflater
    private var mLayoutResource:Int = 0
    private val mContext:Context
    private val mReference:DatabaseReference
    private var currentUsername = ""
    interface OnLoadMoreItemsListener {
        fun onLoadMoreItems()
    }
    init{
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mLayoutResource = resource
        this.mContext = context
        mReference = FirebaseDatabase.getInstance().reference
    }
    internal class ViewHolder {
        lateinit var mprofileImage:CircleImageView
        lateinit var likesString:String
        lateinit var username:TextView
        lateinit var timeDetla:TextView
        lateinit var caption:TextView
        lateinit var likes:TextView
        lateinit var comments:TextView
        lateinit var image:SquareImageView
        lateinit var heartRed:ImageView
        lateinit var heartWhite:ImageView
        lateinit var comment:ImageView
        var settings = UserAccountSettings()
        var user = User()
        lateinit var users:StringBuilder
        lateinit var mLikesString:String
        var likeByCurrentUser:Boolean = false
        lateinit var heart:Heart
        lateinit var detector:GestureDetector
        lateinit var photo:Photo
    }
    @SuppressLint("SetTextI18n")
    @NonNull
    override fun getView(position:Int, @Nullable convertView:View?, @NonNull parent:ViewGroup):View? {
        var converTView = convertView
        val holder:ViewHolder
        if (converTView == null) {
            converTView = mInflater.inflate(mLayoutResource, parent, false)
            holder = ViewHolder()
            holder.username = converTView.findViewById(R.id.username) as TextView
            holder.image = converTView.findViewById(R.id.post_image) as SquareImageView
            holder.heartRed = converTView.findViewById(R.id.image_heart_red) as ImageView
            holder.heartWhite = converTView.findViewById(R.id.image_heart) as ImageView
            holder.comment = converTView.findViewById(R.id.speech_bubble) as ImageView
            holder.likes = converTView.findViewById(R.id.image_likes) as TextView
            holder.comments = converTView.findViewById(R.id.image_comments_link) as TextView
            holder.caption = converTView.findViewById(R.id.image_caption) as TextView
            holder.timeDetla = converTView.findViewById(R.id.image_time_posted) as TextView
            holder.mprofileImage = converTView.findViewById(R.id.profile_photo) as CircleImageView
            holder.heart = Heart(holder.heartWhite, holder.heartRed)
            holder.photo = getItem(position)
            holder.detector = GestureDetector(mContext, GestureListener(holder))
            holder.users = StringBuilder()
            converTView.tag = holder
        }
        else
        {
            holder = converTView.tag as ViewHolder
        }
        //get the current users username (need for checking likes string)
        getCurrentUsername()
        //get likes string
        getLikesString(holder)
        //set the caption
        holder.caption.text = getItem(position).caption
        //set the comment
        val comments = getItem(position).comments
        holder.comments.setText("View all " + comments.size + " comments")
        holder.comments.setOnClickListener(object:View.OnClickListener {
            override fun onClick(v:View) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).photo_id)
                (mContext as HomeActivity).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.home_activity))
                //going to need to do something else?
                mContext.hideLayout()
            }
        })
        //set the time it was posted
        val timestampDifference = getTimestampDifference(getItem(position))
        if (timestampDifference != "0")
        {
            holder.timeDetla.text = "$timestampDifference DAYS AGO"
        }
        else
        {
            holder.timeDetla.text = "TODAY"
        }
        //set the profile image
        val imageLoader = ImageLoader.getInstance()
        imageLoader.displayImage(getItem(position).image_path, holder.image)
        //get the profile image and username
        val reference = FirebaseDatabase.getInstance().getReference()
        val query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).user_id)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                for (singleSnapshot in dataSnapshot.getChildren())
                {
                    // currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, ("onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings::class.java)!!.username))
                    holder.username.text = singleSnapshot.getValue(UserAccountSettings::class.java)!!.username
                    holder.username.setOnClickListener(object:View.OnClickListener {
                        override fun onClick(v:View) {
                            Log.d(TAG, ("onClick: navigating to profile of: " + holder.user.username))
                            val intent = Intent(mContext, ProfileActivity::class.java)
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity))
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user)
                            mContext.startActivity(intent)
                        }
                    })
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings::class.java)!!.profile_photo,
                            holder.mprofileImage)
                    holder.mprofileImage.setOnClickListener(object:View.OnClickListener {
                        override fun onClick(v:View) {
                            Log.d(TAG, ("onClick: navigating to profile of: " + holder.user.username))
                            val intent = Intent(mContext, ProfileActivity::class.java)
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity))
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user)
                            mContext.startActivity(intent)
                        }
                    })
                    holder.settings = singleSnapshot.getValue(UserAccountSettings::class.java)!!
                    holder.comment.setOnClickListener(object:View.OnClickListener {
                        override fun onClick(v:View) {
                            (mContext as HomeActivity).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity))
                            //another thing?
                            mContext.hideLayout()
                        }
                    })
                }
            }
            override fun onCancelled(databaseError:DatabaseError) {
            }
        })
        //get the user object
        val userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).user_id)
        userQuery.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children)
                {
                    Log.d(TAG, ("onDataChange: found user: " + singleSnapshot.getValue(User::class.java)!!.username))
                    holder.user = singleSnapshot.getValue(User::class.java)!!
                }
            }
            override fun onCancelled(databaseError:DatabaseError) {
            }
        })
        if (reachedEndOfList(position))
        {
            loadMoreData()
        }
        return convertView
    }
    private fun reachedEndOfList(position:Int):Boolean {
        return position == getCount() - 1
    }
    private fun loadMoreData() {
        try
        {
            mOnLoadMoreItemsListener = getContext() as OnLoadMoreItemsListener
        }
        catch (e:ClassCastException) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + e.message)
        }
        try
        {
            mOnLoadMoreItemsListener.onLoadMoreItems()
        }
        catch (e:NullPointerException) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + e.message)
        }
    }
    inner class GestureListener internal constructor(holder:ViewHolder):GestureDetector.SimpleOnGestureListener() {
        internal var mHolder:ViewHolder
        init{
            mHolder = holder
        }
        override fun onDown(e:MotionEvent):Boolean {
            return true
        }
        override fun onDoubleTap(e:MotionEvent):Boolean {
            Log.d(TAG, "onDoubleTap: double tap detected.")
            val reference = FirebaseDatabase.getInstance().getReference()
            val query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.photo_id)
                    .child(mContext.getString(R.string.field_likes))
            query.addListenerForSingleValueEvent(object: ValueEventListener {
               override fun onDataChange(dataSnapshot:DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.getChildren())
                    {
                        val keyID = singleSnapshot.key
                        //case1: Then user already liked the photo
                        if ((mHolder.likeByCurrentUser && singleSnapshot.getValue(Like::class.java)!!.user_id
                                        .equals(FirebaseAuth.getInstance().getCurrentUser()!!.getUid())))
                        {
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.photo_id)
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID.toString())
                                    .removeValue()
                            ///
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().currentUser!!.getUid())
                                    .child(mHolder.photo.photo_id)
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID.toString())
                                    .removeValue()
                            mHolder.heart.toggleLike()
                            getLikesString(mHolder)
                        }
                        else if (!mHolder.likeByCurrentUser)
                        {
                            //add new like
                            addNewLike(mHolder)
                            break
                        }//case2: The user has not liked the photo
                    }
                    if (!dataSnapshot.exists())
                    {
                        //add new like
                        addNewLike(mHolder)
                    }
                }
              override  fun onCancelled(databaseError:DatabaseError) {
                }
            })
            return true
        }
    }
    private fun addNewLike(holder:ViewHolder) {
        Log.d(TAG, "addNewLike: adding new like")
        val newLikeID = mReference.push().getKey()
        val like = Like()
        like.user_id = (FirebaseAuth.getInstance().getCurrentUser()!!.getUid())
        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.photo_id)
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID.toString())
                .setValue(like)
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.user_id)
                .child(holder.photo.photo_id)
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID.toString())
                .setValue(like)
        holder.heart.toggleLike()
        getLikesString(holder)
    }
    private fun getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings")
        val reference = FirebaseDatabase.getInstance().getReference()
        val query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser()!!.getUid())
        query.addListenerForSingleValueEvent(object:ValueEventListener {
           override fun onDataChange(dataSnapshot:DataSnapshot) {
                for (singleSnapshot in dataSnapshot.getChildren())
                {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings::class.java)!!.username
                }
            }
           override fun onCancelled(databaseError:DatabaseError) {
            }
        })
    }
    private fun getLikesString(holder:ViewHolder) {
        Log.d(TAG, "getLikesString: getting likes string")
        try
        {
            val reference = FirebaseDatabase.getInstance().getReference()
            val query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.photo_id)
                    .child(mContext.getString(R.string.field_likes))
            query.addListenerForSingleValueEvent(object:ValueEventListener {
               override fun onDataChange(dataSnapshot:DataSnapshot) {
                    holder.users = StringBuilder()
                    for (singleSnapshot in dataSnapshot.getChildren())
                    {
                        val reference = FirebaseDatabase.getInstance().getReference()
                        val query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like::class.java)!!.user_id)
                        query.addListenerForSingleValueEvent(object:ValueEventListener {
                          override  fun onDataChange(dataSnapshot:DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.getChildren())
                                {
                                    Log.d(TAG, ("onDataChange: found like: " + singleSnapshot.getValue(User::class.java)!!.username))
                                    holder.users.append(singleSnapshot.getValue(User::class.java)!!.username)
                                    holder.users.append(",")
                                }
                                val splitUsers = holder.users.toString().split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                if (holder.users.toString().contains(currentUsername + ","))
                                {//mitch, mitchell.tabian
                                    holder.likeByCurrentUser = true
                                }
                                else
                                {
                                    holder.likeByCurrentUser = false
                                }
                                val length = splitUsers.size
                                if (length == 1)
                                {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                }
                                else if (length == 2)
                                {
                                    holder.likesString = ("Liked by " + splitUsers[0]
                                            + " and " + splitUsers[1])
                                }
                                else if (length == 3)
                                {
                                    holder.likesString = ("Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2])
                                }
                                else if (length == 4)
                                {
                                    holder.likesString = ("Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3])
                                }
                                else if (length > 4)
                                {
                                    holder.likesString = ("Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.size - 3) + " others")
                                }
                                Log.d(TAG, "onDataChange: likes string: " + holder.likesString)
                                //setup likes string
                                setupLikesString(holder, holder.likesString)
                            }
                           override fun onCancelled(databaseError:DatabaseError) {
                            }
                        })
                    }
                    if (!dataSnapshot.exists())
                    {
                        holder.likesString = ""
                        holder.likeByCurrentUser = false
                        //setup likes string
                        setupLikesString(holder, holder.likesString)
                    }
                }
               override fun onCancelled(databaseError:DatabaseError) {
                }
            })
        }
        catch (e:NullPointerException) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.message)
            holder.likesString = ""
            holder.likeByCurrentUser = false
            //setup likes string
            setupLikesString(holder, holder.likesString)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupLikesString(holder:ViewHolder, likesString:String) {
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString)
        if (holder.likeByCurrentUser)
        {
            Log.d(TAG, "setupLikesString: photo is liked by current user")
            holder.heartWhite.setVisibility(View.GONE)
            holder.heartRed.setVisibility(View.VISIBLE)
            holder.heartRed.setOnTouchListener(object:View.OnTouchListener {
               override fun onTouch(v:View, event:MotionEvent):Boolean {
                    return holder.detector.onTouchEvent(event)
                }
            })
        }
        else
        {
            Log.d(TAG, "setupLikesString: photo is not liked by current user")
            holder.heartWhite.setVisibility(View.VISIBLE)
            holder.heartRed.setVisibility(View.GONE)
            holder.heartWhite.setOnTouchListener(object:View.OnTouchListener {
              override  fun onTouch(v:View, event:MotionEvent):Boolean {
                    return holder.detector.onTouchEvent(event)
                }
            })
        }
        holder.likes.setText(likesString)
    }
    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private fun getTimestampDifference(photo:Photo):String {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.")
        var difference = ""
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"))//google 'android list of timezones'
        val today = c.getTime()
        sdf.format(today)
        val timestamp:Date
        val photoTimestamp = photo.date_created
        try
        {
            timestamp = sdf.parse(photoTimestamp)
            difference = Math.round(((today.time - timestamp.time) / 1000 / 60 / 60 / 24).toFloat()).toString()
        }
        catch (e:ParseException) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.message)
            difference = "0"
        }
        return difference
    }
    companion object {
        private val TAG = "MainfeedListAdapter"
    }
}