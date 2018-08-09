package com.example.moham.instagramapp.Utils

import android.os.Environment

class FilePaths {

    //"storage/emulated/0"
    private var ROOT_DIR: String = Environment.getExternalStorageDirectory().path

    var PICTURES = "$ROOT_DIR/Pictures"
    var CAMERA = "$ROOT_DIR/DCIM/camera"

    var FIREBASE_IMAGE_STORAGE = "photos/users/"
}
