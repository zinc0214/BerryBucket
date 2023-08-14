package com.zinc.berrybucket.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

@Parcelize
data class UserSelectedImageInfo(
    private val key: Int,
    val uri: Uri,
    val file: File,
    val path: String
) : java.io.Serializable, Parcelable {
    fun parseKey() = key.toString() + uri.toString()
    fun intKey() = key
}