package com.example.moham.instagramapp.Utils

import android.content.Context
import android.support.annotation.LayoutRes
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.User
import com.example.moham.instagramapp.models.UserAccountSettings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView

class UserListAdapter(private val mContext: Context, @param:LayoutRes private val layoutResource: Int, objects: List<User>) : ArrayAdapter<User>(mContext, layoutResource, objects) {


    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mUsers: List<User>

    init {
        this.mUsers = objects
    }

    private class ViewHolder {
        lateinit var username: TextView
        internal lateinit var email: TextView
        internal lateinit var profileImage: CircleImageView
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var converTView = convertView


        val holder: ViewHolder

        if (converTView == null) {
            converTView = mInflater.inflate(layoutResource, parent, false)
            holder = ViewHolder()

            holder.username = converTView.findViewById<View>(R.id.username) as TextView
            holder.email = converTView.findViewById<View>(R.id.email) as TextView
            holder.profileImage = converTView.findViewById<View>(R.id.profile_image) as CircleImageView

            converTView.tag = holder
        } else {
            holder = converTView.tag as ViewHolder
        }


        holder.username.text = getItem(position).username
        holder.email.text = getItem(position).email

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).user_id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue<UserAccountSettings>(UserAccountSettings::class.java)!!.toString())

                    val imageLoader = ImageLoader.getInstance()

                    imageLoader.displayImage(singleSnapshot.getValue<UserAccountSettings>(UserAccountSettings::class.java)!!.profile_photo,
                            holder.profileImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        return converTView
    }

    companion object {

        private const val TAG = "UserListAdapter"
    }


}
