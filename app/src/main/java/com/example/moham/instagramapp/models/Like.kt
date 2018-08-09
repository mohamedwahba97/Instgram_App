package com.example.moham.instagramapp.models

class Like {

    lateinit var user_id: String

    constructor(user_id: String) {
        this.user_id = user_id
    }

    constructor() {

    }

    override fun toString(): String {
        return "Like{" +
                "user_id='" + user_id + '\''.toString() +
                '}'.toString()
    }

}
