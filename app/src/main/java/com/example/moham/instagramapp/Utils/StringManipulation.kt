package com.example.moham.instagramapp.Utils

object StringManipulation {

    fun expandUsername(username: String): String {
        return username.replace(".", " ")
    }

    fun condenseUsername(username: String): String {
        return username.replace(" ", ".")
    }

    fun getTags(string: String): String {
        if (string.indexOf("#") > 0) {
            val sb = StringBuilder()
            val charArray = string.toCharArray()
            var foundWord = false
            for (c in charArray) {
                if (c == '#') {
                    foundWord = true
                    sb.append(c)
                } else {
                    if (foundWord) {
                        sb.append(c)
                    }
                }
                if (c == ' ') {
                    foundWord = false
                }
            }
            val s = sb.toString().replace(" ", "").replace("#", ",#")
            return s.substring(1, s.length)
        }
        return string
    }

}
