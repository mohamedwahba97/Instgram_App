package com.example.moham.instagramapp.Profile

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RelativeLayout
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.BottomNavigationViewHelper
import com.example.moham.instagramapp.Utils.FirebaseMethods
import com.example.moham.instagramapp.Utils.SectionsStatePagerAdapter
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import java.util.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var mContext:Context
    lateinit var pagerAdapter:SectionsStatePagerAdapter
    private lateinit var mViewPager:ViewPager
    private lateinit var mRelativeLayout:RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        mContext = this@AccountSettingsActivity
        Log.d(TAG, "onCreate: started.")
        mViewPager = findViewById(R.id.viewpager_container)
        mRelativeLayout = findViewById(R.id.relLayout1)

        setupSettingsList()
        setupBottomNavigationView()
        setupFragments()
        getIncomingIntent()

        //setup the backarrow for navigating back to "ProfileActivity"
        val backArrow = findViewById<View>(R.id.backArrow)
        backArrow.setOnClickListener {
            Log.d(TAG, "onClick: navigating back to 'ProfileActivity'")
            finish()
        }
    }


    private fun getIncomingIntent() {
        val intent = intent
        if ((intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))))
        {
            //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl")
            if (intent.getStringExtra(getString(R.string.return_to_fragment)) == getString(R.string.edit_profile_fragment))
            {
                if (intent.hasExtra(getString(R.string.selected_image)))
                {
                    //set the new profile picture
                    val firebaseMethods = FirebaseMethods(this@AccountSettingsActivity)
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),
                            null.toString(), 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null)
                }
                else if (intent.hasExtra(getString(R.string.selected_bitmap)))
                {
                    //set the new profile picture
                    val firebaseMethods = FirebaseMethods(this@AccountSettingsActivity)
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null.toString(), 0, null.toString(), intent.getParcelableExtra(getString(R.string.selected_bitmap)) as Bitmap)
                }
            }
        }
        if (intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "getIncomingIntent: received incoming intent from " + getString(R.string.profile_activity))
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment))!!)
        }
    }

    private fun setupFragments() {
        pagerAdapter = SectionsStatePagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(EditProfileFragment(), getString(R.string.edit_profile_fragment)) //fragment 0
        pagerAdapter.addFragment(SignOutFragment(), getString(R.string.sign_out_fragment)) //fragment 1
    }

    fun setViewPager(fragmentNumber: Int) {
        mRelativeLayout.visibility = View.GONE
        Log.d(TAG, "setViewPager: navigating to fragment #: $fragmentNumber")
        mViewPager.adapter = pagerAdapter
        mViewPager.currentItem = fragmentNumber
    }

    private fun setupSettingsList() {
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' list.")
        val listView = findViewById<View>(R.id.lvAccountSettings) as ListView

        val options = ArrayList<String>()
        options.add(getString(R.string.edit_profile_fragment)) //fragment 0
        options.add(getString(R.string.sign_out_fragment)) //fragement 1

        val adapter = ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.d(TAG, "onItemClick: navigating to fragment#: $position")
            setViewPager(position)
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

        private const val TAG = "AccountSettingsActivity"
        private const val ACTIVITY_NUM = 4
    }

}
