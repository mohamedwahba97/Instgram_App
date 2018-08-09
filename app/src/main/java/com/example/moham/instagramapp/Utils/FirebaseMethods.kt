package com.example.moham.instagramapp.Utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.example.moham.instagramapp.Home.HomeActivity
import com.example.moham.instagramapp.Profile.AccountSettingsActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.models.Photo
import com.example.moham.instagramapp.models.User
import com.example.moham.instagramapp.models.UserAccountSettings
import com.example.moham.instagramapp.models.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class FirebaseMethods(//vars
         context: Context) {

    //firebase
    private val mAuth:FirebaseAuth
    private lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private val mFirebaseDatabase:FirebaseDatabase
    private val myRef:DatabaseReference
    private val mStorageReference:StorageReference
    private lateinit var userID:String
    //vars
    private val mContext:Context
    private var mPhotoUploadProgress = 0.0
    private val timestamp:String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA)
            sdf.timeZone = TimeZone.getTimeZone("Canada/Pacific")
            return sdf.format(Date())
        }
    init{
        mAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase.reference
        mStorageReference = FirebaseStorage.getInstance().reference
        mContext = context
        if (mAuth.currentUser != null)
        {
            userID = mAuth.currentUser!!.uid
        }
    }

    fun uploadNewPhoto(photoType: String, caption: String, count: Int, imgUrl: String,
                       bm: Bitmap?) {
        var bmm = bm
        Log.d(TAG, "uploadNewPhoto: attempting to uplaod new photo.")

        val filePaths = FilePaths()
        //case1) new photo
        if (photoType == mContext.getString(R.string.new_photo)) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.")

            val user_id = FirebaseAuth.getInstance().currentUser!!.uid
            val storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1))

            //convert image url to bitmap
            if (bmm == null) {
                bmm = ImageManager.getBitmap(imgUrl)
            }

            val bytes = ImageManager.getBytesFromBitmap(bmm!!, 100)

            val uploadTask: UploadTask
            uploadTask = storageReference.putBytes(bytes)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                val firebaseUrl = taskSnapshot.storage.downloadUrl.toString()

                Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show()

                //add the new photo to 'photos' node and 'user_photos' node
                addPhotoToDatabase(caption, firebaseUrl)

                //navigate to the main feed so the user can see their photo
                val intent = Intent(mContext, HomeActivity::class.java)
                mContext.startActivity(intent)
            }.addOnFailureListener {
                Log.d(TAG, "onFailure: Photo upload failed.")
                Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toDouble()

                if (progress - 15 > mPhotoUploadProgress) {
                    Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show()
                    mPhotoUploadProgress = progress
                }

                Log.d(TAG, "onProgress: upload progress: $progress% done")
            }

        } else if (photoType == mContext.getString(R.string.profile_photo)) {
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo")


            val user_id = FirebaseAuth.getInstance().currentUser!!.uid
            val storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo")

            //convert image url to bitmap
            if (bmm == null) {
                bmm = ImageManager.getBitmap(imgUrl)
            }
            val bytes = ImageManager.getBytesFromBitmap(bmm!!, 100)

            val uploadTask: UploadTask
            uploadTask = storageReference.putBytes(bytes)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                val firebaseUrl = taskSnapshot.storage.downloadUrl.toString()

                Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show()

                //insert into 'user_account_settings' node
                setProfilePhoto(firebaseUrl)

                (mContext as AccountSettingsActivity).setViewPager(
                        mContext.pagerAdapter
                                .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))!!
                )

            }.addOnFailureListener {
                Log.d(TAG, "onFailure: Photo upload failed.")
                Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toDouble()

                if (progress - 15 > mPhotoUploadProgress) {
                    Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show()
                    mPhotoUploadProgress = progress
                }

                Log.d(TAG, "onProgress: upload progress: $progress% done")
            }
        }//case new profile photo

    }

    private fun setProfilePhoto(url: String) {
        Log.d(TAG, "setProfilePhoto: setting new profile image: $url")

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url)
    }

    private fun addPhotoToDatabase(caption: String, url: String) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.")

        val tags = StringManipulation.getTags(caption)
        val newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().key
        val photo = Photo()
        photo.caption = caption
        photo.date_created = timestamp
        photo.image_path = url
        photo.tags = tags
        photo.user_id = FirebaseAuth.getInstance().currentUser!!.uid
        photo.photo_id = newPhotoKey.toString()

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().currentUser!!
                        .uid).child(newPhotoKey!!).setValue(photo)
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo)

    }

    fun getImageCount(dataSnapshot: DataSnapshot): Int {
        var count = 0
        for (ds in dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .children) {
            count++
        }
        return count
    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    fun updateUserAccountSettings(displayName: String?, website: String?, description: String?, phoneNumber: Long) {

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.")

        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName)
        }


        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website)
        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description)
        }

        if (phoneNumber != 0L) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber)
        }
    }

    /**
     * update username in the 'users' node and 'user_account_settings' node
     * @param username
     */
    fun updateUsername(username: String) {
        Log.d(TAG, "updateUsername: upadting username to: $username")

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username)

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username)
    }

    /**
     * update the email in the 'user's' node
     * @param email
     */
    fun updateEmail(email: String) {
        Log.d(TAG, "updateEmail: upadting email to: $email")

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email)

    }

    //    public boolean checkIfUsernameExists(String username, DataSnapshot datasnapshot){
    //        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists.");
    //
    //        User user = new User();
    //
    //        for (DataSnapshot ds: datasnapshot.child(userID).getChildren()){
    //            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + ds);
    //
    //            user.setUsername(ds.getValue(User.class).getUsername());
    //            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());
    //
    //            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
    //                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    fun registerNewEmail(email: String, password: String, username: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Toast.makeText(mContext, R.string.auth_failed,
                                Toast.LENGTH_SHORT).show()
                    } else if (task.isSuccessful) {
                        //send verificaton email
                        sendVerificationEmail()
                        userID = mAuth.currentUser!!.uid
                        Log.d(TAG, "onComplete: Authstate changed: $userID")
                    }
                }
    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
        {
            user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                        } else {
                            Toast.makeText(mContext, "couldn't send verification email.", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    /**
     * Add information to the users nodes
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    fun addNewUser(email: String, username: String, description: String, website: String, profile_photo: String) {

        val user = User(this.userID, 1, email, StringManipulation.condenseUsername(username))

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user)


        val settings = UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        )

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings)

    }


    /**
     * Retrieves the account settings for teh user currently logged in
     * Database: user_acount_Settings node
     * @param dataSnapshot
     * @return
     */
    fun getUserSettings(dataSnapshot: DataSnapshot): UserSettings {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.")


        val settings = UserAccountSettings()
        val user = User()

        for (ds in dataSnapshot.children) {

            // user_account_settings node
            if (ds.key == mContext.getString(R.string.dbname_user_account_settings)) {
                Log.d(TAG, "getUserAccountSettings: user account settings node datasnapshot: $ds")

                try {

                    settings.display_name = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .display_name
                    settings.username = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .username
                    settings.website = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .website
                    settings.description = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .description
                    settings.profile_photo = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .profile_photo
                    settings.posts = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .posts
                    settings.following = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .following
                    settings.followers = ds.child(userID)
                            .getValue<UserAccountSettings>(UserAccountSettings::class.java)!!
                            .followers

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString())
                } catch (e: NullPointerException) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.message)
                }

            }


            // users node
            Log.d(TAG, "getUserSettings: snapshot key: " + ds.key!!)
            if (ds.key == mContext.getString(R.string.dbname_users)) {
                Log.d(TAG, "getUserAccountSettings: users node datasnapshot: $ds")

                user.username = ds.child(userID)
                        .getValue<User>(User::class.java)!!
                        .username
                user.email = ds.child(userID)
                        .getValue<User>(User::class.java)!!
                        .email
                user.phone_number = ds.child(userID)
                        .getValue<User>(User::class.java)!!
                        .phone_number
                user.user_id = ds.child(userID)
                        .getValue<User>(User::class.java)!!
                        .user_id

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString())
            }
        }
        return UserSettings(user, settings)

    }

    companion object {

        private const val TAG = "FirebaseMethods"
    }

}
