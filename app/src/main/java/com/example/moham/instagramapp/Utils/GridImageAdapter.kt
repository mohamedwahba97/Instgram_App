package com.example.moham.instagramapp.Utils

import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import com.example.moham.instagramapp.R
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import java.util.*

class GridImageAdapter(context:Context, layoutResource:Int, append:String, imgURLs:ArrayList<String>):ArrayAdapter<String>(context, layoutResource, imgURLs) {
    private val mContext:Context
    private val mInflater:LayoutInflater
    private var layoutResource:Int = 0
    private val mAppend:String
    private val imgURLs:ArrayList<String>
    init{
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mContext = context
        this.layoutResource = layoutResource
        mAppend = append
        this.imgURLs = imgURLs
    }
    private class ViewHolder {
        lateinit var image:SquareImageView
        lateinit var mProgressBar:ProgressBar
    }
    @NonNull
    override fun getView(position:Int, @Nullable convertView:View ?, @NonNull parent:ViewGroup):View ? {
        /*
     Viewholder build pattern (Similar to recyclerview)
     */
        var converTView = convertView
        val holder:ViewHolder
        if (converTView == null)
        {
            converTView = mInflater.inflate(layoutResource, parent, false)
            holder = ViewHolder()
            holder.mProgressBar = converTView.findViewById(R.id.gridImageProgressbar) as ProgressBar
            holder.image = converTView.findViewById(R.id.gridImageView) as SquareImageView
            converTView.setTag(holder)
        }
        else
        {
            holder = converTView.getTag() as ViewHolder
        }
        val imgURL = getItem(position)
        val imageLoader = ImageLoader.getInstance()
        imageLoader.displayImage(mAppend + imgURL, holder.image, object:ImageLoadingListener {
            override fun onLoadingStarted(imageUri:String, view:View) {
                if (holder.mProgressBar != null)
                {
                    holder.mProgressBar.visibility = View.VISIBLE
                }
            }
            override fun onLoadingFailed(imageUri:String, view:View, failReason:FailReason) {
                if (holder.mProgressBar != null)
                {
                    holder.mProgressBar.visibility = View.GONE
                }
            }
            override fun onLoadingComplete(imageUri:String, view:View, loadedImage:Bitmap) {
                if (holder.mProgressBar != null)
                {
                    holder.mProgressBar.visibility = View.GONE
                }
            }
            override fun onLoadingCancelled(imageUri:String, view:View) {
                if (holder.mProgressBar != null)
                {
                    holder.mProgressBar.visibility = View.GONE
                }
            }
        })
        return converTView
    }
}