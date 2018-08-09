package com.example.moham.instagramapp.models

class UserSettings {

    lateinit var user:User
    lateinit var settings:UserAccountSettings

    constructor(user: User, settings: UserAccountSettings) {
        this.user = user
        this.settings = settings
    }

    constructor() {

    }

    override fun toString(): String {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}'.toString()
    }

}
