package com.example.moham.instagramapp.models

class Comment {

    lateinit var comment:String
    lateinit var user_id:String
    lateinit var likes:List<Like>
    lateinit var date_created:String

    constructor() {

    }

    constructor(comment: String, user_id: String, likes: List<Like>, date_created: String) {
        this.comment = comment
        this.user_id = user_id
        this.likes = likes
        this.date_created = date_created
    }

    override fun toString():String {
        return ("Comment{" +
                "comment='" + comment + '\''.toString() +
                ", user_id='" + user_id + '\''.toString() +
                ", likes=" + likes +
                ", date_created='" + date_created + '\''.toString() +
                '}'.toString())
    }

}
