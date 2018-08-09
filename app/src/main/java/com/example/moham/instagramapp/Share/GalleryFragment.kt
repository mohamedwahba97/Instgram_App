package com.example.moham.instagramapp.Share

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.moham.instagramapp.Profile.AccountSettingsActivity
import com.example.moham.instagramapp.R
import com.example.moham.instagramapp.Utils.FilePaths
import com.example.moham.instagramapp.Utils.FileSearch
import com.example.moham.instagramapp.Utils.GridImageAdapter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import java.util.*

class GalleryFragment : Fragment() {

    //widgets
    private lateinit var gridView:GridView
    private lateinit var galleryImage:ImageView
    private lateinit var mProgressBar:ProgressBar
    private lateinit var directorySpinner:Spinner
    //vars
    private lateinit var directories:ArrayList<String>
    private val mAppend = "file:/"
    private lateinit var mSelectedImage:String

    private val isRootTask: Boolean
        get() = (activity as ShareActivity).task == 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        galleryImage = view.findViewById(R.id.galleryImageView)
        gridView = view.findViewById(R.id.gridView)
        directorySpinner = view.findViewById(R.id.spinnerDirectory)
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.GONE
        directories = ArrayList()
        Log.d(TAG, "onCreateView: started.")

        val shareClose = view.findViewById<ImageView>(R.id.ivCloseShare)
        shareClose.setOnClickListener {
            Log.d(TAG, "onClick: closing the gallery fragment.")
            activity!!.finish()
        }

        val nextScreen = view.findViewById<TextView>(R.id.tvNext)
        nextScreen.setOnClickListener {
            Log.d(TAG, "onClick: navigating to the final share screen.")

            if (isRootTask) {
                val intent = Intent(activity, NextActivity::class.java)
                intent.putExtra(getString(R.string.selected_image), mSelectedImage)
                startActivity(intent)
            } else
            {
                val intent = Intent(activity, AccountSettingsActivity::class.java)
                intent.putExtra(getString(R.string.selected_image), mSelectedImage)
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment))
                startActivity(intent)
                activity!!.finish()
            }
        }

        init()

        return view
    }

    private fun init() {
        val filePaths = FilePaths()

        //check for other folders indide "/storage/emulated/0/pictures"
        directories = FileSearch.getDirectoryPaths(filePaths.PICTURES)
        directories.add(filePaths.CAMERA)

        val directoryNames = ArrayList<String>()
        for (i in directories.indices) {
            Log.d(TAG, "init: directory: " + directories[i])
            val index = directories[i].lastIndexOf("/")
            val string = directories[i].substring(index)
            directoryNames.add(string)
        }

        val adapter = ArrayAdapter(activity!!,
                android.R.layout.simple_spinner_item, directoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        directorySpinner.adapter = adapter

        directorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Log.d(TAG, "onItemClick: selected: " + directories[position])

                //setup our image grid for the directory chosen
                setupGridView(directories[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }


    private fun setupGridView(selectedDirectory: String) {
        Log.d(TAG, "setupGridView: directory chosen: $selectedDirectory")
        val imgURLs = FileSearch.getFilePaths(selectedDirectory)

        //set the grid column width
        val gridWidth = resources.displayMetrics.widthPixels
        val imageWidth = gridWidth / NUM_GRID_COLUMNS
        gridView.columnWidth = imageWidth

        //use the grid adapter to adapter the images to gridview
        val adapter = GridImageAdapter(this.activity!!, R.layout.layout_grid_imageview, mAppend, imgURLs)
        gridView.adapter = adapter

        //set the first image to be displayed when the activity fragment view is inflated
        try {
            setImage(imgURLs[0], galleryImage, mAppend)
            mSelectedImage = imgURLs[0]
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.message)
        }

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.d(TAG, "onItemClick: selected an image: " + imgURLs[position])

            setImage(imgURLs[position], galleryImage, mAppend)
            mSelectedImage = imgURLs[position]
        }

    }


    private fun setImage(imgURL: String, image: ImageView?, append: String) {
        Log.d(TAG, "setImage: setting image")

        val imageLoader = ImageLoader.getInstance()

        imageLoader.displayImage(append + imgURL, image, object : ImageLoadingListener {
            override fun onLoadingStarted(imageUri: String, view: View) {
                mProgressBar.visibility = View.VISIBLE
            }

            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                mProgressBar.visibility = View.INVISIBLE
            }

            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                mProgressBar.visibility = View.INVISIBLE
            }

            override fun onLoadingCancelled(imageUri: String, view: View) {
                mProgressBar.visibility = View.INVISIBLE
            }
        })
    }

    companion object {

        private const val TAG = "GalleryFragment"

        //constants
        private const val NUM_GRID_COLUMNS = 3
    }

}
