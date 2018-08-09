package com.example.moham.instagramapp.models

import android.os.Parcel
import android.os.Parcelable

class UserAccountSettings : Parcelable {

    lateinit var description:String
    lateinit var display_name:String
    var followers:Long = 0
    var following:Long = 0
    var posts:Long = 0
    lateinit var profile_photo:String
    lateinit var username:String
    lateinit var website:String
    lateinit var user_id:String

    constructor(description:String, display_name:String, followers:Long,
                following:Long, posts:Long, profile_photo:String, username:String,
                website:String, user_id:String) {
        this.description = description
        this.display_name = display_name
        this.followers = followers
        this.following = following
        this.posts = posts
        this.profile_photo = profile_photo
        this.username = username
        this.website = website
        this.user_id = user_id
    }
    constructor() {
    }
    protected constructor(`in`:Parcel) {
        description = `in`.readString()
        display_name = `in`.readString()
        followers = `in`.readLong()
        following = `in`.readLong()
        posts = `in`.readLong()
        profile_photo = `in`.readString()
        username = `in`.readString()
        website = `in`.readString()
        user_id = `in`.readString()
    }

    override fun toString():String {
        return ("UserAccountSettings{" +
                "description='" + description + '\''.toString() +
                ", display_name='" + display_name + '\''.toString() +
                ", followers=" + followers +
                ", following=" + following +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\''.toString() +
                ", username='" + username + '\''.toString() +
                ", website='" + website + '\''.toString() +
                '}'.toString())
    }
    override fun describeContents():Int {
        return 0
    }
    override fun writeToParcel(dest:Parcel, flags:Int) {
        dest.writeString(description)
        dest.writeString(display_name)
        dest.writeLong(followers)
        dest.writeLong(following)
        dest.writeLong(posts)
        dest.writeString(profile_photo)
        dest.writeString(username)
        dest.writeString(website)
        dest.writeString(user_id)
    }
    companion object {
        val CREATOR: Parcelable.Creator<UserAccountSettings> = object: Parcelable.Creator<UserAccountSettings> {
            override fun createFromParcel(`in`:Parcel):UserAccountSettings {
                return UserAccountSettings(`in`)
            }
            override fun newArray(size:Int): Array<UserAccountSettings?> {
                return arrayOfNulls<UserAccountSettings>(size)
            }
        }
    }

}
