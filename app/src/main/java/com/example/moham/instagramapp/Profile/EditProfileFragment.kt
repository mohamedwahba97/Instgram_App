package com.example.moham.instagramapp.Profile

import android.content.Intent
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Share.ShareActivity
import com.example.moham.instagramapp.Utils.FirebaseMethods
import com.example.moham.instagramapp.Utils.UniversalImageLoader
import com.example.moham.instagramapp.dialogs.ConfirmPasswordDialog
import com.example.moham.instagramapp.models.User
import com.example.moham.instagramapp.models.UserSettings
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class EditProfileFragment : Fragment(), ConfirmPasswordDialog.OnConfirmPasswordListener {

    //firebase
    private  lateinit var mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseDatabase:FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    private lateinit var mFirebaseMethods:FirebaseMethods
    private lateinit var userID:String
    //EditProfile Fragment widgets
    private lateinit var mDisplayName:EditText
    private lateinit var mUsername:EditText
    private lateinit var mWebsite:EditText
    private lateinit var mDescription:EditText
    private lateinit var mEmail:EditText
    private lateinit var mPhoneNumber:EditText
    private lateinit var mChangeProfilePhoto:TextView
    private lateinit var mProfilePhoto:CircleImageView
    //vars
    private lateinit var mUserSettings:UserSettings

    override fun onConfirmPassword(password:String) {
        Log.d(TAG, "onConfirmPassword: got the password: $password")
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        val credential = EmailAuthProvider
                .getCredential(mAuth.currentUser!!.email!!, password)
        ///////////////////// Prompt the user to re-provide their sign-in credentials
        mAuth.currentUser!!.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User re-authenticated.")
                        ///////////////////////check to see if the email is not already present in the database
                        mAuth.fetchSignInMethodsForEmail(mEmail.text.toString()).addOnCompleteListener(object:OnCompleteListener<SignInMethodQueryResult> {
                            override fun onComplete(@NonNull task:Task<SignInMethodQueryResult>) {
                                if (task.isSuccessful) {
                                    try {
                                        if (task.result.signInMethods!!.size == 1) {
                                            Log.d(TAG, "onComplete: that email is already in use.")
                                            Toast.makeText(activity, "That email is already in use", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Log.d(TAG, "onComplete: That email is available.")
                                            //////////////////////the email is available so update it
                                            mAuth.currentUser!!.updateEmail(mEmail.text.toString())
                                                    .addOnCompleteListener(object:OnCompleteListener<Void> {
                                                        override fun onComplete(@NonNull task:Task<Void>) {
                                                            if (task.isSuccessful) {
                                                                Log.d(TAG, "User email address updated.")
                                                                Toast.makeText(activity, "email updated", Toast.LENGTH_SHORT).show()
                                                                mFirebaseMethods.updateEmail(mEmail.text.toString())
                                                            }
                                                        }
                                                    })
                                        }
                                    } catch (e:NullPointerException) {
                                        Log.e(TAG, "onComplete: NullPointerException: " + e.message)
                                    }
                                }
                            }
                        })
                    } else {
                        Log.d(TAG, "onComplete: re-authentication failed.")
                    }
                }
    }
    @Nullable
    fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup):View {
        val view = inflater.inflate(R.layout.fragment_editprofile, container, false)
        mProfilePhoto = view.findViewById(R.id.profile_photo) as CircleImageView
        mDisplayName = view.findViewById(R.id.display_name) as EditText
        mUsername = view.findViewById(R.id.username) as EditText
        mWebsite = view.findViewById(R.id.website) as EditText
        mDescription = view.findViewById(R.id.description) as EditText
        mEmail = view.findViewById(R.id.email) as EditText
        mPhoneNumber = view.findViewById(R.id.phoneNumber) as EditText
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto) as TextView
        mFirebaseMethods = FirebaseMethods(activity!!)
        //setProfileImage();
        setupFirebaseAuth()
        //back arrow for navigating back to "ProfileActivity"
        val backArrow = view.findViewById(R.id.backArrow) as ImageView
        backArrow.setOnClickListener {
            Log.d(TAG, "onClick: navigating back to ProfileActivity")
            activity!!.finish()
        }
        val checkmark = view.findViewById(R.id.saveChanges) as ImageView
        checkmark.setOnClickListener {
            Log.d(TAG, "onClick: attempting to save changes.")
            saveProfileSettings()
        }
        return view
    }
    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * Before donig so it chekcs to make sure the username chosen is unqiue
     */
    private fun saveProfileSettings() {
        val displayName = mDisplayName.text.toString()
        val username = mUsername.text.toString()
        val website = mWebsite.text.toString()
        val description = mDescription.text.toString()
        val email = mEmail.text.toString()
        val phoneNumber = java.lang.Long.parseLong(mPhoneNumber.text.toString())
        //case1: if the user made a change to their username
        if (mUserSettings.user.username != username)
        {
            checkIfUsernameExists(username)
        }
        //case2: if the user made a change to their email
        if (mUserSettings.user.email != email)
        {
            // step1) Reauthenticate
            // -Confirm the password and email
            val dialog = ConfirmPasswordDialog()
            dialog.show(fragmentManager, getString(R.string.confirm_password_dialog))
            dialog.setTargetFragment(this@EditProfileFragment, 1)
            // step2) check if the email already is registered
            // -'fetchProvidersForEmail(String email)'
            // step3) change the email
            // -submit the new email to the database and authentication
        }
        /**
         * change the rest of the settings that do not require uniqueness
         */
        if (mUserSettings.settings.display_name != displayName)
        {
            //update displayname
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0)
        }
        if (mUserSettings.settings.website != website)
        {
            //update website
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0)
        }
        if (mUserSettings.settings.description != description)
        {
            //update description
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0)
        }
        if (!mUserSettings.settings.profile_photo.equals(phoneNumber))
        {
            //update phoneNumber
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber)
        }
    }
    /**
     * Check is @param username already exists in teh database
     * @param username
     */
    private fun checkIfUsernameExists(username:String) {
        Log.d(TAG, "checkIfUsernameExists: Checking if $username already exists.")
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    //add the username
                    mFirebaseMethods.updateUsername(username)
                    Toast.makeText(activity, "saved username.", Toast.LENGTH_SHORT).show()
                }
                for (singleSnapshot in dataSnapshot.children)
                {
                    if (singleSnapshot.exists())
                    {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User::class.java)!!.username)
                        Toast.makeText(activity, "That username already exists.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onCancelled(databaseError:DatabaseError) {
            }
        })
    }
    private fun setProfileWidgets(userSettings:UserSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString())
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.user.email)
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.user.phone_number)
        mUserSettings = userSettings
        //User user = userSettings.getUser();
        val settings = userSettings.settings
        UniversalImageLoader.setImage(settings.profile_photo, mProfilePhoto, null, "")
        mDisplayName.setText(settings.display_name)
        mUsername.setText(settings.username)
        mWebsite.setText(settings.website)
        mDescription.setText(settings.description)
        mEmail.setText(userSettings.user.email)
        mPhoneNumber.setSelection(userSettings.user.phone_number.toInt())
        mChangeProfilePhoto.setOnClickListener {
            Log.d(TAG, "onClick: changing profile photo")
            val intent = Intent(activity, ShareActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //268435456
            activity!!.startActivity(intent)
            activity!!.finish()
        }
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
        userID = mAuth.currentUser!!.uid
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
        myRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {
                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot))
                //retrieve images for the user in question
            }
            override fun onCancelled(databaseError:DatabaseError) {
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
        private const val TAG = "EditProfileFragment"
    }

}
