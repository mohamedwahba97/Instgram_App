package com.example.moham.instagramapp.Home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.MainfeedListAdapter
import com.example.moham.instagramapp.models.Comment
import com.example.moham.instagramapp.models.Photo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class HomeFragment : Fragment() {

    //vars
    private lateinit var mPhotos: ArrayList<Photo>
    private lateinit var mPaginatedPhotos: ArrayList<Photo>
    private lateinit var mFollowing: ArrayList<String>
    private lateinit var mListView: ListView
    private lateinit var mAdapter: MainfeedListAdapter
    private var mResults: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mListView = view.findViewById(R.id.listView)
        mFollowing = ArrayList()
        mPhotos = ArrayList()

        getFollowing()

        return view
    }

    private fun getFollowing() {
        Log.d(TAG, "getFollowing: searching for following")

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.child(getString(R.string.field_user_id)).value!!)

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).value.toString())
                }
                mFollowing.add(FirebaseAuth.getInstance().currentUser!!.uid)
                //get the photos
                getPhotos()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getPhotos() {
        Log.d(TAG, "getPhotos: getting photos")
        val reference = FirebaseDatabase.getInstance().reference

        for (i in 0 until mFollowing.size) {
            val count = i
            val query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing[i])
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing[i])
            query.addListenerForSingleValueEvent(object:ValueEventListener {
                override fun onDataChange(dataSnapshot:DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val photo = Photo()
                        val objectMap = singleSnapshot.value as HashMap<*, *>
                        photo.caption=(objectMap[getString(R.string.field_caption)].toString())
                        photo.tags=(objectMap[getString(R.string.field_tags)].toString())
                        photo.photo_id=(objectMap[getString(R.string.field_photo_id)].toString())
                        photo.user_id=(objectMap[getString(R.string.field_user_id)].toString())
                        photo.date_created=(objectMap[getString(R.string.field_date_created)].toString())
                        photo.image_path=(objectMap[getString(R.string.field_image_path)].toString())
                        val comments = ArrayList<Comment>()
                        for (dSnapshot in singleSnapshot.child(getString(R.string.field_comments)).children) {
                            val comment = Comment()
                            comment.user_id=(dSnapshot.getValue(Comment::class.java)!!.user_id)
                            comment.comment=(dSnapshot.getValue(Comment::class.java)!!.comment)
                            comment.date_created=(dSnapshot.getValue(Comment::class.java)!!.date_created)
                            comments.add(comment)
                        }
                        photo.comments=(comments)
                        mPhotos.add(photo)
                    }
                    if (count >= mFollowing.size - 1) {
                        //display our photos
                        displayPhotos()
                    }
                }
                override fun onCancelled(databaseError:DatabaseError) {
                }
            })
        }
    }

    private fun displayPhotos() {
        mPaginatedPhotos = ArrayList()
        try {
            mPhotos.sortWith(Comparator { o1, o2 -> o2.date_created.compareTo(o1.date_created) })

            var iterations = mPhotos.size

            if (iterations > 10) {
                iterations = 10
            }

            mResults = 10
            for (i in 0 until iterations) {
                mPaginatedPhotos.add(mPhotos[i])
            }

            mAdapter = MainfeedListAdapter(activity!!, R.layout.layout_mainfeed_listitem, mPaginatedPhotos)
            mListView.adapter = mAdapter

        } catch (e: NullPointerException) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.message)
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.message)
        }
    }

    fun displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: displaying more photos")

        try {

            if (mPhotos.size > mResults && mPhotos.size > 0) {

                val iterations: Int
                if (mPhotos.size > mResults + 10) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos")
                    iterations = 10
                }
                else {
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos")
                    iterations = mPhotos.size - mResults
                }

                //add the new photos to the paginated results
                for (i in mResults until mResults + iterations) {
                    mPaginatedPhotos.add(mPhotos[i])
                }
                mResults += iterations
                mAdapter.notifyDataSetChanged()
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.message)
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.message)
        }

    }

    companion object {

        private const val TAG = "HomeFragment"
    }


}
