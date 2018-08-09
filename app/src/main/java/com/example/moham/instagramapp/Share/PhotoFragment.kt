package com.example.moham.instagramapp.Share

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.moham.instagramapp.Profile.AccountSettingsActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.Permissions

class PhotoFragment : Fragment() {

    private val isRootTask: Boolean
        get() = (activity as ShareActivity).task == 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)
        Log.d(TAG, "onCreateView: started.")

        val btnLaunchCamera = view.findViewById<Button>(R.id.btnLaunchCamera)
        btnLaunchCamera.setOnClickListener {
            Log.d(TAG, "onClick: launching camera.")

            if ((activity as ShareActivity).currentTabNumber == PHOTO_FRAGMENT_NUM) {
                if ((activity as ShareActivity).checkPermissions(Permissions.CAMERA_PERMISSION[0])) {
                    Log.d(TAG, "onClick: starting camera")
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                } else {
                    val intent = Intent(activity, ShareActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo.")
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.")

            val bitmap: Bitmap = data.extras.get("data") as Bitmap

            if (isRootTask) {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: $bitmap")
                    val intent = Intent(activity, NextActivity::class.java)
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap)
                    startActivity(intent)
                } catch (e: NullPointerException) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.message)
                }

            } else {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: $bitmap")
                    val intent = Intent(activity, AccountSettingsActivity::class.java)
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap)
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment))
                    startActivity(intent)
                    activity!!.finish()
                } catch (e: NullPointerException) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.message)
                }

            }

        }
    }

    companion object {

        private const val TAG = "PhotoFragment"

        //constant
        private const val PHOTO_FRAGMENT_NUM = 1
        private const val GALLERY_FRAGMENT_NUM = 2
        private const val CAMERA_REQUEST_CODE = 5
    }


}
