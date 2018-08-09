package com.example.moham.instagramapp.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("ParcelCreator")
class Photo : Parcelable {

    lateinit var caption:String
    lateinit var date_created:String
    lateinit var image_path:String
    lateinit var photo_id:String
    lateinit var user_id:String
    lateinit var tags:String
    lateinit var likes:List<Like>
    lateinit var comments:List<Comment>

    constructor()

    constructor(caption: String, date_created: String, image_path: String, photo_id: String,
                user_id: String, tags: String, likes: List<Like>, comments: List<Comment>) {
        this.caption = caption
        this.date_created = date_created
        this.image_path = image_path
        this.photo_id = photo_id
        this.user_id = user_id
        this.tags = tags
        this.likes = likes
        this.comments = comments
    }

    protected constructor(`in`: Parcel) {
        caption = `in`.readString()
        date_created = `in`.readString()
        image_path = `in`.readString()
        photo_id = `in`.readString()
        user_id = `in`.readString()
        tags = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(caption)
        dest.writeString(date_created)
        dest.writeString(image_path)
        dest.writeString(photo_id)
        dest.writeString(user_id)
        dest.writeString(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Photo{" +
                "caption='" + caption + '\''.toString() +
                ", date_created='" + date_created + '\''.toString() +
                ", image_path='" + image_path + '\''.toString() +
                ", photo_id='" + photo_id + '\''.toString() +
                ", user_id='" + user_id + '\''.toString() +
                ", tags='" + tags + '\''.toString() +
                ", likes=" + likes +
                '}'.toString()
    }

    companion object {

        val creator: Parcelable.Creator<Photo> = object : Parcelable.Creator<Photo> {
            override fun createFromParcel(`in`: Parcel): Photo {
                return Photo(`in`)
            }

            override fun newArray(size: Int): Array<Photo?> {
                return arrayOfNulls(size)
            }
        }
    }

}

