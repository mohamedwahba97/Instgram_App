package com.example.moham.instagramapp.Share

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.BottomNavigationViewHelper
import com.example.moham.instagramapp.Utils.Permissions
import com.example.moham.instagramapp.Utils.SectionsPagerAdapter
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx

class ShareActivity : AppCompatActivity() {

    private lateinit var mViewPager: ViewPager


    private val mContext = this@ShareActivity

    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    val currentTabNumber: Int
        get() = mViewPager.currentItem

    val task: Int
        get() {
            Log.d(TAG, "getTask: TASK: " + intent.flags)
            return intent.flags
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate: started.")

        if (checkPermissionsArray(Permissions.PERMISSIONS)) {
            setupViewPager()
        } else {
            verifyPermissions(Permissions.PERMISSIONS)
        }

    }

    /**
     * setup viewpager for manager the tabs
     */
    private fun setupViewPager() {
        val adapter = SectionsPagerAdapter(supportFragmentManager)
        adapter.addFragment(GalleryFragment())
        adapter.addFragment(PhotoFragment())

        mViewPager = findViewById<View>(R.id.viewpager_container) as ViewPager
        mViewPager.adapter = adapter

        val tabLayout = findViewById<View>(R.id.tabsBottom) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        tabLayout.getTabAt(0)!!.text = getString(R.string.gallery)
        tabLayout.getTabAt(1)!!.text = getString(R.string.photo)

    }

    /**
     * verifiy all the permissions passed to the array
     * @param permissions
     */
    fun verifyPermissions(permissions: Array<String>) {
        Log.d(TAG, "verifyPermissions: verifying permissions.")

        ActivityCompat.requestPermissions(
                this@ShareActivity,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        )
    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    fun checkPermissionsArray(permissions: Array<String>): Boolean {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.")

        for (i in permissions.indices) {
            val check = permissions[i]
            if (!checkPermissions(check)) {
                return false
            }
        }
        return true
    }

    /**
     * Check a single permission is it has been verified
     * @param permission
     * @return
     */
    fun checkPermissions(permission: String): Boolean {
        Log.d(TAG, "checkPermissions: checking permission: $permission")

        val permissionRequest = ActivityCompat.checkSelfPermission(this@ShareActivity, permission)

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: $permission")
            return false
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: $permission")
            return true
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

        private const val TAG = "ShareActivity"

        //constants
        private const val ACTIVITY_NUM = 2
        private const val VERIFY_PERMISSIONS_REQUEST = 1
    }

}
