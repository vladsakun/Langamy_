package com.langamy.provider

import android.net.Uri

interface UserProvider {
    fun getUserEmail():String
    fun getUserPhotoUrl(): Uri?
}