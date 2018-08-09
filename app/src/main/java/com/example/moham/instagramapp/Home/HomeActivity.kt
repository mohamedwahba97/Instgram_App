package com.example.moham.instagramapp.Home

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.moham.instagramapp.Login.LoginActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.*
import com.example.moham.instagramapp.models.Photo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import com.nostra13.universalimageloader.core.ImageLoader

class HomeActivity : AppCompatActivity(), MainfeedListAdapter.OnLoadMoreItemsListener {

    private val mContext = this@HomeActivity

    //firebase
    lateinit var mAuth: FirebaseAuth
    lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    //widgets
    private lateinit var mViewPager: ViewPager
    private lateinit var mFrameLayout: FrameLayout
    private lateinit var mRelativeLayout: RelativeLayout

    override fun onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos")
        val fragment = supportFragmentManager
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.currentItem) as HomeFragment
        fragment.displayMorePhotos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d(TAG, "onCreate: starting.")
        mViewPager = findViewById(R.id.viewpager_container)
        mFrameLayout = findViewById(R.id.container)
        mRelativeLayout = findViewById(R.id.relLayoutParent)

        setupFirebaseAuth()

        initImageLoader()
        setupBottomNavigationView()
        setupViewPager()

    }

    fun onCommentThreadSelected(photo: Photo, callingActivity: String) {
        Log.d(TAG, "onCommentThreadSelected: selected a coemment thread")

        val fragment = ViewCommentsFragment()
        val args = Bundle()
        args.putParcelable(getString(R.string.photo), photo)
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity))
        fragment.arguments = args

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(getString(R.string.view_comments_fragment))
        transaction.commit()

    }

    fun hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout")
        mRelativeLayout.visibility = View.GONE
        mFrameLayout.visibility = View.VISIBLE
    }

    fun showLayout() {
        Log.d(TAG, "hideLayout: showing layout")
        mRelativeLayout.visibility = View.VISIBLE
        mFrameLayout.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mFrameLayout.visibility == View.VISIBLE) {
            showLayout()
        }
    }

    private fun initImageLoader() {
        val universalImageLoader = UniversalImageLoader(mContext)
        ImageLoader.getInstance().init(universalImageLoader.config)
    }

    /**
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private fun setupViewPager() {
        val adapter = SectionsPagerAdapter(supportFragmentManager)
        adapter.addFragment(CameraFragment()) //index 0
        adapter.addFragment(HomeFragment()) //index 1
        adapter.addFragment(MessagesFragment()) //index 2
        mViewPager.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(mViewPager)

        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_camera)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_instagram_black)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_arrow)
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


    /*
    ------------------------------------ Firebase ---------------------------------------------
    */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
    */
    private fun checkCurrentUser(user: FirebaseUser?) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.")

        if (user == null) {
            val intent = Intent(mContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Setup the firebase auth object
     */
    private fun setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.")

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            //check if the user is logged in
            checkCurrentUser(user)

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

    public override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
        mViewPager.currentItem = HOME_FRAGMENT
        checkCurrentUser(mAuth.currentUser)
    }

    public override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }

    companion object {

        private const val TAG = "HomeActivity"
        private const val ACTIVITY_NUM = 0
        private const val HOME_FRAGMENT = 1
    }


}
