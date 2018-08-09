package com.example.moham.instagramapp.models

import android.os.Parcel
import android.os.Parcelable

class User : Parcelable {

    lateinit var user_id:String
    var phone_number:Long = 0
    lateinit var email:String
    lateinit var username:String
    constructor(user_id: String, phone_number: Long, email: String, username: String) {
        this.user_id = user_id
        this.phone_number = phone_number
        this.email = email
        this.username = username
    }

    constructor()


    constructor(`in`: Parcel) {
        user_id = `in`.readString()
        phone_number = `in`.readLong()
        email = `in`.readString()
        username = `in`.readString()
    }

    override fun toString(): String {
        return "User{" +
                "user_id='" + user_id + '\''.toString() +
                ", phone_number='" + phone_number + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", username='" + username + '\''.toString() +
                '}'.toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(user_id)
        dest.writeLong(phone_number)
        dest.writeString(email)
        dest.writeString(username)
    }

    companion object {

        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(`in`: Parcel): User {
                return User(`in`)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }

}
