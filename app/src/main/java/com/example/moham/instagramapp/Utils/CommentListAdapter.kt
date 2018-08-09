package com.example.moham.instagramapp.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.Comment
import com.example.moham.instagramapp.models.UserAccountSettings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CommentListAdapter(@NonNull context:Context, @LayoutRes resource:Int, @NonNull objects:List<Comment>):ArrayAdapter<Comment>(context, resource, objects) {
    private val mInflater:LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var layoutResource:Int = 0
    private val mContext:Context = context

    init{
        layoutResource = resource
    }
    private class ViewHolder {
        lateinit var comment:TextView
        lateinit var username:TextView
        lateinit var timestamp:TextView
        lateinit var reply:TextView
        lateinit var likes:TextView
        lateinit var profileImage:CircleImageView
        lateinit var like:ImageView
    }
    @SuppressLint("SetTextI18n")
    @NonNull
    override fun getView(position:Int, @Nullable convertView:View ?, @NonNull parent:ViewGroup):View? {
        var converTView = convertView

        val holder:ViewHolder
        if (converTView == null) {
            converTView = mInflater.inflate(layoutResource, parent, false)
            holder = ViewHolder()
            holder.comment = converTView.findViewById(R.id.comment) as TextView
            holder.username = converTView.findViewById(R.id.comment_username) as TextView
            holder.timestamp = converTView.findViewById(R.id.comment_time_posted) as TextView
            holder.reply = converTView.findViewById(R.id.comment_reply) as TextView
            holder.like = converTView.findViewById(R.id.comment_like) as ImageView
            holder.likes = converTView.findViewById(R.id.comment_likes) as TextView
            holder.profileImage = converTView.findViewById(R.id.comment_profile_image) as CircleImageView
            converTView.tag = holder
        }
        else
        {
            holder = converTView.tag as ViewHolder
        }
        //set the comment
        holder.comment.text = getItem(position).comment
        //set the timestamp difference
        val timestampDifference = getTimestampDifference(getItem(position))
        if (timestampDifference != "0")
        {
            holder.timestamp.text = "$timestampDifference d"
        }
        else
        {
            holder.timestamp.text = "today"
        }
        //set the username and profile image
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).user_id)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children)
                {
                    holder.username.text = singleSnapshot.getValue(UserAccountSettings::class.java)!!.username
                    val imageLoader = ImageLoader.getInstance()
                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings::class.java)!!.profile_photo,
                            holder.profileImage)
                }
            }
            override fun onCancelled(databaseError:DatabaseError) {
                Log.d(TAG, "onCancelled: query cancelled.")
            }
        })
        try
        {
            if (position == 0)
            {
                holder.like.visibility = View.GONE
                holder.likes.visibility = View.GONE
                holder.reply.visibility = View.GONE
            }
        }
        catch (e:NullPointerException) {
            Log.e(TAG, "getView: NullPointerException: " + e.message)
        }
        return converTView
    }
    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private fun getTimestampDifference(comment:Comment):String {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.")
        var difference: String
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
        sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific")//google 'android list of timezones'
        val today = c.time
        sdf.format(today)
        val timestamp:Date
        val photoTimestamp = comment.date_created
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
        private const val TAG = "CommentListAdapter"
    }
}
