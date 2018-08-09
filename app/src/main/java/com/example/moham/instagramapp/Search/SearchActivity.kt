package com.example.moham.instagramapp.Search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import com.example.moham.instagramapp.Profile.ProfileActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.BottomNavigationViewHelper
import com.example.moham.instagramapp.Utils.UserListAdapter
import com.example.moham.instagramapp.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity() {

    private val mContext = this@SearchActivity
    //widgets
    private lateinit var mSearchParam:EditText
    private lateinit var mListView:ListView
    //vars
    private lateinit var mUserList:MutableList<User>
    private lateinit var mAdapter:UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mSearchParam = findViewById(R.id.search)
        mListView = findViewById(R.id.listView)
        Log.d(TAG, "onCreate: started.")
        hideSoftKeyboard()
        setupBottomNavigationView()
        initTextListener()
    }

    private fun initTextListener() {
        Log.d(TAG, "initTextListener: initializing")

        mUserList = ArrayList()

        mSearchParam.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                val text = mSearchParam.text.toString().toLowerCase(Locale.getDefault())
                searchForMatch(text)
            }
        })
    }

    private fun searchForMatch(keyword: String) {
        Log.d(TAG, "searchForMatch: searching for a match: $keyword")
        mUserList
        //update the users list view
        if (keyword.isEmpty()) {

        } else {
            val reference = FirebaseDatabase.getInstance().reference
            val query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username)).equalTo(keyword)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue<User>(User::class.java)!!.toString())

                        mUserList.add(singleSnapshot.getValue<User>(User::class.java)!!)
                        //update the users list view
                        updateUsersList()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }

    private fun updateUsersList() {
        Log.d(TAG, "updateUsersList: updating users list")

        mAdapter = UserListAdapter(this@SearchActivity, R.layout.layout_user_listitem, mUserList)

        mListView.adapter = mAdapter

        mListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.d(TAG, "onItemClick: selected user: " + mUserList[position].toString())

            //navigate to profile activity
            val intent = Intent(this@SearchActivity, ProfileActivity::class.java)
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity))
            intent.putExtra(getString(R.string.intent_user), mUserList[position])
            startActivity(intent)
        }
    }


    private fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }


    /**
     * BottomNavigationView setup
     */
    private fun setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView")
        val bottomNavigationViewEx = findViewById<View>(R.id.bottomNavViewBar) as BottomNavigationViewEx
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx)
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx)
        val menu = bottomNavigationViewEx.menu
        val menuItem = menu.getItem(ACTIVITY_NUM)
        menuItem.isChecked = true
    }

    companion object {

        private const val TAG = "SearchActivity"
        private const val ACTIVITY_NUM = 1
    }

}
