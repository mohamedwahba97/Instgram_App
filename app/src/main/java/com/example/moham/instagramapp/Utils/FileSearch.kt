package com.example.moham.instagramapp.Utils

import java.io.File
import java.util.ArrayList

object FileSearch {

    /**
     * Search a directory and return a list of all **directories** contained inside
     * @param directory
     * @return
     */
    fun getDirectoryPaths(directory: String): ArrayList<String> {
        val pathArray = ArrayList<String>()
        val file = File(directory)
        val listfiles = file.listFiles()
        for (i in listfiles.indices) {
            if (listfiles[i].isDirectory) {
                pathArray.add(listfiles[i].absolutePath)
            }
        }
        return pathArray
    }

    /**
     * Search a directory and return a list of all **files** contained inside
     * @param directory
     * @return
     */
    fun getFilePaths(directory: String): ArrayList<String> {
        val pathArray = ArrayList<String>()
        val file = File(directory)
        val listfiles = file.listFiles()
        for (i in listfiles.indices) {
            if (listfiles[i].isFile) {
                pathArray.add(listfiles[i].absolutePath)
            }
        }
        return pathArray
    }

}
